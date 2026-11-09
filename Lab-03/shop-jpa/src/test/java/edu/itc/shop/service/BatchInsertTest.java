package edu.itc.shop.service;

import java.util.ArrayList;
import java.util.List;
import org.hibernate.LazyInitializationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionTemplate;
import edu.itc.shop.domain.Category;
import edu.itc.shop.domain.Money;
import edu.itc.shop.domain.Order;
import edu.itc.shop.domain.Product;
import edu.itc.shop.repository.OrderRepository;
import edu.itc.shop.repository.ProductRepository;
import edu.itc.shop.support.QueryCounter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class BatchInsertTest {

    @Autowired ProductRepository products;
    @Autowired OrderRepository orders;
    @Autowired TransactionTemplate tx;
    @Autowired QueryCounter counter;
    @Autowired TestData data;

    @Test
    void saveAllBatchesThousandProductsInFarFewerStatements() {
        counter.reset();

        tx.executeWithoutResult(status -> {
            List<Product> batch = new ArrayList<>(1000);
            long runId = System.nanoTime();
            for (int i = 0; i < 1000; i++) {
                Product p = new Product(
                        "B-" + runId + "-" + i,
                        "Batch Product " + i,
                        Money.eur("12.50"),
                        Category.HARDWARE
                );
                p.setStock(10);
                batch.add(p);
            }
            products.saveAll(batch);
            products.flush();
        });

        
        assertThat(counter.inserts()).isEqualTo(1000);

        assertThat(counter.count()).isLessThan(100);
    }

    @Test
    void openInViewFalseThrowsLazyInitializationOutsideTransaction() {
        Long orderId = data.newOrder();


        Order detached = orders.findById(orderId).orElseThrow();


        assertThatThrownBy(() -> detached.getLines().size())
                .isInstanceOf(LazyInitializationException.class);
    }
}