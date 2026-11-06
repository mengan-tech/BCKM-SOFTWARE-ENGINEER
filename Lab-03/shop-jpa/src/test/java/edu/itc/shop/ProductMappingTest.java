package edu.itc.shop;

import edu.itc.shop.domain.Category;
import edu.itc.shop.domain.Money;
import edu.itc.shop.domain.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace;

import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
public class ProductMappingTest {

    @Autowired
    private TestEntityManager em;

    @Test
    public void productMappingWorks() {
        // 1. Create a Product using the Money factory and Category enum
        Money price = Money.eur("19.99");
        Product product = new Product("SKU-1001", "Gaming Mouse", price, Category.HARDWARE);

        // 2. Persist to the database
        em.persist(product);

        // 3. Force database sync and clear the Hibernate memory cache
        em.flush();
        em.clear();

        // 4. Reload the product directly from the database by its ID
        Product loadedProduct = em.find(Product.class, product.getId());

        // 5. Assert that Money and Category survived the roundtrip perfectly
        assertThat(loadedProduct.getPrice().amount()).isEqualByComparingTo(new BigDecimal("19.99"));
        assertThat(loadedProduct.getPrice().currency()).isEqualTo("EUR");
        assertThat(loadedProduct.getCategory()).isEqualTo(Category.HARDWARE);

        // 6. Assert using a native SQL query that the category was stored as text, not a number
        String categoryDbValue = (String) em.getEntityManager()
                .createNativeQuery("SELECT category FROM product WHERE id = :id")
                .setParameter("id", product.getId())
                .getSingleResult();

        assertThat(categoryDbValue).isEqualTo("HARDWARE");
    }
}