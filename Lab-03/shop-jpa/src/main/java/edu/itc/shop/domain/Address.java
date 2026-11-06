package edu.itc.shop.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record Address(
        @Column(nullable = false) String street,
        @Column(nullable = false) String city,
        @Column(nullable = false) String zip) {
}
