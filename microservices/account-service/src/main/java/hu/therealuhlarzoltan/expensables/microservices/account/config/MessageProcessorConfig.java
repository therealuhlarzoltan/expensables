package hu.therealuhlarzoltan.expensables.microservices.account.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import hu.therealuhlarzoltan.expensables.api.microservices.core.account.Account;
import hu.therealuhlarzoltan.expensables.api.microservices.core.account.AccountController;
import hu.therealuhlarzoltan.expensables.api.microservices.events.AccountEvent;
import hu.therealuhlarzoltan.expensables.api.microservices.events.CrudEvent;
import hu.therealuhlarzoltan.expensables.api.microservices.events.Event;
import hu.therealuhlarzoltan.expensables.api.microservices.exceptions.EventProcessingException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.function.Consumer;

@Configuration
@RequiredArgsConstructor
public class MessageProcessorConfig {
    private static final Logger LOG = LoggerFactory.getLogger(MessageProcessorConfig.class);

    private final ObjectMapper objectMapper;
    private final AccountController accountController;

    @Bean
    public Consumer<Event<?, ?>> messageProcessor() {
        return event -> {
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
                @SuppressWarnings(value = "unchecked") // We know that the key is a String
                CrudEvent<String, ?> eventWithKey = (CrudEvent<String, ?>) event;
                CrudEvent<String, Account> crudEvent = new CrudEvent<String, Account>(eventWithKey.getEventType(), eventWithKey.getKey(), crudEventData);
                switch (crudEvent.getEventType()) {
                    case CREATE:
                        Account account = crudEvent.getData();
                        accountController.createAccount(account).block();
                        break;
                    case UPDATE:
                        Account accountToUpdate = crudEvent.getData();
                        accountController.updateAccount(accountToUpdate).block();
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
}
