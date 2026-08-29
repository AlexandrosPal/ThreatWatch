package org.threatwatch.ProductMatcherTests;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.threatwatch.cve.matching.ProductMatcher;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@ExtendWith(MockitoExtension.class)
public class ProductMatcherMongoDBTest {
    @InjectMocks
    ProductMatcher productMatcherService;
    Set<String> products;

    @BeforeEach
    void setUp() throws IOException {
        List<Map<String, Object>> productsObject = loadJsonFile("supported_products");
        products = productsObject.stream()
                .map(product -> (String) product.get("name"))
                .collect(Collectors.toSet());
    }

    private <T> T loadJsonFile(String filePath) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        InputStream is = new ClassPathResource(filePath + ".json").getInputStream();

        return mapper.readValue(is, new TypeReference<>() {});
    }

    @Test
    void MongoDBTestOne() throws IOException {
        String description = "When mongosqld is configured with a client certificate authority file, the listener requests a client certificate during the TLS handshake but does not require one, so a client that presents no certificate is still accepted. In deployments that rely on client certificates as the sole means of identifying users, a remote party with network access to the listener can therefore establish a session and read the MongoDB data exposed through the connector.";
        
        Assertions.assertTrue(productMatcherService.extractMainProduct(description, products).equalsIgnoreCase("MongoDB"));
    }

}
