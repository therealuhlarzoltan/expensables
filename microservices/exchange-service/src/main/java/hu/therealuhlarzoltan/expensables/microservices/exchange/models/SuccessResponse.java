package hu.therealuhlarzoltan.expensables.microservices.exchange.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SuccessResponse {
    private String date;
    private Info info;
    private Query query;
    private double result;
    private boolean success;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Info {
        private double rate;
        private long timestamp;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Query {
        private double amount;
        private String from;
        private String to;

    }
}
