package hu.therealuhlarzoltan.expensables.microservices.cashflow.components.sagas;

import com.fasterxml.jackson.databind.ObjectMapper;
import hu.therealuhlarzoltan.expensables.api.microservices.core.expense.ExpenseRecord;
import hu.therealuhlarzoltan.expensables.api.microservices.events.AccountEvent;
import hu.therealuhlarzoltan.expensables.api.microservices.events.CrudEvent;
import hu.therealuhlarzoltan.expensables.api.microservices.events.Event;
import hu.therealuhlarzoltan.expensables.api.microservices.events.ResponsePayload;
import hu.therealuhlarzoltan.expensables.api.microservices.exceptions.EventProcessingException;
import hu.therealuhlarzoltan.expensables.api.microservices.exceptions.NotFoundException;
import hu.therealuhlarzoltan.expensables.api.microservices.exceptions.ServiceResponseException;
import hu.therealuhlarzoltan.expensables.microservices.cashflow.components.gateways.ExpenseGateway;
import hu.therealuhlarzoltan.expensables.microservices.cashflow.services.ResponseListenerService;
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
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static hu.therealuhlarzoltan.expensables.api.microservices.events.HttpResponseEvent.Type.ERROR;
import static hu.therealuhlarzoltan.expensables.api.microservices.events.HttpResponseEvent.Type.SUCCESS;

@Component
public class ExpenseSagaImpl implements ExpenseSaga {
    private static final Logger LOG = LoggerFactory.getLogger(ExpenseSagaImpl.class);
    private final ResponseListenerService responseListener;
    private final Scheduler publishEventScheduler;
    private final StreamBridge streamBridge;
    private final ObjectMapper mapper;
    private final int RESPONSE_EVENT_WAIT_DURATION;
    private final ExpenseGateway expenseGateway;

    private enum ExpenseCreationState {
        INIT,
        EXPENSE_CREATED,
        ACCOUNT_UPDATED
    }

    private enum ExpenseUpdateState {
        INIT,
        EXPENSE_UPDATED,
        ACCOUNT_UPDATED
    }

    private enum ExpenseDeletionState {
        INIT,
        EXPENSE_DELETED,
        ACCOUNT_UPDATED
    }

    @Autowired
    public ExpenseSagaImpl(
            @Qualifier("publishEventScheduler") Scheduler publishEventScheduler,
            WebClient webClient,
            ObjectMapper mapper,
            StreamBridge streamBridge,
            ResponseListenerService responseListener,
            ExpenseGateway expenseGateway,
            @Value("${app.response-event-wait-duration:10}") int responseEventWaitDuration) {
        this.publishEventScheduler = publishEventScheduler;
        this.mapper = mapper;
        this.streamBridge = streamBridge;
        this.responseListener = responseListener;
        this.expenseGateway = expenseGateway;
        this.RESPONSE_EVENT_WAIT_DURATION = responseEventWaitDuration;
    }

