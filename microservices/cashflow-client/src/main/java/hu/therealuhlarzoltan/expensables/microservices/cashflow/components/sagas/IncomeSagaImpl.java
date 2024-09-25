package hu.therealuhlarzoltan.expensables.microservices.cashflow.components.sagas;

import com.fasterxml.jackson.databind.ObjectMapper;
import hu.therealuhlarzoltan.expensables.api.microservices.core.income.IncomeRecord;
import hu.therealuhlarzoltan.expensables.api.microservices.events.*;
import hu.therealuhlarzoltan.expensables.api.microservices.exceptions.EventProcessingException;
import hu.therealuhlarzoltan.expensables.api.microservices.exceptions.InvalidInputDataException;
import hu.therealuhlarzoltan.expensables.api.microservices.exceptions.NotFoundException;
import hu.therealuhlarzoltan.expensables.api.microservices.exceptions.ServiceResponseException;
import hu.therealuhlarzoltan.expensables.microservices.cashflow.components.gateways.IncomeGateway;
import hu.therealuhlarzoltan.expensables.microservices.cashflow.services.ResponseListenerService;
import hu.therealuhlarzoltan.expensables.util.HttpErrorInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import static hu.therealuhlarzoltan.expensables.api.microservices.events.HttpResponseEvent.Type.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class IncomeSagaImpl implements IncomeSaga {
    private static final Logger LOG = LoggerFactory.getLogger(IncomeSagaImpl.class);
    private final ResponseListenerService responseListener;
    private final Scheduler publishEventScheduler;
    private final StreamBridge streamBridge;
    private final ObjectMapper mapper;
    private final int RESPONSE_EVENT_WAIT_DURATION;
    private final IncomeGateway incomeGateway;

    private enum IncomeCreationState {
        INIT,
        INCOME_CREATED,
        ACCOUNT_UPDATED
    }

    private enum IncomeUpdateState {
        INIT,
        INCOME_UPDATED,
        ACCOUNT_UPDATED
    }

    private enum IncomeDeletionState {
        INIT,
        INCOME_DELETED,
        ACCOUNT_UPDATED
    }

    @Autowired
    public IncomeSagaImpl(
            @Qualifier("publishEventScheduler") Scheduler publishEventScheduler,
            WebClient webClient,
            ObjectMapper mapper,
            StreamBridge streamBridge,
            ResponseListenerService responseListener,
            IncomeGateway incomeGateway,
            @Value("${app.response-event-wait-duration:10}") int responseEventWaitDuration) {
        this.publishEventScheduler = publishEventScheduler;
        this.mapper = mapper;
        this.streamBridge = streamBridge;
        this.responseListener = responseListener;
        this.incomeGateway = incomeGateway;
        this.RESPONSE_EVENT_WAIT_DURATION = responseEventWaitDuration;
    }

    @Override
    public Mono<IncomeRecord> createIncome(IncomeRecord incomeRecord) {
        AtomicReference<IncomeCreationState> state = new AtomicReference<>(IncomeCreationState.INIT);
        String creatIncomeCorrId = UUID.randomUUID().toString();
        String updateAccountCorrId = UUID.randomUUID().toString();
        AtomicReference<String> responseMessage = new AtomicReference<>();
        AtomicReference<HttpStatus> responseStatus = new AtomicReference<>(HttpStatus.OK);
        return Mono.fromRunnable(() -> {
            LOG.info("Starting the income creation saga for income: {}", incomeRecord);
            sendMessage("incomes-out-0", creatIncomeCorrId, new CrudEvent<String, IncomeRecord>(CrudEvent.Type.CREATE, incomeRecord.getRecordId(), incomeRecord));
        }).then(responseListener.waitForResponse(creatIncomeCorrId, Duration.ofSeconds(RESPONSE_EVENT_WAIT_DURATION)))
                .flatMap(response -> {
                    if (response.getEventType() == SUCCESS) {
                        LOG.info("Income created successfully with id: {}", incomeRecord.getRecordId());
                        state.set(IncomeCreationState.INCOME_CREATED);
                        return Mono.empty();
                    }
                    else if (response.getEventType() == ERROR) {
                        LOG.warn("Income creation failed with error: {}", response.getData().getMessage());
                        responseMessage.set(response.getData().getMessage());
                        responseStatus.set(response.getData().getStatus());
                        return Mono.error(createMessageResponseError(response.getData()));
                    } else {
                        LOG.error("Unknown response event received during income creation");
                       return Mono.error(new EventProcessingException("Could not process unknown response"));
                    }
                }).then(Mono.fromRunnable(() -> {
                    if (state.get() == IncomeCreationState.INCOME_CREATED) {
                        sendMessage("accounts-out-0", updateAccountCorrId, new AccountEvent<>(AccountEvent.Type.DEPOSIT, incomeRecord.getAccountId(), incomeRecord.getAmount()));
                }
            })).then(responseListener.waitForResponse(updateAccountCorrId, Duration.ofSeconds(RESPONSE_EVENT_WAIT_DURATION)))
            .flatMap(response -> {
                if (response.getEventType() == SUCCESS) {
                    LOG.info("Account updated successfully with id: {} because of new income entity with id: {}", incomeRecord.getAccountId(), incomeRecord.getRecordId());
                    state.set(IncomeCreationState.ACCOUNT_UPDATED);
                    return Mono.empty();
                }
                else if (response.getEventType() == ERROR) {
                    LOG.warn("Failed to update account with id {} for new income entity with id: {}, error: {}", incomeRecord.getAccountId(), incomeRecord.getRecordId(), response.getData().getMessage());
                    responseMessage.set(response.getData().getMessage());
                    responseStatus.set(response.getData().getStatus());
                    return Mono.error(createMessageResponseError(response.getData()));
                } else {
                    LOG.error("Unknown response event received during income creation");
                    return Mono.error(new EventProcessingException("Could not process unknown response event"));
                }
            }).doOnError((ex) -> ex instanceof ServiceResponseException, (ex) -> {
                LOG.warn("Couldn't complete income creation saga due to error: {}", ex.getMessage());
                LOG.warn("Rolling back the income creation saga for income with id: {}", incomeRecord.getRecordId());
                restoreChanges(state.get(), incomeRecord);
            }).subscribeOn(publishEventScheduler).then(Mono.defer(() -> {
                    if (responseMessage.get() == null && responseStatus.get() == HttpStatus.OK) {
                        return Mono.just(incomeRecord);
                    } else {
                        return Mono.error(mapException(responseMessage.get(), responseStatus.get()));
                    }
                }));
    }

    private Throwable mapException(String message, HttpStatus status) {
        return new ServiceResponseException(message, status);
    }

    @Override
    public Mono<IncomeRecord> updateIncome(IncomeRecord incomeRecord) {
        return null;
    }

    @Override
    public Mono<Void> deleteIncome(IncomeRecord incomeRecord) {
        return null;
    }

    @Override
    public Mono<Void> deleteIncome(IncomeRecord incomeRecord, BigDecimal amount) {
        return null;
    }

    private void sendMessage(String bindingName, String correlationId, Event<?, ?> event) {
        LOG.debug("Sending a {} message to {}", event.getEventType(), bindingName);
        Message message = MessageBuilder.withPayload(event)
                .setHeader("partitionKey", event.getKey())
                .setHeader("correlationId", correlationId)
                .build();
        streamBridge.send(bindingName, message);
    }

    private void sendMessage(String bindingName, Event<?, ?> event) {
        LOG.debug("Sending a {} message to {}", event.getEventType(), bindingName);
        Message message = MessageBuilder.withPayload(event)
                .setHeader("partitionKey", event.getKey())
                .build();
        streamBridge.send(bindingName, message);
    }

    private Throwable handleException(Throwable ex) {

        if (!(ex instanceof WebClientResponseException)) {
            LOG.warn("Got a unexpected error: {}, will rethrow it", ex.toString());
            return ex;
        }

        WebClientResponseException wcre = (WebClientResponseException)ex;

        switch (HttpStatus.resolve(wcre.getStatusCode().value())) {
            case NOT_FOUND:
                return new NotFoundException(getErrorMessage(wcre));
            case UNPROCESSABLE_ENTITY:
                return new InvalidInputDataException(getErrorMessage(wcre));

            default:
                LOG.warn("Got an unexpected HTTP error: {}, will rethrow it", wcre.getStatusCode());
                LOG.warn("Error body: {}", wcre.getResponseBodyAsString());
                return ex;
        }
    }


    private String getErrorMessage(WebClientResponseException ex) {
        try {
            return mapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
        } catch (IOException ioex) {
            return ex.getMessage();
        }
    }

    private <T> T deserializeObjectFromJson(String json, Class<T> clazz) {
        T obj = null;
        try {
            obj = mapper.readValue(json, clazz);
        } catch (IOException e) {
            LOG.error("Couldn't deserialize object from json: {}", e.getMessage());
        }
        return obj;
    }

    private Throwable createMessageResponseError(ResponsePayload data) {
        return new ServiceResponseException(data.getMessage(), data.getStatus());
    }

    private void restoreChanges(IncomeCreationState state, IncomeRecord record) {
        switch (state) {
            case INIT:
                LOG.info("No rollback needed after failed income creation with income id: {}", record.getRecordId());
                break;
            case ACCOUNT_UPDATED:
                LOG.info("Rolling back account update for income creation with income id: {}", record.getRecordId());
                sendMessage("accounts-out-0", new AccountEvent<>(AccountEvent.Type.WITHDRAW, record.getAccountId(), record.getAmount()));
                LOG.info("Rolling back income creation with income id: {}", record.getRecordId());
                sendMessage("incomes-out-0", new CrudEvent<String, IncomeRecord>(CrudEvent.Type.DELETE, record.getRecordId(), record));
                break;
            case INCOME_CREATED:
                LOG.info("Rolling back income creation with income id: {}", record.getRecordId());
                sendMessage("incomes-out-0", new CrudEvent<String, IncomeRecord>(CrudEvent.Type.DELETE, record.getRecordId(), record));
                break;
            default:
                LOG.warn("Couldn't determine restore action for income creation state: {} with income id: {}", state, record.getRecordId());
                return;
        }

    }
}
