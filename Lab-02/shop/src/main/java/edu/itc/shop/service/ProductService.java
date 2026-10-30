package edu.itc.shop.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import edu.itc.shop.domain.Product;
import edu.itc.shop.repository.ProductRepository;

@Service
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository repo;

    public ProductService(ProductRepository repo) {
        this.repo = repo;
    }

    /* --- read-only (no @Transactional needed) --- */

    public List<Product> search(String q) {
        if (q == null || q.isBlank()) {
            return repo.findAll();
        }
        return repo.findByNameContaining(q);
    }

    public Optional<Product> findById(long id) {
        return repo.findById(id);
    }

    /* --- write operations --- */

    @Transactional
    public Product create(String name, BigDecimal price, int stock) {
        Product product = new Product(null, name, price, stock);
        long id = repo.insert(product);
        log.info("Created product id={} name='{}' price={} stock={}", id, name, price, stock);
        return new Product(id, name, price, stock);
    }

    @Transactional
    public Product updateStock(long id, int delta) {
        Product product = repo.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        int rows = repo.updateStock(id, delta);
        if (rows == 0) {
            throw new InsufficientStockException(id, product.stock(), -delta);
        }
        return repo.findById(id).orElseThrow();
    }

    @Transactional
    public void delete(long id) {
        if (repo.delete(id) == 0) {
            throw new ProductNotFoundException(id);
        }
    }

    @Transactional
    public int deleteAll() {
        return repo.deleteAll();
    }

    @Transactional
    public void insertSeedData() {
        repo.insertSeedData();
    }

    @Transactional
    public void resetSeedData() {
        repo.deleteAll();
        repo.insertSeedData();
    }

    @Transactional
    public void restock(long fromId, long toId, int qty) {
        Product fromProduct = repo.findById(fromId).orElseThrow(() -> new ProductNotFoundException(fromId));
        repo.findById(toId).orElseThrow(() -> new ProductNotFoundException(toId));

        int removed = repo.updateStock(fromId, -qty);
        if (removed == 0) {
            throw new InsufficientStockException(fromId, fromProduct.stock(), qty);
        }

        int added = repo.updateStock(toId, qty);
        if (added == 0) {
            throw new ProductNotFoundException(toId);
        }
    }
}