package edu.itc.shop.domain;

import jakarta.persistence.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tag")
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tag_seq")
    @SequenceGenerator(name = "tag_seq", sequenceName = "tag_seq", allocationSize = 50)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @ManyToMany(mappedBy = "tags")
    private Set<Product> products = new HashSet<>();

    protected Tag() { }

    public Tag(String name) {
        this.name = name;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public Set<Product> getProducts() { return Collections.unmodifiableSet(products); }

    void addProduct(Product product) {
        products.add(product);
    }

    @Override
    public boolean equals(Object other) {
        return this == other || (other != null && getClass() == other.getClass()
                && id != null && id.equals(((Tag) other).id));
    }

    @Override
    public int hashCode() {
        return Tag.class.hashCode();
    }

    @Override
    public String toString() {
        return "Tag[id=%s, name=%s]".formatted(id, name);
    }
}
