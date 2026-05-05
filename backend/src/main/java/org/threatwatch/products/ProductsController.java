package org.threatwatch.products;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.threatwatch.common.ApiResponseDto;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/products")
public class ProductsController {

    private final ProductsService productsService;

    public ProductsController(ProductsService productsService) {
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

}
