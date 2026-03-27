package org.threatwatch.services;

import org.springframework.stereotype.Service;
import org.threatwatch.models.ParsedCveModel;
import tools.jackson.databind.JsonNode;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.stream.StreamSupport;

@Service
public class CveParserService {

        private static final DateTimeFormatter stringDateTimeParser = DateTimeFormatter
                .ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")
                .withZone(ZoneOffset.UTC);

        private static final DateTimeFormatter emailDateTimeFormatter = DateTimeFormatter
                .ofPattern("yyyy-MM-dd HH:mm")
                .withZone(ZoneOffset.UTC);

        public ParsedCveModel parseCve(JsonNode cve) {
            String cveId = cve.get("id").asString();
            String description = StreamSupport.stream(cve.path("descriptions").spliterator(), false)
                    .filter(desc -> "en".equals(desc.path("lang").asString()))
                    .map(desc -> desc.path("value").asString())
                    .findFirst()
                    .orElse("");
            JsonNode metricsNode = cve.path("metrics");
            String severity;
            String score;

            if (!metricsNode.isEmpty()) {
                JsonNode metrics = metricsNode.has("cvssMetricV40") ? metricsNode.get("cvssMetricV40") :
                        metricsNode.has("cvssMetricV31") ? metricsNode.get("cvssMetricV31") :
                                metricsNode.has("cvssMetricV30") ? metricsNode.get("cvssMetricV30") :
                                        null;
                JsonNode cvssData = metrics.get(0).path("cvssData");
                severity = cvssData.path("baseSeverity").asString();
                score = cvssData.path("baseScore").asString();
            } else {
                severity = "UNKNOWN";
                score = "-1";
            }

            String published = emailDateTimeFormatter.format(
                    stringDateTimeParser.parse(
                            cve.get("published").asString()
                    )
            );

            return new ParsedCveModel(cveId, description, severity, score, published);
        }
    }

