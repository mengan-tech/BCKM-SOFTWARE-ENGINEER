package edu.itc.shop.web;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import edu.itc.shop.domain.Product;
import edu.itc.shop.service.InsufficientStockException;
import edu.itc.shop.service.ProductService;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    ProductService service;

    @Test
    void listReturns200() throws Exception {
        when(service.search(null)).thenReturn(List.of(
                new Product(1L, "Laptop", new BigDecimal("999.99"), 10)
        ));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Laptop"));
    }

    @Test
    void getByIdReturns404WhenMissing() throws Exception {
        when(service.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/products/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createReturns400WithTwoFieldErrors() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"","price":0,"stock":0}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors.length()").value(2));
    }

    @Test
    void createReturns201WithLocationHeader() throws Exception {
        when(service.create("Mouse", new BigDecimal("29.99"), 50))
                .thenReturn(new Product(10L, "Mouse", new BigDecimal("29.99"), 50));

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Mouse","price":29.99,"stock":50}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/products/10"))
                .andExpect(jsonPath("$.name").value("Mouse"));
    }

    @Test
    void updateStockReturns200() throws Exception {
        when(service.updateStock(1L, -2))
                .thenReturn(new Product(1L, "Laptop", new BigDecimal("999.99"), 8));

        mockMvc.perform(patch("/api/products/1/stock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"delta":-2}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock").value(8));
    }

    @Test
    void updateStockReturns409WhenInsufficient() throws Exception {
        when(service.updateStock(1L, -20))
                .thenThrow(new InsufficientStockException(1L, 10, 20));

        mockMvc.perform(patch("/api/products/1/stock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"delta":-20}
                                """))
                .andExpect(status().isConflict());
    }
}
