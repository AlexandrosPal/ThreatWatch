package org.threatwatch.services.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.threatwatch.models.ProductModel;
import org.threatwatch.services.ProductsService;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

@Service
public class ProductsServiceImpl implements ProductsService {

    private Map<String, List<ProductModel>> supportedProducts;

    private Map<String, List<ProductModel>> loadProducts() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        InputStream is = new ClassPathResource("products.json").getInputStream();

        return mapper.readValue(is, new TypeReference<>() {});
    }

    @PostConstruct
    public void init() throws IOException {
        try {
            this.supportedProducts = loadProducts();
        } catch (Exception e) {
            throw new IOException("Failed to load products.json", e);
        }
    }

    @Override
    public Map<String, List<ProductModel>> getProducts() {
        return supportedProducts;
    }

    public boolean isSupportedProduct(String product) {
        return this.supportedProducts.keySet().stream()
                .anyMatch(p -> p.equalsIgnoreCase(product));
    }

    public String normalizeProduct(String product) {
        return this.supportedProducts.keySet().stream()
                .filter(p -> p.equalsIgnoreCase(product))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException("Unsupported product: " + product)
                );
    }
}
