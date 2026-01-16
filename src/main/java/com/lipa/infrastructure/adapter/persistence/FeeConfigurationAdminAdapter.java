package com.lipa.infrastructure.adapter.persistence;

import com.lipa.application.port.out.FeeConfigurationAdminPort;
import com.lipa.infrastructure.persistence.jpa.entity.FeeConfigurationEntity;
import com.lipa.infrastructure.persistence.jpa.repo.FeeConfigurationJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Component
public class FeeConfigurationAdminAdapter implements FeeConfigurationAdminPort {

    private final FeeConfigurationJpaRepository repo;

    public FeeConfigurationAdminAdapter(FeeConfigurationJpaRepository repo) {
        this.repo = repo;
    }

    @Override
    @Transactional
    public void updateActive(BigDecimal percentage,
                             BigDecimal minAmount,
                             BigDecimal maxAmount,
                             String currency,
                             Instant updatedAt) {

        FeeConfigurationEntity active = repo.findByActiveTrue().orElse(null);

        if (active == null) {
            // First time setup (no active config)
            FeeConfigurationEntity e = new FeeConfigurationEntity();
            e.setId(UUID.randomUUID());
            e.setPercentage(percentage);
            e.setMinAmount(minAmount);
            e.setMaxAmount(maxAmount);
            e.setCurrency(currency);
            e.setActive(true);
            e.setUpdatedAt(updatedAt);
            repo.save(e);
            return;
        }

        // Update existing active row (no unique constraint issue)
        active.setPercentage(percentage);
        active.setMinAmount(minAmount);
        active.setMaxAmount(maxAmount);
        active.setCurrency(currency);
        active.setUpdatedAt(updatedAt);

        repo.save(active);
    }
}
