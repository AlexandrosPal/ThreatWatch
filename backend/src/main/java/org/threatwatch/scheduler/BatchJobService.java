package org.threatwatch.scheduler;

import jakarta.mail.MessagingException;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.threatwatch.notifications.NotificationRequestDto;
import org.threatwatch.cve.parsing.CveParserService;
import org.threatwatch.cve.state.CveStateService;
import org.threatwatch.notifications.NotificationSender;
import org.threatwatch.cve.ingestion.NvdRestService;
import org.threatwatch.notifications.teams.TeamsNotificationSender;
import org.threatwatch.settings.SettingsResponseDto;
import org.threatwatch.loggers.AppLogger;
import org.threatwatch.loggers.LogEvents;
import org.threatwatch.cve.model.CveAlertItem;
import org.threatwatch.notifications.NotificationChannel;
import org.threatwatch.cve.parsing.ParsedCveModel;
import org.threatwatch.notifications.discord.DiscordNotificationSender;
import org.threatwatch.notifications.email.EmailNotificationSender;
import org.threatwatch.notifications.slack.SlackNotificationSender;
import org.threatwatch.settings.SettingsService;
import tools.jackson.databind.JsonNode;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BatchJobService {

    private final Map<NotificationChannel, NotificationSender> senders;
    private final EmailNotificationSender emailNotificationSender;
    private final DiscordNotificationSender discordNotificationSender;
    private final SlackNotificationSender slackNotificationSender;
    private final TeamsNotificationSender teamsNotificationSender;
    private final SettingsService settingsService;
    private final NvdRestService nvdRestService;
    private final CveParserService cveParserService;
    private final CveStateService cveStateService;

    private static final AppLogger appLogger = new AppLogger(LoggerFactory.getLogger(BatchJobService.class));

    @Value("${email.cve.description.product.length.lookup}")
    private int descriptionProductLookupLength;

    @Value("${backend.nvd.requests.interval}")
    private int nvdReqeustsInterval;

    public BatchJobService(List<NotificationSender> senderList, EmailNotificationSender emailNotificationSender, DiscordNotificationSender discordNotificationSender, SlackNotificationSender slackNotificationSender, TeamsNotificationSender teamsNotificationSender, SettingsService settingsService, NvdRestService nvdRestService, CveParserService cveParserService, CveStateService cveStateService) {
        this.senders = senderList.stream()
                .collect(Collectors.toMap(NotificationSender::supports, s -> s));
        this.emailNotificationSender = emailNotificationSender;
        this.discordNotificationSender = discordNotificationSender;
        this.slackNotificationSender = slackNotificationSender;
        this.teamsNotificationSender = teamsNotificationSender;
        this.settingsService = settingsService;
        this.nvdRestService = nvdRestService;
        this.cveParserService = cveParserService;
        this.cveStateService = cveStateService;
    }

    private boolean descriptionMatchesProduct(String description, String product) {
        String firstPart = description.length() > descriptionProductLookupLength ? description.substring(0, descriptionProductLookupLength) : description;
        return firstPart.toLowerCase().contains(product.toLowerCase());
    }

    private boolean referencesMatchProduct(List<String> references, String product) {
        return references.stream()
                .anyMatch(url -> url.toLowerCase().contains(product.toLowerCase()));
    }

    public void executeScheduledRun() throws IOException, InterruptedException, MessagingException {

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter
                .ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
                .withZone(ZoneOffset.UTC);

        SettingsResponseDto settings = settingsService.retrieveSettings();

        Set<NotificationChannel> notificationsSelected = settings.getNotificationsSelected().stream()
                .map(String::toUpperCase)
                .map(NotificationChannel::valueOf)
                .collect(Collectors.toSet());

        int lookbackWindowInteger = Integer.parseInt(settings.getLookbackWindow());
        Instant rawPublishEndDatetime = Instant.now();
        Instant rawPublishStartDatetime = rawPublishEndDatetime.minusSeconds(lookbackWindowInteger);

        String publishEndDatetime = dateTimeFormatter.format(rawPublishEndDatetime);
        String publishStartDatetime = dateTimeFormatter.format(rawPublishStartDatetime);
        Set<String> products = settings.getProductsSelected();

//        StringBuilder cveListHtml = new StringBuilder();
        String html = emailNotificationSender.loadHtmlTemplate();
        Set<String> emails = settings.getEmails();
        Set<String> cveIdsToSend = new HashSet<>();
        List<CveAlertItem> cvesToSend = new ArrayList<>();

        for (String product : products) {
            Thread.sleep(nvdReqeustsInterval);
            JsonNode response = nvdRestService.getRecentVulnerabilitiesByProduct(product.toLowerCase(), publishStartDatetime, publishEndDatetime);
            JsonNode vulnerabilities = response.path("vulnerabilities");

            int validCveCounter = 0;

            for (JsonNode vulnerability : vulnerabilities) {
                JsonNode cve = vulnerability.path("cve");

                ParsedCveModel parsedCve = cveParserService.parseCve(cve);
                String cveId = parsedCve.getCveId();
                String description = parsedCve.getDescription();
                List<String> references = parsedCve.getReferences();

                boolean cveAlreadyPresent = cveIdsToSend.contains(cveId);
                boolean isPastCve = !cveStateService.isNewCve(cveId);
                boolean earlyCve = Objects.equals(String.valueOf(parsedCve.getScore()), "-1");
                boolean outsideSeverityThreshold = !earlyCve && Float.parseFloat(parsedCve.getScore()) < Float.parseFloat(settings.getSeverityThreshold());
                boolean earlyAlertsEnabled = Boolean.parseBoolean(settings.getEarlyAlerts());

                if ((!descriptionMatchesProduct(description, product) || !referencesMatchProduct(references, product)) || cveAlreadyPresent || isPastCve || (outsideSeverityThreshold && !earlyCve) || (earlyCve && !earlyAlertsEnabled)) {
                    continue;
                }

                validCveCounter += 1;
                cveIdsToSend.add(cveId);
                cvesToSend.add(new CveAlertItem(
                        product,
                        parsedCve.getCveId(),
                        parsedCve.getDescription(),
                        parsedCve.getSeverity(),
                        Float.valueOf(parsedCve.getScore()),
                        parsedCve.getPublished()
                ));
            }
            appLogger.info(LogEvents.BATCH_RUN, "Searched vulnerabilities for product '%s'".formatted(product), new LinkedHashMap<>(Map.of("vulnerabilities", validCveCounter, "product", product)));
        }

        cvesToSend.sort(
                Comparator.comparing(
                        CveAlertItem::getScore,
                        Comparator.nullsLast(Comparator.reverseOrder())
                )
        );

        String cveListHtml = emailNotificationSender.buildEmailAlertHtml(cvesToSend);

        if (!cveIdsToSend.isEmpty()) {
            html = html.replace("{{cveList}}", cveListHtml);

            for (NotificationChannel channel : notificationsSelected) {
                NotificationRequestDto request = new NotificationRequestDto();

                switch (channel) {
                    case EMAIL:
                        request.setEmails(emails);
                        request.setTitle("New Vulnerabilities Report");
                        request.setMessage(html);

                        appLogger.info(LogEvents.EMAIL_SENT, "Finished run and sent alert email", new LinkedHashMap<>(Map.of("vulnerabilities", cvesToSend.size())));
                        break;
                    case DISCORD:
                        request.setWebhookUrls(settings.getDiscordWebhookUrls());
                        request.setMessage(discordNotificationSender.buildDiscordAlertMessage(cvesToSend));

                        appLogger.info(LogEvents.DISCORD_MESSAGE_SENT, "Finished run and sent alert Discord message", new LinkedHashMap<>(Map.of("vulnerabilities", cvesToSend.size())));
                        break;

                    case SLACK:
                        request.setWebhookUrls(settings.getSlackWebhookUrls());
                        request.setMessage(slackNotificationSender.buildSlackAlertMessage(cvesToSend));

                        appLogger.info(LogEvents.SLACK_MESSAGE_SENT, "Finished run and sent alert Slack message", new LinkedHashMap<>(Map.of("vulnerabilities", cvesToSend.size())));
                        break;

                    case TEAMS:
                        request.setWebhookUrls(settings.getTeamsWebhookUrls());
                        request.setMessage(teamsNotificationSender.buildTeamsAlertMessage(cvesToSend));

                        appLogger.info(LogEvents.TEAMS_MESSAGE_SENT, "Finished run and sent alert Teams message", new LinkedHashMap<>(Map.of("vulnerabilities", cvesToSend.size())));
                        break;
                }

                senders.get(channel).sendNotification(request);
            }

            for (String cveId : cveIdsToSend) {
                cveStateService.markCveAsSeen(cveId);
            }
        } else {
            appLogger.info(LogEvents.BATCH_RUN, "Finished run without sending any alert", new LinkedHashMap<>(Map.of("vulnerabilities", cvesToSend.size())));
        }

    }
}
