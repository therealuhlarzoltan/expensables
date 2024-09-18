package hu.therealuhlarzoltan.expensables.microservices.accountclient.components.gateways;

import hu.therealuhlarzoltan.expensables.api.microservices.core.expense.ExpenseRecord;
import hu.therealuhlarzoltan.expensables.api.microservices.core.income.IncomeRecord;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IncomeGateway {
    Mono<IncomeRecord> getIncome(String id);
    Mono<IncomeRecord> getIncomeWithFallback(String id);
    Flux<IncomeRecord> getIncomes(String accountId);
    Flux<IncomeRecord> getIncomesWithFallback(String accountId);
}
