package com.lipa.infrastructure.adapter.time;

import com.lipa.application.port.out.TimeProviderPort;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class SystemTimeProviderAdapter implements TimeProviderPort {
    @Override
    public Instant now() {
        return Instant.now();
    }
}
