package com.lipa.api.dto;

import java.util.UUID;

public record EnrollCardResponse(
        UUID accountId,
        UUID cardId,
        String cardUid
) {}
