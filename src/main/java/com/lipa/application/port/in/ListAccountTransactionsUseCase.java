package com.lipa.application.port.in;

import java.util.List;
import java.util.UUID;

public interface ListAccountTransactionsUseCase {

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
            UUID transactionId,
            String type,
            String status,
            java.math.BigDecimal amount,
            String currency,
            String direction,
            java.time.Instant createdAt,
            String description
    ) {}
}
