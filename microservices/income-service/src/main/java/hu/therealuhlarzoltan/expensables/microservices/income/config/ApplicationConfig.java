package hu.therealuhlarzoltan.expensables.microservices.income.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import hu.therealuhlarzoltan.expensables.microservices.income.components.codecs.ZonedDateTimeCodec;
import hu.therealuhlarzoltan.expensables.microservices.income.components.converters.StringToZonedDateTimeConverter;
import hu.therealuhlarzoltan.expensables.microservices.income.components.converters.ZonedDateTimeToStringConverter;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.Arrays;
import java.util.List;

@Configuration
public class ApplicationConfig {
    private static final Logger LOG = LoggerFactory.getLogger(ApplicationConfig.class);

    private final Integer threadPoolSize;
    private final Integer taskQueueSize;
    private final ZonedDateTimeCodec zonedDateTimeCodec;

    @Autowired
    public ApplicationConfig(
            @Value("${app.threadPoolSize:10}") Integer threadPoolSize,
            @Value("${app.taskQueueSize:100}") Integer taskQueueSize,
            ZonedDateTimeCodec zonedDateTimeCodec
    ) {
        this.threadPoolSize = threadPoolSize;
        this.taskQueueSize = taskQueueSize;
        this.zonedDateTimeCodec = zonedDateTimeCodec;
    }

    @Bean
    public Scheduler publishEventScheduler() {
        LOG.info("Creates a messagingScheduler with connectionPoolSize = {}", threadPoolSize);
        return Schedulers.newBoundedElastic(threadPoolSize, taskQueueSize, "publish-pool");
    }

    @Bean
    @Lazy
    @ConditionalOnProperty(name = {"spring.data.mongodb.host", "spring.data.mongodb.port", "spring.data.mongodb.database"})
    public MongoClient mongoClient(
            @Value("${spring.data.mongodb.host}") String mongoHost,
            @Value("${spring.data.mongodb.port}") int mongoPort,
            @Value("${spring.data.mongodb.database}") String databaseName
    ) {
        String connectionString = String.format("mongodb://%s:%d/%s", mongoHost, mongoPort, databaseName);

        CodecRegistry codecRegistry = CodecRegistries.fromRegistries(
                MongoClientSettings.getDefaultCodecRegistry(),
                CodecRegistries.fromCodecs(zonedDateTimeCodec)
        );

        MongoClientSettings settings = MongoClientSettings.builder()
                .codecRegistry(codecRegistry)
                .applyConnectionString(new ConnectionString(connectionString))
                .build();

        return MongoClients.create(settings);
    }

    @Bean
    @Lazy
    @DependsOn("mongoClient")
    public MongoCustomConversions customConversions(
            StringToZonedDateTimeConverter stringToZonedDateTimeConverter,
            ZonedDateTimeToStringConverter zonedDateTimeToStringConverter
    ) {
        List<Converter<?, ?>> converters = Arrays.asList(stringToZonedDateTimeConverter, zonedDateTimeToStringConverter);
        return new MongoCustomConversions(converters);
    }
}