package com.cqrs.restaurant.domain.tab;

public class TabNotOpen extends RuntimeException {
    
    public TabNotOpen(String message) {
        super(message);
    }
}
