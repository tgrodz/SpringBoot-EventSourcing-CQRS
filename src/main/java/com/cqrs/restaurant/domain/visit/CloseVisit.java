package com.cqrs.restaurant.domain.visit;

public class CloseVisit {
    private String tabId;
    private Double amountPaid;

    public CloseVisit(String tabId, Double amountPaid) {
        this.tabId = tabId;
        this.amountPaid = amountPaid;
    }

    public String getVisitId() {
        return tabId;
    }

    public Double getAmountPaid() {
        return amountPaid;
    }
}
