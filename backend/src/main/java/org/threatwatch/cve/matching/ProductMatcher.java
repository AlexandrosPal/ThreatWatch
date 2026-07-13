package org.threatwatch.cve.matching;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProductMatcher {
    DescriptionMatcher descriptionMatcher = new DescriptionMatcher();

    public ProductMatcher(DescriptionMatcher descriptionMatcher) {
        this.descriptionMatcher = descriptionMatcher;
    }

    private <T> T loadJsonFile(String filePath) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        InputStream is = new ClassPathResource(filePath + ".json").getInputStream();

        return mapper.readValue(is, new TypeReference<>() {});
    }

    public String extractMainProduct(String text) throws IOException {
        HashMap<String, Integer> scores = new HashMap<>();
        List<HashMap<String, Object>> products = loadJsonFile("supported_products");

        for (HashMap<String, Object> productInfo : products) {
            int score = 0;
            List<String> aliases = (List<String>) productInfo.get("aliases");
            for (String alias : aliases) {
                if (this.descriptionMatcher.keywordMatch(text, alias)) {
                    score += 5;
                }
                if (this.descriptionMatcher.negativeKeywordMatch(text, alias)) {
                    score -= 10;
                }
                if (this.descriptionMatcher.boostedKeywordMatch(text, alias)) {
                    score += 10;
                }
                if (this.descriptionMatcher.insideFirstWords(text, alias)) {
                    score += 15;
                }
                if (this.descriptionMatcher.insideFirstSentence(text, alias)) {
                    score += 10;
                }
            }
            scores.put((String) productInfo.get("name"), score);
        }
        String bestProduct = Collections.max(
                scores.entrySet(),
                Map.Entry.comparingByValue()
        ).getKey();

        if (scores.get(bestProduct) > 10) {
            return bestProduct;
        }
        return null;
    }
}
