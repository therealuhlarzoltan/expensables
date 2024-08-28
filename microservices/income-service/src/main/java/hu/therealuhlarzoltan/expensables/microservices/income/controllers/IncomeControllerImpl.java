package hu.therealuhlarzoltan.expensables.microservices.income.controllers;

import hu.therealuhlarzoltan.expensables.microservices.income.components.mappers.IncomeRecordMapper;
import hu.therealuhlarzoltan.expensables.microservices.income.services.IncomeService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class IncomeControllerImpl implements IncomeController {
    private static final Logger LOG = LoggerFactory.getLogger(IncomeControllerImpl.class);

    private final IncomeRecordMapper mapper;
    private final IncomeService service;
}
