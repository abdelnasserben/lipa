package com.lipa.infrastructure.adapter.security;

import com.lipa.application.port.out.PinHasherPort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BcryptPinHasherAdapter implements PinHasherPort {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    @Override
    public String hash(String rawPin) {
        return encoder.encode(rawPin);
    }

    @Override
    public boolean matches(String rawPin, String hashed) {
        return encoder.matches(rawPin, hashed);
    }
}
