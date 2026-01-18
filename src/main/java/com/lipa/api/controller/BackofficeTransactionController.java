package com.lipa.api.controller;

import com.lipa.application.dto.BackofficeTransactionSearchCriteria;
import com.lipa.application.dto.BackofficeTransactionSearchResult;
import com.lipa.application.port.in.SearchTransactionsBackofficeUseCase;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/backoffice/transactions")
public class BackofficeTransactionController {

    private final SearchTransactionsBackofficeUseCase useCase;

    public BackofficeTransactionController(SearchTransactionsBackofficeUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping
    public BackofficeTransactionSearchResult search(
            @RequestParam(required = false) UUID accountId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String idempotencyKey,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") int offset
    ) {
        // garde-fous simples (MVP)
        if (limit < 1) limit = 1;
        if (limit > 200) limit = 200;
        if (offset < 0) offset = 0;

        var criteria = new BackofficeTransactionSearchCriteria(accountId, type, status, idempotencyKey, from, to);
        return useCase.search(criteria, limit, offset);
    }
}
