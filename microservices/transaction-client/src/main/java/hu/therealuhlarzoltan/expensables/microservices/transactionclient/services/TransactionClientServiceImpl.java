package hu.therealuhlarzoltan.expensables.microservices.transactionclient.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import hu.therealuhlarzoltan.expensables.api.microservices.composite.transaction.TransactionInfo;
import hu.therealuhlarzoltan.expensables.api.microservices.composite.transaction.TransactionRequest;
import hu.therealuhlarzoltan.expensables.api.microservices.composite.transaction.TransactionUpdateRequest;
import hu.therealuhlarzoltan.expensables.api.microservices.core.transaction.TransactionRecord;
import hu.therealuhlarzoltan.expensables.microservices.transactionclient.components.gateways.AccountGateway;
import hu.therealuhlarzoltan.expensables.microservices.transactionclient.components.gateways.TransactionGateway;
import hu.therealuhlarzoltan.expensables.microservices.transactionclient.components.mappers.TransactionMapper;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@Service
@RequiredArgsConstructor
public class TransactionClientServiceImpl implements TransactionClientService {
    private static final Logger LOG = LoggerFactory.getLogger(TransactionClientServiceImpl.class);
    private final TransactionIntegration integration;
    private final TransactionMapper transactionMapper;
    private final ObjectMapper objectMapper;
    private final AccountGateway accountGateway;
    private final TransactionGateway transactionGateway;

    @Override
    public Mono<TransactionInfo> getTransaction(String transactionId) {
        LOG.info("Will call the getTransaction API in the integration layer with id: {}", transactionId);
        return integration.getTransaction(transactionId)
                .flatMap(transactionRecord -> {
                    LOG.info("Resolving account names for transaction: {}", transactionRecord);
                    Mono<String> fromAccountNameMono = integration.getAccountWithFallback(transactionRecord.getFromAccountId())
                            .map(account -> account.getAccountName());
                    Mono<String> toAccountNameMono = integration.getAccountWithFallback(transactionRecord.getToAccountId())
                            .map(account -> account.getAccountName());

                    return Mono.zip(fromAccountNameMono, toAccountNameMono)
                            .map(tuple -> {
                                LOG.info("Resolved accounts {} and {} for transaction with id {}", tuple.getT1(), tuple.getT2(), transactionId);
                                var transactionInfo = transactionMapper.transactionRecordToInfo(transactionRecord);
                                transactionInfo.setFromAccountName(tuple.getT1());
                                transactionInfo.setToAccountName(tuple.getT2());
                                return transactionInfo;
                            });
                });
    }

    @Override
    public Flux<TransactionInfo> getOutgoingTransactions(UUID accountId) {
        LOG.info("Will call the getOutgoingTransactions API in the integration layer with id: {}", accountId);
        return integration.getOutgoingTransactions(accountId.toString())
                .flatMap(transactionRecord -> {
            Mono<String> fromAccountNameMono = integration.getAccountWithFallback(transactionRecord.getFromAccountId())
                    .map(account -> account.getAccountName());
            Mono<String> toAccountNameMono = integration.getAccountWithFallback(transactionRecord.getToAccountId())
                    .map(account -> account.getAccountName());

            return Mono.zip(fromAccountNameMono, toAccountNameMono)
                    .map(tuple -> {
                        var transactionInfo = transactionMapper.transactionRecordToInfo(transactionRecord);
                        transactionInfo.setFromAccountName(tuple.getT1());
                        transactionInfo.setToAccountName(tuple.getT2());
                        return transactionInfo;
                    });
        });
    }

    @Override
    public Flux<TransactionInfo> getIncomingTransaction(UUID accountId) {
        LOG.info("Will call the getIncomingTransactions API in the integration layer with id: {}", accountId);
        return integration.getIncomingTransactions(accountId.toString())
                .flatMap(transactionRecord -> {
                    Mono<String> fromAccountNameMono = integration.getAccountWithFallback(transactionRecord.getFromAccountId())
                            .map(account -> account.getAccountName());
                    Mono<String> toAccountNameMono = integration.getAccountWithFallback(transactionRecord.getToAccountId())
                            .map(account -> account.getAccountName());

                    return Mono.zip(fromAccountNameMono, toAccountNameMono)
                            .map(tuple -> {
                                var transactionInfo = transactionMapper.transactionRecordToInfo(transactionRecord);
                                transactionInfo.setFromAccountName(tuple.getT1());
                                transactionInfo.setToAccountName(tuple.getT2());
                                return transactionInfo;
                            });
                });
    }

