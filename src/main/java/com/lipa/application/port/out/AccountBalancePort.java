package com.lipa.application.port.out;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Generic port for balance computation based on ledger sums.
 * Implemented by existing ledger query ports via interface inheritance.
 */
public interface AccountBalancePort {

    BigDecimal sumCredits(UUID accountId);

    BigDecimal sumDebits(UUID accountId);
}
