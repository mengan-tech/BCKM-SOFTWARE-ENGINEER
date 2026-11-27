package edu.itc.shop.service;

import edu.itc.shop.domain.Order;
import edu.itc.shop.domain.OrderStatus;
import edu.itc.shop.repository.OrderRepository;
import edu.itc.shop.repository.ProductRepository;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrderService {
    
    private final OrderRepository orders;
    private final ProductRepository products;

    public OrderService(OrderRepository orders, ProductRepository products){
        this.orders = orders;
        this.products = products;
    }

   @Transactional
    @PreAuthorize("#customerId == authentication.name")
    public Order place(String customerId, PlaceOrder cmd) {
        if (!products.existsById(cmd.productId())) {
            throw new ProductNotFoundException(cmd.productId());
        }
        return orders.save(new Order(customerId, cmd.productId(), cmd.quantity()));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN') or #customerId == authentication.name")
    public List<Order> history(String customerId) {
        return orders.findByCustomer(customerId);
    }

    @Transactional(readOnly = true)
    @PostAuthorize("hasRole('ADMIN') or returnObject.customer == authentication.name")
    public Order get(long id) {
        return orders.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public Order ship(long id) {
        Order order = orders.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
        order.setStatus(OrderStatus.SHIPPED);
        return orders.save(order);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public List<Order> all() {
        return orders.findAll();
    }
}