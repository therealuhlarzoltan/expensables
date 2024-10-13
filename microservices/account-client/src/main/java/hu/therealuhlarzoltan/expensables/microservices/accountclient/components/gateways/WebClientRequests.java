package hu.therealuhlarzoltan.expensables.microservices.accountclient.components.gateways;

import com.fasterxml.jackson.databind.ObjectMapper;
import hu.therealuhlarzoltan.expensables.api.microservices.exceptions.InvalidInputDataException;
import hu.therealuhlarzoltan.expensables.api.microservices.exceptions.NotFoundException;
import hu.therealuhlarzoltan.expensables.util.HttpErrorInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.TimeoutException;

import static java.util.logging.Level.FINE;

@Component
public class WebClientRequests {
    private static final Logger LOG = LoggerFactory.getLogger(WebClientRequests.class);
    private static WebClient webClient;
    private static ObjectMapper objectMapper;

    private WebClientRequests() {
    }

    @Autowired
    public void setWebClient(WebClient webClient) {
        WebClientRequests.webClient = webClient;
    }

    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        WebClientRequests.objectMapper = objectMapper;
    }

    public static <T> Mono<T> getForSingleReactive(URI url, Class<T> clazz) {
        return webClient.get().uri(url)
                .retrieve().bodyToMono(clazz)
                .log(LOG.getName(), FINE)
                .onErrorMap(Throwable.class, ex -> handleWebClientException(ex));
    }

    public static <T> Flux<T> getForManyReactive(URI url, Class<T> clazz) {
        return webClient.get().uri(url)
                .retrieve().bodyToFlux(clazz)
                .log(LOG.getName(), FINE)
                .onErrorMap(Throwable.class, ex -> handleWebClientException(ex));
    }

    public static Throwable handleWebClientException(Throwable ex) {
        if (!(ex instanceof WebClientResponseException) && !(ex instanceof TimeoutException)) {
            LOG.warn("Got a unexpected error: {}, will rethrow it", ex.toString());
            return ex;
        }

        WebClientResponseException wcre = (WebClientResponseException) ex;

        // Resolving error
        switch (HttpStatus.resolve(wcre.getStatusCode().value())) {
            case NOT_FOUND:
                return new NotFoundException(getErrorMessage(wcre));
            case BAD_REQUEST, UNPROCESSABLE_ENTITY:
                return new InvalidInputDataException(getErrorMessage(wcre));
            default:
                LOG.warn("Got an unexpected HTTP error: {}, will rethrow it", wcre.getStatusCode());
                LOG.warn("Error body: {}", wcre.getResponseBodyAsString());
                return ex;
        }
    }

    public static String getErrorMessage(WebClientResponseException ex) {
        try {
            return objectMapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
        } catch (IOException ioex) {
            return ex.getMessage();
        }
    }
}
