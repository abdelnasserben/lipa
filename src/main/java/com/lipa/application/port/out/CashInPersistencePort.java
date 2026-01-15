package com.lipa.application.port.out;

import com.lipa.application.dto.CashInPersistCommand;
import com.lipa.application.dto.CashInPersistResult;

/**
 * Persists a cash-in operation (transaction + ledger entries + audit) in the underlying storage.
 */
public interface CashInPersistencePort {

    CashInPersistResult persist(CashInPersistCommand command);
}
