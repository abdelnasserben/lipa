package com.lipa.application.port.out;

import com.lipa.application.dto.AccountSnapshot;

import java.util.Optional;
import java.util.UUID;

public interface PaymentAccountPort {

    Optional<AccountSnapshot> findById(UUID accountId);

    Optional<AccountSnapshot> findByIdForUpdate(UUID accountId);
}
