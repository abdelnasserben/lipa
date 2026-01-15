package com.lipa.application.port.in;

import java.math.BigDecimal;
import java.util.UUID;

public interface CreatePaymentUseCase {

    Result create(Command command);

    record Command(
            String cardUid,
            String pin,
            UUID merchantAccountId,
            BigDecimal amount,
            String currency,
            String idempotencyKey,
            String description
    ) {}

    record Result(
            UUID transactionId,
            String status
    ) {}
}
