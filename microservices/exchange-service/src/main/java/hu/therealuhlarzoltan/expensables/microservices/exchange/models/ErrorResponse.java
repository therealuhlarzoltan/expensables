package hu.therealuhlarzoltan.expensables.microservices.exchange.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    private boolean success;
    private Error error;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Error {
        private int code;
        private String type;
        private String info;
    }
}
