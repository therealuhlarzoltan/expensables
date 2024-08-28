package hu.therealuhlarzoltan.expensables.util;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Data
@RequiredArgsConstructor
public class ApiError {
    private final String message;
    private final HttpStatus status;
    private final LocalDateTime timestamp;
}

