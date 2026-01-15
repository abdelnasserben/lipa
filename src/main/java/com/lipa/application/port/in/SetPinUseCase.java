package com.lipa.application.port.in;

public interface SetPinUseCase {
    void setPin(Command command);

    record Command(
            String cardUid,
            String rawPin,
            String reason
    ) {}
}