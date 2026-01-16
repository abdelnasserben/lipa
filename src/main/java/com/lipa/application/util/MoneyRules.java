package com.lipa.application.util;

import com.lipa.application.exception.BusinessRuleException;

import java.math.BigDecimal;

public final class MoneyRules {

    private MoneyRules() {
    }

    public static BigDecimal zeroIfNull(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    public static void requirePositive(BigDecimal amount, String fieldName) {
        if (amount == null) {
            throw new BusinessRuleException(fieldName + " is required");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException(fieldName + " must be > 0");
        }
    }

    public static String normalizeCurrency(String currency) {
        if (currency == null) {
            throw new BusinessRuleException("currency is required");
        }
        String c = currency.trim();
        if (c.length() != 3) {
            throw new BusinessRuleException("currency must be 3 letters");
        }
        return c.toUpperCase();
    }
}
