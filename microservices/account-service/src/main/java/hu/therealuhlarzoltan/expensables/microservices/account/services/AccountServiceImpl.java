package hu.therealuhlarzoltan.expensables.microservices.account.services;

import com.mongodb.DuplicateKeyException;
import hu.therealuhlarzoltan.expensables.api.microservices.core.account.Account;
import hu.therealuhlarzoltan.expensables.api.microservices.exceptions.InsufficientFundsException;
import hu.therealuhlarzoltan.expensables.api.microservices.exceptions.InvalidInputDataException;
import hu.therealuhlarzoltan.expensables.api.microservices.exceptions.NotFoundException;
import hu.therealuhlarzoltan.expensables.microservices.account.components.mappers.AccountMapper;
import hu.therealuhlarzoltan.expensables.microservices.account.components.validators.AccountStateValidator;
import hu.therealuhlarzoltan.expensables.microservices.account.components.validators.PositiveIfNotCreditValidator;
import hu.therealuhlarzoltan.expensables.microservices.account.repositories.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.validation.annotation.Validated;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements  AccountService {

    private static final Logger LOG = LoggerFactory.getLogger(AccountServiceImpl.class);
    private final AccountMapper accountMapper;
    private final Validator validator;
    private final PositiveIfNotCreditValidator positiveIfNotCreditValidator;
    private final AccountStateValidator accountStateValidator;
    private final AccountRepository accountRepository;


    @Override
    public Flux<Account> getAllAccounts() {
        LOG.info("Will get account information for all accounts");
        var entities = accountRepository.findAll();
        return entities.map(accountMapper::accountEntityToAccount);
    }

    @Override
    public Mono<Account> getAccountById(UUID accountId) {
        LOG.info("Will get account information for id={}", accountId);
        return accountRepository.findByEntityId(accountId.toString())
                .map(accountMapper::accountEntityToAccount)
                .switchIfEmpty(Mono.error(new NotFoundException("No account found for accountId: " + accountId)));
    }

    @Override
    public Flux<Account> getAccountsByOwnerId(long ownerId) {
        LOG.info("Will get account information for ownerId={}", ownerId);
        return accountRepository.findByOwnerId(ownerId)
                .map(accountMapper::accountEntityToAccount);
    }

    @Override
    public Mono<Account> createAccount(Account account) {
        LOG.info("Will create account with information: {}", account);
        return internalCreate(account);
    }

    @Override
    public Mono<Account> updateAccount(Account account) {
        LOG.info("Will update account with information: {}", account);
        return internalUpdate(account);
    }


    @Override
    public Mono<Void> deleteAccount(UUID accountId) {
        LOG.info("Will delete account with id={}", accountId);
        return accountRepository.deleteByEntityId(accountId.toString());
    }

    @Override
    public Mono<Account> deposit(UUID accountId, BigDecimal amount) {
        LOG.info("Will deposit {} to account with id={}", amount, accountId);
        return accountRepository.findByEntityId(accountId.toString())
                .switchIfEmpty(Mono.error(new NotFoundException("No account found for accountId: " + accountId)))
                .flatMap(entity -> {
                    entity.setBalance(entity.getBalance().add(amount));
                    return accountRepository.save(entity).map(accountMapper::accountEntityToAccount);
                });
    }

    @Override
    public Mono<Account> withdraw(UUID accountId, BigDecimal amount) {
        LOG.info("Will withdraw {} from account with id={}", amount, accountId);
        return accountRepository.findByEntityId(accountId.toString())
                .switchIfEmpty(Mono.error(new NotFoundException("No account found for accountId: " + accountId)))
                .flatMap(entity -> {
                    boolean isValid = positiveIfNotCreditValidator.isValid(amount, entity);
                    if (!isValid) {
                        return Mono.error(new InsufficientFundsException("Insufficient funds for account with name: " + entity.getName()));
                    }
                    entity.setBalance(entity.getBalance().subtract(amount));
                    return accountRepository.save(entity).map(accountMapper::accountEntityToAccount);
                });
    }

    private Mono<Account> internalCreate(Account account) {
        var entity = accountMapper.accountToAccountEntity(account);
        if (entity.getEntityId() == null) {
            entity.setEntityId(UUID.randomUUID().toString());
        }
        try {
            accountStateValidator.validate(entity);
        } catch (InvalidInputDataException e) {
            return Mono.error(e);
        }
        Errors errors = new BeanPropertyBindingResult(entity, "account");
        validator.validate(entity, errors);
        if (errors.hasErrors()) {
            return Mono.error(new InvalidInputDataException(errors.getAllErrors().get(0).getDefaultMessage()));
        }
        entity.setVersion(null); // auto handled by mongo
        return accountRepository.save(entity).onErrorMap(
                DuplicateKeyException.class,
                e -> new InvalidInputDataException("Account with id " + entity.getEntityId() + " already exists")
        ).map(accountMapper::accountEntityToAccount);
    }


    private Mono<Account> internalUpdate(Account account) {
        LOG.info("Patching account with id={}", account.getAccountId());
        var newEntity = accountMapper.accountToAccountEntity(account);
        Errors errors = new BeanPropertyBindingResult(newEntity, "account");
        return accountRepository.findByEntityId(account.getAccountId())
                .switchIfEmpty(Mono.error(new NotFoundException("No account found for accountId: " + account.getAccountId())))
                .flatMap(entity -> {
                    entity.setName(newEntity.getName());
                    entity.setBank(newEntity.getBank());
                    entity.setCategory(newEntity.getCategory());
                    entity.setType(newEntity.getType());
                    entity.setBalance(newEntity.getBalance());
                    try {
                        accountStateValidator.validate(entity);
                    } catch (InvalidInputDataException e) {
                        return Mono.error(e);
                    }
                    validator.validate(entity, errors);
                    if (errors.hasErrors()) {
                        return Mono.error(new InvalidInputDataException(errors.getAllErrors().get(0).getDefaultMessage()));
                    }
                    return accountRepository.save(entity).map(accountMapper::accountEntityToAccount);
                });
    }
}
