package com.lipa.infrastructure.persistence.jpa.repo;

import com.lipa.infrastructure.persistence.jpa.entity.LedgerEntryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.UUID;

public interface LedgerEntryJpaRepository extends JpaRepository<LedgerEntryEntity, UUID> {
    @Query("""
           select coalesce(sum(le.amount), 0)
           from LedgerEntryEntity le
           where le.account.id = :accountId
             and le.direction = com.lipa.infrastructure.persistence.jpa.entity.LedgerEntryEntity$Direction.CREDIT
           """)
    BigDecimal sumCredits(@Param("accountId") UUID accountId);

    @Query("""
           select coalesce(sum(le.amount), 0)
           from LedgerEntryEntity le
           where le.account.id = :accountId
             and le.direction = com.lipa.infrastructure.persistence.jpa.entity.LedgerEntryEntity$Direction.DEBIT
           """)
    BigDecimal sumDebits(@Param("accountId") UUID accountId);
}
