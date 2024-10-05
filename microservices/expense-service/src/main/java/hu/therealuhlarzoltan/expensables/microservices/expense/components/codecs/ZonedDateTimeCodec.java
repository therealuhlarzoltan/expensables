package hu.therealuhlarzoltan.expensables.microservices.expense.components.codecs;

import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class ZonedDateTimeCodec implements Codec<ZonedDateTime> {

    @Override
    public void encode(BsonWriter writer, ZonedDateTime value, org.bson.codecs.EncoderContext encoderContext) {
        writer.writeString(value.format(DateTimeFormatter.ISO_ZONED_DATE_TIME));
    }

    @Override
    public ZonedDateTime decode(BsonReader reader, org.bson.codecs.DecoderContext decoderContext) {
        return ZonedDateTime.parse(reader.readString(), DateTimeFormatter.ISO_ZONED_DATE_TIME);
    }

    @Override
    public Class<ZonedDateTime> getEncoderClass() {
        return ZonedDateTime.class;
    }
}
