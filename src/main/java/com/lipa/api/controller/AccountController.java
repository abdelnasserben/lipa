package com.lipa.api.controller;

import com.lipa.api.dto.*;
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

    public AccountController(GetAccountBalanceUseCase getBalance, ListAccountTransactionsUseCase listTransactions, ListAccountLedgerUseCase listLedger) {
        this.getBalance = getBalance;
        this.listTransactions = listTransactions;
        this.listLedger = listLedger;
    }

    @GetMapping("/{accountId}/balance")
    public BalanceResponse getBalance(@PathVariable UUID accountId) {
        var result = getBalance.getBalance(accountId);
        return new BalanceResponse(result.accountId(), result.balance(), result.currency(), result.asOf());
    }

    @GetMapping("/{accountId}/transactions")
    public AccountTransactionsResponse transactions(@PathVariable UUID accountId,
                                                    @RequestParam(defaultValue = "20") int limit,
                                                    @RequestParam(defaultValue = "0") int offset) {
        var result = listTransactions.list(accountId, limit, offset);

        var items = result.items().stream()
                .map(i -> new AccountTransactionItem(
                        i.transactionId(),
                        i.type(),
                        i.status(),
                        i.amount(),
                        i.currency(),
                        i.direction(),
                        i.createdAt(),
                        i.description()
                ))
                .toList();

        return new AccountTransactionsResponse(
                result.accountId(),
                result.limit(),
                result.offset(),
                result.returned(),
                result.nextOffset(),
                items
        );
    }

    @GetMapping("/{accountId}/ledger")
    public AccountLedgerResponse ledger(@PathVariable UUID accountId,
                                        @RequestParam(defaultValue = "50") int limit,
                                        @RequestParam(defaultValue = "0") int offset) {
        var result = listLedger.list(accountId, limit, offset);

        var items = result.items().stream()
                .map(i -> new LedgerEntryItem(
                        i.ledgerEntryId(),
                        i.transactionId(),
                        i.direction(),
                        i.amount(),
                        i.createdAt()
                ))
                .toList();

        return new AccountLedgerResponse(
                result.accountId(),
                result.limit(),
                result.offset(),
                result.returned(),
                result.nextOffset(),
                items
        );
    }
}
