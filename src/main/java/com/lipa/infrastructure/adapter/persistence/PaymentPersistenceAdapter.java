package com.lipa.infrastructure.adapter.persistence;

import com.lipa.application.dto.PaymentPersistCommand;
import com.lipa.application.dto.PaymentPersistResult;
import com.lipa.application.port.out.PaymentPersistencePort;
import com.lipa.application.util.FeeCalculator;
import com.lipa.infrastructure.persistence.jpa.entity.AccountEntity;
import com.lipa.infrastructure.persistence.jpa.entity.LedgerEntryEntity;
import com.lipa.infrastructure.persistence.jpa.entity.TransactionEntity;
import com.lipa.infrastructure.persistence.jpa.repo.AccountJpaRepository;
import com.lipa.infrastructure.persistence.jpa.repo.FeeConfigurationJpaRepository;
import com.lipa.infrastructure.persistence.jpa.repo.LedgerEntryJpaRepository;
import com.lipa.infrastructure.persistence.jpa.repo.TransactionJpaRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class PaymentPersistenceAdapter implements PaymentPersistencePort {

    private static final String PLATFORM_FEES_DISPLAY_NAME = "Platform Fees";

    private final AccountJpaRepository accountRepo;
    private final TransactionJpaRepository transactionRepo;
    private final LedgerEntryJpaRepository ledgerRepo;
    private final FeeConfigurationJpaRepository feeRepo;

    public PaymentPersistenceAdapter(AccountJpaRepository accountRepo,
                                     TransactionJpaRepository transactionRepo,
                                     LedgerEntryJpaRepository ledgerRepo,
                                     FeeConfigurationJpaRepository feeRepo) {
        this.accountRepo = accountRepo;
        this.transactionRepo = transactionRepo;
        this.ledgerRepo = ledgerRepo;
        this.feeRepo = feeRepo;
    }

    @Override
    public PaymentPersistResult persist(PaymentPersistCommand command) {

        var payer = accountRepo.findById(command.payerAccountId())
                .orElseThrow(() -> new IllegalStateException("Payer account missing id=" + command.payerAccountId()));

        var merchant = accountRepo.findById(command.merchantAccountId())
                .orElseThrow(() -> new IllegalStateException("Merchant account missing id=" + command.merchantAccountId()));

        // 1) Load active fee config
        var fee = feeRepo.findByActiveTrue()
                .orElseThrow(() -> new IllegalStateException("Active fee missing"));

        // 2) Fee amount (percentage + min/max)
        BigDecimal feeAmount = FeeCalculator.calculate(
                command.amount(),
                fee.getPercentage(),
                fee.getMinAmount(),
                fee.getMaxAmount()
        );

        // 3) Load fee technical account (TECHNICAL + displayName = "Platform Fees")
        var feeAccount = accountRepo.findByTypeAndDisplayName(AccountEntity.AccountType.TECHNICAL, PLATFORM_FEES_DISPLAY_NAME)
                .orElseThrow(() -> new IllegalStateException("Fee account missing type=TECHNICAL displayName=" + PLATFORM_FEES_DISPLAY_NAME));

        // Transaction (keeps amount = payment amount, not total)
        TransactionEntity txn = new TransactionEntity();
        txn.setId(UUID.randomUUID());
        txn.setType(TransactionEntity.TransactionType.PAYMENT);
        txn.setStatus(TransactionEntity.TransactionStatus.SUCCESS);
        txn.setAmount(command.amount());
        txn.setCurrency(command.currency());
        txn.setIdempotencyKey(command.idempotencyKey());
        txn.setDescription(command.description());
        txn.setCreatedAt(command.createdAt());

        txn = transactionRepo.save(txn);

        // Ledger: payer DEBIT (amount + fee)
        LedgerEntryEntity debit = new LedgerEntryEntity();
        debit.setId(UUID.randomUUID());
        debit.setTransaction(txn);
        debit.setAccount(payer);
        debit.setDirection(LedgerEntryEntity.Direction.DEBIT);
        debit.setAmount(command.amount().add(feeAmount));
        debit.setCreatedAt(command.createdAt());
        ledgerRepo.save(debit);

        // Ledger: merchant CREDIT (amount)
        LedgerEntryEntity creditMerchant = new LedgerEntryEntity();
        creditMerchant.setId(UUID.randomUUID());
        creditMerchant.setTransaction(txn);
        creditMerchant.setAccount(merchant);
        creditMerchant.setDirection(LedgerEntryEntity.Direction.CREDIT);
        creditMerchant.setAmount(command.amount());
        creditMerchant.setCreatedAt(command.createdAt());
        ledgerRepo.save(creditMerchant);

        // Ledger: platform fees CREDIT (fee)
        LedgerEntryEntity creditFee = new LedgerEntryEntity();
        creditFee.setId(UUID.randomUUID());
        creditFee.setTransaction(txn);
        creditFee.setAccount(feeAccount);
        creditFee.setDirection(LedgerEntryEntity.Direction.CREDIT);
        creditFee.setAmount(feeAmount);
        creditFee.setCreatedAt(command.createdAt());
        ledgerRepo.save(creditFee);

        return new PaymentPersistResult(txn.getId(), txn.getStatus().name());
    }
}
