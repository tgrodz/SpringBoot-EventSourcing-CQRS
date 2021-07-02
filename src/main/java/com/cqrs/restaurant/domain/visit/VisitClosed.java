package com.cqrs.restaurant.domain.visit;

import com.cqrs.restaurant.domain.DomainEvent;

import java.util.Date;

public class VisitClosed implements DomainEvent {

    private Date occurredOn;
    private String visitId;
    private Double amountPaid;
    private Double orderValue;
    private Double tipValue;

    VisitClosed(String visitId, Double amountPaid, Double orderValue, Double tipValue) {
        this.occurredOn = new Date();
        this.visitId = visitId;
        this.amountPaid = amountPaid;
        this.orderValue = orderValue;
        this.tipValue = tipValue;
    }

    @Override
    public Date occurredOn() {
        return this.occurredOn;
    }

    public String tabId() {
        return visitId;
    }

    public Double getAmountPaid() {
        return amountPaid;
    }

    public Double getOrderValue() {
        return orderValue;
    }

    public Double getTipValue() {
        return tipValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VisitClosed visitClosed = (VisitClosed) o;

        if (!visitId.equals(visitClosed.visitId)) return false;
        if (!amountPaid.equals(visitClosed.amountPaid)) return false;
        if (!orderValue.equals(visitClosed.orderValue)) return false;
        return tipValue.equals(visitClosed.tipValue);
    }

    @Override
    public int hashCode() {
        int result = visitId.hashCode();
        result = 31 * result + amountPaid.hashCode();
        result = 31 * result + orderValue.hashCode();
        result = 31 * result + tipValue.hashCode();
        return result;
    }
}
