package edu.itc.shop.domain;

import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.core.Response;

public class InsufficientStockException extends ClientErrorException{
    public InsufficientStockException(){
        super("stock cannot be negative", Response.Status.CONFLICT);
    }
}
