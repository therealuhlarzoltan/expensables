package hu.therealuhlarzoltan.expensables.microservices.account.models;

import java.util.Arrays;

public enum Currency {
    USD,
    EUR,
    HUF,
    JPY;

    public static Currency fromString(String currency) {
        var match = Arrays.stream(Currency.values()).filter(e -> e.toString().equalsIgnoreCase(currency)).findFirst();
        return match.orElseThrow(() -> new IllegalArgumentException("Currency with name of " + currency + " ot found"));
    }
}
