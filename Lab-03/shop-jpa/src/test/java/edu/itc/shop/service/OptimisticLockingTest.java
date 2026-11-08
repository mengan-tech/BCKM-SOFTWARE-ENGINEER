package edu.itc.shop.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;

import edu.itc.shop.domain.Order;
import edu.itc.shop.domain.OrderStatus;
import edu.itc.shop.repository.OrderRepository;

@SpringBootTest
@ActiveProfiles("test")   
class OptimisticLockingTest {

    @Autowired OrderRepository orders;
    @Autowired TransactionTemplate tx;
    @Autowired TestData data;
    @Autowired OrderFacade facade;

    @AfterEach
    void cleanUp() {
        data.cleanUp();
    }

    
    @Test
    void secondWriterFailsWithOptimisticLock() throws Exception {
        Long orderId = data.newOrder();
        long versionBefore = orders.findById(orderId).orElseThrow().getVersion();
        CountDownLatch bothLoaded = new CountDownLatch(2);

        Function<OrderStatus, Callable<Throwable>> writer = target -> () -> {
            try {
                tx.executeWithoutResult(status -> {
                    Order o = orders.findById(orderId).orElseThrow();   
                    bothLoaded.countDown();
                    await(bothLoaded);
                    o.setStatus(target);                                
                });
                return null;
            } catch (RuntimeException e) { return e; }
        };

        List<OrderStatus> targets = List.of(OrderStatus.PAID, OrderStatus.CANCELLED);

        try (ExecutorService pool = Executors.newFixedThreadPool(2)) {
            List<Future<Throwable>> results = pool.invokeAll(
                    List.of(writer.apply(targets.get(0)), writer.apply(targets.get(1))));
            List<Throwable> outcomes = results.stream().map(this::get).toList();   
            List<Throwable> failures = outcomes.stream().filter(Objects::nonNull).toList();

            assertThat(failures).hasSize(1);
            assertThat(failures.get(0)).isInstanceOf(ObjectOptimisticLockingFailureException.class);
            Order after = orders.findById(orderId).orElseThrow();
            assertThat(after.getVersion()).isEqualTo(versionBefore + 1);

            // the winner is the writer whose outcome is null (no exception)
            OrderStatus winner = outcomes.get(0) == null ? targets.get(0) : targets.get(1);
            assertThat(after.getStatus()).isEqualTo(winner);
        }
    }

    
    @Test
    void withRetryBothWritersSucceed() throws Exception {
        Long orderId = data.newOrder();
        long versionBefore = orders.findById(orderId).orElseThrow().getVersion();
        CountDownLatch start = new CountDownLatch(1);   

        Function<OrderStatus, Callable<Throwable>> writer = target -> () -> {
            try {
                await(start);
                facade.changeStatusWithRetry(orderId, target);
                return null;
            } catch (RuntimeException e) { return e; }
        };

        try (ExecutorService pool = Executors.newFixedThreadPool(2)) {
            List<Future<Throwable>> results = new ArrayList<>();
            results.add(pool.submit(writer.apply(OrderStatus.PAID)));
            results.add(pool.submit(writer.apply(OrderStatus.CANCELLED)));
            start.countDown();   

            List<Throwable> failures = results.stream().map(this::get).filter(Objects::nonNull).toList();

            assertThat(failures).isEmpty();                                  
            Order after = orders.findById(orderId).orElseThrow();
            assertThat(after.getVersion()).isEqualTo(versionBefore + 2);       
            assertThat(after.getStatus()).isIn(OrderStatus.PAID, OrderStatus.CANCELLED);
        }
    }

    private static void await(CountDownLatch l) {
        try { if (!l.await(5, TimeUnit.SECONDS)) throw new IllegalStateException("latch timeout"); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new IllegalStateException(e); }
    }
    private Throwable get(Future<Throwable> f) {
        try { return f.get(); } catch (Exception e) { return e; }
    }
}