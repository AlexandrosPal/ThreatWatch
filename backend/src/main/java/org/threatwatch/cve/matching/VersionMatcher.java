package org.threatwatch.cve.matching;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VersionMatcher {

    public MatchResult match(String version, String description) {
        if (version == null || version.isBlank()) {
            return new MatchResult(
                    MatchConfidence.POSSIBLE,
                    "No version provided"
            );
        }

        description = description.toLowerCase();

        if (description.contains("version")) {
            return new MatchResult(
                    MatchConfidence.CONFIRMED,
                    "Exact version mentioned in CVE description
            );
        }

        List<String> wildcardVersions = extractWildcards(version);
        for (String wildcardVersion : wildcardVersions) {
            if (matchesWildcard(version, wildcardVersion)) {
                return new MatchResult(
                        MatchConfidence.LIKELY,
                        "Version matches wildcard pattern: " + wildcardVersion
                );
            }
        }
    }

    private List<String> extractWildcards(String text) {
        List<String> results = new ArrayList<>();

        Pattern pattern = Pattern.compile("\\b\\d+(\\.\\d+)?\\.x\\b|\\b\\d+\\.x\\b");
        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            results.add(matcher.group());
        }

        return results;
    }

    public boolean matchesWildcard(String version, String pattern) {
        String regex = pattern
                .replace(".", "\\.")
                .replace("x", "\\d+");

        return version.matches(regex);
    }
}
