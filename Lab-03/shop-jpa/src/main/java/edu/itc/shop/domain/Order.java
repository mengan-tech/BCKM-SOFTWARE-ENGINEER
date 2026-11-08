package edu.itc.shop.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "orders_seq")
    @SequenceGenerator(name = "orders_seq", sequenceName = "orders_seq", allocationSize = 50)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status = OrderStatus.NEW;

    @Column(name = "placed_at", nullable = false)
    private Instant placedAt = Instant.now();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderLine> lines = new ArrayList<>();

    @Version
    private long version;

    protected Order() { }

    public Order(Customer customer) {
        this.customer = customer;
    }

    public Order(Customer customer, OrderStatus status, Instant placedAt) {
        this.customer = customer;
        this.status = status;
        this.placedAt = placedAt;
    }

    public OrderLine addLine(Product product, int quantity) {
        OrderLine line = new OrderLine(this, product, quantity, product.getPrice());
        lines.add(line);
        return line;
    }

    public void removeLine(OrderLine line) {
        int index = lines.indexOf(line);
        if (index >= 0) {
            lines.remove(index).setOrder(null);
        }
    }

    public List<OrderLine> getLines() {
        return Collections.unmodifiableList(lines);
    }

    public Money total() {
        Money total = null;
        for (OrderLine line : lines) {
            Money lineTotal = line.getUnitPrice().times(line.getQuantity());
            if (total == null) {
                total = lineTotal;
            } else {
                if (!total.currency().equals(lineTotal.currency())) {
                    throw new IllegalStateException("Cannot total order lines with different currencies");
                }
                total = new Money(total.amount().add(lineTotal.amount()), total.currency());
            }
        }
        return total == null ? Money.eur("0") : total;
    }

    public Long getId() { return id; }
    public Customer getCustomer() { return customer; }
    public OrderStatus getStatus() { return status; }
    public Instant getPlacedAt() { return placedAt; }
    public long getVersion() { return version; }

    public void setStatus(OrderStatus status) { this.status = status; }

    @Override
    public boolean equals(Object other) {
        return this == other || (other != null && getClass() == other.getClass()
                && id != null && id.equals(((Order) other).id));
    }

    @Override
    public int hashCode() {
        return Order.class.hashCode();
    }

    @Override
    public String toString() {
        return "Order[id=%s, status=%s, placedAt=%s, version=%d]".formatted(id, status, placedAt, version);
    }
}
