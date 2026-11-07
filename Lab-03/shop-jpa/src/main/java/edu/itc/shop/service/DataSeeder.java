package edu.itc.shop.service;

import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

@Configuration
@Profile("dev")
class DataSeeder {

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

    @Bean
    ApplicationRunner seedRunner(Seeder seeder) {
        return args -> seeder.seed();
    }

    @Component
    @Profile("dev")
    static class Seeder {
        private final CustomerRepository customers;
        private final ProductRepository products;
        private final OrderRepository orders;
        private final EntityManager em;

        Seeder(CustomerRepository customers, ProductRepository products,
               OrderRepository orders, EntityManager em) {
            this.customers = customers;
            this.products = products;
            this.orders = orders;
            this.em = em;
        }

        @Transactional
        void seed() {
            // 3 customers
            Customer c1 = customers.save(new Customer(
                    "Ada Lovelace", "ada@example.com",
                    new Address("1 Math St", "London", "EC1A"),
                    new Address("1 Math St", "London", "EC1A")));
            Customer c2 = customers.save(new Customer(
                    "Grace Hopper", "grace@example.com",
                    new Address("2 Code Ave", "Boston", "02101"),
                    new Address("2 Code Ave", "Boston", "02101")));
            Customer c3 = customers.save(new Customer(
                    "Alan Turing", "alan@example.com",
                    new Address("3 Logic Rd", "Cambridge", "CB1"),
                    new Address("3 Logic Rd", "Cambridge", "CB1")));
            Customer[] custs = {c1, c2, c3};

            // Tags - persist them first
            Tag[] tags = new Tag[TAG_NAMES.length];
            for (int i = 0; i < TAG_NAMES.length; i++) {
                tags[i] = new Tag(TAG_NAMES[i]);
                em.persist(tags[i]);
            }
            em.flush();

            // 12 products with tags
            Random rnd = new Random(42);
            Product[] prods = new Product[12];
            for (int i = 0; i < 12; i++) {
                Money price = Money.eur(String.valueOf(rnd.nextInt(500) + 10));
                Product p = new Product(SKUS[i], NAMES[i], price, CATEGORIES[i]);
                p.setStock(rnd.nextInt(30));
                // assign 1-3 random tags
                int tagCount = rnd.nextInt(3) + 1;
                for (int t = 0; t < tagCount; t++) {
                    p.tag(tags[rnd.nextInt(tags.length)]);
                }
                prods[i] = products.save(p);
            }

            // 30 orders with 2-4 lines each, mixed statuses
            OrderStatus[] statuses = OrderStatus.values();
            List<Order> allOrders = new ArrayList<>();
            for (int i = 0; i < 30; i++) {
                Customer cust = custs[rnd.nextInt(custs.length)];
                OrderStatus status = statuses[rnd.nextInt(statuses.length)];
                Instant placedAt = Instant.now().minusSeconds(rnd.nextInt(86400 * 30));
                Order order = new Order(cust, status, placedAt);
                int lineCount = rnd.nextInt(3) + 2; // 2-4 lines
                for (int l = 0; l < lineCount; l++) {
                    Product prod = prods[rnd.nextInt(prods.length)];
                    order.addLine(prod, rnd.nextInt(3) + 1);
                }
                allOrders.add(order);
            }
            orders.saveAll(allOrders);
        }
    }
}
