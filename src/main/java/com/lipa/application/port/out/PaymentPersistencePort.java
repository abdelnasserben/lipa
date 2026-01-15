package com.lipa.application.port.out;

import com.lipa.application.dto.PaymentPersistCommand;
import com.lipa.application.dto.PaymentPersistResult;

public interface PaymentPersistencePort {

    PaymentPersistResult persist(PaymentPersistCommand command);
}
