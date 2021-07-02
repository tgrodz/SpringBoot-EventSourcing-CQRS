package com.cqrs.restaurant.domain.visit;

import com.cqrs.restaurant.domain.product.DrinksOrdered;
import com.cqrs.restaurant.domain.product.FoodOrdered;
import com.cqrs.restaurant.domain.product.*;
import com.cqrs.restaurant.domain.order.PlaceOrder;
import com.cqrs.restaurant.domain.DomainEventPublisher;
import com.cqrs.restaurant.domain.order.OrderItem;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class VisitTest {

    private static final String testVisitId = UUID.randomUUID().toString();
    private static final int testTable = 3;
    private static final String testWaiter = "Anna";

    private static final OrderItem testDrink1 = new OrderItem(1, "beer", true, 3.0);
    private static final OrderItem testDrink2 = new OrderItem(2, "juice", true, 1.0);
    private static final OrderItem testFood1 = new OrderItem(3, "pizza", false, 8.0);
    private static final OrderItem testFood2 = new OrderItem(4, "salad", false, 7.0);

    private VisitFacade aggregate;
    private DomainEventPublisher eventPublisherMock;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setup() {
        eventPublisherMock = mock(DomainEventPublisher.class);
        aggregate = new VisitFacade(eventPublisherMock);
    }

    @Test
    public void can_open_a_new_visit() {
        OpenVisit openVisit = new OpenVisit(testVisitId, testTable, testWaiter);

        aggregate.handle(openVisit);

        verify(eventPublisherMock).publish(new VisitOpened(testVisitId, testTable, testWaiter));
    }

    @Test
    public void can_not_order_with_unopened_visit() {
        List<OrderItem> items = Arrays.asList(testDrink1, testDrink2);
        PlaceOrder placeOrder = new PlaceOrder(testVisitId, items);

        exception.expect(VisitNotOpen.class);

        aggregate.handle(placeOrder);
    }

    @Test
    public void can_place_drinks_order() {
        aggregate.apply(new VisitOpened(testVisitId, testTable, testWaiter));
        List<OrderItem> drinks = Arrays.asList(testDrink1, testDrink2);
        PlaceOrder placeOrder = new PlaceOrder(testVisitId, drinks);

        aggregate.handle(placeOrder);

        verify(eventPublisherMock).publish(new DrinksOrdered(testVisitId, drinks));
    }

    @Test
    public void can_place_food_order() {
        aggregate.apply(new VisitOpened(testVisitId, testTable, testWaiter));
        List<OrderItem> food = Arrays.asList(testFood1, testFood2);
        PlaceOrder placeOrder = new PlaceOrder(testVisitId, food);

        aggregate.handle(placeOrder);

        verify(eventPublisherMock).publish(new FoodOrdered(testVisitId, food));
    }

    @Test
    public void can_place_drinks_and_food_order() {
        aggregate.apply(new VisitOpened(testVisitId, testTable, testWaiter));
        PlaceOrder placeOrder = new PlaceOrder(testVisitId, Arrays.asList(testDrink1, testFood1));

        aggregate.handle(placeOrder);

        verify(eventPublisherMock).publish(new DrinksOrdered(testVisitId, Collections.singletonList(testDrink1)));
        verify(eventPublisherMock).publish(new FoodOrdered(testVisitId, Collections.singletonList(testFood1)));
    }

    @Test
    public void ordered_drinks_can_be_served() {
        aggregate.apply(new VisitOpened(testVisitId, testTable, testWaiter));
        aggregate.apply(new DrinksOrdered(testVisitId, Arrays.asList(testDrink1, testDrink2)));
        List<Integer> menuNumbers = Arrays.asList(testDrink1.menuNumber(), testDrink2.menuNumber());
        MarkDrinksServed markDrinksServed = new MarkDrinksServed(testVisitId, menuNumbers);

        aggregate.handle(markDrinksServed);

        verify(eventPublisherMock).publish(new DrinksServed(testVisitId, menuNumbers));
    }

    @Test
    public void can_not_serve_an_unordered_drink() {
        aggregate.apply(new VisitOpened(testVisitId, testTable, testWaiter));
        aggregate.apply(new DrinksOrdered(testVisitId, Collections.singletonList(testDrink1)));
        List<Integer> menuNumbers = Collections.singletonList(testDrink2.menuNumber());
        MarkDrinksServed markDrinksServed = new MarkDrinksServed(testVisitId, menuNumbers);

        exception.expect(DrinksNotOutstanding.class);

        aggregate.handle(markDrinksServed);
    }

    @Test
    public void can_not_serve_an_ordered_drink_twice() {
        aggregate.apply(new VisitOpened(testVisitId, testTable, testWaiter));
        aggregate.apply(new DrinksOrdered(testVisitId, Collections.singletonList(testDrink1)));
        aggregate.apply(new DrinksServed(testVisitId, Collections.singletonList(testDrink1.menuNumber())));
        MarkDrinksServed markDrinksServed = new MarkDrinksServed(testVisitId, Collections.singletonList(testDrink1.menuNumber()));

        exception.expect(DrinksNotOutstanding.class);

        aggregate.handle(markDrinksServed);
    }

    @Test
    public void can_close_visit_with_tip() {
        aggregate.apply(new VisitOpened(testVisitId, testTable, testWaiter));
        aggregate.apply(new DrinksOrdered(testVisitId, Collections.singletonList(testDrink2)));
        aggregate.apply(new DrinksServed(testVisitId, Collections.singletonList(testDrink2.menuNumber())));
        Double amountPaid = testDrink2.price() + 0.5;
        CloseVisit closeVisit = new CloseVisit(testVisitId, amountPaid);

        aggregate.handle(closeVisit);

        verify(eventPublisherMock).publish(new VisitClosed(testVisitId, amountPaid, testDrink2.price(), 0.5));
    }

}
