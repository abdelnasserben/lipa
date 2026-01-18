package com.lipa.infrastructure.adapter.persistence;

import com.lipa.application.exception.BusinessRuleException;
import com.lipa.application.util.FeeCalculator;
import com.lipa.infrastructure.persistence.jpa.entity.AccountEntity;
import com.lipa.infrastructure.persistence.jpa.repo.AccountJpaRepository;
import com.lipa.infrastructure.persistence.jpa.repo.FeeConfigurationJpaRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class PlatformFeeEngineAdapter {

    private static final String PLATFORM_FEES_DISPLAY_NAME = "Platform Fees";

    private final AccountJpaRepository accountRepo;
    private final FeeConfigurationJpaRepository feeRepo;

    public PlatformFeeEngineAdapter(AccountJpaRepository accountRepo,
                                    FeeConfigurationJpaRepository feeRepo) {
        this.accountRepo = accountRepo;
        this.feeRepo = feeRepo;
    }

    public FeeComputation compute(BigDecimal amount) {
        if (amount == null) {
            throw new BusinessRuleException("amount is required");
        }

        var fee = feeRepo.findByActiveTrue()
                .orElseThrow(() -> new BusinessRuleException("Fee configuration is not set"));

        BigDecimal feeAmount = FeeCalculator.calculate(
                amount,
                fee.getPercentage(),
                fee.getMinAmount(),
                fee.getMaxAmount()
        );

        var feeAccount = accountRepo
                .findByTypeAndDisplayName(AccountEntity.AccountType.TECHNICAL, PLATFORM_FEES_DISPLAY_NAME)
                .orElseThrow(() -> new BusinessRuleException("Platform fee account is not configured"));

        return new FeeComputation(feeAmount, feeAccount);
    }

    public record FeeComputation(BigDecimal feeAmount, AccountEntity feeAccount) {
    }
}
