package com.lipa.infrastructure.persistence.repo;

import com.lipa.infrastructure.persistence.entity.TransactionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.UUID;

public interface TransactionSearchJpaRepository extends Repository<TransactionEntity, UUID> {

    @Query(
            value = """
            SELECT t.*
            FROM transaction t
            WHERE
              (CAST(:type AS varchar) IS NULL OR t.type = CAST(:type AS varchar))
              AND (CAST(:status AS varchar) IS NULL OR t.status = CAST(:status AS varchar))
              AND (CAST(:idempotencyKey AS varchar) IS NULL OR t.idempotency_key = CAST(:idempotencyKey AS varchar))
              AND (CAST(:from AS timestamptz) IS NULL OR t.created_at >= CAST(:from AS timestamptz))
              AND (CAST(:to   AS timestamptz) IS NULL OR t.created_at <= CAST(:to   AS timestamptz))
              AND (
                CAST(:accountId AS uuid) IS NULL OR EXISTS (
                  SELECT 1
                  FROM ledger_entry le
                  WHERE le.transaction_id = t.id
                    AND le.account_id = CAST(:accountId AS uuid)
                )
              )
            ORDER BY t.created_at DESC
            """,
            countQuery = """
            SELECT COUNT(*)
            FROM transaction t
            WHERE
              (CAST(:type AS varchar) IS NULL OR t.type = CAST(:type AS varchar))
              AND (CAST(:status AS varchar) IS NULL OR t.status = CAST(:status AS varchar))
              AND (CAST(:idempotencyKey AS varchar) IS NULL OR t.idempotency_key = CAST(:idempotencyKey AS varchar))
              AND (CAST(:from AS timestamptz) IS NULL OR t.created_at >= CAST(:from AS timestamptz))
              AND (CAST(:to   AS timestamptz) IS NULL OR t.created_at <= CAST(:to   AS timestamptz))
              AND (
                CAST(:accountId AS uuid) IS NULL OR EXISTS (
                  SELECT 1
                  FROM ledger_entry le
                  WHERE le.transaction_id = t.id
                    AND le.account_id = CAST(:accountId AS uuid)
                )
              )
            """,
            nativeQuery = true
    )
    Page<TransactionEntity> search(
            @Param("accountId") UUID accountId,
            @Param("type") String type,
            @Param("status") String status,
            @Param("idempotencyKey") String idempotencyKey,
            @Param("from") Instant from,
            @Param("to") Instant to,
            Pageable pageable
    );
}
