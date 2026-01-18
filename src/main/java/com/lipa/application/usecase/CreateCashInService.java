package com.lipa.application.usecase;

import com.lipa.application.exception.BusinessRuleException;
import com.lipa.application.exception.NotFoundException;
import com.lipa.application.port.in.CreateCashInUseCase;
import com.lipa.application.port.out.AccountSnapshotPort;
import com.lipa.application.port.out.CashInPersistencePort;
import com.lipa.application.port.out.IdempotencyPort;
import com.lipa.application.port.out.TimeProviderPort;
import com.lipa.application.util.DomainRules;
import com.lipa.application.util.InputRules;
import com.lipa.application.util.MoneyRules;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class CreateCashInService implements CreateCashInUseCase {

    private final AccountSnapshotPort accounts;
    private final IdempotencyPort idempotency;
    private final CashInPersistencePort persistence;
    private final TimeProviderPort time;

    public CreateCashInService(AccountSnapshotPort accounts,
                               @Qualifier("cashInIdempotencyPort") IdempotencyPort idempotency,
                               CashInPersistencePort persistence,
                               TimeProviderPort time) {
        this.accounts = accounts;
        this.idempotency = idempotency;
        this.persistence = persistence;
        this.time = time;
    }

    @Override
    @Transactional
    public Result create(Command command) {
        Validated v = validate(command);

        // 1) Idempotency
        var existing = idempotency.findByIdempotencyKey(v.idempotencyKey);
        if (existing.isPresent()) {
            return new Result(existing.get().transactionId(), existing.get().status());
        }

        // 2) Load accounts (light snapshots)
        var client = accounts.findById(command.clientAccountId())
                .orElseThrow(() -> new NotFoundException("Client account not found id=" + command.clientAccountId()));

        var technical = accounts.findById(command.technicalAccountId())
                .orElseThrow(() -> new NotFoundException("Technical account not found id=" + command.technicalAccountId()));

        // 3) Business rules
        DomainRules.requireStatusActive("Client account", client.status());
        DomainRules.requireStatusActive("Technical account", technical.status());
        DomainRules.requireType("Source account", technical.type(), "TECHNICAL");

        // 4) Persist transaction + ledger + audit
        Instant now = time.now();

        var persisted = persistence.persist(new CashInPersistencePort.PersistCommand(
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

    private record Validated(String currency, String idempotencyKey, String description) {}
}
