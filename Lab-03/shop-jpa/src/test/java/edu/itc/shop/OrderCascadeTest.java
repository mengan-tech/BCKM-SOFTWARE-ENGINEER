package edu.itc.shop;

import edu.itc.shop.domain.Address;
import edu.itc.shop.domain.Category;
import edu.itc.shop.domain.Customer;
import edu.itc.shop.domain.Money;
import edu.itc.shop.domain.Order;
import edu.itc.shop.domain.OrderLine;
import edu.itc.shop.domain.Product;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@ExtendWith(OutputCaptureExtension.class)
class OrderCascadeTest {

    @Autowired
    private TestEntityManager em;

    @Test
    void orderLinesCascadePersistAndRemove(CapturedOutput output) {
        Address address = new Address("1 Main St", "Helsinki", "00100");
        Customer customer = new Customer("Ada Lovelace", "ada@example.com", address, address);
        Product firstProduct = new Product("SKU-ORDER-1", "First", Money.eur("12.50"), Category.BOOKS);
        Product secondProduct = new Product("SKU-ORDER-2", "Second", Money.eur("4.25"), Category.TOYS);
        em.persist(customer);
        em.persist(firstProduct);
        em.persist(secondProduct);

        Order order = new Order(customer);
        order.addLine(firstProduct, 2);
        order.addLine(secondProduct, 3);
        em.persist(order);
        em.flush();

        String sqlLog = output.getAll();
        assertThat(occurrences(sqlLog, "insert into orders")).isEqualTo(1);
        assertThat(occurrences(sqlLog, "insert into order_line")).isEqualTo(2);
        assertThat(sqlLog).doesNotMatch("(?is).*\\bupdate\\s+\\w+\\b.*");

        Long orderId = order.getId();
        em.clear();

        Order loadedOrder = em.find(Order.class, orderId);
        assertThat(loadedOrder.getLines()).hasSize(2);
        assertThat(loadedOrder.total().amount()).isEqualByComparingTo("37.75");
        assertThat(loadedOrder.total().currency()).isEqualTo("EUR");

        loadedOrder.removeLine(loadedOrder.getLines().get(0));
        em.flush();

        assertThat(lineCount()).isEqualTo(1);
        assertThat(occurrences(output.getAll(), "delete from order_line")).isEqualTo(1);

        em.remove(loadedOrder);
        em.flush();

        assertThat(lineCount()).isZero();
        assertThat(occurrences(output.getAll(), "delete from order_line")).isEqualTo(2);
    }

    private long lineCount() {
        return em.getEntityManager()
                .createQuery("select count(line) from OrderLine line", Long.class)
                .getSingleResult();
    }

    private int occurrences(String text, String sql) {
        return text.split("(?i)" + sql.replace(" ", "\\s+") + "\\b", -1).length - 1;
    }
}
