package edu.itc.shop.web;

import java.math.BigDecimal;

import edu.itc.shop.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import edu.itc.shop.domain.Category;
import edu.itc.shop.web.dto.ProductView;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public Page<ProductView> list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Category category,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Boolean inStock,
            @PageableDefault(size = 20) Pageable pageable) {
        return productService.search(q, category, maxPrice, inStock, pageable);
    }
}
