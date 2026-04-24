package uk.co.sainsburys.interview.sainsburys.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.co.sainsburys.interview.sainsburys.service.ProductService;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping("/ingest")
    public ResponseEntity<Void> ingest() {
        productService.ingestProducts();
        return ResponseEntity.accepted().build();
    }
}
