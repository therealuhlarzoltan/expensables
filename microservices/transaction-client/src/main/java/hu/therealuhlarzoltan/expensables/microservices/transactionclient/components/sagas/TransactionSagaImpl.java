package hu.therealuhlarzoltan.expensables.microservices.transactionclient.components.sagas;

import com.fasterxml.jackson.databind.ObjectMapper;
import hu.therealuhlarzoltan.expensables.api.microservices.core.transaction.TransactionRecord;
import hu.therealuhlarzoltan.expensables.api.microservices.events.AccountEvent;
import hu.therealuhlarzoltan.expensables.api.microservices.events.CrudEvent;
import hu.therealuhlarzoltan.expensables.api.microservices.events.Event;
import hu.therealuhlarzoltan.expensables.api.microservices.events.ResponsePayload;
import hu.therealuhlarzoltan.expensables.api.microservices.exceptions.EventProcessingException;
import hu.therealuhlarzoltan.expensables.api.microservices.exceptions.NotFoundException;
import hu.therealuhlarzoltan.expensables.api.microservices.exceptions.ServiceResponseException;
import hu.therealuhlarzoltan.expensables.microservices.transactionclient.components.gateways.TransactionGateway;
import hu.therealuhlarzoltan.expensables.microservices.transactionclient.services.ResponseListenerService;
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
public class TransactionSagaImpl implements TransactionSaga {
    private static final Logger LOG = LoggerFactory.getLogger(TransactionSagaImpl.class);
    private final ResponseListenerService responseListener;
    private final Scheduler publishEventScheduler;
    private final StreamBridge streamBridge;
    private final ObjectMapper mapper;
    private final TransactionGateway transactionGateway;
    private final int RESPONSE_EVENT_WAIT_DURATION;

    @Autowired
    public TransactionSagaImpl(
            ResponseListenerService responseListener,
            @Qualifier("publishEventScheduler") Scheduler publishEventScheduler,
            StreamBridge streamBridge,
            ObjectMapper mapper,
            TransactionGateway transactionGateway,
            @Value("${app.response-event-wait-duration:10}") int responseEventWaitDuration
    ) {
        this.responseListener = responseListener;
        this.publishEventScheduler = publishEventScheduler;
        this.streamBridge = streamBridge;
        this.mapper = mapper;
        this.transactionGateway = transactionGateway;
        this.RESPONSE_EVENT_WAIT_DURATION = responseEventWaitDuration;
    }

    private enum TransactionCreationState {
        INIT,
        FROM_ACCOUNT_WITHDRAWN,
        TO_ACCOUNT_DEPOSITED,
        TRANSACTION_CREATED,
    }

    private enum TransactionUpdateState {
        INIT,
        FROM_ACCOUNT_UPDATED,
        TO_ACCOUNT_UPDATED,
        TRANSACTION_UPDATED,
    }

    private enum TransactionDeletionState {
        INIT,
        FROM_ACCOUNT_DEPOSITED,
        TO_ACCOUNT_WITHDRAWN,
        TRANSACTION_DELETED,
    }

