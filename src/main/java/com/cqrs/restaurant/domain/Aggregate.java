package com.cqrs.restaurant.domain;

public interface Aggregate {
    void setRootId(Object id);

    Object getRootId(Object id);
}
