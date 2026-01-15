package com.lipa.api.dto;

import java.util.UUID;

public record CreatePaymentResponse(
        UUID transactionId,
        String status
) {}
