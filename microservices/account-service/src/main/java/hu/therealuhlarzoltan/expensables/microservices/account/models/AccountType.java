package hu.therealuhlarzoltan.expensables.microservices.account.models;

import java.util.Arrays;

public enum AccountType {
    PHYSICAL("Physical"),
    DEBIT("Debit"),
    CREDIT("Credit");

    private final String name;

    private AccountType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static AccountType fromString(String type) {
        var match = Arrays.stream(AccountType.values()).filter(e -> e.name.equalsIgnoreCase(type)).findFirst();
        return match.orElseThrow(() -> new IllegalArgumentException("Account Type with name of " + type + " not found"));
    }}
