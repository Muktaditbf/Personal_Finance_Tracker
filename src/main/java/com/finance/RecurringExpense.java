package com.finance;

public class RecurringExpense {
    private int id;
    private String name;
    private double amount;
    private int dueDay; // 1-31

    // Default constructor
    public RecurringExpense() {
    }

    // Constructor without id (for new recurring expenses)
    public RecurringExpense(String name, double amount, int dueDay) {
        this.name = name;
        this.amount = amount;
        this.dueDay = dueDay;
    }

    // Full constructor
    public RecurringExpense(int id, String name, double amount, int dueDay) {
        this.id = id;
        this.name = name;
        this.amount = amount;
        this.dueDay = dueDay;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public int getDueDay() {
        return dueDay;
    }

    public void setDueDay(int dueDay) {
        if (dueDay < 1 || dueDay > 31) {
            throw new IllegalArgumentException("Due day must be between 1 and 31");
        }
        this.dueDay = dueDay;
    }

    @Override
    public String toString() {
        return "RecurringExpense{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", amount=" + amount +
                ", dueDay=" + dueDay +
                '}';
    }
}
