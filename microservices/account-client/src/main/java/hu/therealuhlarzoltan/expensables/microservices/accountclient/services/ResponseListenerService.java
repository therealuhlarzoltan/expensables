package hu.therealuhlarzoltan.expensables.microservices.accountclient.services;

import hu.therealuhlarzoltan.expensables.api.microservices.events.HttpResponseEvent;
import hu.therealuhlarzoltan.expensables.api.microservices.events.ResponsePayload;
import hu.therealuhlarzoltan.expensables.api.microservices.exceptions.ServiceResponseException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class
ResponseListenerService {
    private static final Logger LOG = LoggerFactory.getLogger(ResponseListenerService.class);
    private final Map<String, MonoSink<HttpResponseEvent>> responseSinks = new ConcurrentHashMap<>();

    @Bean
    public Consumer<Message<HttpResponseEvent>> httpResponseProcessor() {
        return message -> {
            HttpResponseEvent event = message.getPayload();
            String correlationId = (String) message.getHeaders().get("correlationId");
            HttpResponseEvent.Type eventType = event.getEventType();
            LOG.info("Processing message created at {}...", event.getEventCreatedAt());
            LOG.info("CorrelationId: {}", correlationId);
            LOG.info("Event type: {}", eventType);
            MonoSink<HttpResponseEvent> sink = responseSinks.remove(correlationId);
            if (sink != null) {
                sink.success(event);
            } else {
                LOG.warn("No responseSink found for correlationId: {}", correlationId);
            }
        };
    }

    public Mono<HttpResponseEvent> waitForResponse(String correlationId, Duration timeout) {
        return Mono.<HttpResponseEvent>create(sink -> responseSinks.put(correlationId, sink))
                .timeout(timeout, Mono.error(new ServiceResponseException("Dependent service failed", HttpStatus.FAILED_DEPENDENCY)))
                .doOnTerminate(() -> responseSinks.remove(correlationId));
    }
}
