package hu.therealuhlarzoltan.expensables.microservices.transaction.components;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class StringToZonedDateTimeConverter implements Converter<String, ZonedDateTime> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_ZONED_DATE_TIME;

    @Override
    public ZonedDateTime convert(String source) {
        return ZonedDateTime.parse(source, FORMATTER);
    }
}
