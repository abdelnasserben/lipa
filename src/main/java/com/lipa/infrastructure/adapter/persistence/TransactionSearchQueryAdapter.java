package com.lipa.infrastructure.adapter.persistence;

import com.lipa.application.dto.BackofficeTransactionItemDto;
import com.lipa.application.dto.BackofficeTransactionSearchCriteria;
import com.lipa.application.dto.BackofficeTransactionSearchResult;
import com.lipa.application.port.out.TransactionSearchQueryPort;
import com.lipa.infrastructure.persistence.entity.TransactionEntity;
import com.lipa.infrastructure.persistence.repo.TransactionSearchJpaRepository;
import com.lipa.infrastructure.persistence.support.OffsetBasedPageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
public class TransactionSearchQueryAdapter implements TransactionSearchQueryPort {

    private final TransactionSearchJpaRepository repo;

    public TransactionSearchQueryAdapter(TransactionSearchJpaRepository repo) {
        this.repo = repo;
    }

    @Override
    public BackofficeTransactionSearchResult search(
            BackofficeTransactionSearchCriteria criteria,
            int limit,
            int offset
    ) {
        var pageable = new OffsetBasedPageRequest(
                offset,
                limit,
                Sort.by(Sort.Direction.DESC, "created_at")
        );

        var page = repo.search(
                criteria.accountId(),
                criteria.type(),
                criteria.status(),
                criteria.idempotencyKey(),
                criteria.from(),
                criteria.to(),
                pageable
        );

        var items = page.getContent().stream()
                .map(this::toDto)
                .toList();

        return new BackofficeTransactionSearchResult(limit, offset, page.getTotalElements(), items);
    }

    private BackofficeTransactionItemDto toDto(TransactionEntity t) {
        return new BackofficeTransactionItemDto(
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
