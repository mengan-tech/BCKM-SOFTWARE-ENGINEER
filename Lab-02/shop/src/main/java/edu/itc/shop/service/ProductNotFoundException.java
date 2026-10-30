package edu.itc.shop.service;

public class ProductNotFoundException extends RuntimeException {

    private final long id;

    public ProductNotFoundException(long id) {
        super("Product " + id + " not found");
        this.id = id;
    }

    public long getId() {
        return id;
    }
}
