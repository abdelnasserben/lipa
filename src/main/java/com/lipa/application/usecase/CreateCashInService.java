package com.lipa.application.usecase;

import com.lipa.application.dto.CashInPersistCommand;
import com.lipa.application.exception.BusinessRuleException;
import com.lipa.application.exception.NotFoundException;
import com.lipa.application.port.in.CreateCashInUseCase;
import com.lipa.application.port.out.CashInAccountPort;
import com.lipa.application.port.out.CashInIdempotencyPort;
import com.lipa.application.port.out.CashInPersistencePort;
import com.lipa.application.port.out.TimeProviderPort;
import com.lipa.application.util.DomainRules;
import com.lipa.application.util.InputRules;
import com.lipa.application.util.MoneyRules;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class CreateCashInService implements CreateCashInUseCase {

    private final CashInAccountPort accountPort;
    private final CashInIdempotencyPort idempotencyPort;
    private final CashInPersistencePort persistencePort;
    private final TimeProviderPort time;

    public CreateCashInService(CashInAccountPort accountPort,
                               CashInIdempotencyPort idempotencyPort,
                               CashInPersistencePort persistencePort,
                               TimeProviderPort time) {
        this.accountPort = accountPort;
        this.idempotencyPort = idempotencyPort;
        this.persistencePort = persistencePort;
        this.time = time;
    }

    @Override
    @Transactional
    public Result create(Command command) {
        Validated v = validate(command);

        // 1) Idempotency
        var existing = idempotencyPort.findByIdempotencyKey(v.idempotencyKey);
        if (existing.isPresent()) {
            return new Result(existing.get().transactionId(), existing.get().status());
        }

        // 2) Load accounts (light snapshots)
        var client = accountPort.findById(command.clientAccountId())
                .orElseThrow(() -> new NotFoundException("Client account not found id=" + command.clientAccountId()));

        var technical = accountPort.findById(command.technicalAccountId())
                .orElseThrow(() -> new NotFoundException("Technical account not found id=" + command.technicalAccountId()));

        // 3) Business rules
        DomainRules.requireStatusActive("Client account", client.status());
        DomainRules.requireStatusActive("Technical account", technical.status());
        DomainRules.requireType("Source account", technical.type(), "TECHNICAL");

        // 4) Persist transaction + ledger
        Instant now = time.now();

        var persisted = persistencePort.persist(new CashInPersistCommand(
                command.clientAccountId(),
                command.technicalAccountId(),
                command.amount(),
                v.currency,
                v.idempotencyKey,
                v.description,
                now
        ));

        return new Result(persisted.transactionId(), persisted.status());
    }

    private Validated validate(Command command) {
        if (command.clientAccountId() == null) {
            throw new BusinessRuleException("clientAccountId is required");
        }
        if (command.technicalAccountId() == null) {
            throw new BusinessRuleException("technicalAccountId is required");
        }

        MoneyRules.requirePositive(command.amount(), "amount");

        String currency = MoneyRules.normalizeCurrency(command.currency());
        String idemKey = InputRules.requireTrimmedNotBlank(command.idempotencyKey(), "idempotencyKey");
        String description = InputRules.trimToNull(command.description());

        return new Validated(currency, idemKey, description);
    }

    private record Validated(String currency, String idempotencyKey, String description) {
    }
}
