package hu.therealuhlarzoltan.expensables.util;

import hu.therealuhlarzoltan.expensables.api.microservices.exceptions.InsufficientFundsException;
import hu.therealuhlarzoltan.expensables.api.microservices.exceptions.InvalidInputDataException;
import hu.therealuhlarzoltan.expensables.api.microservices.exceptions.NotFoundException;
import hu.therealuhlarzoltan.expensables.api.microservices.exceptions.ServiceResponseException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(InvalidInputDataException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ResponseEntity<ApiError> handleInvalidInputDataException(InvalidInputDataException e) {
        return new ResponseEntity<>(createApiError(e, HttpStatus.UNPROCESSABLE_ENTITY), HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ApiError> handleNotFoundException(NotFoundException e) {
        return new ResponseEntity<>(createApiError(e, HttpStatus.NOT_FOUND), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InsufficientFundsException.class)
    @ResponseStatus(HttpStatus.PRECONDITION_FAILED)
    public ResponseEntity<ApiError> handleInsufficientFundsException(InsufficientFundsException e) {
        return new ResponseEntity<>(createApiError(e, HttpStatus.PRECONDITION_FAILED), HttpStatus.PRECONDITION_FAILED);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ResponseEntity<ApiError> handleIllegalArgumentException(IllegalArgumentException e) {
        return new ResponseEntity<>(createApiError(e, HttpStatus.UNPROCESSABLE_ENTITY), HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ResponseEntity<ApiError> handleIllegalStateException(IllegalStateException e) {
        return new ResponseEntity<>(createApiError(e, HttpStatus.UNPROCESSABLE_ENTITY), HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<ApiError> handleOptimisticLockingFailureException(OptimisticLockingFailureException e) {
        return new ResponseEntity<>(createApiError("Resource already updated", HttpStatus.CONFLICT), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ResponseEntity<ApiError> handleConstraintViolationException(ConstraintViolationException e) {
        String msg = e.getConstraintViolations().stream().findFirst().map(ConstraintViolation::getMessage).orElse("Constraint violation");
        return new ResponseEntity<>(createApiError(msg, HttpStatus.UNPROCESSABLE_ENTITY), HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        String msg = ex.getAllErrors().get(0).getDefaultMessage();
        return createApiError(msg, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DuplicateKeyException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<ApiError> handleDuplicateKeyException(DuplicateKeyException e) {
        return new ResponseEntity<>(createApiError("Id already exists", HttpStatus.CONFLICT), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(ServiceResponseException.class)
    public ResponseEntity<ApiError> handleServiceResponseException(ServiceResponseException e) {
        var error = createApiError(e.getMessage(), e.getResponseStatus());
        return new ResponseEntity<>(error, e.getResponseStatus());
    }

    /*@ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleException(Exception e) {
        return createApiError("An unhandled exception occurred", HttpStatus.INTERNAL_SERVER_ERROR);
    }*/


    private ApiError createApiError(Exception ex, HttpStatus status) {
        LOG.info("Returning error response: {}, with status: {}", ex.getMessage(), status);
        return new ApiError(ex.getMessage(), status, ZonedDateTime.now());
    }

    private ApiError createApiError(String message, HttpStatus status) {
        LOG.info("Returning error response: {}, with status: {}", message, status);
        return new ApiError(message, status, ZonedDateTime.now());
    }
}
