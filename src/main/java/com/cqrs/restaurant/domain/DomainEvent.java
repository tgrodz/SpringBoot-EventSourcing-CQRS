package com.cqrs.restaurant.domain;

import java.util.Date;

public interface DomainEvent {
    public Date occurredOn();
}
