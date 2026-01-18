package com.lipa.application.port.out;

import com.lipa.domain.fees.FeeConfiguration;

import java.util.Optional;

public interface FeeConfigurationRepositoryPort {

    Optional<FeeConfiguration> findActive();

    void upsertActive(FeeConfiguration configuration);
}
