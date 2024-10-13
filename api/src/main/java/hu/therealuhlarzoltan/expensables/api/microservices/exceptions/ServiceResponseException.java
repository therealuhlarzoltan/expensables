package hu.therealuhlarzoltan.expensables.api.microservices.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

public class ServiceResponseException extends RuntimeException {
    private final HttpStatus responseStatus;

    public ServiceResponseException() {
        this.responseStatus = null;
    }

    public ServiceResponseException(String message, HttpStatus responseStatus) {
        super(message);
        this.responseStatus = responseStatus;
    }

    public ServiceResponseException(String message, Throwable cause, HttpStatus responseStatus) {
        super(message, cause);
        this.responseStatus = responseStatus;
    }

    public ServiceResponseException(Throwable cause, HttpStatus responseStatus) {
        super(cause);
        this.responseStatus = responseStatus;
    }

    public ServiceResponseException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, HttpStatus responseStatus) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.responseStatus = responseStatus;
    }

    public HttpStatus getResponseStatus() {
        return responseStatus;
    }
}
