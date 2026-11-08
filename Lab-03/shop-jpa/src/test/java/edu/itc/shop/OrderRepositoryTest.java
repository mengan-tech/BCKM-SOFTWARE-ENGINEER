package edu.itc.shop;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import edu.itc.shop.domain.Address;
import edu.itc.shop.domain.Category;
import edu.itc.shop.domain.Customer;
import edu.itc.shop.domain.Money;
import edu.itc.shop.domain.Order;
import edu.itc.shop.domain.OrderStatus;
import edu.itc.shop.domain.Product;
import edu.itc.shop.repository.OrderRepository;
import edu.itc.shop.repository.ProductRepository;
import edu.itc.shop.web.dto.OrderSummary;
import edu.itc.shop.web.dto.ProductView;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)   // Flyway + validate, as in Task 1
class OrderRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private OrderRepository orderRepo;

    @Autowired
    private ProductRepository productRepo;

    private Long paidOrderId;

    @BeforeEach
    void setUp() {
        Customer customer = em.persist(new Customer(
                "Test User", "test@example.com",
                new Address("1 Test St", "TestCity", "00001"),
                new Address("1 Test St", "TestCity", "00001")));

        Product product1 = new Product("SKU-001", "Product One", Money.eur("29.99"), Category.HARDWARE);
        product1.setStock(10);
        em.persist(product1);
        Product product2 = new Product("SKU-002", "Product Two", Money.eur("49.99"), Category.TOYS);
        product2.setStock(5);
        em.persist(product2);

        Order order1 = new Order(customer, OrderStatus.PAID, Instant.now().minusSeconds(3600));
        order1.addLine(product1, 2);
        order1.addLine(product2, 1);
        em.persist(order1);

        Order order2 = new Order(customer, OrderStatus.NEW, Instant.now());
        order2.addLine(product1, 1);
        em.persist(order2);

        em.flush();
        em.clear();   // every test starts with an empty persistence context
        paidOrderId = order1.getId();
    }

    // getLines() returns Collections.unmodifiableList(lines). Hibernate.isInitialized()
    // does not recognise that wrapper and would ALWAYS return true, so we read the
    // real Hibernate collection from the private field.
    private static boolean linesInitialized(Order order) throws Exception {
        Field field = Order.class.getDeclaredField("lines");
        field.setAccessible(true);
        return Hibernate.isInitialized(field.get(order));
    }

    // ---------- derived queries ----------

    @Test
    void findByCustomerEmailAndStatusOrderByPlacedAtDesc_returnsMatchingOrders() {
        var orders = orderRepo.findByCustomerEmailAndStatusOrderByPlacedAtDesc("test@example.com", OrderStatus.PAID);
        assertThat(orders).hasSize(1);
        assertThat(orders.get(0).getStatus()).isEqualTo(OrderStatus.PAID);
    }

    @Test
    void countByStatus_returnsCorrectCount() {
        assertThat(orderRepo.countByStatus(OrderStatus.PAID)).isEqualTo(1);
        assertThat(orderRepo.countByStatus(OrderStatus.NEW)).isEqualTo(1);
    }

    @Test
    void productRepository_existsBySku() {
        assertThat(productRepo.existsBySku("SKU-001")).isTrue();
        assertThat(productRepo.existsBySku("NONEXISTENT")).isFalse();
    }

    // ---------- fetch join vs. plain findById ----------

    @Test
    void findWithLinesByCustomerEmail_fetchJoinsLines() throws Exception {
        List<Order> orders = orderRepo.findWithLinesByCustomerEmail("test@example.com");

        assertThat(orders).hasSize(2);
        for (Order order : orders) {
            assertThat(linesInitialized(order)).isTrue();   // loaded by the join fetch
        }
    }

    @Test
    void findById_doesNotFetchLines() throws Exception {
        Order found = orderRepo.findById(paidOrderId).orElseThrow();

        assertThat(linesInitialized(found)).isFalse();      // LAZY: not loaded yet
    }

    // ---------- aggregate ----------

    @Test
    void countLinesByStatus_returnsCorrectCount() {
        assertThat(orderRepo.countLinesByStatus(OrderStatus.PAID)).isEqualTo(2);   // order1 has 2 lines
        assertThat(orderRepo.countLinesByStatus(OrderStatus.NEW)).isEqualTo(1);
    }

    // ---------- native query ----------

    @Test
    void productRepository_findLowStock_nativeQuery() {
        List<Product> lowStock = productRepo.findLowStock(10);   // stock < 10

        assertThat(lowStock).extracting(Product::getSku).containsExactly("SKU-002");
    }

    // ---------- projections ----------

    @Test
    void findByStatus_interfaceProjection() {
        Page<OrderSummary> page = orderRepo.findByStatus(OrderStatus.PAID,
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "placedAt")));

        assertThat(page.getTotalElements()).isEqualTo(1);
        OrderSummary summary = page.getContent().get(0);
        assertThat(summary.getId()).isEqualTo(paidOrderId);
        assertThat(summary.getStatus()).isEqualTo(OrderStatus.PAID);
        assertThat(summary.getPlacedAt()).isNotNull();
        assertThat(summary.getCustomer().getName()).isEqualTo("Test User");
    }

    @Test
    void productRepository_findByCategory_recordProjection() {
        Page<ProductView> page = productRepo.findByCategory(Category.HARDWARE, PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSize(1);
        ProductView view = page.getContent().get(0);
        assertThat(view.sku()).isEqualTo("SKU-001");
        assertThat(view.amount()).isEqualByComparingTo(new BigDecimal("29.99"));
    }
}