    @Override
    public Mono<ExpenseRecord> createExpense(ExpenseRecord expenseRecord) {
        AtomicReference<ExpenseCreationState> state = new AtomicReference<>(ExpenseCreationState.INIT);
        String creatExpenseCorrId = UUID.randomUUID().toString();
        String updateAccountCorrId = UUID.randomUUID().toString();
        AtomicReference<String> responseMessage = new AtomicReference<>();
        AtomicReference<HttpStatus> responseStatus = new AtomicReference<>(HttpStatus.CREATED);
        return Mono.fromRunnable(() -> {
                    LOG.info("Starting the expense creation saga for expense: {}", expenseRecord);
                    sendMessage("expenses-out-0", creatExpenseCorrId, new CrudEvent<String, ExpenseRecord>(CrudEvent.Type.CREATE, expenseRecord.getRecordId(), expenseRecord));
                }).then(responseListener.waitForResponse(creatExpenseCorrId, Duration.ofSeconds(RESPONSE_EVENT_WAIT_DURATION)))
                .flatMap(response -> {
                    if (response.getEventType() == SUCCESS) {
                        LOG.info("Expense created successfully with id: {}", expenseRecord.getRecordId());
                        state.set(ExpenseCreationState.EXPENSE_CREATED);
                        responseMessage.set(response.getData().getMessage());
                        return Mono.empty();
                    }
                    else if (response.getEventType() == ERROR) {
                        LOG.warn("Expense creation failed with error: {}", response.getData().getMessage());
                        responseMessage.set(response.getData().getMessage());
                        responseStatus.set(response.getData().getStatus());
                        return Mono.error(createMessageResponseError(response.getData()));
                    } else {
                        LOG.error("Unknown response event received during expense creation");
                        return Mono.error(new EventProcessingException("Could not process unknown response"));
                    }
                })
                .doOnError((ex) -> ex instanceof ServiceResponseException && ((ServiceResponseException) ex).getResponseStatus().equals(HttpStatus.FAILED_DEPENDENCY), (ex) -> {
                    state.set(ExpenseCreationState.EXPENSE_CREATED);
                })
                .then(Mono.fromRunnable(() -> {
                    if (state.get() == ExpenseCreationState.EXPENSE_CREATED) {
                        sendMessage("accounts-out-0", updateAccountCorrId, new AccountEvent<>(AccountEvent.Type.WITHDRAW, expenseRecord.getAccountId(), expenseRecord.getAmount()));
                    }
                })).then(responseListener.waitForResponse(updateAccountCorrId, Duration.ofSeconds(RESPONSE_EVENT_WAIT_DURATION)))
                .flatMap(response -> {
                    if (response.getEventType() == SUCCESS) {
                        LOG.info("Account updated successfully with id: {} because of new expense entity with id: {}", expenseRecord.getAccountId(), expenseRecord.getRecordId());
                        state.set(ExpenseCreationState.ACCOUNT_UPDATED);
                        return Mono.empty();
                    }
                    else if (response.getEventType() == ERROR) {
                        LOG.warn("Failed to update account with id {} for new expense entity with id: {}, error: {}", expenseRecord.getAccountId(), expenseRecord.getRecordId(), response.getData().getMessage());
                        responseMessage.set(response.getData().getMessage());
                        responseStatus.set(response.getData().getStatus());
                        return Mono.error(createMessageResponseError(response.getData()));
                    } else {
                        LOG.error("Unknown response event received during expense creation");
                        return Mono.error(new EventProcessingException("Could not process unknown response event"));
                    }
                }).doOnError((ex) -> ex instanceof ServiceResponseException, (ex) -> {
                    LOG.warn("Couldn't complete expense creation saga due to error: {}", ex.getMessage());
                    LOG.warn("Rolling back the expense creation saga for expense with id: {}", expenseRecord.getRecordId());
                    if (((ServiceResponseException) ex).getResponseStatus().equals(HttpStatus.FAILED_DEPENDENCY)) {
                        state.set(ExpenseCreationState.ACCOUNT_UPDATED);
                    }
                    restoreExpenseCreation(state.get(), expenseRecord);
                }).subscribeOn(publishEventScheduler).then(Mono.defer(() -> {
                    if (responseStatus.get() == HttpStatus.CREATED) {
                        String jsonString = responseMessage.get();
                        ExpenseRecord createdExpense = deserializeObjectFromJson(jsonString, ExpenseRecord.class);
                        return Mono.just(createdExpense);
                    } else {
                        return Mono.error(mapException(responseMessage.get(), responseStatus.get()));
                    }
                }));
    }

