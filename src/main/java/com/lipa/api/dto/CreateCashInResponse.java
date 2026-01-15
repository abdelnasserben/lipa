package com.lipa.api.dto;

import java.util.UUID;

public record CreateCashInResponse(
        UUID transactionId,
        String status
) {}
