package hu.therealuhlarzoltan.expensables.microservices.exchange.components;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import hu.therealuhlarzoltan.expensables.api.microservices.core.exchange.ExchangeRequest;
import hu.therealuhlarzoltan.expensables.api.microservices.core.exchange.ExchangeResponse;
import hu.therealuhlarzoltan.expensables.api.microservices.exceptions.InvalidInputDataException;
import hu.therealuhlarzoltan.expensables.api.microservices.exceptions.NotFoundException;
import hu.therealuhlarzoltan.expensables.api.microservices.exceptions.ServiceResponseException;
import hu.therealuhlarzoltan.expensables.microservices.exchange.models.ErrorResponse;
import hu.therealuhlarzoltan.expensables.microservices.exchange.models.SuccessResponse;
import hu.therealuhlarzoltan.expensables.util.HttpErrorInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.math.BigDecimal;
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

    public static Mono<ExchangeResponse> getForConversionReactive(String uri) {
        return webClient.get().uri(uri)
                .retrieve().bodyToMono(String.class)
                .flatMap(rawResponse -> {
                    try {
                        JsonNode root = objectMapper.readTree(rawResponse);
                        if (root.get("success").asBoolean()) {
                            SuccessResponse response = objectMapper.treeToValue(root, SuccessResponse.class);
                            return Mono.just(ExchangeResponse.builder().result(BigDecimal.valueOf(response.getResult())).build());
                        } else {
                            ErrorResponse response = objectMapper.treeToValue(root, ErrorResponse.class);
                            String exceptionMsg = "";
                            if (response.getError().getInfo().contains("["))
                                exceptionMsg = response.getError().getInfo().substring(0, response.getError().getInfo().indexOf('[') - 1);
                            else
                                exceptionMsg = response.getError().getInfo();
                            WebClientException ex = new WebClientResponseException(422, exceptionMsg, null, null, null);
                            return Mono.error(ex);
                        }
                    } catch (IOException ex) {
                        return Mono.error(new ServiceResponseException("Error parsing response", HttpStatus.UNPROCESSABLE_ENTITY));
                    }
                })
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