    @Override
    public Mono<TransactionRecord> createTransaction(TransactionRecord transactionRecord) {
        AtomicReference<TransactionCreationState> state = new AtomicReference<>(TransactionCreationState.INIT);
        String createTransactionCorrId = UUID.randomUUID().toString();
        String updateFromAccountCorrId = UUID.randomUUID().toString();
        String updateToAccountCorrId = UUID.randomUUID().toString();
        AtomicReference<String> responseMessage = new AtomicReference<>();
        AtomicReference<HttpStatus> responseStatus = new AtomicReference<>(HttpStatus.CREATED);

        return Mono.fromRunnable(() -> {
                    LOG.info("Starting the transaction creation saga for transaction: {}", transactionRecord);
                    sendMessage("transactions-out-0", createTransactionCorrId, new CrudEvent<String, TransactionRecord>(CrudEvent.Type.CREATE, transactionRecord.getRecordId(), transactionRecord));
                }).then(responseListener.waitForResponse(createTransactionCorrId, Duration.ofSeconds(RESPONSE_EVENT_WAIT_DURATION)))
                .flatMap(response -> {
                    if (response.getEventType() == SUCCESS) {
                        LOG.info("Transaction created successfully with id: {}", transactionRecord.getRecordId());
                        state.set(TransactionCreationState.TRANSACTION_CREATED);
                        responseMessage.set(response.getData().getMessage());
                        return Mono.empty();
                    } else if (response.getEventType() == ERROR) {
                        LOG.warn("Transaction creation failed with error: {}", response.getData().getMessage());
                        responseMessage.set(response.getData().getMessage());
                        responseStatus.set(response.getData().getStatus());
                        return Mono.error(createMessageResponseError(response.getData()));
                    } else {
                        LOG.error("Unknown response event received during transaction creation");
                        return Mono.error(new EventProcessingException("Could not process unknown response"));
                    }
                })
                .then(Mono.fromRunnable(() -> {
                    if (state.get() == TransactionCreationState.TRANSACTION_CREATED) {
                        sendMessage("accounts-out-0", updateFromAccountCorrId, new AccountEvent<>(AccountEvent.Type.WITHDRAW, transactionRecord.getFromAccountId(), transactionRecord.getAmount()));
                    }
                })).then(responseListener.waitForResponse(updateFromAccountCorrId, Duration.ofSeconds(RESPONSE_EVENT_WAIT_DURATION)))
                .flatMap(response -> {
                    if (response.getEventType() == SUCCESS) {
                        LOG.info("Account updated successfully with id: {} because of new transaction entity with id: {}", transactionRecord.getFromAccountId(), transactionRecord.getRecordId());
                        state.set(TransactionCreationState.FROM_ACCOUNT_WITHDRAWN);
                        return Mono.empty();
                    } else if (response.getEventType() == ERROR) {
                        LOG.warn("Failed to update account with id {} for new transaction entity with id: {}, error: {}", transactionRecord.getFromAccountId(), transactionRecord.getRecordId(), response.getData().getMessage());
                        responseMessage.set(response.getData().getMessage());
                        responseStatus.set(response.getData().getStatus());
                        return Mono.error(createMessageResponseError(response.getData()));
                    } else {
                        LOG.error("Unknown response event received during transaction creation");
                        return Mono.error(new EventProcessingException("Could not process unknown response event"));
                    }
                })
                .then(Mono.fromRunnable(() -> {
                    if (state.get() == TransactionCreationState.FROM_ACCOUNT_WITHDRAWN) {
                        sendMessage("accounts-out-0", updateToAccountCorrId, new AccountEvent<>(AccountEvent.Type.DEPOSIT, transactionRecord.getToAccountId(), transactionRecord.getAmount()));
                    }
                })).then(responseListener.waitForResponse(updateToAccountCorrId, Duration.ofSeconds(RESPONSE_EVENT_WAIT_DURATION)))
                .flatMap(response -> {
                    if (response.getEventType() == SUCCESS) {
                        LOG.info("Account updated successfully with id: {} because of new transaction entity with id: {}", transactionRecord.getFromAccountId(), transactionRecord.getRecordId());
                        state.set(TransactionCreationState.TO_ACCOUNT_DEPOSITED);
                        return Mono.empty();
                    } else if (response.getEventType() == ERROR) {
                        LOG.warn("Failed to update account with id {} for new transaction entity with id: {}, error: {}", transactionRecord.getFromAccountId(), transactionRecord.getRecordId(), response.getData().getMessage());
                        responseMessage.set(response.getData().getMessage());
                        responseStatus.set(response.getData().getStatus());
                        return Mono.error(createMessageResponseError(response.getData()));
                    } else {
                        LOG.error("Unknown response event received during transaction creation");
                        return Mono.error(new EventProcessingException("Could not process unknown response event"));
                    }
                })
                .onErrorResume(ex -> {
                    if (ex instanceof ServiceResponseException && ((ServiceResponseException) ex).getResponseStatus().equals(HttpStatus.FAILED_DEPENDENCY)) {
                        switch (state.get()) {
                            case INIT:
                                state.set(TransactionCreationState.TRANSACTION_CREATED);
                                break;
                            case TRANSACTION_CREATED:
                                state.set(TransactionCreationState.FROM_ACCOUNT_WITHDRAWN);
                                break;
                            case FROM_ACCOUNT_WITHDRAWN:
                                state.set(TransactionCreationState.TO_ACCOUNT_DEPOSITED);
                                break;
                            default:
                                break;
                        }
                    }
                    LOG.warn("Couldn't complete transaction creation saga due to error: {}", ex.getMessage());
                    LOG.warn("Rolling back the transaction creation saga for expense with id: {}", transactionRecord.getRecordId());
                    restoreTransactionCreation(state.get(), transactionRecord, transactionRecord.getAmount(), transactionRecord.getAmount());
                    return Mono.error(ex);
                })
                .subscribeOn(publishEventScheduler).then(Mono.defer(() -> {
                    if (responseStatus.get() == HttpStatus.CREATED) {
                        String jsonString = responseMessage.get();
                        TransactionRecord createdTransaction = deserializeObjectFromJson(jsonString, TransactionRecord.class);
                        return Mono.just(createdTransaction);
                    } else {
                        return Mono.error(mapException(responseMessage.get(), responseStatus.get()));
                    }
                }));
        }

