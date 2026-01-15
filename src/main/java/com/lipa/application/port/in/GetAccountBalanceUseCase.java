package com.lipa.application.port.in;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public interface GetAccountBalanceUseCase {

    Result getBalance(UUID accountId);

    record Result(
            UUID accountId,
            BigDecimal balance,
            String currency,
            Instant asOf
    ) {}
}
