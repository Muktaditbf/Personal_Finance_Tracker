package com.finance;

import java.time.LocalDate;

public class Transaction {
    private int id;
    private int accountId;
    private int categoryId;
    private double amount;
    private LocalDate date;
    private String note;
    private String imagePath;

    // Default constructor
    public Transaction() {
    }

    // Constructor without id (for new transactions)
    public Transaction(int accountId, int categoryId, double amount, LocalDate date, String note, String imagePath) {
        this.accountId = accountId;
        this.categoryId = categoryId;
        this.amount = amount;
        this.date = date;
        this.note = note;
        this.imagePath = imagePath;
    }

    // Full constructor
    public Transaction(int id, int accountId, int categoryId, double amount, LocalDate date, String note, String imagePath) {
        this.id = id;
        this.accountId = accountId;
        this.categoryId = categoryId;
        this.amount = amount;
        this.date = date;
        this.note = note;
        this.imagePath = imagePath;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", accountId=" + accountId +
                ", categoryId=" + categoryId +
                ", amount=" + amount +
                ", date=" + date +
                ", note='" + note + '\'' +
                ", imagePath='" + imagePath + '\'' +
                '}';
    }
}
