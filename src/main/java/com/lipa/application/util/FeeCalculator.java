package com.lipa.application.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class FeeCalculator {

    private FeeCalculator() {}

    public static BigDecimal calculate(
            BigDecimal amount,
            BigDecimal percentage,
            BigDecimal min,
            BigDecimal max
    ) {
        BigDecimal fee = amount
                .multiply(percentage)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        if (fee.compareTo(min) < 0) {
            return min;
        }
        if (fee.compareTo(max) > 0) {
            return max;
        }
        return fee;
    }
}
