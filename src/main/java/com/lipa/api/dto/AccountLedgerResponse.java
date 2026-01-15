package com.lipa.api.dto;

import java.util.List;
import java.util.UUID;

public record AccountLedgerResponse(
        UUID accountId,
        int limit,
        int offset,
        int returned,
        int nextOffset,
        List<LedgerEntryItem> items
) {}
