package edu.itc.shop.config;

import java.util.List;

import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

import edu.itc.shop.repository.ProductRepository;

@Component("lowStock")
public class LowStockHealthIndicator implements HealthIndicator {

    private final ProductRepository repo;
    private final ShopProperties props;

    public LowStockHealthIndicator(ProductRepository repo, ShopProperties props) {
        this.repo = repo;
        this.props = props;
    }

    @Override
    public Health health() {
        int threshold = props.lowStockThreshold();
        List<String> lowStockProducts = repo.findAll().stream()
                .filter(p -> p.stock() < threshold)
                .map(p -> p.name() + " (stock=" + p.stock() + ")")
                .toList();

        if (!lowStockProducts.isEmpty()) {
            return Health.down()
                    .withDetail("threshold", threshold)
                    .withDetail("lowStockProducts", lowStockProducts)
                    .build();
        }
        return Health.up()
                .withDetail("threshold", threshold)
                .build();
    }
}