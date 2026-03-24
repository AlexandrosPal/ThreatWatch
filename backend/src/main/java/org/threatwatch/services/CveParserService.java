package org.threatwatch.services;

import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.stream.StreamSupport;

@Service
public class CveParserService {

        private String cveId;
        private String description;
        private JsonNode metrics;
        private JsonNode cvssData;
        private String severity;
        private String score;
        private String published;

        DateTimeFormatter stringDateTimeParser = DateTimeFormatter
                .ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")
                .withZone(ZoneOffset.UTC);

        DateTimeFormatter emailDateTimeFormatter = DateTimeFormatter
                .ofPattern("yyyy-MM-dd HH:mm")
                .withZone(ZoneOffset.UTC);

        public void parseCve(JsonNode cve) {
            this.cveId = cve.get("id").asString();
            this.description = StreamSupport.stream(cve.path("descriptions").spliterator(), false)
                    .filter(desc -> "en".equals(desc.path("lang").asString()))
                    .map(desc -> desc.path("value").asString())
                    .findFirst()
                    .orElse("");
            JsonNode metricsNode = cve.path("metrics");
            this.metrics = metricsNode.has("cvssMetricV40") ? metricsNode.get("cvssMetricV40") :
                    metricsNode.has("cvssMetricV31") ? metricsNode.get("cvssMetricV31") :
                            metricsNode.has("cvssMetricV30") ? metricsNode.get("cvssMetricV30") :
                                    null;
            this.cvssData =this.metrics.get(0).path("cvssData");
            this.severity = this.cvssData.path("baseSeverity").asString();
            this.score = this.cvssData.path("baseScore").asString();;
            this.published = emailDateTimeFormatter.format(
                    stringDateTimeParser.parse(
                            cve.get("published").asString()
                    )
            );
        }

        public String getCveId() { return cveId; }

        public String getDescription() { return description; }

        public String getSeverity() { return severity; }

        public String getScore() { return score; }

        public String getPublished() { return published; }
    }

