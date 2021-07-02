package com.cqrs.restaurant.domain.visit;

import com.cqrs.restaurant.domain.product.DrinksOrdered;
import com.cqrs.restaurant.domain.product.FoodOrdered;
import com.cqrs.restaurant.domain.product.*;
import com.cqrs.restaurant.domain.order.PlaceOrder;
import com.cqrs.restaurant.domain.Aggregate;
import com.cqrs.restaurant.domain.DomainEventPublisher;
import com.cqrs.restaurant.domain.order.OrderItem;
import com.cqrs.restaurant.domain.vo.Address;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

//Root Aggregate
public class VisitFacade implements Aggregate {
    private String visitId;
    private DomainEventPublisher domainEventPublisher;
    private boolean open = false;
    private List<OrderItem> outstandingDrinks = new ArrayList<>();
    private Double servedItemsValue = 0.0;
    private Address address;

    VisitFacade(DomainEventPublisher domainEventPublisher) {
        this.domainEventPublisher = domainEventPublisher;
    }

    @Override
    public void setRootId(Object id) {
        if (id instanceof String) this.visitId = (String) id;
        else throw new RuntimeException("Wrong type Id! The type must be String");
    }

    @Override
    public Object getRootId(Object id) {
        return visitId;
    }

    //--------- Handle Commands -------------

    void handle(OpenVisit c) {
        VisitOpened visitOpened = new VisitOpened(
                c.getId(),
                c.getTableNumber(),
                c.getWaiter());

        domainEventPublisher.publish(visitOpened);
        apply(visitOpened);
    }

    void handle(PlaceOrder c) {
        if (!open) throw new VisitNotOpen("Visit can't open");

        List<OrderItem> drinks = c.getItems().stream()
                .filter(OrderItem::isDrink)
                .collect(Collectors.toList());

        if (!drinks.isEmpty()) {
            DrinksOrdered drinksOrdered = new DrinksOrdered(
                    c.getId(),
                    drinks);
            domainEventPublisher.publish(drinksOrdered);
            // TODO apply
        }

        List<OrderItem> food = c.getItems().stream()
                .filter(item -> !item.isDrink())
                .collect(Collectors.toList());
        if (!food.isEmpty()) {
            FoodOrdered foodOrdered = new FoodOrdered(
                    c.getId(),
                    food);
            domainEventPublisher.publish(foodOrdered);
            // TODO apply
        }
    }

    void handle(MarkDrinksServed c) {
        if (!areDrinksOutstanding(c.getMenuNumbers())) {
            throw new DrinksNotOutstanding();
        }

        DrinksServed drinksServed = new DrinksServed(c.getVisitId(), c.getMenuNumbers());

        domainEventPublisher.publish(drinksServed);
        // TODO apply
    }

    private boolean areDrinksOutstanding(List<Integer> menuNumbers) {
        List<Integer> outstandingDrinkNumbers = this.outstandingDrinks.stream()
                .map(OrderItem::menuNumber)
                .collect(Collectors.toList());
        return outstandingDrinkNumbers.containsAll(menuNumbers);
    }

    void handle(CloseVisit c) {
        Double tipValue = c.getAmountPaid() - servedItemsValue;
        VisitClosed visitClosed = new VisitClosed(c.getVisitId(), c.getAmountPaid(), servedItemsValue, tipValue);

        domainEventPublisher.publish(visitClosed);
        // TODO apply
    }


    // ------------- Apply Events ----------------
    void apply(VisitOpened e) {
        this.open = true;
    }

    void apply(DrinksOrdered e) {
        this.outstandingDrinks.addAll(e.getItems());
    }

    void apply(DrinksServed e) {
        for (Integer menuNumber : e.getMenuNumbers()) {
            OrderItem drink = this.outstandingDrinks.stream().filter(d -> d.menuNumber() == menuNumber).findFirst().get();
            this.servedItemsValue += drink.price();
            this.outstandingDrinks.remove(drink);
        }
    }


}
