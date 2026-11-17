package edu.itc.shop.domain;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class ReviewRepository {
    private final Map<Long, Review> store = new ConcurrentHashMap<>();
    private final AtomicLong ids = new AtomicLong();

    public List<Review> findByProduct(long productId) {
        return store.values().stream()
                .filter(r -> r.productId() == productId)
                .sorted(Comparator.comparingLong(Review::id))
                .toList();
    }

    public Optional<Review> findById(long id) {
        return Optional.ofNullable(store.get(id));
    }

    // Assigns a new id when id == 0.
    public Review save(Review r) {
        long id = (r.id() == 0) ? ids.incrementAndGet() : r.id();
        Review saved = new Review(id, r.productId(), r.stars(), r.author(), r.text());
        store.put(id, saved);
        return saved;
    }
}