package com.lipa.application.usecase;

import com.lipa.api.dto.BackofficeTransactionItem;
import com.lipa.api.dto.BackofficeTransactionSearchResponse;
import com.lipa.infrastructure.persistence.jpa.entity.TransactionEntity;
import com.lipa.infrastructure.persistence.jpa.repo.TransactionSearchJpaRepository;
import com.lipa.infrastructure.persistence.jpa.support.OffsetBasedPageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class SearchTransactionsBackofficeService {

    private final TransactionSearchJpaRepository repo;

    public SearchTransactionsBackofficeService(TransactionSearchJpaRepository repo) {
        this.repo = repo;
    }

    public BackofficeTransactionSearchResponse search(
            UUID accountId,
            String type,
            String status,
            String idempotencyKey,
            Instant from,
            Instant to,
            int limit,
            int offset
    ) {
        // Tri: les plus récentes d'abord
        var pageable = new OffsetBasedPageRequest(
                offset,
                limit,
                Sort.unsorted());

        var page = repo.search(accountId, type, status, idempotencyKey, from, to, pageable);

        var items = page.getContent().stream()
                .map(this::toItem)
                .toList();

        return new BackofficeTransactionSearchResponse(limit, offset, page.getTotalElements(), items);
    }

    private BackofficeTransactionItem toItem(TransactionEntity t) {
        return new BackofficeTransactionItem(
                t.getId(),
                t.getType().name(),
                t.getStatus().name(),
                t.getAmount(),
                t.getCurrency(),
                t.getIdempotencyKey(),
                t.getDescription(),
                t.getCreatedAt()
        );
    }
}
