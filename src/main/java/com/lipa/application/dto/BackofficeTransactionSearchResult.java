package com.lipa.application.dto;

import java.util.List;

public record BackofficeTransactionSearchResult(
        int limit,
        int offset,
        long total,
        List<BackofficeTransactionItemDto> items
) {
}
