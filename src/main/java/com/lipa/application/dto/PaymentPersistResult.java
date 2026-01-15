package com.lipa.application.dto;

import java.util.UUID;

public record PaymentPersistResult(
        UUID transactionId,
        String status
) {
}
