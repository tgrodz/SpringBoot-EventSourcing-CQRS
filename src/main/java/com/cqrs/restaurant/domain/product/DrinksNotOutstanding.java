package com.cqrs.restaurant.domain.product;

class DrinksNotOutstanding extends RuntimeException {
    public DrinksNotOutstanding(String message) {
        super(message);
    }
}
