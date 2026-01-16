package com.lipa.application.util;

import com.lipa.application.exception.BusinessRuleException;

public final class DomainRules {

    private DomainRules() {
    }

    public static void requireStatusActive(String entityLabel, String status) {
        if (!"ACTIVE".equalsIgnoreCase(status)) {
            throw new BusinessRuleException(entityLabel + " is not active");
        }
    }

    public static void requireType(String entityLabel, String actualType, String expectedType) {
        if (!expectedType.equalsIgnoreCase(actualType)) {
            throw new BusinessRuleException(entityLabel + " must be " + expectedType);
        }
    }

    public static void requireNotNull(Object value, String message) {
        if (value == null) {
            throw new BusinessRuleException(message);
        }
    }
}
