package edu.itc.shop.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.itc.shop.domain.Money;
import edu.itc.shop.domain.Order;
import edu.itc.shop.domain.OrderLine;
import edu.itc.shop.domain.OrderStatus;
import edu.itc.shop.repository.OrderRepository;
import edu.itc.shop.web.dto.OrderDto;
import edu.itc.shop.web.dto.OrderLineDto;
import edu.itc.shop.web.dto.OrderSummary;

@Service
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orders;

    public OrderService(OrderRepository orders) {
        this.orders = orders;
    }

    public Page<OrderSummary> findByStatus(OrderStatus status, Pageable pageable) {
        return orders.findByStatus(status, pageable);
    }

    public List<OrderDto> ordersOfCustomer(String email) {
        return orders.findWithLinesByCustomerEmail(email).stream()
                .map(OrderService::toOrderDto)
                .toList();
    }

    private static OrderDto toOrderDto(Order order) {
        List<OrderLineDto> lines = order.getLines().stream()
                .map(OrderService::toLineDto)
                .toList();
        Money total = order.total();
        return new OrderDto(order.getId(), order.getStatus(), order.getPlacedAt(),
                total.amount(), total.currency(), lines);
    }

    private static OrderLineDto toLineDto(OrderLine line) {
        return new OrderLineDto(
                line.getId(),
                line.getProduct().getSku(),
                line.getProduct().getName(),
                line.getQuantity(),
                line.getUnitPrice().amount(),
                line.getUnitPrice().times(line.getQuantity()).amount());
    }

    @Transactional
    public void markPaid(Long orderId) {
        Order order = orders.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
        order.setStatus(OrderStatus.PAID);
    }

    @Transactional(readOnly = true)
    public OrderDto view(Long id) {
        Order order = orders.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + id));
        return toOrderDto(order);
    }

    @Transactional(readOnly = true)
    public void markPaidInReadOnlyTransaction_BUG(Long id) {
        Order order = orders.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + id));
        order.setStatus(OrderStatus.PAID);
    }
    @Transactional
    public void changeStatus(Long id, OrderStatus next) {
        Order order = orders.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + id));
        order.setStatus(next);
    }
}