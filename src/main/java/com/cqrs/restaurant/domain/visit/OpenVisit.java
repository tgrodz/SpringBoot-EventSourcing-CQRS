package com.cqrs.restaurant.domain.visit;

public class OpenVisit {
    private String id;
    private int tableNumber;
    private String waiter;

    public OpenVisit(String id, int tableNumber, String waiter) {
        this.id = id;
        this.tableNumber = tableNumber;
        this.waiter = waiter;
    }

    public String getId() {
        return id;
    }

    public int getTableNumber() {
        return tableNumber;
    }

    public String getWaiter() {
        return waiter;
    }
}
