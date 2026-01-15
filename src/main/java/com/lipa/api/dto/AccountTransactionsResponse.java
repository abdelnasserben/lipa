package com.lipa.api.dto;

import java.util.List;
import java.util.UUID;

public record AccountTransactionsResponse(
        UUID accountId,
        int limit,
        int offset,
        int returned,
        int nextOffset,
        List<AccountTransactionItem> items
) {}
