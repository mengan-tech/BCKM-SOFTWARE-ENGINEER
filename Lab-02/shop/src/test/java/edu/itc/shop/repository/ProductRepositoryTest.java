package edu.itc.shop.repository;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DuplicateKeyException;

import edu.itc.shop.domain.Product;

@JdbcTest                          // start only the database part; roll back after each test
@Import(ProductRepository.class)   // add our repository (@JdbcTest doesn't load it by itself)
class ProductRepositoryTest {

    @Autowired ProductRepository repo;

    @Test
    void findAllReturnsTheFiveStartingProducts() {
        assertEquals(5, repo.findAll().size());
    }

    @Test
    void findByIdFindsLaptop() {
        assertEquals("Laptop", repo.findById(1).orElseThrow().name());
    }

    @Test
    void findByIdReturnsEmptyForUnknownId() {
        assertTrue(repo.findById(99).isEmpty());
    }

    @Test
    void searchIgnoresUpperAndLowerCase() {
        List<String> names = repo.findByNameContaining("MO").stream()
                                 .map(Product::name).toList();
        assertEquals(List.of("Mouse", "Monitor"), names);
    }

    @Test
    void insertReturnsTheNewId() {
        long id = repo.insert(new Product(null, "USB-C cable", new BigDecimal("7.90"), 25));
        assertEquals("USB-C cable", repo.findById(id).orElseThrow().name());
    }

    @Test
    void updateStockChangesStock() {
        int before = repo.findById(3).orElseThrow().stock();

        assertEquals(1, repo.updateStock(3, -2));
        assertEquals(before - 2, repo.findById(3).orElseThrow().stock());
    }

    @Test
    void updateStockRefusesToGoNegative() {
        int before = repo.findById(3).orElseThrow().stock();

        assertEquals(0, repo.updateStock(3, -1000));                  // refused
        assertEquals(before, repo.findById(3).orElseThrow().stock()); // unchanged
    }

    @Test
    void deleteRemovesTheProduct() {
        assertEquals(1, repo.delete(5));
        assertTrue(repo.findById(5).isEmpty());
    }
    @Test
    void insertingDuplicateNameIsRejected() {
        String existingName = repo.findById(1).orElseThrow().name();   // e.g. "Laptop"
        Product duplicate = new Product(null, existingName, new BigDecimal("1.00"), 1);

        Exception ex = assertThrows(Exception.class, () -> repo.insert(duplicate));

        System.out.println(">>> Exception reaching us: " + ex.getClass().getName());
        System.out.println(">>> Original cause:        " + ex.getCause().getClass().getName());
        System.out.println(">>> Message:               " + ex.getMessage());

        assertInstanceOf(DuplicateKeyException.class, ex);
    }
}