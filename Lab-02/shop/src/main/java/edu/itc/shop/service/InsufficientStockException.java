package edu.itc.shop.service;

public class InsufficientStockException extends RuntimeException {

    private final long id;
    private final int currentStock;
    private final int requested;

    public InsufficientStockException(long id, int currentStock, int requested) {
        super("Product " + id + " has " + currentStock + " units, cannot remove " + requested);
        this.id = id;
        this.currentStock = currentStock;
        this.requested = requested;
    }

    public long getId() {
        return id;
    }

    public int getCurrentStock() {
        return currentStock;
    }

    public int getRequested() {
        return requested;
    }
}

