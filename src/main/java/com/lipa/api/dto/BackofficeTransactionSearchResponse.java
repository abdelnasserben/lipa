package com.lipa.api.dto;

import java.util.List;

public record BackofficeTransactionSearchResponse(
        int limit,
        int offset,
        long total,
        List<BackofficeTransactionItem> items
) {}
