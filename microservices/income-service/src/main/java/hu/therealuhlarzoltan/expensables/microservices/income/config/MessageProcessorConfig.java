package hu.therealuhlarzoltan.expensables.microservices.income.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.DuplicateKeyException;
import hu.therealuhlarzoltan.expensables.api.microservices.core.expense.ExpenseController;
import hu.therealuhlarzoltan.expensables.api.microservices.core.expense.ExpenseRecord;
import hu.therealuhlarzoltan.expensables.api.microservices.core.income.IncomeController;
import hu.therealuhlarzoltan.expensables.api.microservices.core.income.IncomeRecord;
import hu.therealuhlarzoltan.expensables.api.microservices.events.CrudEvent;
import hu.therealuhlarzoltan.expensables.api.microservices.events.Event;
import hu.therealuhlarzoltan.expensables.api.microservices.events.HttpResponseEvent;
import hu.therealuhlarzoltan.expensables.api.microservices.events.ResponsePayload;
import hu.therealuhlarzoltan.expensables.api.microservices.exceptions.EventProcessingException;
import hu.therealuhlarzoltan.expensables.api.microservices.exceptions.InsufficientFundsException;
import hu.therealuhlarzoltan.expensables.api.microservices.exceptions.InvalidInputDataException;
import hu.therealuhlarzoltan.expensables.api.microservices.exceptions.NotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.io.IOException;
import java.util.function.Consumer;

@Configuration
public class MessageProcessorConfig {
    private static final Logger LOG = LoggerFactory.getLogger(MessageProcessorConfig.class);
    private final StreamBridge streamBridge;
    private final IncomeController controller;
    private final ObjectMapper objectMapper;

    @Autowired
    public MessageProcessorConfig(StreamBridge streamBridge, ObjectMapper objectMapper, IncomeController controller) {
        this.objectMapper = objectMapper;
        this.controller = controller;
        this.streamBridge = streamBridge;
    }

    @Bean
    public Consumer<Message<Event<?, ?>>> messageProcessor() {
        return message -> {
            Event<?, ?> event = message.getPayload();
            String correlationId = (String) message.getHeaders().get("correlationId");
            LOG.info("Processing message created at {}...", event.getEventCreatedAt());
            if (event instanceof CrudEvent<?, ?>) {
                LOG.info("CRUD event detected...");
                if (!(event.getKey() instanceof String)) {
                    String errorMessage = "Incorrect CRUD event parameters, expected <String, IncomeRecord>";
                    LOG.warn(errorMessage);
                    throw new EventProcessingException(errorMessage);
                }
               IncomeRecord crudEventData;
                try {
                    crudEventData = objectMapper.convertValue(event.getData(), IncomeRecord.class);
                } catch (IllegalArgumentException e) {
                    String errorMessage = "Incorrect CRUD event parameters, expected <String, IncomeRecord>";
                    LOG.warn(errorMessage);
                    throw new EventProcessingException(errorMessage);
                }
                @SuppressWarnings(value = "unchecked") // We know that the Event is a CrudEvent and the key is a String
                CrudEvent<String, ?> eventWithKey = (CrudEvent<String, ?>) event;
                CrudEvent<String, IncomeRecord> crudEvent = new CrudEvent<String, IncomeRecord>(eventWithKey.getEventType(), eventWithKey.getKey(), crudEventData);
                switch (crudEvent.getEventType()) {
                    case CREATE:
                        IncomeRecord income = crudEvent.getData();
                        controller.createIncome(income)
                                .doOnSuccess(createdExpense -> {
                                    String jsonString = serializeObjectToJson(createdExpense);
                                    ResponsePayload httpInfo = new ResponsePayload(jsonString, HttpStatus.CREATED);
                                    HttpResponseEvent responseEvent = new HttpResponseEvent(HttpResponseEvent.Type.SUCCESS, correlationId, httpInfo);
                                    sendResponseMessage("incomeResponses-out-0", correlationId, responseEvent);
                                })
                                .doOnError(throwable -> {
                                    LOG.error("Failed to create income, exception message: {}", throwable.getMessage());
                                    ResponsePayload httpInfo = new ResponsePayload(throwable.getMessage(), resolveHttpStatus(throwable));
                                    HttpResponseEvent responseEvent = new HttpResponseEvent(HttpResponseEvent.Type.ERROR, correlationId, httpInfo);
                                    sendResponseMessage("incomeResponses-out-0", correlationId, responseEvent);
                                })
                                .subscribe();
                        break;
                    case UPDATE:
                        IncomeRecord incomeToUpdate = crudEvent.getData();
                        controller.updateIncome(incomeToUpdate)
                                .doOnSuccess(updatedExpense -> {
                                    String jsonString = serializeObjectToJson(updatedExpense);
                                    ResponsePayload httpInfo = new ResponsePayload(jsonString, HttpStatus.ACCEPTED);
                                    HttpResponseEvent responseEvent = new HttpResponseEvent(HttpResponseEvent.Type.SUCCESS, correlationId, httpInfo);
                                    sendResponseMessage("incomeResponses-out-0", correlationId, responseEvent);
                                })
                                .doOnError(throwable -> {
                                    LOG.error("Failed to update income, exception message: {}", throwable.getMessage());
                                    ResponsePayload httpInfo = new ResponsePayload(throwable.getMessage(), resolveHttpStatus(throwable));
                                    HttpResponseEvent responseEvent = new HttpResponseEvent(HttpResponseEvent.Type.ERROR, correlationId, httpInfo);
                                    sendResponseMessage("incomeResponses-out-0", correlationId, responseEvent);
                                })
                                .subscribe();
                        break;
                    case DELETE:
                        String incomeId = crudEvent.getKey();
                        controller.deleteIncome(incomeId).block();
                        break;
                    case DELETE_ALL:
                        String accountId = crudEvent.getKey();
                        controller.deleteIncomesByAccount(accountId).block();
                        break;
                    default:
                        String errorMessage = "Incorrect event type: " + event.getEventType() + ", expected a CREATE, UPDATE, DELETE or DELETE_ALL event";
                        LOG.warn(errorMessage);
                        throw new EventProcessingException(errorMessage);
                }
            } else {
                String errorMessage = "Incorrect event type: " + event.getClass().getSimpleName() + ", expected a CrudEvent";
                LOG.warn(errorMessage);
                throw new EventProcessingException(errorMessage);
            }
            LOG.info("Message processing done!");
        };
    }

