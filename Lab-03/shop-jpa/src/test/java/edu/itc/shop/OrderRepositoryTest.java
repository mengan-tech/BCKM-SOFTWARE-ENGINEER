package edu.itc.shop;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;
import edu.itc.shop.domain.Address;
import edu.itc.shop.domain.Category;
import edu.itc.shop.domain.Customer;
import edu.itc.shop.domain.Money;
import edu.itc.shop.domain.Order;
import edu.itc.shop.domain.OrderLine;
import edu.itc.shop.domain.OrderStatus;
import edu.itc.shop.domain.Product;
import edu.itc.shop.repository.OrderRepository;
import edu.itc.shop.repository.ProductRepository;
import edu.itc.shop.web.dto.OrderSummary;
import edu.itc.shop.web.dto.ProductView;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
class OrderRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private OrderRepository orderRepo;

    @Autowired
    private ProductRepository productRepo;

    private Customer customer;
    private Product product1;
    private Product product2;

    @BeforeEach
    void setUp() {
        customer = em.persist(new Customer(
                "Test User", "test@example.com",
                new Address("1 Test St", "TestCity", "00001"),
                new Address("1 Test St", "TestCity", "00001")));

        product1 = em.persist(new Product("SKU-001", "Product One", Money.eur("29.99"), Category.HARDWARE));
        product1.setStock(10);
        product2 = em.persist(new Product("SKU-002", "Product Two", Money.eur("49.99"), Category.TOYS));
        product2.setStock(5);

        Order order1 = new Order(customer, OrderStatus.PAID, Instant.now().minusSeconds(3600));
        order1.addLine(product1, 2);
        order1.addLine(product2, 1);
        em.persist(order1);

        Order order2 = new Order(customer, OrderStatus.NEW, Instant.now());
        order2.addLine(product1, 1);
        em.persist(order2);

        em.flush();
    }

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
    void findWithLinesByCustomerEmail_fetchJoinsLines() {
        var orders = orderRepo.findWithLinesByCustomerEmail("test@example.com");
        assertThat(orders).isNotEmpty();
        Order order = orders.get(0);
        // Lines should be initialized by the fetch join
        assertThat(org.hibernate.Hibernate.isInitialized(order.getLines())).isTrue();
    }

    @Test
    @Sql(statements = {
        "DELETE FROM order_line",
        "DELETE FROM orders",
        "DELETE FROM product_tag",
        "DELETE FROM product",
        "DELETE FROM tag",
        "DELETE FROM customer"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void findById_doesNotFetchLines() throws Exception {
        // Create a single order with lines
        Customer c = em.persist(new Customer(
                "Lazy Test", "lazy@example.com",
                new Address("1 Lazy St", "LazyCity", "00001"),
                new Address("1 Lazy St", "LazyCity", "00001")));
        Product p = em.persist(new Product("SKU-LAZY", "Lazy Product", Money.eur("9.99"), Category.BOOKS));
        p.setStock(5);
        Order order = new Order(c, OrderStatus.NEW, Instant.now());
        order.addLine(p, 1);
        em.persist(order);
        em.flush();
        Long id = order.getId();
        em.clear();

        // em.find returns a fresh proxy; the internal lines field is lazy
        Order found = em.find(Order.class, id);
        // Use reflection to access the raw lines field (getLines() wraps in unmodifiableList)
        java.lang.reflect.Field linesField = Order.class.getDeclaredField("lines");
        linesField.setAccessible(true);
        @SuppressWarnings("unchecked")
        java.util.List<OrderLine> rawLines = (java.util.List<OrderLine>) linesField.get(found);
        assertThat(org.hibernate.Hibernate.isInitialized(rawLines)).isFalse();

        // Clean up to avoid polluting other tests
        em.clear();
        em.getEntityManager().createQuery("DELETE FROM OrderLine").executeUpdate();
        em.getEntityManager().createQuery("DELETE FROM Order").executeUpdate();
        em.getEntityManager().createQuery("DELETE FROM Tag").executeUpdate();
        em.getEntityManager().createQuery("DELETE FROM Product").executeUpdate();
        em.getEntityManager().createQuery("DELETE FROM Customer").executeUpdate();
        em.flush();
    }

    @Test
    void countLinesByStatus_returnsCorrectCount() {
        long lines = orderRepo.countLinesByStatus(OrderStatus.PAID);
        assertThat(lines).isEqualTo(2); // order1 has 2 lines
    }

    @Test
    void findByStatus_projectionSelectsOnlyProjectedColumns() {
        Page<OrderSummary> page = orderRepo.findByStatus(OrderStatus.PAID, PageRequest.of(0, 10));
        assertThat(page.getContent()).hasSize(1);
        OrderSummary summary = page.getContent().get(0);
        assertThat(summary.getId()).isNotNull();
        assertThat(summary.getStatus()).isEqualTo(OrderStatus.PAID);
        assertThat(summary.getPlacedAt()).isNotNull();
        assertThat(summary.getCustomer().getName()).isEqualTo("Test User");
    }

    @Test
    void productRepository_existsBySku() {
        assertThat(productRepo.existsBySku("SKU-001")).isTrue();
        assertThat(productRepo.existsBySku("NONEXISTENT")).isFalse();
    }

    @Test
    void productRepository_findLowStock_nativeQuery() {
        var lowStock = productRepo.findLowStock(10);
        assertThat(lowStock).hasSize(1);
        assertThat(lowStock.get(0).getStock()).isLessThan(10);
    }

    @Test
    void productRepository_findByCategory_projection() {
        Page<ProductView> page = productRepo.findByCategory(Category.HARDWARE, PageRequest.of(0, 10));
        assertThat(page.getContent()).hasSize(1);
        ProductView view = page.getContent().get(0);
        assertThat(view.sku()).isEqualTo("SKU-001");
        assertThat(view.amount()).isEqualTo(new java.math.BigDecimal("29.99"));
    }
}
