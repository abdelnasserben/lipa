package com.lipa.application.port.out;

import com.lipa.application.dto.FeeConfigurationSnapshot;

import java.util.Optional;

public interface FeeConfigurationPort {
    Optional<FeeConfigurationSnapshot> findActive();
}
