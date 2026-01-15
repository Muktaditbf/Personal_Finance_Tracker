package com.finance;

public class Account {
    private int id;
    private String name;
    private AccountType type;
    private double balance;

    // Default constructor
    public Account() {
    }

    // Constructor without id (for new accounts)
    public Account(String name, AccountType type, double balance) {
        this.name = name;
        this.type = type;
        this.balance = balance;
    }

    // Full constructor
    public Account(int id, String name, AccountType type, double balance) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.balance = balance;
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

    public AccountType getType() {
        return type;
    }

    public void setType(AccountType type) {
        this.type = type;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    @Override
    public String toString() {
        return "Account{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", balance=" + balance +
                '}';
    }
}
