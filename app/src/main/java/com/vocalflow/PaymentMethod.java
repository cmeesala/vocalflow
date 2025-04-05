package com.vocalflow;

public class PaymentMethod {
    private String name;
    private String details;
    private int iconResourceId;

    public PaymentMethod(String name, String details, int iconResourceId) {
        this.name = name;
        this.details = details;
        this.iconResourceId = iconResourceId;
    }

    public String getName() {
        return name;
    }

    public String getDetails() {
        return details;
    }

    public int getIconResourceId() {
        return iconResourceId;
    }
} 