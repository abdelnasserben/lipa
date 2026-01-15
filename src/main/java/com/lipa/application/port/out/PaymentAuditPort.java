package com.lipa.application.port.out;

import com.lipa.application.dto.PaymentAuditCommand;

public interface PaymentAuditPort {

    void record(PaymentAuditCommand command);
}
