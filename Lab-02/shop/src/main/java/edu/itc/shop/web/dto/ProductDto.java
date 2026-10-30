package edu.itc.shop.web.dto;

import edu.itc.shop.domain.Product;
import java.math.BigDecimal;

public record ProductDto(long id, String name, BigDecimal price, int stock) {

    public static ProductDto from(Product product) {
        return new ProductDto(
            product.id(),
            product.name(),
            product.price(),
            product.stock()
        );
    }
}