    @Override
    public Mono<TransactionInfo> createTransaction(TransactionRequest transactionRequest) {
        LOG.info("Will call the integration layer to create transaction with body: {}", transactionRequest);
        AtomicReference<String> fromAccountCurrency = new AtomicReference<>();
        AtomicReference<String> toAccountCurrency = new AtomicReference<>();
        TransactionRecord record = transactionMapper.transactionRequestToRecord(transactionRequest);
        record.setRecordId(new ObjectId().toHexString());
        return integration.getAccountCurrency(transactionRequest.getFromAccountId())
                .doOnSuccess(fromAccountCurrency::set)
                .doOnError(throwable -> LOG.error("Error while getting 'to' account currency: {}", throwable.getMessage()))
                .then(integration.getAccountCurrency(transactionRequest.getToAccountId()))
                .doOnSuccess(toAccountCurrency::set)
                .doOnError(throwable -> LOG.error("Error while getting 'from' account currency: {}", throwable.getMessage()))
                .flatMap(_ -> {
                            record.setFromCurrency(fromAccountCurrency.get());
                            record.setToCurrency(toAccountCurrency.get());
                            if (fromAccountCurrency.get().equals(toAccountCurrency.get())) {
                                return integration.createTransaction(record);
                            } else {
                                return integration.createTransactionWithExchange(record);
                            }
                        }
                ).map(transactionMapper::transactionRecordToInfo)
                .flatMap(info -> {
                    LOG.info("Resolving account names for transaction: {}", info);
                    Mono<String> fromAccountNameMono = integration.getAccountWithFallback(info.getFromAccountId())
                            .map(account -> account.getAccountName());
                    Mono<String> toAccountNameMono = integration.getAccountWithFallback(info.getToAccountId())
                            .map(account -> account.getAccountName());

                    return Mono.zip(fromAccountNameMono, toAccountNameMono)
                            .map(tuple -> {
                                LOG.info("Resolved accounts {} and {} for transaction with id {}", tuple.getT1(), tuple.getT2(), info.getTransactionId());
                                info.setFromAccountName(tuple.getT1());
                                info.setToAccountName(tuple.getT2());
                                return info;
                            });
                });
    }
    

    @Override
    public Mono<TransactionInfo> updateTransaction(String transactionId, TransactionUpdateRequest updateRequest) {
        LOG.info("Will call the integration layer to update transaction with body: {}", updateRequest);
        return integration.getTransaction(transactionId)
                .doOnError(ex -> LOG.error("Transaction with id {} not found", transactionId))
                .flatMap(transactionRecord -> {
                   if (transactionRecord.getFromCurrency().equals(transactionRecord.getToCurrency())) {
                       return integration.updateTransaction(transactionMapper.transactionUpdateToRecord(updateRequest))
                               .map(transactionMapper::transactionRecordToInfo);
                   } else {
                       return integration.updateTransactionWithExchange(transactionMapper.transactionUpdateToRecord(updateRequest))
                               .map(transactionMapper::transactionRecordToInfo);
                   }
                });
    }

    @Override
    public Mono<Void> deleteTransaction(String transactionId) {
        LOG.info("Will call the integration layer to delete transaction with id: {}", transactionId);
        return integration.getTransaction(transactionId)
                .doOnError(ex -> LOG.error("Transaction with id {} not found", transactionId))
                .flatMap(transactionRecord -> {
                    if (transactionRecord.getFromCurrency().equals(transactionRecord.getToCurrency())) {
                        return integration.deleteTransaction(transactionRecord);
                    } else {
                        return integration.deleteTransactionWithExchange(transactionRecord);
                    }
                });
    }

}
