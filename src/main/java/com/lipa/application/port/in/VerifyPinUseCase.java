package com.lipa.application.port.in;

public interface VerifyPinUseCase {
    Result verify(Command command);

    record Command(
            String cardUid,
            String rawPin
    ) {}

    record Result(
            boolean success,
            boolean cardBlocked
    ) {}
}
