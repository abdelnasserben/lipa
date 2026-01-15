package com.lipa.application.port.out;

import com.lipa.application.dto.AccountSnapshot;

import java.util.Optional;
import java.util.UUID;

/**
 * Read-only access to accounts for the cash-in use case.
 */
public interface CashInAccountPort {

    Optional<AccountSnapshot> findById(UUID accountId);
}
