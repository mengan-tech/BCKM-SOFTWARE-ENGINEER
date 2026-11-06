package edu.itc.shop.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity 
@Table(name = "orders")
public class Order {

    @Id 
    @GeneratedValue (strategy = GenerationType.SEQUENCE, generator = "customer_seq")
    @SequenceGenerator (name = "customer_seq", sequenceName = "customer_seq", allocationSize = 50)
    private Long id;

    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn (name = "customer_id", nullable = false )
    private Customer customer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    @Column(nullable = false)
    private Instant placedAt;

    // Automatically managed by Hibernate for concurrent update safety
    @Version
    @Column(nullable = false)
    private long version;

    protected Order() {}

    public Order(Customer customer, OrderStatus status, Instant placedAt) {
        this.customer = customer;
        this.status = status;
        this.placedAt = placedAt;
    }

    public Long getId() { return id; }
    public Customer getCustomer() { return customer; }
    public OrderStatus getStatus() { return status; }
    public Instant getPlacedAt() { return placedAt; }
    public long getVersion() { return version; }

}
