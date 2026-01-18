package com.lipa.domain.model;

import java.time.Duration;
import java.util.Objects;

public record PinPolicy(int maxFails, Duration blockDuration) {

    public PinPolicy {
        if (maxFails <= 0)
            throw new IllegalArgumentException("maxFails must be > 0");

        Objects.requireNonNull(blockDuration, "blockDuration is required");

        if (blockDuration.isZero() || blockDuration.isNegative()) {
            throw new IllegalArgumentException("blockDuration must be > 0");
        }
    }

    public static PinPolicy standard() {
        return new PinPolicy(5, Duration.ofMinutes(15));
    }
}
