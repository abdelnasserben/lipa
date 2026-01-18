package com.lipa.application.port.out;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Unified read capability for account existence checks, history queries and ledger-based balance sums.
 * This replaces AccountLookupPort + AccountHistoryQueryPort + LedgerQueryPort.
 */
public interface AccountReadPort extends AccountBalancePort {

    boolean existsById(UUID accountId);

    List<AccountTransactionRow> findAccountTransactions(UUID accountId, int limit, int offset);

    List<LedgerEntryRow> findAccountLedger(UUID accountId, int limit, int offset);

    record AccountTransactionRow(
            UUID transactionId,
            String type,
            String status,
            BigDecimal amount,
            String currency,
            String direction,
            Instant createdAt,
            String description
    ) {}

    record LedgerEntryRow(
            UUID ledgerEntryId,
            UUID transactionId,
            String direction,
            BigDecimal amount,
            Instant createdAt
    ) {}
}
