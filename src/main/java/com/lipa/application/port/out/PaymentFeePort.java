package com.lipa.application.port.out;

import java.math.BigDecimal;

public interface PaymentFeePort {

    FeeQuote quote(BigDecimal amount);

    record FeeQuote(BigDecimal feeAmount) {
    }
}
