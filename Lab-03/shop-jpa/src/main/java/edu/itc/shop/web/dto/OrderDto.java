package edu.itc.shop.web.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import edu.itc.shop.domain.OrderStatus;


public record OrderDto(
        Long id,
        OrderStatus status,
        Instant placedAt,
        BigDecimal total,
        String currency,
        List<OrderLineDto> lines) {
}