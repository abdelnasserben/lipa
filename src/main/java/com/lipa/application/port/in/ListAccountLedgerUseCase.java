package com.lipa.application.port.in;

import java.util.List;
import java.util.UUID;

public interface ListAccountLedgerUseCase {

    Result list(UUID accountId, int limit, int offset);

    record Result(
            UUID accountId,
            int limit,
            int offset,
            int returned,
            int nextOffset,
            List<Item> items
    ) {}

    record Item(
            UUID ledgerEntryId,
            UUID transactionId,
            String direction,
            java.math.BigDecimal amount,
            java.time.Instant createdAt
    ) {}
}
