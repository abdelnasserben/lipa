package com.lipa.domain.fees;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

public final class FeeConfiguration {

    private final BigDecimal percentage;
    private final BigDecimal minAmount;
    private final BigDecimal maxAmount;
    private final String currency;
    private final Instant updatedAt;

    private FeeConfiguration(BigDecimal percentage,
                             BigDecimal minAmount,
                             BigDecimal maxAmount,
                             String currency,
                             Instant updatedAt) {
        this.percentage = percentage;
        this.minAmount = minAmount;
        this.maxAmount = maxAmount;
        this.currency = currency;
        this.updatedAt = updatedAt;
    }

    public static FeeConfiguration of(BigDecimal percentage,
                                      BigDecimal minAmount,
                                      BigDecimal maxAmount,
                                      String currency,
                                      Instant updatedAt) {

        if (percentage == null || percentage.signum() < 0) {
            throw new FeeConfigurationValidationException("Invalid percentage");
        }

        requirePositive(minAmount, "minAmount");
        requirePositive(maxAmount, "maxAmount");

        if (minAmount.compareTo(maxAmount) > 0) {
            throw new FeeConfigurationValidationException("minAmount must be <= maxAmount");
        }

        if (currency == null) {
            throw new FeeConfigurationValidationException("currency is required");
        }
        String c = currency.trim();
        if (c.isBlank()) {
            throw new FeeConfigurationValidationException("currency is required");
        }
        if (c.length() != 3) {
            throw new FeeConfigurationValidationException("currency must be 3 letters");
        }

        Objects.requireNonNull(updatedAt, "updatedAt is required");

        return new FeeConfiguration(percentage, minAmount, maxAmount, c, updatedAt);
    }

    private static void requirePositive(BigDecimal amount, String fieldName) {
        if (amount == null) {
            throw new FeeConfigurationValidationException(fieldName + " is required");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new FeeConfigurationValidationException(fieldName + " must be > 0");
        }
    }

    public BigDecimal percentage() {
        return percentage;
    }

    public BigDecimal minAmount() {
        return minAmount;
    }

    public BigDecimal maxAmount() {
        return maxAmount;
    }

    public String currency() {
        return currency;
    }

    public Instant updatedAt() {
        return updatedAt;
    }
}
