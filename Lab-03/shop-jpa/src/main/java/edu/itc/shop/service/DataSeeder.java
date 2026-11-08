package edu.itc.shop.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import edu.itc.shop.domain.Address;
import edu.itc.shop.domain.Category;
import edu.itc.shop.domain.Customer;
import edu.itc.shop.domain.Money;
import edu.itc.shop.domain.Order;
import edu.itc.shop.domain.OrderStatus;
import edu.itc.shop.domain.Product;
import edu.itc.shop.domain.Tag;
import edu.itc.shop.repository.CustomerRepository;
import edu.itc.shop.repository.OrderRepository;
import edu.itc.shop.repository.ProductRepository;
import edu.itc.shop.repository.TagRepository;

@Component
@Profile("dev")   // only runs with --spring.profiles.active=dev
public class DataSeeder implements ApplicationRunner {

    private static final String[] NAMES = {
            "Mechanical Keyboard", "Wireless Mouse", "USB-C Hub", "Monitor Stand",
            "Webcam HD", "Desk Lamp", "Headphones", "Mousepad XL",
            "Cable Organizer", "Laptop Stand", "Keyboard Wrist Rest", "Screen Cleaner"
    };
    private static final String[] SKUS = {
            "KB-1", "MS-2", "HB-3", "ST-4", "WC-5", "LP-6",
            "HP-7", "MP-8", "CO-9", "LS-10", "WR-11", "SC-12"
    };
    private static final Category[] CATEGORIES = {
            Category.HARDWARE, Category.HARDWARE, Category.HARDWARE, Category.HARDWARE,
            Category.HARDWARE, Category.HARDWARE, Category.HARDWARE, Category.HARDWARE,
            Category.TOYS, Category.TOYS, Category.TOYS, Category.TOYS
    };
    private static final String[] TAG_NAMES = {
            "featured", "sale", "new", "popular", "clearance"
    };

    private final CustomerRepository customers;
    private final ProductRepository products;
    private final TagRepository tags;
    private final OrderRepository orders;

    public DataSeeder(CustomerRepository customers, ProductRepository products,
                      TagRepository tags, OrderRepository orders) {
        this.customers = customers;
        this.products = products;
        this.tags = tags;
        this.orders = orders;
    }

    @Override
    @Transactional   
    public void run(ApplicationArguments args) {
        if (customers.count() > 0) {
            return;   
        }

        // 3 customers
        List<Customer> custs = customers.saveAll(List.of(
                new Customer("Ada Lovelace", "ada@example.com",
                        new Address("1 Math St", "London", "EC1A"),
                        new Address("1 Math St", "London", "EC1A")),
                new Customer("Grace Hopper", "grace@example.com",
                        new Address("2 Code Ave", "Boston", "02101"),
                        new Address("2 Code Ave", "Boston", "02101")),
                new Customer("Alan Turing", "alan@example.com",
                        new Address("3 Logic Rd", "Cambridge", "CB1"),
                        new Address("3 Logic Rd", "Cambridge", "CB1"))));

        
        List<Tag> tagList = new ArrayList<>();
        for (String name : TAG_NAMES) {
            tagList.add(tags.save(new Tag(name)));
        }

        // 12 products with 1-3 tags
        Random rnd = new Random(42);
        List<Product> prods = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            Money price = Money.eur(String.valueOf(rnd.nextInt(500) + 10));
            Product p = new Product(SKUS[i], NAMES[i], price, CATEGORIES[i]);
            p.setStock(rnd.nextInt(30));
            int tagCount = rnd.nextInt(3) + 1;
            for (int t = 0; t < tagCount; t++) {
                p.tag(tagList.get(rnd.nextInt(tagList.size())));
            }
            prods.add(products.save(p));
        }

        // 30 orders with 2-4 lines each
        OrderStatus[] statuses = OrderStatus.values();
        List<Order> allOrders = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            Customer cust = custs.get(rnd.nextInt(custs.size()));
            OrderStatus status = statuses[rnd.nextInt(statuses.length)];
            Instant placedAt = Instant.now().minusSeconds(rnd.nextInt(86400 * 30));
            Order order = new Order(cust, status, placedAt);
            int lineCount = rnd.nextInt(3) + 2; 
            for (int l = 0; l < lineCount; l++) {
                Product prod = prods.get(rnd.nextInt(prods.size()));
                order.addLine(prod, rnd.nextInt(3) + 1);
            }
            allOrders.add(order);
        }
        orders.saveAll(allOrders); 
    }
}