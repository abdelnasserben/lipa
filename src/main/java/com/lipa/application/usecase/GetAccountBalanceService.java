package com.lipa.application.usecase;

import com.lipa.application.exception.NotFoundException;
import com.lipa.application.port.in.GetAccountBalanceUseCase;
import com.lipa.application.port.out.AccountReadPort;
import com.lipa.application.port.out.TimeProviderPort;
import com.lipa.application.util.BalanceCalculator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
public class GetAccountBalanceService implements GetAccountBalanceUseCase {

    private final AccountReadPort accounts;
    private final TimeProviderPort time;

    public GetAccountBalanceService(AccountReadPort accounts, TimeProviderPort time) {
        this.accounts = accounts;
        this.time = time;
    }

    @Override
    @Transactional(readOnly = true)
    public Result getBalance(UUID accountId) {
        if (accountId == null) {
            throw new NotFoundException("Account not found id=null");
        }

        if (!accounts.existsById(accountId)) {
            throw new NotFoundException("Account not found id=" + accountId);
        }

        BigDecimal balance = BalanceCalculator.balanceOf(accounts, accountId);
        Instant now = time.now();

        return new Result(accountId, balance, "KMF", now);
    }
}
