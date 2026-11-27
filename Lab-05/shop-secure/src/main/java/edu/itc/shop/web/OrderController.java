package edu.itc.shop.web;

import edu.itc.shop.domain.Order;
import edu.itc.shop.service.OrderService;
import edu.itc.shop.service.PlaceOrder;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orders;

    public OrderController(OrderService orders) {
        this.orders = orders;
    }

    @PostMapping
    public ResponseEntity<Order> place(@Valid @RequestBody PlaceOrder cmd, Authentication auth) {
        // Customer always comes from the authenticated principal, never the request body
        Order saved = orders.place(auth.getName(), cmd);
        return ResponseEntity.created(URI.create("/api/orders/" + saved.getId())).body(saved);
    }

    @GetMapping
    public List<Order> history(@RequestParam(required = false) String customer, Authentication auth) {
        String target = (customer == null || customer.isBlank()) ? auth.getName() : customer;
        return orders.history(target);
    }

    @GetMapping("/{id}")
    public Order get(@PathVariable long id) {
        return orders.get(id);
    }

    @PostMapping("/{id}/ship")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void ship(@PathVariable long id) {
        orders.ship(id);
    }
}