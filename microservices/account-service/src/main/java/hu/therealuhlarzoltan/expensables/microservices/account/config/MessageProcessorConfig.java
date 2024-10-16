package hu.therealuhlarzoltan.expensables.microservices.account.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.DuplicateKeyException;
import hu.therealuhlarzoltan.expensables.api.microservices.core.account.Account;
import hu.therealuhlarzoltan.expensables.api.microservices.core.account.AccountController;
import hu.therealuhlarzoltan.expensables.api.microservices.events.*;
import hu.therealuhlarzoltan.expensables.api.microservices.exceptions.*;
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
import reactor.core.publisher.Mono;
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
                    ResponsePayload httpInfo = new ResponsePayload(errorMessage, HttpStatus.BAD_REQUEST);
                    HttpResponseEvent responseEvent = new HttpResponseEvent(HttpResponseEvent.Type.ERROR, correlationId, httpInfo);
                    sendResponseMessage("accountResponses-out-0", correlationId, responseEvent);
                    return;
                }
                Account crudEventData;
                try {
                    crudEventData = objectMapper.convertValue(event.getData(), Account.class);
                } catch (IllegalArgumentException e) {
                    String errorMessage = "Incorrect CRUD event parameters, expected <String, Account>";
                    LOG.warn(errorMessage);
                    ResponsePayload httpInfo = new ResponsePayload(errorMessage, HttpStatus.BAD_REQUEST);
                    HttpResponseEvent responseEvent = new HttpResponseEvent(HttpResponseEvent.Type.ERROR, correlationId, httpInfo);
                    sendResponseMessage("accountResponses-out-0", correlationId, responseEvent);
                    return;
                }
                @SuppressWarnings(value = "unchecked") // We know that the Event is a CrudEvent and the key is a String
                CrudEvent<String, ?> eventWithKey = (CrudEvent<String, ?>) event;
                CrudEvent<String, Account> crudEvent = new CrudEvent<String, Account>(eventWithKey.getEventType(), eventWithKey.getKey(), crudEventData);
                switch (crudEvent.getEventType()) {
                    case CREATE:
                        Account account = crudEvent.getData();
                        // Design flaw - Some exception inside the controller are being thrown directly instead of wrapping them in a Mono.error or Flux.error
                        try {
                            accountController.createAccount(account)
                                    .doOnSuccess(createdAccount -> {
                                        String jsonString = serializeObjectToJson(createdAccount);
                                        ResponsePayload httpInfo = new ResponsePayload(jsonString, HttpStatus.CREATED);
                                        HttpResponseEvent responseEvent = new HttpResponseEvent(HttpResponseEvent.Type.SUCCESS, correlationId, httpInfo);
                                        sendResponseMessage("accountResponses-out-0", correlationId, responseEvent);
                                    })
                                    .onErrorResume(throwable -> {
                                        LOG.error("Failed to create account, exception message: {}", throwable.getMessage());
                                        ResponsePayload httpInfo = new ResponsePayload(throwable.getMessage(), resolveHttpStatus(throwable));
                                        HttpResponseEvent responseEvent = new HttpResponseEvent(HttpResponseEvent.Type.ERROR, correlationId, httpInfo);
                                        sendResponseMessage("accountResponses-out-0", correlationId, responseEvent);
                                        return Mono.empty();
                                    })
                                    .subscribe();
                        } catch (Exception ex) {
                            LOG.error("Failed to create account, exception message: {}", ex.getMessage());
                            ResponsePayload httpInfo = new ResponsePayload(ex.getMessage(), resolveHttpStatus(ex));
                            HttpResponseEvent responseEvent = new HttpResponseEvent(HttpResponseEvent.Type.ERROR, correlationId, httpInfo);
                            sendResponseMessage("accountResponses-out-0", correlationId, responseEvent);
                        }
                        break;
                    case UPDATE:
                        Account accountToUpdate = crudEvent.getData();
                        try {
                            accountController.updateAccount(accountToUpdate)
                                    .doOnSuccess(updatedAccount -> {
                                        String jsonString = serializeObjectToJson(updatedAccount);
                                        ResponsePayload httpInfo = new ResponsePayload(jsonString, HttpStatus.ACCEPTED);
                                        HttpResponseEvent responseEvent = new HttpResponseEvent(HttpResponseEvent.Type.SUCCESS, correlationId, httpInfo);
                                        sendResponseMessage("accountResponses-out-0", correlationId, responseEvent);
                                    })
                                    .onErrorResume((throwable) -> {
                                        LOG.error("Failed to update account, exception message: {}", throwable.getMessage());
                                        ResponsePayload httpInfo = new ResponsePayload(throwable.getMessage(), resolveHttpStatus(throwable));
                                        HttpResponseEvent responseEvent = new HttpResponseEvent(HttpResponseEvent.Type.ERROR, correlationId, httpInfo);
                                        sendResponseMessage("accountResponses-out-0", correlationId, responseEvent);
                                        return Mono.empty();
                                    })
                                    .subscribe();
                        } catch (Exception ex) {
                            LOG.error("Failed to update account, exception message: {}", ex.getMessage());
                            ResponsePayload httpInfo = new ResponsePayload(ex.getMessage(), resolveHttpStatus(ex));
                            HttpResponseEvent responseEvent = new HttpResponseEvent(HttpResponseEvent.Type.ERROR, correlationId, httpInfo);
                            sendResponseMessage("accountResponses-out-0", correlationId, responseEvent);
                        }
                        break;
                    case DELETE:
                        String accountId = crudEvent.getKey();
                        accountController.deleteAccount(UUID.fromString(accountId)).block();
                        break;
                    default:
                        String errorMessage = "Incorrect event type: " + event.getEventType() + ", expected a CREATE, UPDATE or DELETE event";
                        LOG.warn(errorMessage);
                        ResponsePayload httpInfo = new ResponsePayload(errorMessage, HttpStatus.BAD_REQUEST);
                        HttpResponseEvent responseEvent = new HttpResponseEvent(HttpResponseEvent.Type.ERROR, correlationId, httpInfo);
                        sendResponseMessage("accountResponses-out-0", correlationId, responseEvent);
                        return;
                }
            } else if (event instanceof AccountEvent<?, ?>) {
                LOG.info("Account event detected...");
                if (!(event.getKey() instanceof String) || (!(event.getData() instanceof BigDecimal) && !(event.getData() instanceof Double) && !(event.getData() instanceof Integer))) {
                    String errorMessage = "Incorrect Account event parameters, expected <String, BigDecimal/Double>/Integer";
                    LOG.warn(errorMessage);
                    ResponsePayload httpInfo = new ResponsePayload(errorMessage, HttpStatus.BAD_REQUEST);
                    HttpResponseEvent responseEvent = new HttpResponseEvent(HttpResponseEvent.Type.ERROR, correlationId, httpInfo);
                    sendResponseMessage("accountResponses-out-0", correlationId, responseEvent);
                    return;
                }
                AccountEvent<String, BigDecimal> accountEvent =  new AccountEvent<String, BigDecimal>((AccountEvent.Type) event.getEventType(), (String) event.getKey(), convertToBigdecimal(event.getData()));
                switch (accountEvent.getEventType()) {
                    case DEPOSIT:
                        UUID depositAccountId = UUID.fromString(accountEvent.getKey());
                        BigDecimal depositAmount = accountEvent.getData();
                        try {
                            accountController.deposit(depositAccountId, depositAmount)
                                    .doOnSuccess(updatedAccount -> {
                                        String jsonString = serializeObjectToJson(updatedAccount);
                                        ResponsePayload httpInfo = new ResponsePayload(jsonString, HttpStatus.OK);
                                        HttpResponseEvent responseEvent = new HttpResponseEvent(HttpResponseEvent.Type.SUCCESS, correlationId, httpInfo);
                                        sendResponseMessage("accountResponses-out-0", correlationId, responseEvent);
                                    })
                                    .onErrorResume((throwable) -> {
                                        LOG.error("Failed to deposit to account, exception message: {}", throwable.getMessage());
                                        ResponsePayload httpInfo = new ResponsePayload(throwable.getMessage(), resolveHttpStatus(throwable));
                                        HttpResponseEvent responseEvent = new HttpResponseEvent(HttpResponseEvent.Type.ERROR, correlationId, httpInfo);
                                        sendResponseMessage("accountResponses-out-0", correlationId, responseEvent);
                                        return Mono.empty();
                                    })
                                    .subscribe();
                        } catch (Exception ex) {
                            LOG.error("Failed to deposit account, exception message: {}", ex.getMessage());
                            ResponsePayload httpInfo = new ResponsePayload(ex.getMessage(), resolveHttpStatus(ex));
                            HttpResponseEvent responseEvent = new HttpResponseEvent(HttpResponseEvent.Type.ERROR, correlationId, httpInfo);
                            sendResponseMessage("accountResponses-out-0", correlationId, responseEvent);
                        }
                        break;
                    case WITHDRAW:
                        UUID withdrawAccountId = UUID.fromString(accountEvent.getKey());
                        BigDecimal withdrawAmount = accountEvent.getData();
                        try {
                            accountController.withdraw(withdrawAccountId, withdrawAmount)
                                    .doOnSuccess(updatedAccount -> {
                                        String jsonString = serializeObjectToJson(updatedAccount);
                                        ResponsePayload httpInfo = new ResponsePayload(jsonString, HttpStatus.OK);
                                        HttpResponseEvent responseEvent = new HttpResponseEvent(HttpResponseEvent.Type.SUCCESS, correlationId, httpInfo);
                                        sendResponseMessage("accountResponses-out-0", correlationId, responseEvent);
                                    })
                                    .onErrorResume((throwable) -> {
                                        LOG.error("Failed to withdraw from account, exception message: {}", throwable.getMessage());
                                        ResponsePayload httpInfo = new ResponsePayload(throwable.getMessage(), resolveHttpStatus(throwable));
                                        HttpResponseEvent responseEvent = new HttpResponseEvent(HttpResponseEvent.Type.ERROR, correlationId, httpInfo);
                                        sendResponseMessage("accountResponses-out-0", correlationId, responseEvent);
                                        return Mono.empty();
                                    })
                                    .subscribe();
                        } catch (Exception ex) {
                            LOG.error("Failed to withdraw from account, exception message: {}", ex.getMessage());
                            ResponsePayload httpInfo = new ResponsePayload(ex.getMessage(), resolveHttpStatus(ex));
                            HttpResponseEvent responseEvent = new HttpResponseEvent(HttpResponseEvent.Type.ERROR, correlationId, httpInfo);
                            sendResponseMessage("accountResponses-out-0", correlationId, responseEvent);
                        }
                        break;
                    default:
                        String errorMessage = "Incorrect event type: " + event.getEventType() + ", expected a DEPOSIT or WITHDRAW event";
                        LOG.warn(errorMessage);
                        ResponsePayload httpInfo = new ResponsePayload(errorMessage, HttpStatus.BAD_REQUEST);
                        HttpResponseEvent responseEvent = new HttpResponseEvent(HttpResponseEvent.Type.ERROR, correlationId, httpInfo);
                        sendResponseMessage("accountResponses-out-0", correlationId, responseEvent);
                        return;
                }
            } else {
                String errorMessage = "Incorrect event type: " + event.getClass().getSimpleName() + ", expected a CrudEvent or AccountEvent";
                LOG.warn(errorMessage);
                ResponsePayload httpInfo = new ResponsePayload(errorMessage, HttpStatus.BAD_REQUEST);
                HttpResponseEvent responseEvent = new HttpResponseEvent(HttpResponseEvent.Type.ERROR, correlationId, httpInfo);
                sendResponseMessage("accountResponses-out-0", correlationId, responseEvent);
                return;
            }
            LOG.info("Message processing done!");
        };
    }

    private BigDecimal convertToBigdecimal(Object data) {
        return switch (data) {
            case BigDecimal bigDecimal -> bigDecimal;
            case Double v -> BigDecimal.valueOf(v);
            case Integer i -> BigDecimal.valueOf(i);
            case null, default -> null;
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