    private void sendResponseMessage(String bindingName, String correlationId, HttpResponseEvent event) {
        if (correlationId == null) {
            LOG.warn("No correlationId found in the message headers, will not send a response message");
            return;
        }
        LOG.info("Sending a response message to {} with correlationId {}", bindingName, correlationId);
        Message<HttpResponseEvent> responseMessage = MessageBuilder.withPayload(event)
                .setHeader("correlationId", correlationId)
                .build();
        boolean success = streamBridge.send(bindingName, responseMessage);
        if (!success) {
            LOG.error("Failed to send the response message to {} with correlationId {}", bindingName, correlationId);
        }
    }

    private String serializeObjectToJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (IOException e) {
            LOG.error("Failed to serialize object to JSON: {}", e.getMessage());
            return null;
        }
    }

    private HttpStatus resolveHttpStatus(Throwable throwable) {
        return switch (throwable) {
            case NotFoundException notFoundException -> HttpStatus.NOT_FOUND;
            case InsufficientFundsException insufficientFundsException -> HttpStatus.PRECONDITION_FAILED;
            case EventProcessingException eventProcessingException -> HttpStatus.FAILED_DEPENDENCY;
            case IllegalArgumentException illegalArgumentException -> HttpStatus.UNPROCESSABLE_ENTITY;
            case IllegalStateException illegalStateException -> HttpStatus.UNPROCESSABLE_ENTITY;
            case ConstraintViolationException constraintViolationException -> HttpStatus.UNPROCESSABLE_ENTITY;
            case MethodArgumentNotValidException methodArgumentNotValidException -> HttpStatus.BAD_REQUEST;
            case DuplicateKeyException duplicateKeyException -> HttpStatus.CONFLICT;
            case OptimisticLockingFailureException optimisticLockingFailureException -> HttpStatus.CONFLICT;
            case InvalidInputDataException invalidInputDataException -> HttpStatus.UNPROCESSABLE_ENTITY;
            case null, default -> HttpStatus.FAILED_DEPENDENCY;
        };
    }
}
