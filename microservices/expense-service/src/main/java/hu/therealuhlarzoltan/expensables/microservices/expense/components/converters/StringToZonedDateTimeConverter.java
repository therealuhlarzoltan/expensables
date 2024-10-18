package hu.therealuhlarzoltan.expensables.microservices.expense.components.converters;

import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class StringToZonedDateTimeConverter implements Converter<String, ZonedDateTime> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_ZONED_DATE_TIME;

    @Override
    public ZonedDateTime convert(@NonNull String source) {
        return ZonedDateTime.parse(source, FORMATTER);
    }
}
