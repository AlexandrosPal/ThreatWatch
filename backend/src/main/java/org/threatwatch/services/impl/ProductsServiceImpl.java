package org.threatwatch.services.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.threatwatch.models.ProductModel;
import org.threatwatch.services.ProductsService;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;

@Service
public class ProductsServiceImpl implements ProductsService {

    private Map<String, ArrayList<ProductModel>> supportedProducts;

    private Map<String, ArrayList<ProductModel>> loadProducts() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        InputStream is = new ClassPathResource("products.json").getInputStream();

        return mapper.readValue(is, new TypeReference<Map<String, ArrayList<ProductModel>>>() {});
    }

    @PostConstruct
    public void init() {
        try {
            supportedProducts = loadProducts();
        } catch (Exception e) {
            throw new RuntimeException("Failed to load products.json", e);
        }
    }

    @Override
    public Map<String, ArrayList<ProductModel>> getProducts() {
        return supportedProducts;
    }
}
