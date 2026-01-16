package com.lipa.application.util;

import com.lipa.application.exception.BusinessRuleException;

public final class InputRules {

    private InputRules() {
    }

    public static String requireTrimmedNotBlank(String value, String fieldName) {
        if (value == null) {
            throw new BusinessRuleException(fieldName + " is required");
        }
        String v = value.trim();
        if (v.isEmpty()) {
            throw new BusinessRuleException(fieldName + " is required");
        }
        return v;
    }

    public static String trimToNull(String value) {
        if (value == null) return null;
        String v = value.trim();
        return v.isEmpty() ? null : v;
    }
}
