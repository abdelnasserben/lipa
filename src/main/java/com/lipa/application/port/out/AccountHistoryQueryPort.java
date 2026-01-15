package com.lipa.application.port.out;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface AccountHistoryQueryPort {

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
