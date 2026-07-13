package org.threatwatch.products;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.threatwatch.common.ApiResponseDto;
import org.threatwatch.cve.matching.ProductMatcher;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/products")
public class ProductsController {

    private final ProductMatcher productMatcherService;
    private final ProductsService productsService;

    public ProductsController(ProductMatcher productMatcherService, ProductsService productsService) {
        this.productMatcherService = productMatcherService;
        this.productsService = productsService;
    }

    @GetMapping
    public ResponseEntity<ApiResponseDto> retrieveProducts() {

        Map<String, List<ProductModel>> supportedProducts = productsService.getProducts();

        return ResponseEntity.ok(new ApiResponseDto(
                Instant.now(),
                UUID.randomUUID().toString(),
                "ok",
                supportedProducts
        ));
    }

    @GetMapping("/extract")
    public ResponseEntity<ApiResponseDto> extractProduct(@RequestBody String text) throws IOException {

        String product = productMatcherService.extractMainProduct(text);

        return ResponseEntity.ok(new ApiResponseDto(
                Instant.now(),
                UUID.randomUUID().toString(),
                "ok",
                product
        ));
    }
}
