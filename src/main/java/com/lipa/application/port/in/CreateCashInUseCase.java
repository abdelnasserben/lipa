package com.lipa.application.port.in;

import java.math.BigDecimal;
import java.util.UUID;

public interface CreateCashInUseCase {

    Result create(Command command);

    record Command(
            UUID clientAccountId,
            BigDecimal amount,
            String currency,
            String idempotencyKey,
            String description,
            UUID technicalAccountId
    ) {}

    record Result(
            UUID transactionId,
            String status
    ) {}
}
