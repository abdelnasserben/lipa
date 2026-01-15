package com.lipa.application.port.out;

public interface PinHasherPort {
    String hash(String rawPin);
    boolean matches(String rawPin, String hashed);
}