    @Override
    public Mono<TransactionRecord> createTransaction(TransactionRecord transactionRecord, BigDecimal amount) {
        AtomicReference<TransactionCreationState> state = new AtomicReference<>(TransactionCreationState.INIT);
        String createTransactionCorrId = UUID.randomUUID().toString();
        String updateFromAccountCorrId = UUID.randomUUID().toString();
        String updateToAccountCorrId = UUID.randomUUID().toString();
        AtomicReference<String> responseMessage = new AtomicReference<>();
        AtomicReference<HttpStatus> responseStatus = new AtomicReference<>(HttpStatus.CREATED);
        return Mono.fromRunnable(() -> {
                    LOG.info("Starting the transaction creation saga for transaction: {}", transactionRecord);
                    sendMessage("transactions-out-0", createTransactionCorrId, new CrudEvent<String, TransactionRecord>(CrudEvent.Type.CREATE, transactionRecord.getRecordId(), transactionRecord));
                }).then(responseListener.waitForResponse(createTransactionCorrId, Duration.ofSeconds(RESPONSE_EVENT_WAIT_DURATION)))
                .flatMap(response -> {
                    if (response.getEventType() == SUCCESS) {
                        LOG.info("Transaction created successfully with id: {}", transactionRecord.getRecordId());
                        state.set(TransactionCreationState.TRANSACTION_CREATED);
                        responseMessage.set(response.getData().getMessage());
                        return Mono.empty();
                    }
                    else if (response.getEventType() == ERROR) {
                        LOG.warn("Transaction creation failed with error: {}", response.getData().getMessage());
                        responseMessage.set(response.getData().getMessage());
                        responseStatus.set(response.getData().getStatus());
                        return Mono.error(createMessageResponseError(response.getData()));
                    } else {
                        LOG.error("Unknown response event received during transaction creation");
                        return Mono.error(new EventProcessingException("Could not process unknown response"));
                    }
                })
                .then(Mono.fromRunnable(() -> {
                    if (state.get() == TransactionCreationState.TRANSACTION_CREATED) {
                        sendMessage("accounts-out-0", updateFromAccountCorrId, new AccountEvent<>(AccountEvent.Type.WITHDRAW, transactionRecord.getFromAccountId(), transactionRecord.getAmount()));
                    }
                })).then(responseListener.waitForResponse(updateFromAccountCorrId, Duration.ofSeconds(RESPONSE_EVENT_WAIT_DURATION)))
                .flatMap(response -> {
                    if (response.getEventType() == SUCCESS) {
                        LOG.info("Account updated successfully with id: {} because of new transaction entity with id: {}", transactionRecord.getFromAccountId(), transactionRecord.getRecordId());
                        state.set(TransactionCreationState.FROM_ACCOUNT_WITHDRAWN);
                        return Mono.empty();
                    }
                    else if (response.getEventType() == ERROR) {
                        LOG.warn("Failed to update account with id {} for new transaction entity with id: {}, error: {}", transactionRecord.getFromAccountId(), transactionRecord.getRecordId(), response.getData().getMessage());
                        responseMessage.set(response.getData().getMessage());
                        responseStatus.set(response.getData().getStatus());
                        return Mono.error(createMessageResponseError(response.getData()));
                    } else {
                        LOG.error("Unknown response event received during transaction creation");
                        return Mono.error(new EventProcessingException("Could not process unknown response event"));
                    }
                })
                .then(Mono.fromRunnable(() -> {
                    if (state.get() == TransactionCreationState.FROM_ACCOUNT_WITHDRAWN) {
                        sendMessage("accounts-out-0", updateToAccountCorrId, new AccountEvent<>(AccountEvent.Type.DEPOSIT, transactionRecord.getToAccountId(), amount));
                    }
                })).then(responseListener.waitForResponse(updateToAccountCorrId, Duration.ofSeconds(RESPONSE_EVENT_WAIT_DURATION)))
                .flatMap(response -> {
                    if (response.getEventType() == SUCCESS) {
                        LOG.info("Account updated successfully with id: {} because of new transaction entity with id: {}", transactionRecord.getFromAccountId(), transactionRecord.getRecordId());
                        state.set(TransactionCreationState.TO_ACCOUNT_DEPOSITED);
                        return Mono.empty();
                    }
                    else if (response.getEventType() == ERROR) {
                        LOG.warn("Failed to update account with id {} for new transaction entity with id: {}, error: {}", transactionRecord.getFromAccountId(), transactionRecord.getRecordId(), response.getData().getMessage());
                        responseMessage.set(response.getData().getMessage());
                        responseStatus.set(response.getData().getStatus());
                        return Mono.error(createMessageResponseError(response.getData()));
                    } else {
                        LOG.error("Unknown response event received during transaction creation");
                        return Mono.error(new EventProcessingException("Could not process unknown response event"));
                    }
                })
                .onErrorResume(ex -> {
                    if (ex instanceof ServiceResponseException && ((ServiceResponseException) ex).getResponseStatus().equals(HttpStatus.FAILED_DEPENDENCY)) {
                        switch (state.get()) {
                            case INIT:
                                state.set(TransactionCreationState.TRANSACTION_CREATED);
                                break;
                            case TRANSACTION_CREATED:
                                state.set(TransactionCreationState.FROM_ACCOUNT_WITHDRAWN);
                                break;
                            case FROM_ACCOUNT_WITHDRAWN:
                                state.set(TransactionCreationState.TO_ACCOUNT_DEPOSITED);
                                break;
                            default:
                                break;
                        }
                    }
                    LOG.warn("Couldn't complete transaction creation saga due to error: {}", ex.getMessage());
                    LOG.warn("Rolling back the transaction creation saga for expense with id: {}", transactionRecord.getRecordId());
                    restoreTransactionCreation(state.get(), transactionRecord, transactionRecord.getAmount(), amount);
                    return Mono.error(ex);
                })
               .subscribeOn(publishEventScheduler).then(Mono.defer(() -> {
                    if (responseStatus.get() == HttpStatus.CREATED) {
                        String jsonString = responseMessage.get();
                        TransactionRecord createdTransaction = deserializeObjectFromJson(jsonString, TransactionRecord.class);
                        return Mono.just(createdTransaction);
                    } else {
                        return Mono.error(mapException(responseMessage.get(), responseStatus.get()));
                    }
                }));
    }

