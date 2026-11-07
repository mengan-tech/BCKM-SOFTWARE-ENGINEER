package edu.itc.shop.service;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import edu.itc.shop.domain.Category;
import edu.itc.shop.domain.Product;
import edu.itc.shop.repository.ProductRepository;
import edu.itc.shop.repository.ProductSpecs;
import edu.itc.shop.web.dto.ProductView;

@Service
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository products;

    public ProductService(ProductRepository products) {
        this.products = products;
    }

    public Page<ProductView> search(String q, Category category, BigDecimal maxPrice, Boolean inStock, Pageable pageable) {
        var spec = ProductSpecs.filter(q, category, maxPrice, inStock);
        Page<Product> page = products.findAll(spec, pageable);
        List<ProductView> views = page.getContent().stream()
                .map(p -> new ProductView(p.getId(), p.getSku(), p.getName(),
                        p.getPrice().amount(), p.getStock()))
                .toList();
        return new PageImpl<>(views, pageable, page.getTotalElements());
    }
}
