package com.cqrs.restaurant.domain;

public interface DomainEventPublisher {
    void publish(DomainEvent event);
}
