package edu.itc.shop.repository;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.springframework.data.jpa.domain.Specification;
import edu.itc.shop.domain.Category;
import edu.itc.shop.domain.Money;
import edu.itc.shop.domain.Product;

public final class ProductSpecs {
    private ProductSpecs() { }

    public static Specification<Product> nameContains(String q) {
        return (root, query, cb) ->
                cb.like(cb.lower(root.<String>get("name")), "%" + q.toLowerCase() + "%");
    }

    public static Specification<Product> inCategory(Category category) {
        return (root, query, cb) ->
                cb.equal(root.get("category"), category);
    }

    public static Specification<Product> cheaperThan(BigDecimal max) {
        return (root, query, cb) ->
                cb.le(root.<Money>get("price").<BigDecimal>get("amount"), max);
    }

    public static Specification<Product> inStock() {
        return (root, query, cb) ->
                cb.gt(root.get("stock"), 0);
    }

    public static Specification<Product> filter(String q, Category category, BigDecimal max, Boolean inStock) {
        List<Specification<Product>> specs = Arrays.asList(
                q != null && !q.isBlank() ? nameContains(q) : null,
                category != null ? inCategory(category) : null,
                max != null ? cheaperThan(max) : null,
                Boolean.TRUE.equals(inStock) ? inStock() : null
        );
        return specs.stream().filter(Objects::nonNull).reduce(Specification::and).orElse(null);
    }

}