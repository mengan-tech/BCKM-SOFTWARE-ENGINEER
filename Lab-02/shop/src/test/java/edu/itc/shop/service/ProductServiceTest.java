package edu.itc.shop.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import edu.itc.shop.repository.ProductRepository;

@SpringBootTest
class ProductServiceTest {
    @Autowired ProductService service;
    @Autowired ProductRepository repo;

    private int stockOf(long id) {
        return repo.findById(id).orElseThrow().stock();
    }

    @Test
    void restockMovesStockBetweenProducts() {
        int before1 = stockOf(1);
        int before2 = stockOf(2);

        service.restock(1, 2, 3);

        assertEquals(before1 - 3, stockOf(1));
        assertEquals(before2 + 3, stockOf(2));
    }

    @Test
    void restockRollsBackWhenSourceHasTooLittleStock() {
        int before1 = stockOf(1);
        int before2 = stockOf(2);

        assertThrows(InsufficientStockException.class, () -> service.restock(1, 2, 1_000));

        assertEquals(before1, stockOf(1));
        assertEquals(before2, stockOf(2));
    }

    @Test
    void restockRollsBackFirstUpdateWhenTargetDoesNotExist() {
        int before1 = stockOf(1);

        assertThrows(ProductNotFoundException.class, () -> service.restock(1, 999, 2));

        assertEquals(before1, stockOf(1));   // the removal was undone
    }
}