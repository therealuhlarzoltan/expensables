package hu.therealuhlarzoltan.expensables.microservices.expense.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum ExpenseCategory {
    FOODS_AND_DRINKS("Foods & Drinks", "GROCERIES", "RESTAURANTS", "FAST_FOOD", "BARS_AND_CAFES"),
    SHOPPING("Shopping", "CLOTHES_AND_SHOES", "JEWELS_AND_ACCESSORIES", "HEALTH_AND_BEAUTY", "ELECTRONICS_AND_ACCESSORIES", "HOME_AND_GARDEN", "FREE_TIME, STATIONERY_AND_TOOLS", "GIFTS_AND_JOY", "PETS_AND_ANIMALS", "KIDS", "DRUG-STORE_AND_CHEMIST"),
    HOUSING("Housing", "RENT" ,"ENERGY_UTILITIES", "MAINTENANCE_AND_REPAIRS", "SERVICES", "MORTGAGE", "PROPERTY_INSURANCE"),
    TRANSPORTATION("Transportation", "PUBLIC_TRANSPORT", "TAXI", "LONG_DISTANCE", "BUSINESS_TRIPS"),
    VEHICLE("Vehicle", "FUEL", "VEHICLE_MAINTENANCE", "VEHICLE_INSURANCE", "PARKING", "TOLLS", "RENTALS", "LEASING"),
    LIFE_AND_ENTERTAINMENT("Life & Entertainment", "HEALTH_CARE_AND_DOCTOR", "WELLNESS_AND_BEAUTY", "ACTIVE_SPORT_AND_FITNESS", "CULTURE_AND_SPORT_EVENTS", "LIFE_EVENTS", "HOBBIES", "EDUCATION_AND_DEVELOPMENT", "BOOKS_AND_AUDIO_AND_SUBSCRIPTIONS", "TV_AND_STREAMING", "HOLIDAY_AND_TRIPS_AND_HOTELS", "CHARITY_AND_GIFTS", "ALCOHOL_AND_TOBACCO", "GAMBLING_AND_LOTTERY"),
    COMMUNICATION_AND_PC("Communication & PC", "PHONE_AND_CELL_PHONE", "INTERNET", "SOFTWARE_AND_APPS_AND_GAMES", "POSTAL_SERVICES"),
    FINANCIAL_EXPENSES("Financial expenses", "TAXES", "INSURANCES", "LOANS_AND_INTERESTS", "FINES", "ADVISORY", "CHARGES_AND_FEES", "CHILD_SUPPORT"),
    INVESTMENTS("Investments", "REALTY", "VEHICLES_AND_CHATTELS", "FINANCIAL_INVESTMENTS", "SAVINGS", "COLLECTIONS"),
    OTHERS("Others", "OTHERS");

    private final String name;
    private final List<String> subCategories = new ArrayList<>();
    private ExpenseCategory(String name, String... subCategories) {
        this.name = name;
        Collections.addAll(this.subCategories, subCategories);
    }

    public String getName() {
        return name;
    }

    public List<String> getSubCategories() {
        return new ArrayList<>(subCategories);
    }

    public List<String> getSubCategoryDisplayNames(Function<String, String> displayNameResolver) {
        return this.getSubCategories()
                .stream()
                .map(displayNameResolver)
                .collect(Collectors.toList());
    }

    public static List<String> getAllSubCategories() {
        return Arrays.stream((ExpenseCategory.values()))
                .flatMap(e -> e.getSubCategories().stream())
                .collect(Collectors.toList());
    }

    public static List<String> getAllSubCategoryDisplayNames(Function<String, String> displayNameResolver) {
        return Arrays.stream((ExpenseCategory.values()))
                .flatMap(e -> e.getSubCategoryDisplayNames(displayNameResolver).stream())
                .collect(Collectors.toList());
    }

    public static ExpenseCategory fromString(String category) {
        var match = Arrays.stream(ExpenseCategory.values()).filter(e -> e.name.equalsIgnoreCase(category)).findFirst();
        return match.orElseThrow(() -> new IllegalArgumentException("Expense Category with name of " + category + " not found"));
    }
}
