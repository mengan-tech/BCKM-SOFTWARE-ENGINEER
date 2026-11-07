package edu.itc.shop.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "order_line")
public class OrderLine {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "order_line_seq")
    @SequenceGenerator(name = "order_line_seq", sequenceName = "order_line_seq", allocationSize = 50)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private int quantity;

    // We save a snapshot of the price here, because the main Product price might increase next year!
    @Embedded
    private Money unitPrice;

    protected OrderLine() {}

    public OrderLine(Order order, Product product, int quantity, Money unitPrice) {
        this.order = order;
        this.product = product;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public Long getId() { return id; }
    public Order getOrder() { return order; }
    public Product getProduct() { return product; }
    public int getQuantity() { return quantity; }
    public Money getUnitPrice() { return unitPrice; }

    void setOrder(Order order) { this.order = order; }

    @Override
    public boolean equals(Object other) {
        return this == other || (other != null && getClass() == other.getClass()
                && id != null && id.equals(((OrderLine) other).id));
    }

    @Override
    public int hashCode() {
        return OrderLine.class.hashCode();
    }

    @Override
    public String toString() {
        return "OrderLine[id=%s, quantity=%d, unitPrice=%s]".formatted(id, quantity, unitPrice);
    }
}