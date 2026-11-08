package edu.itc.shop.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import edu.itc.shop.domain.Order;
import edu.itc.shop.repository.OrderRepository;

@Service
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orders;

    public OrderService(OrderRepository orders) {
        this.orders = orders;
    }

    public List<Order> findWithLinesByCustomerEmail(String email) {
        return orders.findWithLinesByCustomerEmail(email);
    }
}
