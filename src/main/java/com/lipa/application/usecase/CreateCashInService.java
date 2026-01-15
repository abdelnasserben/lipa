package com.lipa.application.usecase;

import com.lipa.application.dto.CashInPersistCommand;
import com.lipa.application.exception.BusinessRuleException;
import com.lipa.application.exception.NotFoundException;
import com.lipa.application.port.in.CreateCashInUseCase;
import com.lipa.application.port.out.CashInAccountPort;
import com.lipa.application.port.out.CashInIdempotencyPort;
import com.lipa.application.port.out.CashInPersistencePort;
import com.lipa.application.port.out.TimeProviderPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
        validate(command);

        // 1) Idempotency
        var existing = idempotencyPort.findByIdempotencyKey(command.idempotencyKey().trim());
        if (existing.isPresent()) {
            return new Result(existing.get().transactionId(), existing.get().status());
        }

        // 2) Load accounts (light snapshots)
        var client = accountPort.findById(command.clientAccountId())
                .orElseThrow(() -> new NotFoundException("Client account not found id=" + command.clientAccountId()));

        var technical = accountPort.findById(command.technicalAccountId())
                .orElseThrow(() -> new NotFoundException("Technical account not found id=" + command.technicalAccountId()));

        // 3) Business rules (same behavior as before)
        if (!"ACTIVE".equalsIgnoreCase(client.status())) {
            throw new BusinessRuleException("Client account is not active");
        }
        if (!"ACTIVE".equalsIgnoreCase(technical.status())) {
            throw new BusinessRuleException("Technical account is not active");
        }
        if (!"TECHNICAL".equalsIgnoreCase(technical.type())) {
            throw new BusinessRuleException("Source account must be TECHNICAL");
        }

        // 4) Persist transaction + ledger + audit (infra does JPA details)
        Instant now = time.now();

        var persisted = persistencePort.persist(new CashInPersistCommand(
                command.clientAccountId(),
                command.technicalAccountId(),
                command.amount(),
                command.currency().trim(),
                command.idempotencyKey().trim(),
                command.description(),
                now
        ));

        return new Result(persisted.transactionId(), persisted.status());
    }

    private void validate(Command command) {
        if (command.clientAccountId() == null) {
            throw new BusinessRuleException("clientAccountId is required");
        }
        if (command.technicalAccountId() == null) {
            throw new BusinessRuleException("technicalAccountId is required");
        }
        if (command.amount() == null) {
            throw new BusinessRuleException("amount is required");
        }
        if (command.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException("amount must be > 0");
        }
        if (command.currency() == null || command.currency().trim().length() != 3) {
            throw new BusinessRuleException("currency must be 3 letters");
        }
        if (command.idempotencyKey() == null || command.idempotencyKey().trim().isEmpty()) {
            throw new BusinessRuleException("idempotencyKey is required");
        }
    }
}