    @Override
    public Mono<ExpenseRecord> createExpense(ExpenseRecord expenseRecord, BigDecimal amount) {
        AtomicReference<ExpenseCreationState> state = new AtomicReference<>(ExpenseCreationState.INIT);
        String creatExpenseCorrId = UUID.randomUUID().toString();
        String updateAccountCorrId = UUID.randomUUID().toString();
        AtomicReference<String> responseMessage = new AtomicReference<>();
        AtomicReference<HttpStatus> responseStatus = new AtomicReference<>(HttpStatus.CREATED);
        return Mono.fromRunnable(() -> {
                    LOG.info("Starting the expense creation saga for expense: {}", expenseRecord);
                    sendMessage("expenses-out-0", creatExpenseCorrId, new CrudEvent<String, ExpenseRecord>(CrudEvent.Type.CREATE, expenseRecord.getRecordId(), expenseRecord));
                }).then(responseListener.waitForResponse(creatExpenseCorrId, Duration.ofSeconds(RESPONSE_EVENT_WAIT_DURATION)))
                .flatMap(response -> {
                    if (response.getEventType() == SUCCESS) {
                        LOG.info("Expense created successfully with id: {}", expenseRecord.getRecordId());
                        state.set(ExpenseCreationState.EXPENSE_CREATED);
                        responseMessage.set(response.getData().getMessage());
                        return Mono.empty();
                    }
                    else if (response.getEventType() == ERROR) {
                        LOG.warn("Expense creation failed with error: {}", response.getData().getMessage());
                        responseMessage.set(response.getData().getMessage());
                        responseStatus.set(response.getData().getStatus());
                        return Mono.error(createMessageResponseError(response.getData()));
                    } else {
                        LOG.error("Unknown response event received during expense creation");
                        return Mono.error(new EventProcessingException("Could not process unknown response"));
                    }
                })
                .doOnError((ex) -> ex instanceof ServiceResponseException && ((ServiceResponseException) ex).getResponseStatus().equals(HttpStatus.FAILED_DEPENDENCY), (ex) -> {
                    state.set(ExpenseCreationState.EXPENSE_CREATED);
                })
                .then(Mono.fromRunnable(() -> {
                    if (state.get() == ExpenseCreationState.EXPENSE_CREATED) {
                        sendMessage("accounts-out-0", updateAccountCorrId, new AccountEvent<>(AccountEvent.Type.WITHDRAW, expenseRecord.getAccountId(), amount));
                    }
                })).then(responseListener.waitForResponse(updateAccountCorrId, Duration.ofSeconds(RESPONSE_EVENT_WAIT_DURATION)))
                .flatMap(response -> {
                    if (response.getEventType() == SUCCESS) {
                        LOG.info("Account updated successfully with id: {} because of new expense entity with id: {}", expenseRecord.getAccountId(), expenseRecord.getRecordId());
                        state.set(ExpenseCreationState.ACCOUNT_UPDATED);
                        return Mono.empty();
                    }
                    else if (response.getEventType() == ERROR) {
                        LOG.warn("Failed to update account with id {} for new expense entity with id: {}, error: {}", expenseRecord.getAccountId(), expenseRecord.getRecordId(), response.getData().getMessage());
                        responseMessage.set(response.getData().getMessage());
                        responseStatus.set(response.getData().getStatus());
                        return Mono.error(createMessageResponseError(response.getData()));
                    } else {
                        LOG.error("Unknown response event received during expense creation");
                        return Mono.error(new EventProcessingException("Could not process unknown response event"));
                    }
                }).doOnError((ex) -> ex instanceof ServiceResponseException, (ex) -> {
                    LOG.warn("Couldn't complete expense creation saga due to error: {}", ex.getMessage());
                    LOG.warn("Rolling back the expense creation saga for expense with id: {}", expenseRecord.getRecordId());
                    if (((ServiceResponseException) ex).getResponseStatus().equals(HttpStatus.FAILED_DEPENDENCY)) {
                        state.set(ExpenseCreationState.ACCOUNT_UPDATED);
                    }
                    restoreExpenseCreation(state.get(), expenseRecord, amount);
                }).subscribeOn(publishEventScheduler).then(Mono.defer(() -> {
                    if (responseStatus.get() == HttpStatus.CREATED) {
                        String jsonString = responseMessage.get();
                        ExpenseRecord createdExpense = deserializeObjectFromJson(jsonString, ExpenseRecord.class);
                        return Mono.just(createdExpense);
                    } else {
                        return Mono.error(mapException(responseMessage.get(), responseStatus.get()));
                    }
                }));
    }

