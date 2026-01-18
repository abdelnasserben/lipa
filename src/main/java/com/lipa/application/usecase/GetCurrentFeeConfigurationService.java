package com.lipa.application.usecase;

import com.lipa.application.exception.NotFoundException;
import com.lipa.application.port.in.GetCurrentFeeConfigurationUseCase;
import com.lipa.application.port.out.FeeConfigurationRepositoryPort;
import org.springframework.stereotype.Service;

@Service
public class GetCurrentFeeConfigurationService implements GetCurrentFeeConfigurationUseCase {

    private final FeeConfigurationRepositoryPort repo;

    public GetCurrentFeeConfigurationService(FeeConfigurationRepositoryPort repo) {
        this.repo = repo;
    }

    @Override
    public Result get() {
        var cfg = repo.findActive()
                .orElseThrow(() -> new NotFoundException("Fee configuration not found"));

        return new Result(
                cfg.percentage(),
                cfg.minAmount(),
                cfg.maxAmount(),
                cfg.currency(),
                cfg.updatedAt()
        );
    }
}
