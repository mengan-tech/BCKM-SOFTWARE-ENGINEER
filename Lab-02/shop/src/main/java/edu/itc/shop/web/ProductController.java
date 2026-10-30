package edu.itc.shop.web;

import java.net.URI;
import java.util.List;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import edu.itc.shop.domain.Product;
import edu.itc.shop.service.ProductNotFoundException;
import edu.itc.shop.service.ProductService;
import edu.itc.shop.web.dto.CreateProduct;
import edu.itc.shop.web.dto.ProductDto;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    /* GET /api/products[?q=keyword] */
    @GetMapping
    public List<ProductDto> list(@RequestParam(required = false) String q) {
        return service.search(q).stream().map(ProductDto::from).toList();
    }

    /* GET /api/products/{id} */
    @GetMapping("/{id}")
    public ProductDto getById(@PathVariable long id) {
        return ProductDto.from(
            service.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id))
        );
    }

    /* POST /api/products */
    @PostMapping
    public ResponseEntity<ProductDto> create(@RequestBody @Valid CreateProduct cmd) {
        Product created = service.create(cmd.name(), cmd.price(), cmd.stock());
        ProductDto dto = ProductDto.from(created);
        return ResponseEntity.created(URI.create("/api/products/" + dto.id())).body(dto);
    }

    /* PATCH /api/products/{id}/stock */
    @PatchMapping("/{id}/stock")
    public ResponseEntity<ProductDto> updateStock(@PathVariable long id,
                                                   @RequestBody StockUpdate update) {
        Product updated = service.updateStock(id, update.delta());
        return ResponseEntity.ok(ProductDto.from(updated));
    }

    /* --- nested record for PATCH body --- */
    public record StockUpdate(int delta) {

    }

    /* DELETE /api/products/{id} */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    /* DELETE /api/products — clear all data */
    @DeleteMapping
    public ResponseEntity<Void> deleteAll() {
        service.deleteAll();
        return ResponseEntity.noContent().build();
    }

    /* POST /api/products/seed — re-insert data.sql seed data */
    @PostMapping("/seed")
    public ResponseEntity<Void> seed() {
        service.insertSeedData();
        return ResponseEntity.ok().build();
    }

    /* POST /api/products/reset — clear all data and re-insert seed data */
    @PostMapping("/reset")
    public ResponseEntity<Void> reset() {
        service.resetSeedData();
        return ResponseEntity.ok().build();
    }

}