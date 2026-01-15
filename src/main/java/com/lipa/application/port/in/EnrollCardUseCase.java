package com.lipa.application.port.in;

import java.util.UUID;

public interface EnrollCardUseCase {
    Result enroll(Command command);

    record Command(
            String cardUid,
            String displayName,
            String phone
    ) {}

    record Result(
            UUID accountId,
            UUID cardId,
            String cardUid
    ) {}
}