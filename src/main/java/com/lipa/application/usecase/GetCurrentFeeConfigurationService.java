package com.lipa.application.usecase;

import com.lipa.application.exception.NotFoundException;
import com.lipa.application.port.in.GetCurrentFeeConfigurationUseCase;
import com.lipa.application.port.out.FeeConfigurationPort;
import org.springframework.stereotype.Service;

@Service
public class GetCurrentFeeConfigurationService implements GetCurrentFeeConfigurationUseCase {

    private final FeeConfigurationPort port;

    public GetCurrentFeeConfigurationService(FeeConfigurationPort port) {
        this.port = port;
    }

    @Override
    public Result get() {
        var cfg = port.findActive()
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
