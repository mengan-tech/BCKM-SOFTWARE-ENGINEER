package edu.itc.shop;

import edu.itc.shop.domain.Category;
import edu.itc.shop.domain.Money;
import edu.itc.shop.domain.Product;
import edu.itc.shop.domain.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class EntityIdentityTest {

    @Autowired
    private TestEntityManager em;

    @Test
    void productIdentityRemainsStableAcrossPersistence() {
        Product product = new Product("SKU-IDENTITY", "Identity", Money.eur("7.99"), Category.HARDWARE);
        Set<Product> products = new HashSet<>();
        products.add(product);

        em.persist(product);
        em.flush();
        assertThat(products).contains(product);

        Long productId = product.getId();
        em.clear();
        Product reloaded = em.find(Product.class, productId);

        assertThat(reloaded).isEqualTo(product);
        assertThat(reloaded).isNotSameAs(product);
    }

    @Test
    void taggingProductUpdatesBothSides() {
        Product product = new Product("SKU-TAGGED", "Tagged", Money.eur("3.50"), Category.BOOKS);
        Tag tag = new Tag("featured");
        em.persist(product);
        em.persist(tag);

        product.tag(tag);
        em.flush();
        em.clear();

        Product reloadedProduct = em.find(Product.class, product.getId());
        Tag reloadedTag = em.find(Tag.class, tag.getId());
        assertThat(reloadedProduct.getTags()).contains(reloadedTag);
        assertThat(reloadedTag.getProducts()).contains(reloadedProduct);
    }
}
