package edu.itc.shop.service;

import java.util.concurrent.ThreadLocalRandom;

import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import edu.itc.shop.domain.OrderStatus;

@Service
public class OrderFacade {

    private static final int MAX_ATTEMPTS = 3;

    private final OrderService orderService;

    public OrderFacade(OrderService orderService) {
        this.orderService = orderService;
    }

   
    public void changeStatusWithRetry(Long id, OrderStatus next) {
        for (int attempt = 1; ; attempt++) {
            try {
                orderService.changeStatus(id, next);   // a NEW transaction every attempt
                return;
            } catch (ObjectOptimisticLockingFailureException e) {
                if (attempt == MAX_ATTEMPTS) throw e;
                backOff(attempt);
            }
        }
    }

    
    private static void backOff(int attempt) {
        try {
            Thread.sleep(ThreadLocalRandom.current().nextLong(10, 50) * attempt);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while retrying", ie);
        }
    }
}