    private Throwable mapException(String message, HttpStatus status) {
        return new ServiceResponseException(message, status);
    }

    @Override
    public Mono<ExpenseRecord> updateExpense(ExpenseRecord expenseRecord, BigDecimal amount) {
        String accountUpdateCorrId = UUID.randomUUID().toString();
        String expenseUpdateCorrId = UUID.randomUUID().toString();
        AtomicReference<ExpenseUpdateState> state = new AtomicReference<>(ExpenseUpdateState.INIT);
        return Mono.fromRunnable(() -> {
            LOG.info("Starting expense update saga with expense id: {}", expenseRecord.getRecordId());
        }).then(
                amount.compareTo(BigDecimal.ZERO) != 0
                        ? handleAccountUpdate(expenseRecord, amount, accountUpdateCorrId, state)
                        : Mono.empty()
        ).then(
                Mono.fromRunnable(() -> {
                    LOG.info("Updating expense record with id: {}", expenseRecord.getRecordId());
                    sendMessage("expenses-out-0", expenseUpdateCorrId, new CrudEvent<>(CrudEvent.Type.UPDATE, expenseRecord.getRecordId(), expenseRecord));
                })
        ).then(
                responseListener.waitForResponse(expenseUpdateCorrId, Duration.ofSeconds(RESPONSE_EVENT_WAIT_DURATION))
                        .flatMap(response -> {
                            if (response.getEventType() == SUCCESS) {
                                state.set(ExpenseUpdateState.EXPENSE_UPDATED);
                                String jsonString = response.getData().getMessage();
                                ExpenseRecord updatedExpenseRecord = deserializeObjectFromJson(jsonString, ExpenseRecord.class);
                                return Mono.just(updatedExpenseRecord);
                            } else if (response.getEventType() == ERROR) {
                                LOG.error("Couldn't update expense record from expense saga for expense id: {}", expenseRecord.getRecordId());
                                return Mono.error(createMessageResponseError(response.getData()));
                            } else {
                                LOG.error("Unknown response event received inside expense update saga for expense id: {}", expenseRecord.getRecordId());
                                return Mono.error(new EventProcessingException("Could not process unknown response event"));
                            }
                        })
                        .doOnError((ex) -> ex instanceof ServiceResponseException && ((ServiceResponseException) ex).getResponseStatus().equals(HttpStatus.FAILED_DEPENDENCY), (ex) -> {
                            state.set(ExpenseUpdateState.EXPENSE_UPDATED);
                        })
        ).onErrorResume(ex -> {
            LOG.info("Encountered an error during expense update with id: {}, exception: {}", expenseRecord.getRecordId(), ex.getMessage());
            restoreExpenseUpdate(state.get(), expenseRecord, amount);
            return Mono.error(ex);
        }).subscribeOn(publishEventScheduler);
    }

    private Mono<Void> handleAccountUpdate(ExpenseRecord expenseRecord, BigDecimal amount, String accountUpdateCorrId, AtomicReference<ExpenseUpdateState> state) {
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            LOG.info("Deducting money from account with id: {}, due to updated expense record with id {}", expenseRecord.getAccountId(), expenseRecord.getRecordId());
            sendMessage("accounts-out-0", accountUpdateCorrId, new AccountEvent<>(AccountEvent.Type.DEPOSIT, expenseRecord.getAccountId(), amount.abs()));
        } else {
            LOG.info("Depositing money to account with id: {}, due to updated expense record with id {}", expenseRecord.getAccountId(), expenseRecord.getRecordId());
            sendMessage("accounts-out-0", accountUpdateCorrId, new AccountEvent<>(AccountEvent.Type.WITHDRAW, expenseRecord.getAccountId(), amount.abs()));
        }

