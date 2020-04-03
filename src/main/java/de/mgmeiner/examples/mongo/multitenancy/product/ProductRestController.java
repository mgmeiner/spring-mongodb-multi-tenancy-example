package de.mgmeiner.examples.mongo.multitenancy.product;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class ProductRestController {

    private ProductRepository productRepository;

    public ProductRestController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @GetMapping("/products")
    public Flux<Product> products() {
        return productRepository.findAll();
    }

    @PutMapping("/products")
    public Mono<Product> put(@RequestBody Product product) {
        return productRepository.save(product);
    }
}
