package com.vocalflow;

public class Bill {
    private String title;
    private String amount;
    private String dueDate;

    public Bill(String title, String amount, String dueDate) {
        this.title = title;
        this.amount = amount;
        this.dueDate = dueDate;
    }

    public String getTitle() {
        return title;
    }

    public String getAmount() {
        return amount;
    }

    public String getDueDate() {
        return dueDate;
    }
} 