package hu.therealuhlarzoltan.expensables.microservices.account.models;

import java.util.Arrays;

public enum AccountCategory {
    SAVINGS("Savings"),
    CHECKING("Checking"),
    RETIREMENT("Retirement"),
    INVESTMENT("Investment"),
    EXCHANGE("Exchange"),
    BONDS("Bonds");

    private final String name;

    private AccountCategory(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static AccountCategory fromString(String category) {
        var match = Arrays.stream(AccountCategory.values()).filter(e -> e.name.equalsIgnoreCase(category)).findFirst();
        return match.orElseThrow(() -> new IllegalArgumentException("Account Category with name of " + category + " not found"));
    }
}
