package com.lipa.application.port.out;

import java.util.UUID;

public interface AccountLookupPort {

    boolean existsById(UUID accountId);
}
