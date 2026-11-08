package edu.itc.shop.service;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

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
import edu.itc.shop.web.dto.OrderDto;
import jakarta.persistence.EntityManagerFactory;

@SpringBootTest
class OrderServiceTest {

    @Autowired OrderService orderService;
    @Autowired OrderRepository orders;
    @Autowired ProductRepository products;
    @Autowired CustomerRepository customers;
    @Autowired EntityManagerFactory emf;

    private Statistics stats;
    private Long customerId;
    private Long productId;
    private Long orderId;

    @BeforeEach
    void setUp() {
        stats = emf.unwrap(SessionFactory.class).getStatistics();

        
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        Address address = new Address("1 Test St", "TestCity", "00001");

        Customer customer = customers.save(
                new Customer("Test User", "test-" + suffix + "@example.com", address, address));

        Product product = new Product("T-" + suffix, "Test Product", Money.eur("10.00"), Category.HARDWARE);
        product.setStock(5);
        product = products.save(product);

        Order order = orders.save(new Order(customer));   

        customerId = customer.getId();
        productId = product.getId();
        orderId = order.getId();
    }

    
    @AfterEach
    void cleanUp() {
        orders.deleteById(orderId);
        products.deleteById(productId);
        customers.deleteById(customerId);
    }

    
    @Test
    void markPaid_updatesManagedEntityWithoutSave() {
        stats.clear();

        orderService.markPaid(orderId);

        
        assertThat(stats.getEntityUpdateCount()).isEqualTo(1);
       
        assertThat(orders.findById(orderId).orElseThrow().getStatus()).isEqualTo(OrderStatus.PAID);
    }

    
    @Test
    void detachedEntity_changeIsLost_untilSaveMerges() {
        
        Product p = products.findById(productId).orElseThrow();
        p.setStock(99);   

       
        assertThat(products.findById(productId).orElseThrow().getStock()).isEqualTo(5);

        stats.clear();
        products.save(p);   

        
        assertThat(stats.getEntityLoadCount()).isEqualTo(1);
        assertThat(stats.getEntityUpdateCount()).isEqualTo(1);
        assertThat(products.findById(productId).orElseThrow().getStock()).isEqualTo(99);
    }
    

    
    @Test
    void view_readsOrderAsDto() {
        stats.clear();

        OrderDto dto = orderService.view(orderId);

        assertThat(dto.id()).isEqualTo(orderId);
        assertThat(dto.status()).isEqualTo(OrderStatus.NEW);
        assertThat(stats.getEntityUpdateCount()).isZero();
    }

    
    @Test
    void readOnlyTransaction_neverFlushesChanges() {
        stats.clear();

        orderService.markPaidInReadOnlyTransaction_BUG(orderId);   

        assertThat(stats.getEntityUpdateCount()).isZero();        
        assertThat(orders.findById(orderId).orElseThrow().getStatus())
                .isEqualTo(OrderStatus.NEW);                       
    }
}