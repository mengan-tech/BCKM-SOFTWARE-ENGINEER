package edu.itc.shop.web.dto;

import java.math.BigDecimal;


public record OrderLineDto(
        Long id,
        String sku,
        String productName,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal lineTotal) {
}