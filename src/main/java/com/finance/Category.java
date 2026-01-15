package com.finance;

public class Category {
    private int id;
    private String name;
    private double budgetLimit;
    private CategoryType type;

    // Default constructor
    public Category() {
    }

    // Constructor without id (for new categories)
    public Category(String name, double budgetLimit, CategoryType type) {
        this.name = name;
        this.budgetLimit = budgetLimit;
        this.type = type;
    }

    // Full constructor
    public Category(int id, String name, double budgetLimit, CategoryType type) {
        this.id = id;
        this.name = name;
        this.budgetLimit = budgetLimit;
        this.type = type;
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

    public double getBudgetLimit() {
        return budgetLimit;
    }

    public void setBudgetLimit(double budgetLimit) {
        this.budgetLimit = budgetLimit;
    }

    public CategoryType getType() {
        return type;
    }

    public void setType(CategoryType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "Category{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", budgetLimit=" + budgetLimit +
                ", type=" + type +
                '}';
    }
}