        return responseListener.waitForResponse(accountUpdateCorrId, Duration.ofSeconds(RESPONSE_EVENT_WAIT_DURATION))
                .flatMap(response -> {
                    if (response.getEventType() == SUCCESS) {
                        state.set(ExpenseUpdateState.ACCOUNT_UPDATED);
                        return Mono.<Void>empty();
                    } else if (response.getEventType() == ERROR) {
                        LOG.error("Couldn't update corresponding account balance from expense saga for expense id: {}", expenseRecord.getRecordId());
                        return Mono.error(createMessageResponseError(response.getData()));
                    } else {
                        LOG.error("Unknown response event received inside expense update saga for expense id: {}", expenseRecord.getRecordId());
                        return Mono.error(new EventProcessingException("Could not process unknown response event"));
                    }
                })
                .doOnError((ex) -> ex instanceof ServiceResponseException && ((ServiceResponseException) ex).getResponseStatus().equals(HttpStatus.FAILED_DEPENDENCY), (ex) ->  {
                    state.set(ExpenseUpdateState.ACCOUNT_UPDATED);
                })
                .subscribeOn(publishEventScheduler);
    }


    @Override
    public Mono<Void> deleteExpense(ExpenseRecord expenseRecord) {
        AtomicReference<ExpenseDeletionState> state = new AtomicReference<>(ExpenseDeletionState.INIT);
        String corrId = UUID.randomUUID().toString();
        return expenseGateway.getExpense(expenseRecord.getRecordId())
                .onErrorMap(e -> {
                    LOG.warn("Expense record not found: {}", expenseRecord.getRecordId());
                    return new NotFoundException("Expense record to delete not found");
                })
                .then(Mono.fromRunnable(() -> {
                    LOG.info("Starting the expense deletion saga for expense: {}", expenseRecord);
                    sendMessage("expenses-out-0", new CrudEvent<>(CrudEvent.Type.DELETE, expenseRecord.getRecordId(), expenseRecord));
                }))
                .then(Mono.delay(Duration.ofSeconds(1)))
                .then(expenseGateway.getExpense(expenseRecord.getRecordId())
                        .onErrorResume(NotFoundException.class, e -> {
                            state.set(ExpenseDeletionState.EXPENSE_DELETED);
                            return Mono.empty();
                        })
                )
                .then(Mono.fromRunnable(() -> {
                    if (state.get() == ExpenseDeletionState.EXPENSE_DELETED) {
                        sendMessage("accounts-out-0", corrId, new AccountEvent<>(AccountEvent.Type.DEPOSIT, expenseRecord.getAccountId(), expenseRecord.getAmount()));
                    }
                }))
                .then(responseListener.waitForResponse(corrId, Duration.ofSeconds(RESPONSE_EVENT_WAIT_DURATION)))
                .flatMap(response -> {
                    if (response.getEventType() == SUCCESS) {
                        LOG.info("Account updated successfully with id: {} because of deleted expense entity with id: {}", expenseRecord.getAccountId(), expenseRecord.getRecordId());
                        state.set(ExpenseDeletionState.ACCOUNT_UPDATED);
                        return Mono.empty();
                    } else if (response.getEventType() == ERROR) {
                        LOG.warn("Failed to update account with id {} for deleted expense entity with id: {}, error: {}", expenseRecord.getAccountId(), expenseRecord.getRecordId(), response.getData().getMessage());
                        return Mono.error(createMessageResponseError(response.getData()));
                    } else {
                        LOG.error("Unknown response event received during expense deletion");
                        return Mono.error(new EventProcessingException("Could not process unknown response event"));
                    }
                })
                .doOnError((ex) -> ex instanceof ServiceResponseException && ((ServiceResponseException) ex).getResponseStatus().equals(HttpStatus.FAILED_DEPENDENCY), (ex) -> {
                    state.set(ExpenseDeletionState.ACCOUNT_UPDATED);
                })
                .onErrorResume(ex -> {
                    LOG.warn("Encountered an error during expense deletion with id: {}, exception: {}", expenseRecord.getRecordId(), ex.getMessage());
                    if (!(ex instanceof NotFoundException))
                        restoreExpenseDeletion(state.get(), expenseRecord, expenseRecord.getAmount());
                    return Mono.empty();
                }).then().subscribeOn(publishEventScheduler);
    }

    @Override
    public Mono<Void> deleteExpense(ExpenseRecord expenseRecord, BigDecimal amount) {
        AtomicReference<ExpenseDeletionState> state = new AtomicReference<>(ExpenseDeletionState.INIT);
        String corrId = UUID.randomUUID().toString();
        return expenseGateway.getExpense(expenseRecord.getRecordId())
                .onErrorMap(e -> {
                    LOG.warn("Expense record not found: {}", expenseRecord.getRecordId());
                    return new NotFoundException("Expense record to delete not found");
                })
                .then(Mono.fromRunnable(() -> {
                    LOG.info("Starting the expense deletion saga for expense: {}", expenseRecord);
                    sendMessage("expenses-out-0", new CrudEvent<>(CrudEvent.Type.DELETE, expenseRecord.getRecordId(), expenseRecord));
                }))
                .then(Mono.delay(Duration.ofSeconds(1)))
                .then(expenseGateway.getExpense(expenseRecord.getRecordId())
                        .onErrorResume(NotFoundException.class, e -> {
                            state.set(ExpenseDeletionState.EXPENSE_DELETED);
                            return Mono.empty();
                        })
                )
                .then(Mono.fromRunnable(() -> {
                    if (state.get() == ExpenseDeletionState.EXPENSE_DELETED) {
                        sendMessage("accounts-out-0", corrId, new AccountEvent<>(AccountEvent.Type.DEPOSIT, expenseRecord.getAccountId(), amount));
                    }
                }))
                .then(responseListener.waitForResponse(corrId, Duration.ofSeconds(RESPONSE_EVENT_WAIT_DURATION)))
                .flatMap(response -> {
                    if (response.getEventType() == SUCCESS) {
                        LOG.info("Account updated successfully with id: {} because of deleted expense entity with id: {}", expenseRecord.getAccountId(), expenseRecord.getRecordId());
                        state.set(ExpenseDeletionState.ACCOUNT_UPDATED);
                        return Mono.empty();
                    } else if (response.getEventType() == ERROR) {
                        LOG.warn("Failed to update account with id {} for deleted expense entity with id: {}, error: {}", expenseRecord.getAccountId(), expenseRecord.getRecordId(), response.getData().getMessage());
                        return Mono.error(createMessageResponseError(response.getData()));
                    } else {
                        LOG.error("Unknown response event received during expenses deletion");
                        return Mono.error(new EventProcessingException("Could not process unknown response event"));
                    }
                })
                .doOnError((ex) -> ex instanceof ServiceResponseException && ((ServiceResponseException) ex).getResponseStatus().equals(HttpStatus.FAILED_DEPENDENCY), (ex) -> {
                    state.set(ExpenseDeletionState.ACCOUNT_UPDATED);
                })
                .onErrorResume(ex -> {
                    LOG.warn("Encountered an error during expense deletion with id: {}, exception: {}", expenseRecord.getRecordId(), ex.getMessage());
                    if (!(ex instanceof NotFoundException))
                        restoreExpenseDeletion(state.get(), expenseRecord, amount);
                    return Mono.empty();
                }).then().subscribeOn(publishEventScheduler);
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

    private void restoreExpenseCreation(ExpenseCreationState state, ExpenseRecord record) {
        switch (state) {
            case INIT:
                LOG.info("No rollback needed after failed expense creation with expense id: {}", record.getRecordId());
                break;
            case ACCOUNT_UPDATED:
                LOG.info("Rolling back account update for expense creation with expense id: {}", record.getRecordId());
                sendMessage("accounts-out-0", new AccountEvent<>(AccountEvent.Type.DEPOSIT, record.getAccountId(), record.getAmount()));
                LOG.info("Rolling back expense creation with expense id: {}", record.getRecordId());
                sendMessage("expenses-out-0", new CrudEvent<String, ExpenseRecord>(CrudEvent.Type.DELETE, record.getRecordId(), record));
                break;
            case EXPENSE_CREATED:
                LOG.info("Rolling back expense creation with expense id: {}", record.getRecordId());
                sendMessage("expenses-out-0", new CrudEvent<String, ExpenseRecord>(CrudEvent.Type.DELETE, record.getRecordId(), record));
                break;
            default:
                LOG.warn("Couldn't determine restore action for expense creation state: {} with expense id: {}", state, record.getRecordId());
                return;
        }

    }

    private void restoreExpenseCreation(ExpenseCreationState state, ExpenseRecord record, BigDecimal amount) {
        switch (state) {
            case INIT:
                LOG.info("No rollback needed after failed expense creation with expense id: {}", record.getRecordId());
                break;
            case ACCOUNT_UPDATED:
                LOG.info("Rolling back account update for expense creation with expense id: {}", record.getRecordId());
                sendMessage("accounts-out-0", new AccountEvent<>(AccountEvent.Type.DEPOSIT, record.getAccountId(), amount));
                LOG.info("Rolling back expense creation with expense id: {}", record.getRecordId());
                sendMessage("expenses-out-0", new CrudEvent<String, ExpenseRecord>(CrudEvent.Type.DELETE, record.getRecordId(), record));
                break;
            case EXPENSE_CREATED:
                LOG.info("Rolling back expense creation with expense id: {}", record.getRecordId());
                sendMessage("expenses-out-0", new CrudEvent<String, ExpenseRecord>(CrudEvent.Type.DELETE, record.getRecordId(), record));
                break;
            default:
                LOG.warn("Couldn't determine restore action for expense creation state: {} with expense id: {}", state, record.getRecordId());
                return;
        }

    }

    private void restoreExpenseDeletion(ExpenseDeletionState state, ExpenseRecord record, BigDecimal amount) {
        switch (state) {
            case INIT:
                LOG.info("No rollback needed after failed expense deletion with expense id: {}", record.getRecordId());
                break;
            case ACCOUNT_UPDATED:
                LOG.info("Rolling back account update for expense deletion with expense id: {}", record.getRecordId());
                sendMessage("accounts-out-0", new AccountEvent<>(AccountEvent.Type.WITHDRAW, record.getAccountId(), amount));
                LOG.info("Rolling back expense deletion with expense id: {}", record.getRecordId());
                sendMessage("expenses-out-0", new CrudEvent<String, ExpenseRecord>(CrudEvent.Type.CREATE, record.getRecordId(), record));
                break;
            case EXPENSE_DELETED:
                LOG.info("Rolling back expense deletion with expense id: {}", record.getRecordId());
                sendMessage("expenses-out-0", new CrudEvent<String, ExpenseRecord>(CrudEvent.Type.CREATE, record.getRecordId(), record));
                break;
            default:
                LOG.warn("Couldn't determine restore action for expense deletion state: {} with expense id: {}", state, record.getRecordId());
                return;
        }

    }

    private void restoreExpenseUpdate(ExpenseUpdateState state, ExpenseRecord record, BigDecimal amount) {
        switch (state) {
            case INIT:
                LOG.info("No rollback needed after failed expense update with expense id: {}", record.getRecordId());
                break;
            case ACCOUNT_UPDATED, EXPENSE_UPDATED:
                LOG.info("Rolling back account update for expense update with expense id: {}", record.getRecordId());
                if (amount.compareTo(BigDecimal.ZERO) < 0) {
                    sendMessage("accounts-out-0", new AccountEvent<>(AccountEvent.Type.WITHDRAW, record.getAccountId(), amount.abs()));
                } else {
                    sendMessage("accounts-out-0", new AccountEvent<>(AccountEvent.Type.DEPOSIT, record.getAccountId(), amount.abs()));
                }
                break;
            default:
                LOG.warn("Couldn't determine restore action for expense update state: {} with expense id: {}", state, record.getRecordId());
                return;
        }
    }
}
