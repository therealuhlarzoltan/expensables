package hu.therealuhlarzoltan.expensables.microservices.income.models;


import java.util.Arrays;

public enum IncomeCategory {
    WAGE_AND_INVOICES("Wage, invoices"),
    INTERESTS_AND_DIVIDENDS("Interests, dividends"),
    SALE("Sale"),
    RENTAL_INCOME("Rental income"),
    DUES_AND_GRANTS("Dues, grants"),
    LENDING_AND_RENTING("Lending, renting"),
    CHECKS_AND_COUPONS("Checks, coupons"),
    LOTTERY_AND_GAMBLING("Lottery, gambling"),
    REFUNDS("Refunds (tax, purchase)"),
    CHILD_SUPPORT("Child support"),
    GIFTS("Gifts"),
    OTHER("Other");

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
