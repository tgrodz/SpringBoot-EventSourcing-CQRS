package com.cqrs.restaurant.domain.order;

public class OrderItem {
    private int MenuNumber;
    private String Description;
    private boolean IsDrink;
    private double Price;

    OrderItem(int menuNumber, String description, boolean isDrink, double price) {
        MenuNumber = menuNumber;
        Description = description;
        IsDrink = isDrink;
        Price = price;
    }

    int menuNumber() {
        return MenuNumber;
    }

    String description() {
        return Description;
    }

    public boolean isDrink() {
        return IsDrink;
    }

    double price() {
        return Price;
    }
}
