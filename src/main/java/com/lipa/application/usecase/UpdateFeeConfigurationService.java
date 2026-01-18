package com.lipa.application.usecase;

import com.lipa.application.exception.BusinessRuleException;
import com.lipa.application.port.in.UpdateFeeConfigurationUseCase;
import com.lipa.application.port.out.FeeConfigurationRepositoryPort;
import com.lipa.application.port.out.TimeProviderPort;
import com.lipa.domain.fees.FeeConfiguration;
import com.lipa.domain.fees.FeeConfigurationValidationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateFeeConfigurationService implements UpdateFeeConfigurationUseCase {

    private final FeeConfigurationRepositoryPort repo;
    private final TimeProviderPort time;

    public UpdateFeeConfigurationService(FeeConfigurationRepositoryPort repo, TimeProviderPort time) {
        this.repo = repo;
        this.time = time;
    }

    @Override
    @Transactional
    public Result update(Command command) {
        var updatedAt = time.now();

        final FeeConfiguration cfg;
        try {
            cfg = FeeConfiguration.of(
                    command.percentage(),
                    command.minAmount(),
                    command.maxAmount(),
                    command.currency(),
                    updatedAt
            );
        } catch (FeeConfigurationValidationException ex) {
            // Preserve API behavior: BusinessRuleException -> 400 via ApiExceptionHandler
            throw new BusinessRuleException(ex.getMessage());
        }

        repo.upsertActive(cfg);

        return new Result(
                cfg.percentage(),
                cfg.minAmount(),
                cfg.maxAmount(),
                cfg.currency(),
                cfg.updatedAt()
        );
    }
}
