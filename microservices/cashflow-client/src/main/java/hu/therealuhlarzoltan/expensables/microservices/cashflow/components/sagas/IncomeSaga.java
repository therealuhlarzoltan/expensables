package hu.therealuhlarzoltan.expensables.microservices.cashflow.components.sagas;

import hu.therealuhlarzoltan.expensables.api.microservices.core.income.IncomeRecord;
import hu.therealuhlarzoltan.expensables.api.microservices.events.Event;
import hu.therealuhlarzoltan.expensables.api.microservices.exceptions.InvalidInputDataException;
import hu.therealuhlarzoltan.expensables.api.microservices.exceptions.NotFoundException;
import hu.therealuhlarzoltan.expensables.util.HttpErrorInfo;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.math.BigDecimal;

public interface IncomeSaga {
    Mono<IncomeRecord> createIncome(IncomeRecord incomeRecord);
    Mono<IncomeRecord> createIncome(IncomeRecord incomeRecord, BigDecimal amount);
    Mono<IncomeRecord> updateIncome(IncomeRecord incomeRecord, BigDecimal amount);
    Mono<Void> deleteIncome(IncomeRecord incomeRecord);
    Mono<Void> deleteIncome(IncomeRecord incomeRecord, BigDecimal amount);

}
