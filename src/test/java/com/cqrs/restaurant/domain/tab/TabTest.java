package com.cqrs.restaurant.domain.tab;

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

public class TabTest {

    private static final String testTabId = UUID.randomUUID().toString();
    private static final int testTable = 3;
    private static final String testWaiter = "Anna";

    private static final OrderItem testDrink1 = new OrderItem(1, "beer", true, 3.0);
    private static final OrderItem testDrink2 = new OrderItem(2, "juice", true, 1.0);
    private static final OrderItem testFood1 = new OrderItem(3, "pizza", false, 8.0);
    private static final OrderItem testFood2 = new OrderItem(4, "salad", false, 7.0);

    private Tab aggregate;
    private DomainEventPublisher eventPublisherMock;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setup() {
        eventPublisherMock = mock(DomainEventPublisher.class);
        aggregate = new Tab(eventPublisherMock);
    }

    @Test
    public void can_open_a_new_tab() {
        OpenTab openTab = new OpenTab(testTabId, testTable, testWaiter);

        aggregate.handle(openTab);

        verify(eventPublisherMock).publish(new TabOpened(testTabId, testTable, testWaiter));
    }

    @Test
    public void can_not_order_with_unopened_tab() {
        List<OrderItem> items = Arrays.asList(testDrink1, testDrink2);
        PlaceOrder placeOrder = new PlaceOrder(testTabId, items);

        exception.expect(TabNotOpen.class);

        aggregate.handle(placeOrder);
    }

    @Test
    public void can_place_drinks_order() {
        aggregate.apply(new TabOpened(testTabId, testTable, testWaiter));
        List<OrderItem> drinks = Arrays.asList(testDrink1, testDrink2);
        PlaceOrder placeOrder = new PlaceOrder(testTabId, drinks);

        aggregate.handle(placeOrder);

        verify(eventPublisherMock).publish(new DrinksOrdered(testTabId, drinks));
    }

    @Test
    public void can_place_food_order() {
        aggregate.apply(new TabOpened(testTabId, testTable, testWaiter));
        List<OrderItem> food = Arrays.asList(testFood1, testFood2);
        PlaceOrder placeOrder = new PlaceOrder(testTabId, food);

        aggregate.handle(placeOrder);

        verify(eventPublisherMock).publish(new FoodOrdered(testTabId, food));
    }

    @Test
    public void can_place_drinks_and_food_order() {
        aggregate.apply(new TabOpened(testTabId, testTable, testWaiter));
        PlaceOrder placeOrder = new PlaceOrder(testTabId, Arrays.asList(testDrink1, testFood1));

        aggregate.handle(placeOrder);

        verify(eventPublisherMock).publish(new DrinksOrdered(testTabId, Collections.singletonList(testDrink1)));
        verify(eventPublisherMock).publish(new FoodOrdered(testTabId, Collections.singletonList(testFood1)));
    }

    @Test
    public void ordered_drinks_can_be_served() {
        aggregate.apply(new TabOpened(testTabId, testTable, testWaiter));
        aggregate.apply(new DrinksOrdered(testTabId, Arrays.asList(testDrink1, testDrink2)));
        List<Integer> menuNumbers = Arrays.asList(testDrink1.menuNumber(), testDrink2.menuNumber());
        MarkDrinksServed markDrinksServed = new MarkDrinksServed(testTabId, menuNumbers);

        aggregate.handle(markDrinksServed);

        verify(eventPublisherMock).publish(new DrinksServed(testTabId, menuNumbers));
    }

    @Test
    public void can_not_serve_an_unordered_drink() {
        aggregate.apply(new TabOpened(testTabId, testTable, testWaiter));
        aggregate.apply(new DrinksOrdered(testTabId, Collections.singletonList(testDrink1)));
        List<Integer> menuNumbers = Collections.singletonList(testDrink2.menuNumber());
        MarkDrinksServed markDrinksServed = new MarkDrinksServed(testTabId, menuNumbers);

        exception.expect(DrinksNotOutstanding.class);

        aggregate.handle(markDrinksServed);
    }

    @Test
    public void can_not_serve_an_ordered_drink_twice() {
        aggregate.apply(new TabOpened(testTabId, testTable, testWaiter));
        aggregate.apply(new DrinksOrdered(testTabId, Collections.singletonList(testDrink1)));
        aggregate.apply(new DrinksServed(testTabId, Collections.singletonList(testDrink1.menuNumber())));
        MarkDrinksServed markDrinksServed = new MarkDrinksServed(testTabId, Collections.singletonList(testDrink1.menuNumber()));

        exception.expect(DrinksNotOutstanding.class);

        aggregate.handle(markDrinksServed);
    }

    @Test
    public void can_close_tab_with_tip() {
        aggregate.apply(new TabOpened(testTabId, testTable, testWaiter));
        aggregate.apply(new DrinksOrdered(testTabId, Collections.singletonList(testDrink2)));
        aggregate.apply(new DrinksServed(testTabId, Collections.singletonList(testDrink2.menuNumber())));
        Double amountPaid = testDrink2.price() + 0.5;
        CloseTab closeTab = new CloseTab(testTabId, amountPaid);

        aggregate.handle(closeTab);

        verify(eventPublisherMock).publish(new TabClosed(testTabId, amountPaid, testDrink2.price(), 0.5));
    }

}
