package com.lipa.application.port.out;

import com.lipa.infrastructure.persistence.entity.LedgerEntryEntity;

public interface LedgerEntryRepositoryPort {
    LedgerEntryEntity save(LedgerEntryEntity entity);
}
