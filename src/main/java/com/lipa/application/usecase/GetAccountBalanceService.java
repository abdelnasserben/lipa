package com.lipa.application.usecase;

import com.lipa.application.exception.NotFoundException;
import com.lipa.application.port.in.GetAccountBalanceUseCase;
import com.lipa.application.port.out.AccountRepositoryPort;
import com.lipa.application.port.out.AuditRepositoryPort;
import com.lipa.application.port.out.LedgerQueryPort;
import com.lipa.application.port.out.TimeProviderPort;
import com.lipa.infrastructure.persistence.jpa.entity.AuditEventEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Service
public class GetAccountBalanceService implements GetAccountBalanceUseCase {

    private final AccountRepositoryPort accountRepository;
    private final LedgerQueryPort ledgerQuery;
    private final TimeProviderPort time;
    private final AuditRepositoryPort auditRepository;

    public GetAccountBalanceService(AccountRepositoryPort accountRepository,
                                    LedgerQueryPort ledgerQuery,
                                    TimeProviderPort time,
                                    AuditRepositoryPort auditRepository) {
        this.accountRepository = accountRepository;
        this.ledgerQuery = ledgerQuery;
        this.time = time;
        this.auditRepository = auditRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Result getBalance(UUID accountId) {
        var account = accountRepository.findById(accountId)
                .orElseThrow(() -> new NotFoundException("Account not found id=" + accountId));

        BigDecimal credits = defaultZero(ledgerQuery.sumCredits(accountId));
        BigDecimal debits = defaultZero(ledgerQuery.sumDebits(accountId));
        BigDecimal balance = credits.subtract(debits);

        var now = time.now();

        // Audit (optionnel, mais utile pour traçabilité)
        AuditEventEntity audit = new AuditEventEntity();
        audit.setId(UUID.randomUUID());
        audit.setActorType(AuditEventEntity.ActorType.SYSTEM);
        audit.setAction("BALANCE_VIEWED");
        audit.setTargetType(AuditEventEntity.TargetType.ACCOUNT);
        audit.setTargetId(accountId);
        audit.setMetadata(Map.of(
                "credits", credits.toPlainString(),
                "debits", debits.toPlainString(),
                "balance", balance.toPlainString(),
                "currency", "KMF"
        ));
        audit.setCreatedAt(now);
        auditRepository.save(audit);

        return new Result(account.getId(), balance, "KMF", now);
    }

    private BigDecimal defaultZero(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}
