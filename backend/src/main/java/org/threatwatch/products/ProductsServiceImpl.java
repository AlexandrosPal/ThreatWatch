package org.threatwatch.products;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.threatwatch.loggers.AppLogger;
import org.threatwatch.loggers.LogEvents;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ProductsServiceImpl implements ProductsService {

    private List<HashMap<String, Object>> supportedProducts;

    private static final AppLogger appLogger = new AppLogger(LoggerFactory.getLogger(ProductsServiceImpl.class));

    private List<HashMap<String, Object>> loadProducts() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        InputStream is = new ClassPathResource("supported_products.json").getInputStream();

        return mapper.readValue(is, new TypeReference<>() {});
    }

    @PostConstruct
    public void init() throws IOException {
        try {
            this.supportedProducts = loadProducts();
        } catch (Exception e) {
            appLogger.error(LogEvents.FILE_READ_ERROR, "Error while trying to read products,json file", e);
            throw new IOException("Failed to load products.json", e);
        }
    }

    @Override
    public Set<String> getProducts() {
        return this.supportedProducts.stream()
                .map(prod -> (String) prod.get("name"))
                .collect(Collectors.toSet());
    }

    public boolean isSupportedProduct(String product) {
        return this.supportedProducts.stream()
                .map(prod -> (String) prod.get("name"))
                .anyMatch(name -> name.equalsIgnoreCase(product));
    }

    public String normalizeProduct(String product) {
        return this.supportedProducts.stream()
                .map(prod -> (String) prod.get("name"))
                .filter(name -> name.equalsIgnoreCase(product))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException("Unsupported product: " + product)
                );
    }
}
