package hu.therealuhlarzoltan.expensables.microservices.income.components.converters;

import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class ZonedDateTimeToStringConverter implements Converter<ZonedDateTime, String> {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_ZONED_DATE_TIME;

    @Override
    public String convert(@NonNull ZonedDateTime source) {
        return source.format(FORMATTER);
    }
}
