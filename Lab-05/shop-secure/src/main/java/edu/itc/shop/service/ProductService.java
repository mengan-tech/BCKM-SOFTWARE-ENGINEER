package edu.itc.shop.service;

import edu.itc.shop.domain.Product;
import edu.itc.shop.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProductService {

    private final ProductRepository repository;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<Product> findAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Product findById(Long id) {
        return repository.findById(id).orElseThrow(() -> new ProductNotFoundException(id));
    }

    public Product create(String name, BigDecimal price, int stock) {
        return repository.save(new Product(name, price, stock));
    }

    public void delete(Long id) {
        repository.delete(findById(id));
    }
}