    @Override
    public Mono<TransactionRecord> updateTransaction(TransactionRecord transactionRecord, BigDecimal amount) {
        AtomicReference<TransactionUpdateState> state = new AtomicReference<>(TransactionUpdateState.INIT);
        AtomicReference<String> responseMessage = new AtomicReference<>("");
        AtomicReference<HttpStatus> responseStatus = new AtomicReference<>(HttpStatus.OK);
        String fromAccountUpdateCorrId = UUID.randomUUID().toString();
        String toAccountUpdateCorrId = UUID.randomUUID().toString();
        String transactionUpdateCorrId = UUID.randomUUID().toString();
        LOG.info("Starting transaction update saga with transaction id: {}", transactionRecord.getRecordId());

        Mono<Void> updateFromAccount = Mono.defer(() -> {
            if (amount.compareTo(BigDecimal.ZERO) != 0) {
                return handleFromAccountUpdate(transactionRecord, amount, fromAccountUpdateCorrId, state);
            }
            return Mono.empty();
        });

        Mono<Void> updateToAccount = updateFromAccount.then(Mono.defer(() -> {
            if (amount.compareTo(BigDecimal.ZERO) != 0) {
                return handleToAccountUpdate(transactionRecord, amount, toAccountUpdateCorrId, state);
            }
            return Mono.empty();
        }));

        return updateToAccount.then(Mono.defer(() -> {
                    LOG.info("Updating transaction record with id: {}", transactionRecord.getRecordId());
                    TransactionRecord copy = TransactionRecord.builder()
                            .recordId(transactionRecord.getRecordId())
                            .userId(transactionRecord.getUserId())
                            .fromAccountId(transactionRecord.getFromAccountId())
                            .toAccountId(transactionRecord.getToAccountId())
                            .amount(transactionRecord.getAmount().add(amount))
                            .fromCurrency(transactionRecord.getFromCurrency())
                            .toCurrency(transactionRecord.getToCurrency())
                            .transactionDate(transactionRecord.getTransactionDate())
                            .build();
                    sendMessage("transactions-out-0", transactionUpdateCorrId, new CrudEvent<>(CrudEvent.Type.UPDATE, transactionRecord.getRecordId(), copy));
                    return responseListener.waitForResponse(transactionUpdateCorrId, Duration.ofSeconds(RESPONSE_EVENT_WAIT_DURATION));
                }))
                .flatMap(response -> {
                    if (response.getEventType() == SUCCESS) {
                        state.set(TransactionUpdateState.TRANSACTION_UPDATED);
                        String jsonString = response.getData().getMessage();
                        TransactionRecord updatedTransaction = deserializeObjectFromJson(jsonString, TransactionRecord.class);
                        return Mono.just(updatedTransaction);
                    } else if (response.getEventType() == ERROR) {
                        LOG.error("Couldn't update transaction record from transaction saga for transaction id: {}", transactionRecord.getRecordId());
                        return Mono.error(createMessageResponseError(response.getData()));
                    } else {
                        LOG.error("Unknown response event received inside transaction update saga for transaction id: {}", transactionRecord.getRecordId());
                        return Mono.error(new EventProcessingException("Could not process unknown response event"));
                    }

            }).onErrorResume(ex -> {
            if (ex instanceof ServiceResponseException && ((ServiceResponseException) ex).getResponseStatus().equals(HttpStatus.FAILED_DEPENDENCY)) {
                switch (state.get()) {
                    case INIT:
                        state.set(TransactionUpdateState.FROM_ACCOUNT_UPDATED);
                        break;
                    case FROM_ACCOUNT_UPDATED:
                        state.set(TransactionUpdateState.TO_ACCOUNT_UPDATED);
                        break;
                    case TO_ACCOUNT_UPDATED:
                        state.set(TransactionUpdateState.TRANSACTION_UPDATED);
                        break;
                    default:
                        break;
                }
            }
            LOG.info("Encountered an error during transaction update with id: {}, exception: {}", transactionRecord.getRecordId(), ex.getMessage());
            restoreTransactionUpdate(state.get(), transactionRecord, amount, amount);
            return Mono.error(ex);
        }).subscribeOn(publishEventScheduler);
    }

    private Mono<Void> handleFromAccountUpdate(TransactionRecord transactionRecord, BigDecimal amount, String accountUpdateCorrId, AtomicReference<TransactionUpdateState> state) {
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            LOG.info("Depositing account with id: {}, due to updated transaction record with id {}", transactionRecord.getFromAccountId(), transactionRecord.getRecordId());
            sendMessage("accounts-out-0", accountUpdateCorrId, new AccountEvent<>(AccountEvent.Type.DEPOSIT, transactionRecord.getFromAccountId(), amount.abs()));
        } else {
            LOG.info("Withdrawing money from account with id: {}, due to updated transaction record with id {}", transactionRecord.getFromAccountId(), transactionRecord.getRecordId());
            sendMessage("accounts-out-0", accountUpdateCorrId, new AccountEvent<>(AccountEvent.Type.WITHDRAW, transactionRecord.getFromAccountId(), amount));
        }

