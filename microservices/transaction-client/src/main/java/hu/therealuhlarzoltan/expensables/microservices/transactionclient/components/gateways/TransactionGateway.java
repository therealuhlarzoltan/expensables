package hu.therealuhlarzoltan.expensables.microservices.transactionclient.components.gateways;

import hu.therealuhlarzoltan.expensables.api.microservices.core.transaction.TransactionRecord;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.net.URI;

public interface TransactionGateway {
    Mono<TransactionRecord> getTransaction(String transactionId);
    Flux<TransactionRecord> getAccountTransactions(String accountId);
    Flux<TransactionRecord> getOutgoingTransactions(String accountId);
    Flux<TransactionRecord> getIncomingTransactions(String accountId);
}
