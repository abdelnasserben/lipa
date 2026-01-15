package com.lipa.application.usecase;

import com.lipa.application.exception.NotFoundException;
import com.lipa.application.port.in.GetAccountBalanceUseCase;
import com.lipa.application.port.out.AccountLookupPort;
import com.lipa.application.port.out.BalanceAuditPort;
import com.lipa.application.port.out.LedgerQueryPort;
import com.lipa.application.port.out.TimeProviderPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
public class GetAccountBalanceService implements GetAccountBalanceUseCase {

    private final AccountLookupPort accountLookup;
    private final LedgerQueryPort ledgerQuery;
    private final TimeProviderPort time;
    private final BalanceAuditPort audit;

    public GetAccountBalanceService(AccountLookupPort accountLookup,
                                    LedgerQueryPort ledgerQuery,
                                    TimeProviderPort time,
                                    BalanceAuditPort audit) {
        this.accountLookup = accountLookup;
        this.ledgerQuery = ledgerQuery;
        this.time = time;
        this.audit = audit;
    }

    @Override
    @Transactional(readOnly = true)
    public Result getBalance(UUID accountId) {
        if (accountId == null) {
            throw new NotFoundException("Account not found id=null");
        }

        boolean exists = accountLookup.existsById(accountId);
        if (!exists) {
            throw new NotFoundException("Account not found id=" + accountId);
        }

        BigDecimal credits = defaultZero(ledgerQuery.sumCredits(accountId));
        BigDecimal debits = defaultZero(ledgerQuery.sumDebits(accountId));
        BigDecimal balance = credits.subtract(debits);

        Instant now = time.now();

        // Audit (optionnel mais utile)
        audit.record(
                "BALANCE_VIEWED",
                accountId,
                Map.of(
                        "credits", credits.toPlainString(),
                        "debits", debits.toPlainString(),
                        "balance", balance.toPlainString(),
                        "currency", "KMF"
                ),
                now
        );

        return new Result(accountId, balance, "KMF", now);
    }

    private BigDecimal defaultZero(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}
