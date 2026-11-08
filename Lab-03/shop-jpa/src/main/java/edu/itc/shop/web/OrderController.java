package edu.itc.shop.web;

import edu.itc.shop.domain.Order;
import edu.itc.shop.domain.OrderStatus;
import edu.itc.shop.repository.OrderRepository;
import edu.itc.shop.service.OrderService;
import edu.itc.shop.web.dto.OrderSummary;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class OrderController {
    private final OrderRepository orders;
    private final OrderService orderService;

    public OrderController(OrderRepository orders, OrderService orderService) {
        this.orders = orders;
        this.orderService = orderService;
    }

    @GetMapping("/orders")
    public Page<OrderSummary> list(@RequestParam OrderStatus status,
                                   @PageableDefault(size = 20, sort = "placedAt",
                                           direction = Sort.Direction.DESC) Pageable pageable) {
        return orders.findByStatus(status, pageable);
    }

    @GetMapping("/customers/{email}/orders")
    public List<Order> customerOrders(@PathVariable String email) {
        return orderService.findWithLinesByCustomerEmail(email);
    }
}