        return responseListener.waitForResponse(accountUpdateCorrId, Duration.ofSeconds(RESPONSE_EVENT_WAIT_DURATION))
                .flatMap(response -> {
                    if (response.getEventType() == SUCCESS) {
                        state.set(TransactionUpdateState.FROM_ACCOUNT_UPDATED);
                        return Mono.<Void>empty();
                    } else if (response.getEventType() == ERROR) {
                        LOG.error("Couldn't update corresponding account balance from transaction saga for transaction id: {}", transactionRecord.getRecordId());
                        return Mono.error(createMessageResponseError(response.getData()));
                    } else {
                        LOG.error("Unknown response event received inside transaction update saga for transaction id: {}", transactionRecord.getRecordId());
                        return Mono.error(new EventProcessingException("Could not process unknown response event"));
                    }
                })
                .subscribeOn(publishEventScheduler);
    }

    private Mono<Void> handleToAccountUpdate(TransactionRecord transactionRecord, BigDecimal amount, String accountUpdateCorrId, AtomicReference<TransactionUpdateState> state) {
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            LOG.info("Withdrawing money from account with id: {}, due to updated transaction record with id {}", transactionRecord.getToAccountId(), transactionRecord.getRecordId());
            sendMessage("accounts-out-0", accountUpdateCorrId, new AccountEvent<>(AccountEvent.Type.WITHDRAW, transactionRecord.getToAccountId(), amount.abs()));
        } else {
            LOG.info("Depositing money to account with id: {}, due to updated transaction record with id {}", transactionRecord.getToAccountId(), transactionRecord.getRecordId());
            sendMessage("accounts-out-0", accountUpdateCorrId, new AccountEvent<>(AccountEvent.Type.DEPOSIT, transactionRecord.getToAccountId(), amount));
        }

        return responseListener.waitForResponse(accountUpdateCorrId, Duration.ofSeconds(RESPONSE_EVENT_WAIT_DURATION))
                .flatMap(response -> {
                    if (response.getEventType() == SUCCESS) {
                        state.set(TransactionUpdateState.TO_ACCOUNT_UPDATED);
                        return Mono.<Void>empty();
                    } else if (response.getEventType() == ERROR) {
                        LOG.error("Couldn't update corresponding account balance from transaction saga for transaction id: {}", transactionRecord.getRecordId());
                        return Mono.error(createMessageResponseError(response.getData()));
                    } else {
                        LOG.error("Unknown response event received inside transaction update saga for transaction id: {}", transactionRecord.getRecordId());
                        return Mono.error(new EventProcessingException("Could not process unknown response event"));
                    }
                })
                .subscribeOn(publishEventScheduler);
    }

    @Override
    public Mono<TransactionRecord> updateTransaction(TransactionRecord transactionRecord, BigDecimal fromAmount, BigDecimal toAmount) {
        AtomicReference<TransactionUpdateState> state = new AtomicReference<>(TransactionUpdateState.INIT);
        AtomicReference<String> responseMessage = new AtomicReference<>("");
        AtomicReference<HttpStatus> responseStatus = new AtomicReference<>(HttpStatus.OK);
        String fromAccountUpdateCorrId = UUID.randomUUID().toString();
        String toAccountUpdateCorrId = UUID.randomUUID().toString();
        String transactionUpdateCorrId = UUID.randomUUID().toString();
        LOG.info("Starting transaction update saga with transaction id: {}", transactionRecord.getRecordId());

        Mono<Void> updateFromAccount = Mono.defer(() -> {
            if (fromAmount.compareTo(BigDecimal.ZERO) != 0) {
                return handleFromAccountUpdate(transactionRecord, fromAmount, fromAccountUpdateCorrId, state);
            }
            return Mono.empty();
        });

        Mono<Void> updateToAccount = updateFromAccount.then(Mono.defer(() -> {
            if (toAmount.compareTo(BigDecimal.ZERO) != 0) {
                return handleToAccountUpdate(transactionRecord, toAmount, toAccountUpdateCorrId, state);
            }
            return Mono.empty();
        }));

        return updateToAccount.then(Mono.defer(() -> {
                    LOG.info("Updating transaction record with id: {}", transactionRecord.getRecordId());
                    TransactionRecord copy = TransactionRecord.builder()
                            .recordId(transactionRecord.getRecordId())
                            .userId(transactionRecord.getUserId())
                            .fromAccountId(transactionRecord.getFromAccountId())
                            .toAccountId(transactionRecord.getToAccountId())
                            .amount(transactionRecord.getAmount().add(fromAmount))
                            .fromCurrency(transactionRecord.getFromCurrency())
                            .toCurrency(transactionRecord.getToCurrency())
                            .transactionDate(transactionRecord.getTransactionDate())
                            .build();
                    sendMessage("transactions-out-0", transactionUpdateCorrId, new CrudEvent<>(CrudEvent.Type.UPDATE, transactionRecord.getRecordId(), copy));
                    return responseListener.waitForResponse(transactionUpdateCorrId, Duration.ofSeconds(RESPONSE_EVENT_WAIT_DURATION));
                }))
                .flatMap(response -> {
                    if (response.getEventType() == SUCCESS) {
                        state.set(TransactionUpdateState.TRANSACTION_UPDATED);
                        String jsonString = response.getData().getMessage();
                        TransactionRecord updatedTransaction = deserializeObjectFromJson(jsonString, TransactionRecord.class);
                        return Mono.just(updatedTransaction);
                    } else if (response.getEventType() == ERROR) {
                        LOG.error("Couldn't update transaction record for transaction id: {}", transactionRecord.getRecordId());
                        return Mono.error(createMessageResponseError(response.getData()));
                    } else {
                        LOG.error("Unknown response event received for transaction id: {}", transactionRecord.getRecordId());
                        return Mono.error(new EventProcessingException("Could not process unknown response event"));
                    }
            }).onErrorResume(ex -> {
            if (ex instanceof ServiceResponseException && ((ServiceResponseException) ex).getResponseStatus().equals(HttpStatus.FAILED_DEPENDENCY)) {
                switch (state.get()) {
                    case INIT:
                        state.set(TransactionUpdateState.FROM_ACCOUNT_UPDATED);
                        break;
                    case FROM_ACCOUNT_UPDATED:
                        state.set(TransactionUpdateState.TO_ACCOUNT_UPDATED);
                        break;
                    case TO_ACCOUNT_UPDATED:
                        state.set(TransactionUpdateState.TRANSACTION_UPDATED);
                        break;
                    default:
                        break;
                }
            }
            LOG.info("Encountered an error during transaction update with id: {}, exception: {}", transactionRecord.getRecordId(), ex.getMessage());
            restoreTransactionUpdate(state.get(), transactionRecord, fromAmount, toAmount);
            return Mono.error(ex);
        }).subscribeOn(publishEventScheduler);
    }

    @Override
    public Mono<Void> deleteTransaction(TransactionRecord transactionRecord) {
        AtomicReference<TransactionDeletionState> state = new AtomicReference<>(TransactionDeletionState.INIT);
        String fromAccountUpdateId = UUID.randomUUID().toString();
        String toAccountUpdateId = UUID.randomUUID().toString();
        return transactionGateway.getTransaction(transactionRecord.getRecordId())
                .onErrorMap(e -> {
                    LOG.warn("Transaction record not found: {}", transactionRecord.getRecordId());
                    return new NotFoundException("Transaction record to delete not found");
                })
                .then(Mono.fromRunnable(() -> {
                    LOG.info("Starting the transaction deletion saga for transaction: {}", transactionRecord);
                    sendMessage("transactions-out-0", new CrudEvent<>(CrudEvent.Type.DELETE, transactionRecord.getRecordId(), transactionRecord));
                }))
                .then(Mono.delay(Duration.ofSeconds(1)))
                .then(transactionGateway.getTransaction(transactionRecord.getRecordId())
                        .onErrorResume(NotFoundException.class, e -> {
                            state.set(TransactionDeletionState.TRANSACTION_DELETED);
                            return Mono.empty();
                        })
                )
                .then(Mono.fromRunnable(() -> {
                    if (state.get() == TransactionDeletionState.TRANSACTION_DELETED) {
                        sendMessage("accounts-out-0", toAccountUpdateId, new AccountEvent<>(AccountEvent.Type.WITHDRAW, transactionRecord.getToAccountId(), transactionRecord.getAmount()));
                    }
                }))
                .then(responseListener.waitForResponse(toAccountUpdateId, Duration.ofSeconds(RESPONSE_EVENT_WAIT_DURATION)))
                .flatMap(response -> {
                    if (response.getEventType() == SUCCESS) {
                        LOG.info("Account updated successfully with id: {} because of deleted transaction entity with id: {}", transactionRecord.getToAccountId(), transactionRecord.getRecordId());
                        state.set(TransactionDeletionState.TO_ACCOUNT_WITHDRAWN);
                        return Mono.empty();
                    } else if (response.getEventType() == ERROR) {
                        LOG.warn("Failed to update account with id {} for deleted transaction entity with id: {}, error: {}", transactionRecord.getToAccountId(), transactionRecord.getRecordId(), response.getData().getMessage());
                        return Mono.error(createMessageResponseError(response.getData()));
                    } else {
                        LOG.error("Unknown response event received during transaction deletion");
                        return Mono.error(new EventProcessingException("Could not process unknown response event"));
                    }
                }).then(Mono.fromRunnable(() -> {
                    if (state.get() == TransactionDeletionState.TO_ACCOUNT_WITHDRAWN) {
                        sendMessage("accounts-out-0", fromAccountUpdateId, new AccountEvent<>(AccountEvent.Type.DEPOSIT, transactionRecord.getFromAccountId(), transactionRecord.getAmount()));
                    }
                })).then(responseListener.waitForResponse(fromAccountUpdateId, Duration.ofSeconds(RESPONSE_EVENT_WAIT_DURATION)))
                .flatMap(response -> {
                    if (response.getEventType() == SUCCESS) {
                        LOG.info("Account updated successfully with id: {} because of deleted transaction entity with id: {}", transactionRecord.getFromAccountId(), transactionRecord.getRecordId());
                        state.set(TransactionDeletionState.FROM_ACCOUNT_DEPOSITED);
                        return Mono.empty();
                    } else if (response.getEventType() == ERROR) {
                        LOG.warn("Failed to update account with id {} for deleted transaction entity with id: {}, error: {}", transactionRecord.getFromAccountId(), transactionRecord.getRecordId(), response.getData().getMessage());
                        return Mono.error(createMessageResponseError(response.getData()));
                    } else {
                        LOG.error("Unknown response event received during transaction deletion");
                        return Mono.error(new EventProcessingException("Could not process unknown response event"));
                    }
                })
                .onErrorResume(ex -> {
                    LOG.warn("Encountered an error during transaction deletion with id: {}, exception: {}", transactionRecord.getRecordId(), ex.getMessage());
                    if (!(ex instanceof NotFoundException)) {
                        if (ex instanceof ServiceResponseException && ((ServiceResponseException) ex).getResponseStatus().equals(HttpStatus.FAILED_DEPENDENCY)) {
                            switch (state.get()) {
                                case INIT:
                                    state.set(TransactionDeletionState.TRANSACTION_DELETED);
                                    break;
                                case TRANSACTION_DELETED:
                                    state.set(TransactionDeletionState.TO_ACCOUNT_WITHDRAWN);
                                    break;
                                case TO_ACCOUNT_WITHDRAWN:
                                    state.set(TransactionDeletionState.FROM_ACCOUNT_DEPOSITED);
                                    break;
                                default:
                                    break;
                            }
                        }
                        restoreTransactionDeletion(state.get(), transactionRecord, transactionRecord.getAmount(), transactionRecord.getAmount());
                    }
                    return Mono.empty();
                }).then().subscribeOn(publishEventScheduler);
    }

    @Override
    public Mono<Void> deleteTransaction(TransactionRecord transactionRecord, BigDecimal amount) {
        AtomicReference<TransactionDeletionState> state = new AtomicReference<>(TransactionDeletionState.INIT);
        String fromAccountUpdateId = UUID.randomUUID().toString();
        String toAccountUpdateId = UUID.randomUUID().toString();
        return transactionGateway.getTransaction(transactionRecord.getRecordId())
                .onErrorMap(e -> {
                    LOG.warn("Transaction record not found: {}", transactionRecord.getRecordId());
                    return new NotFoundException("Transaction record to delete not found");
                })
                .then(Mono.fromRunnable(() -> {
                    LOG.info("Starting the transaction deletion saga for transaction: {}", transactionRecord);
                    sendMessage("transactions-out-0", new CrudEvent<>(CrudEvent.Type.DELETE, transactionRecord.getRecordId(), transactionRecord));
                }))
                .then(Mono.delay(Duration.ofSeconds(1)))
                .then(transactionGateway.getTransaction(transactionRecord.getRecordId())
                        .onErrorResume(NotFoundException.class, e -> {
                            state.set(TransactionDeletionState.TRANSACTION_DELETED);
                            return Mono.empty();
                        })
                )
                .then(Mono.fromRunnable(() -> {
                    if (state.get() == TransactionDeletionState.TRANSACTION_DELETED) {
                        sendMessage("accounts-out-0", toAccountUpdateId, new AccountEvent<>(AccountEvent.Type.WITHDRAW, transactionRecord.getToAccountId(), amount));
                    }
                }))
                .then(responseListener.waitForResponse(toAccountUpdateId, Duration.ofSeconds(RESPONSE_EVENT_WAIT_DURATION)))
                .flatMap(response -> {
                    if (response.getEventType() == SUCCESS) {
                        LOG.info("Account updated successfully with id: {} because of deleted transaction entity with id: {}", transactionRecord.getToAccountId(), transactionRecord.getRecordId());
                        state.set(TransactionDeletionState.TO_ACCOUNT_WITHDRAWN);
                        return Mono.empty();
                    } else if (response.getEventType() == ERROR) {
                        LOG.warn("Failed to update account with id {} for deleted transaction entity with id: {}, error: {}", transactionRecord.getToAccountId(), transactionRecord.getRecordId(), response.getData().getMessage());
                        return Mono.error(createMessageResponseError(response.getData()));
                    } else {
                        LOG.error("Unknown response event received during transaction deletion");
                        return Mono.error(new EventProcessingException("Could not process unknown response event"));
                    }
                }).then(Mono.fromRunnable(() -> {
                    if (state.get() == TransactionDeletionState.TO_ACCOUNT_WITHDRAWN) {
                        sendMessage("accounts-out-0", fromAccountUpdateId, new AccountEvent<>(AccountEvent.Type.DEPOSIT, transactionRecord.getFromAccountId(), transactionRecord.getAmount()));
                    }
                })).then(responseListener.waitForResponse(fromAccountUpdateId, Duration.ofSeconds(RESPONSE_EVENT_WAIT_DURATION)))
                .flatMap(response -> {
                    if (response.getEventType() == SUCCESS) {
                        LOG.info("Account updated successfully with id: {} because of deleted transaction entity with id: {}", transactionRecord.getFromAccountId(), transactionRecord.getRecordId());
                        state.set(TransactionDeletionState.FROM_ACCOUNT_DEPOSITED);
                        return Mono.empty();
                    } else if (response.getEventType() == ERROR) {
                        LOG.warn("Failed to update account with id {} for deleted transaction entity with id: {}, error: {}", transactionRecord.getFromAccountId(), transactionRecord.getRecordId(), response.getData().getMessage());
                        return Mono.error(createMessageResponseError(response.getData()));
                    } else {
                        LOG.error("Unknown response event received during transaction deletion");
                        return Mono.error(new EventProcessingException("Could not process unknown response event"));
                    }
                })
                .onErrorResume(ex -> {
                    LOG.warn("Encountered an error during transaction deletion with id: {}, exception: {}", transactionRecord.getRecordId(), ex.getMessage());
                    if (!(ex instanceof NotFoundException)) {
                        if (ex instanceof ServiceResponseException && ((ServiceResponseException) ex).getResponseStatus().equals(HttpStatus.FAILED_DEPENDENCY)) {
                            switch (state.get()) {
                                case INIT:
                                    state.set(TransactionDeletionState.TRANSACTION_DELETED);
                                    break;
                                case TRANSACTION_DELETED:
                                    state.set(TransactionDeletionState.TO_ACCOUNT_WITHDRAWN);
                                    break;
                                case TO_ACCOUNT_WITHDRAWN:
                                    state.set(TransactionDeletionState.FROM_ACCOUNT_DEPOSITED);
                                    break;
                                default:
                                    break;
                            }
                        }
                        restoreTransactionDeletion(state.get(), transactionRecord, transactionRecord.getAmount(), amount);
                    }
                    return Mono.empty();
                }).then().subscribeOn(publishEventScheduler);
    }

    private Throwable mapException(String message, HttpStatus status) {
        return new ServiceResponseException(message, status);
    }

    private void sendMessage(String bindingName, String correlationId, Event<?, ?> event) {
        LOG.info("Sending a {} message to {} with correlation id {}", event.getEventType(), bindingName, correlationId);
        Message message = MessageBuilder.withPayload(event)
                .setHeader("partitionKey", event.getKey())
                .setHeader("correlationId", correlationId)
                .build();
        streamBridge.send(bindingName, message);
    }

    private void sendMessage(String bindingName, Event<?, ?> event) {
        LOG.info("Sending a {} message to {}", event.getEventType(), bindingName);
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


    private void restoreTransactionCreation(TransactionCreationState state, TransactionRecord transaction, BigDecimal fromAmount, BigDecimal toAmount) {
        LOG.info("Rolling back transaction creation with id {} state {}", transaction.getRecordId(), state);
        switch (state) {
            case INIT:
                LOG.info("No action needed to restore transaction creation with state {}", state);
                break;
            case FROM_ACCOUNT_WITHDRAWN:
                LOG.info("Depositing withdrawn money from account with id {}", transaction.getFromAccountId());
                sendMessage("accounts-out-0", new AccountEvent<>(AccountEvent.Type.DEPOSIT, transaction.getFromAccountId(), fromAmount));
                LOG.info("Deleting created transaction with id {}", transaction.getRecordId());
                sendMessage("transactions-out-0", new CrudEvent<String, TransactionRecord>(CrudEvent.Type.DELETE, transaction.getRecordId(), transaction));
                break;
            case TO_ACCOUNT_DEPOSITED:
                LOG.info("Withdrawing deposited money from account with id {}", transaction.getToAccountId());
                sendMessage("accounts-out-0", new AccountEvent<>(AccountEvent.Type.WITHDRAW, transaction.getToAccountId(), toAmount));
                LOG.info("Depositing withdrawn money from account with id {}", transaction.getFromAccountId());
                sendMessage("accounts-out-0", new AccountEvent<>(AccountEvent.Type.DEPOSIT, transaction.getFromAccountId(), fromAmount));
                LOG.info("Deleting created transaction with id {}", transaction.getRecordId());
                sendMessage("transactions-out-0", new CrudEvent<String, TransactionRecord>(CrudEvent.Type.DELETE, transaction.getRecordId(), transaction));
                break;
            case TRANSACTION_CREATED:
                LOG.info("Deleting created transaction with id {}", transaction.getRecordId());
                sendMessage("transactions-out-0", new CrudEvent<String, TransactionRecord>(CrudEvent.Type.DELETE, transaction.getRecordId(), transaction));
                break;
            default:
                LOG.warn("Couldn't determine restore action for transaction creation with state {}", state);
        }
    }

    private void restoreTransactionUpdate(TransactionUpdateState state, TransactionRecord transaction, BigDecimal fromAmount, BigDecimal toAmount) {
        LOG.info("Rolling back transaction update with id {} state {}", transaction.getRecordId(), state);
        switch (state) {
            case INIT:
                LOG.info("No action needed to restore transaction update with state {}", state);
                break;
            case FROM_ACCOUNT_UPDATED:
                if (fromAmount.compareTo(BigDecimal.ZERO) < 0) {
                    LOG.info("Withdrawing deposited money from account with id {} because of failed transaction update", transaction.getFromAccountId());
                    sendMessage("accounts-out-0", new AccountEvent<>(AccountEvent.Type.WITHDRAW, transaction.getFromAccountId(), fromAmount.abs()));
                } else {
                    LOG.info("Depositing withdrawn money to account with id {} because of failed transaction update", transaction.getFromAccountId());
                    sendMessage("accounts-out-0", new AccountEvent<>(AccountEvent.Type.DEPOSIT, transaction.getFromAccountId(), fromAmount));
                }
                break;
            case TO_ACCOUNT_UPDATED:
                if (toAmount.compareTo(BigDecimal.ZERO) < 0) {
                    LOG.info("Depositing withdrawn money to account with id {} because of failed transaction update", transaction.getToAccountId());
                    sendMessage("accounts-out-0", new AccountEvent<>(AccountEvent.Type.DEPOSIT, transaction.getToAccountId(), toAmount.abs()));
                } else {
                    LOG.info("Withdrawing deposited money from account with id {} because of failed transaction update", transaction.getToAccountId());
                    sendMessage("accounts-out-0", new AccountEvent<>(AccountEvent.Type.WITHDRAW, transaction.getToAccountId(), toAmount));
                }
                if (fromAmount.compareTo(BigDecimal.ZERO) < 0) {
                    LOG.info("Withdrawing deposited money from account with id {} because of failed transaction update", transaction.getFromAccountId());
                    sendMessage("accounts-out-0", new AccountEvent<>(AccountEvent.Type.WITHDRAW, transaction.getFromAccountId(), fromAmount.abs()));
                } else {
                    LOG.info("Depositing withdrawn money to account with id {} because of failed transaction update", transaction.getFromAccountId());
                    sendMessage("accounts-out-0", new AccountEvent<>(AccountEvent.Type.DEPOSIT, transaction.getFromAccountId(), fromAmount));
                };
                break;
            case TRANSACTION_UPDATED:
                if (toAmount.compareTo(BigDecimal.ZERO) < 0) {
                    LOG.info("Depositing withdrawn money to account with id {} because of failed transaction update", transaction.getToAccountId());
                    sendMessage("accounts-out-0", new AccountEvent<>(AccountEvent.Type.DEPOSIT, transaction.getToAccountId(), toAmount.abs()));
                } else {
                    LOG.info("Withdrawing deposited money from account with id {} because of failed transaction update", transaction.getToAccountId());
                    sendMessage("accounts-out-0", new AccountEvent<>(AccountEvent.Type.WITHDRAW, transaction.getToAccountId(), toAmount));
                }
                if (fromAmount.compareTo(BigDecimal.ZERO) < 0) {
                    LOG.info("Withdrawing deposited money from account with id {} because of failed transaction update", transaction.getFromAccountId());
                    sendMessage("accounts-out-0", new AccountEvent<>(AccountEvent.Type.WITHDRAW, transaction.getFromAccountId(), fromAmount.abs()));
                } else {
                    LOG.info("Depositing withdrawn money to account with id {} because of failed transaction update", transaction.getFromAccountId());
                    sendMessage("accounts-out-0", new AccountEvent<>(AccountEvent.Type.DEPOSIT, transaction.getFromAccountId(), fromAmount));
                };
                LOG.info("Rolling back transaction update with id {}", transaction.getRecordId());
                sendMessage("transactions-out-0", new CrudEvent<String, TransactionRecord>(CrudEvent.Type.UPDATE, transaction.getRecordId(), transaction));
                break;
            default:
                LOG.warn("Couldn't determine restore action for transaction update with state {}", state);
        }
    }

    private void restoreTransactionDeletion(TransactionDeletionState state, TransactionRecord transaction, BigDecimal fromAmount, BigDecimal toAmount) {
        LOG.info("Rolling back transaction deletion with id {} and state {}", transaction.getRecordId(), state);
        switch (state) {
            case TRANSACTION_DELETED:
                LOG.info("Rolling back transaction deletion with id {}", transaction.getRecordId());
                sendMessage("transactions-out-0", new CrudEvent<String, TransactionRecord>(CrudEvent.Type.CREATE, transaction.getRecordId(), transaction));
                break;
            case TO_ACCOUNT_WITHDRAWN:
                LOG.info("Rolling back withdrawal made because of transaction deletion with id {}", transaction.getRecordId());
                sendMessage("accounts-out-0", new AccountEvent<>(AccountEvent.Type.DEPOSIT, transaction.getToAccountId(), toAmount));
                LOG.info("Rolling back transaction deletion with id {}", transaction.getRecordId());
                sendMessage("transactions-out-0", new CrudEvent<String, TransactionRecord>(CrudEvent.Type.CREATE, transaction.getRecordId(), transaction));
                break;
            case FROM_ACCOUNT_DEPOSITED:
                LOG.info("Rolling back deposit made because of transaction deletion with id {}", transaction.getRecordId());
                sendMessage("accounts-out-0", new AccountEvent<>(AccountEvent.Type.WITHDRAW, transaction.getFromAccountId(), fromAmount));
                LOG.info("Rolling back withdrawal made because of transaction deletion with id {}", transaction.getRecordId());
                sendMessage("accounts-out-0", new AccountEvent<>(AccountEvent.Type.DEPOSIT, transaction.getToAccountId(), toAmount));
                LOG.info("Rolling back transaction deletion with id {}", transaction.getRecordId());
                sendMessage("transactions-out-0", new CrudEvent<String, TransactionRecord>(CrudEvent.Type.CREATE, transaction.getRecordId(), transaction));
                break;
            default:
                LOG.warn("Couldn't determine rollback action for transaction deletion with state {}", state);
                break;
        }
    }
}
