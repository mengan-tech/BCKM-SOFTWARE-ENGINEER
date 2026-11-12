package edu.itc.shop.domain;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class InMemoryProductRepository implements ProductRepository {

    private final Map<Long, Product> store = new ConcurrentHashMap<>();
    private final AtomicLong idSequence = new AtomicLong(1);

    public InMemoryProductRepository() {
        save(new Product(0, "Kettle", "KT-01", new BigDecimal("24.90"), 12, 0));
        save(new Product(0, "Mug", "MG-01", new BigDecimal("6.50"), 40, 0));
        save(new Product(0, "Tea", "TE-01", new BigDecimal("4.20"), 0, 0));
    }

    @Override
    public List<Product> findAll() {
        return Collections.unmodifiableList(new java.util.ArrayList<>(store.values()));
    }

    @Override
    public Optional<Product> findById(long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public Optional<Product> findBySku(String sku) {
        return store.values().stream()
                .filter(p -> p.sku().equals(sku))
                .findFirst();
    }

    @Override
    public Product save(Product product) {
        long id;
        if (product.id() == 0) {
            id = idSequence.getAndIncrement();
        } else {
            id = product.id();
        }
        int version = product.version() + 1;
        Product updated = new Product(id, product.name(), product.sku(), product.price(), product.stock(), version);
        store.put(id, updated);
        return updated;
    }

    @Override
    public void delete(long id) {
        store.remove(id);
    }
}
