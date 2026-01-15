package com.lipa.application.dto;

import java.util.UUID;

public record CashInPersistResult(
        UUID transactionId,
        String status
) {
}
