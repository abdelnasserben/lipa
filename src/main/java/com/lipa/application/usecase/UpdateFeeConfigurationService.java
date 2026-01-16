package com.lipa.application.usecase;

import com.lipa.application.exception.BusinessRuleException;
import com.lipa.application.port.in.UpdateFeeConfigurationUseCase;
import com.lipa.application.port.out.FeeConfigurationAdminPort;
import com.lipa.application.port.out.TimeProviderPort;
import com.lipa.application.util.MoneyRules;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateFeeConfigurationService implements UpdateFeeConfigurationUseCase {

    private final FeeConfigurationAdminPort port;
    private final TimeProviderPort time;

    public UpdateFeeConfigurationService(FeeConfigurationAdminPort port, TimeProviderPort time) {
        this.port = port;
        this.time = time;
    }

    @Override
    @Transactional
    public Result update(Command command) {

        if (command.percentage() == null || command.percentage().signum() < 0) {
            throw new BusinessRuleException("Invalid percentage");
        }

        MoneyRules.requirePositive(command.minAmount(), "minAmount");
        MoneyRules.requirePositive(command.maxAmount(), "maxAmount");

        if (command.minAmount().compareTo(command.maxAmount()) > 0) {
            throw new BusinessRuleException("minAmount must be <= maxAmount");
        }

        var updatedAt = time.now();

        port.updateActive(
                command.percentage(),
                command.minAmount(),
                command.maxAmount(),
                command.currency(),
                updatedAt
        );

        return new Result(
                command.percentage(),
                command.minAmount(),
                command.maxAmount(),
                command.currency(),
                updatedAt
        );
    }
}
