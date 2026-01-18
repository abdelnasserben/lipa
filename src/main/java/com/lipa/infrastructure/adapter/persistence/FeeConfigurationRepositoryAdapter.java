package com.lipa.infrastructure.adapter.persistence;

import com.lipa.application.port.out.FeeConfigurationRepositoryPort;
import com.lipa.domain.fees.FeeConfiguration;
import com.lipa.infrastructure.persistence.entity.FeeConfigurationEntity;
import com.lipa.infrastructure.persistence.repo.FeeConfigurationJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Component
public class FeeConfigurationRepositoryAdapter implements FeeConfigurationRepositoryPort {

    private final FeeConfigurationJpaRepository repo;

    public FeeConfigurationRepositoryAdapter(FeeConfigurationJpaRepository repo) {
        this.repo = repo;
    }

    @Override
    public Optional<FeeConfiguration> findActive() {
        return repo.findByActiveTrue()
                .map(e -> FeeConfiguration.of(
                        e.getPercentage(),
                        e.getMinAmount(),
                        e.getMaxAmount(),
                        e.getCurrency(),
                        e.getUpdatedAt()
                ));
    }

    @Override
    @Transactional
    public void upsertActive(FeeConfiguration configuration) {
        FeeConfigurationEntity active = repo.findByActiveTrue().orElse(null);

        if (active == null) {
            FeeConfigurationEntity e = new FeeConfigurationEntity();
            e.setId(UUID.randomUUID());
            e.setActive(true);
            apply(configuration, e);
            repo.save(e);
            return;
        }

        apply(configuration, active);
        repo.save(active);
    }

    private static void apply(FeeConfiguration cfg, FeeConfigurationEntity e) {
        e.setPercentage(cfg.percentage());
        e.setMinAmount(cfg.minAmount());
        e.setMaxAmount(cfg.maxAmount());
        e.setCurrency(cfg.currency());
        e.setUpdatedAt(cfg.updatedAt());
    }
}
