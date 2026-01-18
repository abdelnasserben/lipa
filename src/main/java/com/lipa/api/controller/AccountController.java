package com.lipa.api.controller;

import com.lipa.application.port.in.GetAccountBalanceUseCase;
import com.lipa.application.port.in.ListAccountLedgerUseCase;
import com.lipa.application.port.in.ListAccountTransactionsUseCase;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private final GetAccountBalanceUseCase getBalance;
    private final ListAccountTransactionsUseCase listTransactions;
    private final ListAccountLedgerUseCase listLedger;

    public AccountController(GetAccountBalanceUseCase getBalance,
                             ListAccountTransactionsUseCase listTransactions,
                             ListAccountLedgerUseCase listLedger) {
        this.getBalance = getBalance;
        this.listTransactions = listTransactions;
        this.listLedger = listLedger;
    }

    @GetMapping("/{accountId}/balance")
    public GetAccountBalanceUseCase.Result getBalance(@PathVariable UUID accountId) {
        return getBalance.getBalance(accountId);
    }

    @GetMapping("/{accountId}/transactions")
    public ListAccountTransactionsUseCase.Result transactions(@PathVariable UUID accountId,
                                                              @RequestParam(defaultValue = "20") int limit,
                                                              @RequestParam(defaultValue = "0") int offset) {
        return listTransactions.list(accountId, limit, offset);
    }

    @GetMapping("/{accountId}/ledger")
    public ListAccountLedgerUseCase.Result ledger(@PathVariable UUID accountId,
                                                  @RequestParam(defaultValue = "50") int limit,
                                                  @RequestParam(defaultValue = "0") int offset) {
        return listLedger.list(accountId, limit, offset);
    }
}
