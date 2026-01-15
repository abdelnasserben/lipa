package com.lipa.application.dto;

import java.util.UUID;

public record EnrollCardPersistResult(
        UUID accountId,
        UUID cardId,
        String cardUid
) {
}
