package com.cqrs.restaurant.domain.visit;

public class VisitNotOpen extends RuntimeException {
    
    public VisitNotOpen(String message) {
        super(message);
    }
}
