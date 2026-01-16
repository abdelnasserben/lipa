package com.lipa.infrastructure.adapter.persistence;

import com.lipa.application.dto.FeeConfigurationSnapshot;
import com.lipa.application.port.out.FeeConfigurationPort;
import com.lipa.infrastructure.persistence.jpa.repo.FeeConfigurationJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class FeeConfigurationAdapter implements FeeConfigurationPort {

    private final FeeConfigurationJpaRepository repo;

    public FeeConfigurationAdapter(FeeConfigurationJpaRepository repo) {
        this.repo = repo;
    }

    @Override
    public Optional<FeeConfigurationSnapshot> findActive() {
        return repo.findByActiveTrue()
                .map(e -> new FeeConfigurationSnapshot(
                        e.getPercentage(),
                        e.getMinAmount(),
                        e.getMaxAmount(),
                        e.getCurrency(),
                        e.getUpdatedAt()
                ));
    }
}
