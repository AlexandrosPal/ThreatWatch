package org.threatwatch.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.threatwatch.dtos.ApiResponseDto;
import org.threatwatch.models.ProductModel;
import org.threatwatch.services.ProductsService;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/api/products")
public class ProductsController {

    public ProductsService productsService;

    public ProductsController(ProductsService productsService) {
        this.productsService = productsService;
    }

    @GetMapping
    public ResponseEntity<ApiResponseDto> retrieveProducts() {

        Map<String, ArrayList<ProductModel>> supportedProducts = productsService.getProducts();

        return ResponseEntity.ok(new ApiResponseDto(
                Instant.now(),
                UUID.randomUUID().toString(),
                "ok",
                supportedProducts
        ));

    }

}
