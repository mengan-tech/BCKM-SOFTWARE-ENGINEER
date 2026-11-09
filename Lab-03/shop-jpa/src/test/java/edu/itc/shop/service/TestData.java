package edu.itc.shop.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import edu.itc.shop.domain.Address;
import edu.itc.shop.domain.Category;
import edu.itc.shop.domain.Customer;
import edu.itc.shop.domain.Money;
import edu.itc.shop.domain.Order;
import edu.itc.shop.domain.OrderStatus;
import edu.itc.shop.domain.Product;
import edu.itc.shop.repository.CustomerRepository;
import edu.itc.shop.repository.OrderRepository;
import edu.itc.shop.repository.ProductRepository;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TestData {

    private final CustomerRepository customers;
    private final ProductRepository products;
    private final OrderRepository orders;

    private final List<Long> orderIds = new ArrayList<>();
    private final List<Long> productIds = new ArrayList<>();
    private final List<Long> customerIds = new ArrayList<>();

    public TestData(CustomerRepository customers, ProductRepository products, OrderRepository orders) {
        this.customers = customers;
        this.products = products;
        this.orders = orders;
    }

    public Long newOrder() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        Address address = new Address("1 Lock St", "LockCity", "00001");

        Customer customer = customers.save(
                new Customer("Lock Test", "lock-" + suffix + "@example.com", address, address));

        Product product = new Product("L-" + suffix, "Lock Product", Money.eur("10.00"), Category.HARDWARE);
        product.setStock(10);
        product = products.save(product);

        Order order = new Order(customer);
        order.addLine(product, 1);
        order = orders.save(order);

        customerIds.add(customer.getId());
        productIds.add(product.getId());
        orderIds.add(order.getId());
        return order.getId();
    }

    public void cleanUp() {
        orderIds.forEach(orders::deleteById);
        productIds.forEach(products::deleteById);
        customerIds.forEach(customers::deleteById);
        orderIds.clear();
        productIds.clear();
        customerIds.clear();
    }

    @Transactional
    public void paidOrders(int count, int linesPerOrder) {

        if (orders.countByStatus(OrderStatus.PAID) == count) {
            return;
        }

        orders.deleteAll();
        orders.flush();

        Customer customer = customers.save(new Customer(
                "Report Customer",
                "report-" + System.nanoTime() + "@example.com",
                new Address("123 Main St", "City", "10000"),
                new Address("123 Main St", "City", "10000")));

        List<Product> productList = new ArrayList<>();
        for (int j = 0; j < linesPerOrder; j++) {
            Product p = new Product(
                    "RPT-" + j + "-" + UUID.randomUUID().toString().substring(0, 8),
                    "Report Product " + j,
                    Money.eur("15.00"),
                    Category.HARDWARE);
            p.setStock(500);
            productList.add(products.save(p));
        }

        for (int i = 0; i < count; i++) {
            Order order = new Order(customer);
            order.setStatus(OrderStatus.PAID);
            for (int j = 0; j < linesPerOrder; j++) {
                order.addLine(productList.get(j), 1);
            }
            orders.save(order);
        }
        orders.flush();
    }
}