package hu.therealuhlarzoltan.expensables.microservices.cashflow.services;

import org.springframework.messaging.Message;
import hu.therealuhlarzoltan.expensables.api.microservices.events.HttpResponseEvent;
import hu.therealuhlarzoltan.expensables.api.microservices.exceptions.ServiceResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Service
public class ResponseListenerServiceImpl implements ResponseListenerService {
    private static final Logger LOG = LoggerFactory.getLogger(ResponseListenerServiceImpl.class);
    private final Map<String, MonoSink<HttpResponseEvent>> responseSinks = new ConcurrentHashMap<>();

    @Bean
    public Consumer<Message<HttpResponseEvent>> accountResponseProcessor() {
        return message -> {
            HttpResponseEvent event = message.getPayload();
            String correlationId = (String) message.getHeaders().get("correlationId");
            HttpResponseEvent.Type eventType = event.getEventType();
            LOG.info("Processing message created at {}... Correlation Id: {} Event Type: {}", event.getEventCreatedAt(), correlationId, eventType);
            processEvent(correlationId, event);
        };
    }

    @Bean
    public Consumer<Message<HttpResponseEvent>> incomeResponseProcessor() {
        return message -> {
            HttpResponseEvent event = message.getPayload();
            String correlationId = (String) message.getHeaders().get("correlationId");
            HttpResponseEvent.Type eventType = event.getEventType();
            LOG.info("Processing message created at {}... Correlation Id: {} Event Type: {}", event.getEventCreatedAt(), correlationId, eventType);
            processEvent(correlationId, event);
        };
    }

    @Bean
    public Consumer<Message<HttpResponseEvent>> expenseResponseProcessor() {
        return message -> {
            HttpResponseEvent event = message.getPayload();
            String correlationId = (String) message.getHeaders().get("correlationId");
            HttpResponseEvent.Type eventType = event.getEventType();
            LOG.info("Processing message created at {}... Correlation Id: {} Event Type: {}", event.getEventCreatedAt(), correlationId, eventType);
            processEvent(correlationId, event);
        };
    }

    public Mono<HttpResponseEvent> waitForResponse(String correlationId, Duration timeout) {
        return Mono.<HttpResponseEvent>create(sink -> responseSinks.put(correlationId, sink))
                .timeout(timeout, Mono.error(new ServiceResponseException("Dependent service call failed", HttpStatus.FAILED_DEPENDENCY)))
                .doOnTerminate(() -> responseSinks.remove(correlationId));
    }

    private void processEvent(String correlationId, HttpResponseEvent event) {
        MonoSink<HttpResponseEvent> sink = responseSinks.remove(correlationId);
        if (sink != null) {
            sink.success(event);
        } else {
            LOG.warn("No responseSink found for correlationId: {}", correlationId);
        }
    }
}
