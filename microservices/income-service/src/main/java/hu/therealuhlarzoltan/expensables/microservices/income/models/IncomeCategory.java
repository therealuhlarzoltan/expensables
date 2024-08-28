package hu.therealuhlarzoltan.expensables.microservices.income.models;


import java.util.Arrays;

public enum IncomeCategory {

    private final String name;

    private IncomeCategory(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static IncomeCategory fromString(String name) {
        var match = Arrays.stream(IncomeCategory.values()).filter(category -> category.getName().equalsIgnoreCase(name)).findFirst();
        return match.orElseThrow(() -> new IllegalArgumentException("Income Category with name of " + name + " not found"));
    }
}
