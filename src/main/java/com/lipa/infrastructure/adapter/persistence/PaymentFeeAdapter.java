package com.lipa.infrastructure.adapter.persistence;

import com.lipa.application.port.out.PaymentFeePort;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class PaymentFeeAdapter implements PaymentFeePort {

    private final PlatformFeeEngineAdapter feeEngine;

    public PaymentFeeAdapter(PlatformFeeEngineAdapter feeEngine) {
        this.feeEngine = feeEngine;
    }

    @Override
    public FeeQuote quote(BigDecimal amount) {
        var computed = feeEngine.compute(amount);
        return new FeeQuote(computed.feeAmount());
    }
}
