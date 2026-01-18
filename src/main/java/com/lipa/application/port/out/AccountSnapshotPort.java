package com.lipa.application.port.out;

import com.lipa.application.dto.AccountSnapshot;

import java.util.Optional;
import java.util.UUID;

/**
 * Generic read-only access to lightweight account data used by application use cases.
 * Used by Cash-in and Payments.
 */
public interface AccountSnapshotPort {

    Optional<AccountSnapshot> findById(UUID accountId);

    /**
     * Used when a use case must lock the row to avoid race conditions during balance-changing operations.
     */
    Optional<AccountSnapshot> findByIdForUpdate(UUID accountId);
}
