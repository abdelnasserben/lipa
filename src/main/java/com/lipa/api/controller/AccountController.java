package com.lipa.api.controller;

import com.lipa.api.dto.BalanceResponse;
import com.lipa.application.port.in.GetAccountBalanceUseCase;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private final GetAccountBalanceUseCase getBalance;

    public AccountController(GetAccountBalanceUseCase getBalance) {
        this.getBalance = getBalance;
    }

    @GetMapping("/{accountId}/balance")
    public BalanceResponse getBalance(@PathVariable UUID accountId) {
        var result = getBalance.getBalance(accountId);
        return new BalanceResponse(result.accountId(), result.balance(), result.currency(), result.asOf());
    }
}
