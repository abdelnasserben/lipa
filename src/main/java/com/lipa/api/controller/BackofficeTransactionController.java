package com.lipa.api.controller;

import com.lipa.api.dto.BackofficeTransactionSearchResponse;
import com.lipa.application.usecase.SearchTransactionsBackofficeService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/backoffice/transactions")
public class BackofficeTransactionController {

    private final SearchTransactionsBackofficeService service;

    public BackofficeTransactionController(SearchTransactionsBackofficeService service) {
        this.service = service;
    }

    @GetMapping
    public BackofficeTransactionSearchResponse search(
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

        return service.search(accountId, type, status, idempotencyKey, from, to, limit, offset);
    }
}
