package org.threatwatch.cve.matching;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DescriptionMatcher {
    private <T> T loadJsonFile(String filePath, TypeReference<T> typeReference) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        InputStream is = new ClassPathResource("%s.json".format(filePath)).getInputStream();

        return mapper.readValue(is, typeReference);
    }

    public Boolean insideFirstSentence(String text, String keyword) {
        List<String> sentences = List.of(text.toLowerCase()
                .replace("\n\n", "\n")
                .replace(".\n", ". ")
                .replace(". \n", ". ")
                .replace("\n", " ")
                .split(". "));

        return  sentences.get(0).contains(" %s".format(keyword.toLowerCase()));
    }

    public Boolean insideFirstWords(String text, String keyword) {
        int end = Math.min(text.length(), 50);

        return text.substring(0, end).toLowerCase().contains(keyword.toLowerCase());
    }

    public Boolean negativeKeywordMatch(String text, String keyword) throws IOException {
        List<String> negativeKeyphrases = loadJsonFile("matching/negative_keyphrases", new TypeReference<List<String>>() {});
        String description = text.toLowerCase();
        String product = " %s".format(keyword.toLowerCase());

        for (String phrase : negativeKeyphrases) {
            String pattern =
                    "((?:\\S+[ \\t]+){0,4})"
                    + Pattern.quote(phrase)
                    + "((?:[ \\t]+\\S+){0,4})";
            Pattern compiledPattern = Pattern.compile(pattern);
            Matcher matcher = compiledPattern.matcher(description);

            while (matcher.find()) {
                String after = " %s".format(matcher.group(2).trim());
                if (after.contains(product)) {
                    return true;
                }
            }
        }
        return false;
    }
}
