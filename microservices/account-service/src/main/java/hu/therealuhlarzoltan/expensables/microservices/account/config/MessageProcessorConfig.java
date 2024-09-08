package hu.therealuhlarzoltan.expensables.microservices.account.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.DuplicateKeyException;
import hu.therealuhlarzoltan.expensables.api.microservices.core.account.Account;
import hu.therealuhlarzoltan.expensables.api.microservices.core.account.AccountController;
import hu.therealuhlarzoltan.expensables.api.microservices.events.*;
import hu.therealuhlarzoltan.expensables.api.microservices.exceptions.EventProcessingException;
import hu.therealuhlarzoltan.expensables.api.microservices.exceptions.InsufficientFundsException;
import hu.therealuhlarzoltan.expensables.api.microservices.exceptions.InvalidInputDataException;
import hu.therealuhlarzoltan.expensables.api.microservices.exceptions.NotFoundException;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.MethodArgumentNotValidException;
import reactor.core.scheduler.Scheduler;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.UUID;
import java.util.function.Consumer;

@Configuration
public class MessageProcessorConfig {
    private static final Logger LOG = LoggerFactory.getLogger(MessageProcessorConfig.class);

    private final ObjectMapper objectMapper;
    private final AccountController accountController;
    private final StreamBridge streamBridge;

    @Autowired
    public MessageProcessorConfig(StreamBridge streamBridge, ObjectMapper objectMapper, AccountController accountController, ObjectMapper mapper) {
        this.objectMapper = objectMapper;
        this.accountController = accountController;
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
                    String errorMessage = "Incorrect CRUD event parameters, expected <String, Account>";
                    LOG.warn(errorMessage);
                    throw new EventProcessingException(errorMessage);
                }
                Account crudEventData;
                try {
                    crudEventData = objectMapper.convertValue(event.getData(), Account.class);
                } catch (IllegalArgumentException e) {
                    String errorMessage = "Incorrect CRUD event parameters, expected <String, Account>";
                    LOG.warn(errorMessage);
                    throw new EventProcessingException(errorMessage);
                }
                @SuppressWarnings(value = "unchecked") // We know that the Event is a CrudEvent and the key is a String
                CrudEvent<String, ?> eventWithKey = (CrudEvent<String, ?>) event;
                CrudEvent<String, Account> crudEvent = new CrudEvent<String, Account>(eventWithKey.getEventType(), eventWithKey.getKey(), crudEventData);
                switch (crudEvent.getEventType()) {
                    case CREATE:
                        Account account = crudEvent.getData();
                        //accountController.createAccount(account).block();
                        accountController.createAccount(account)
                                .doOnSuccess(createdAccount -> {
                                    String jsonString = serializeObjectToJson(createdAccount);
                                    ResponsePayload httpInfo = new ResponsePayload(jsonString, HttpStatus.CREATED);
                                    HttpResponseEvent responseEvent = new HttpResponseEvent(HttpResponseEvent.Type.SUCCESS, correlationId, httpInfo);
                                    sendResponseMessage("accountResponses-out-0", correlationId, responseEvent);
                                })
                                .doOnError(throwable -> {
                                    LOG.error("Failed to create account, exception message: {}", throwable.getMessage());
                                    ResponsePayload httpInfo = new ResponsePayload(throwable.getMessage(), resolveHttpStatus(throwable));
                                    HttpResponseEvent responseEvent = new HttpResponseEvent(HttpResponseEvent.Type.ERROR, correlationId, httpInfo);
                                    sendResponseMessage("accountResponses-out-0", correlationId, responseEvent);
                                })
                                .subscribe();
                        break;
                    case UPDATE:
                        Account accountToUpdate = crudEvent.getData();
                        //accountController.updateAccount(accountToUpdate).block()
                        accountController.updateAccount(accountToUpdate)
                                .doOnSuccess(updatedAccount -> {
                                    String jsonString = serializeObjectToJson(updatedAccount);
                                    ResponsePayload httpInfo = new ResponsePayload(jsonString, HttpStatus.ACCEPTED);
                                    HttpResponseEvent responseEvent = new HttpResponseEvent(HttpResponseEvent.Type.SUCCESS, correlationId, httpInfo);
                                    sendResponseMessage("accountResponses-out-0", correlationId, responseEvent);
                                })
                                .doOnError(throwable -> {
                                    LOG.error("Failed to update account, exception message: {}", throwable.getMessage());
                                    ResponsePayload httpInfo = new ResponsePayload(throwable.getMessage(), resolveHttpStatus(throwable));
                                    HttpResponseEvent responseEvent = new HttpResponseEvent(HttpResponseEvent.Type.ERROR, correlationId, httpInfo);
                                    sendResponseMessage("accountResponses-out-0", correlationId, responseEvent);
                                })
                                .subscribe();
                        break;
                    case DELETE:
                        String accountId = crudEvent.getKey();
                        accountController.deleteAccount(UUID.fromString(accountId)).block();
                        break;
                    default:
                        String errorMessage = "Incorrect event type: " + event.getEventType() + ", expected a CREATE, UPDATE or DELETE event";
                        LOG.warn(errorMessage);
                        throw new EventProcessingException(errorMessage);
                }
            } else if (event instanceof AccountEvent<?, ?>) {
                LOG.info("Account event detected...");
                if (!(event.getKey() instanceof String) || !(event.getData() instanceof BigDecimal)) {
                    String errorMessage = "Incorrect Account event parameters, expected <String, BigDecimal>";
                    LOG.warn(errorMessage);
                    throw new EventProcessingException(errorMessage);
                }
                @SuppressWarnings(value = "unchecked") // We know that the key is a String and the data is a BigDecimal
                AccountEvent<String, BigDecimal> accountEvent = (AccountEvent<String, BigDecimal>) event;
                switch (accountEvent.getEventType()) {
                    case DEPOSIT:
                        UUID depositAccountId = UUID.fromString(accountEvent.getKey());
                        BigDecimal depositAmount = accountEvent.getData();
                        accountController.deposit(depositAccountId, depositAmount).block();
                        break;
                    case WITHDRAW:
                        UUID withdrawAccountId = UUID.fromString(accountEvent.getKey());
                        BigDecimal withdrawAmount = accountEvent.getData();
                        accountController.withdraw(withdrawAccountId, withdrawAmount).block();
                        break;
                    default:
                        String errorMessage = "Incorrect event type: " + event.getEventType() + ", expected a DEPOSIT or WITHDRAW event";
                        LOG.warn(errorMessage);
                        throw new EventProcessingException(errorMessage);
                }
            } else {
                String errorMessage = "Incorrect event type: " + event.getClass().getSimpleName() + ", expected a CrudEvent or AccountEvent";
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
