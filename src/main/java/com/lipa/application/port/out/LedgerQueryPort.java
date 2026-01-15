package com.lipa.application.port.out;

import java.math.BigDecimal;
import java.util.UUID;

public interface LedgerQueryPort {

    BigDecimal sumCredits(UUID accountId);

    BigDecimal sumDebits(UUID accountId);
}
