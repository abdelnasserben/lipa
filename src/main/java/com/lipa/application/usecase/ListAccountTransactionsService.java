package com.lipa.application.usecase;

import com.lipa.application.dto.PageRequest;
import com.lipa.application.dto.PageResult;
import com.lipa.application.exception.BusinessRuleException;
import com.lipa.application.exception.NotFoundException;
import com.lipa.application.port.in.ListAccountTransactionsUseCase;
import com.lipa.application.port.out.AccountHistoryQueryPort;
import com.lipa.application.port.out.AccountLookupPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ListAccountTransactionsService implements ListAccountTransactionsUseCase {

    private final AccountLookupPort accountLookup;
    private final AccountHistoryQueryPort historyQuery;

    public ListAccountTransactionsService(AccountLookupPort accountLookup,
                                          AccountHistoryQueryPort historyQuery) {
        this.accountLookup = accountLookup;
        this.historyQuery = historyQuery;
    }

    @Override
    @Transactional(readOnly = true)
    public Result list(UUID accountId, int limit, int offset) {
        if (accountId == null) {
            throw new BusinessRuleException("accountId is required");
        }

        if (!accountLookup.existsById(accountId)) {
            throw new NotFoundException("Account not found id=" + accountId);
        }

        PageRequest page = PageRequest.of(limit, offset);

        var rows = historyQuery.findAccountTransactions(
                accountId,
                page.limit(),
                page.offset()
        );

        var items = rows.stream()
                .map(r -> new Item(
                        r.transactionId(),
                        r.type(),
                        r.status(),
                        r.amount(),
                        r.currency(),
                        r.direction(),
                        r.createdAt(),
                        r.description()
                ))
                .toList();

        PageResult<Item> pageResult = PageResult.of(page, items);

        return new Result(
                accountId,
                pageResult.limit(),
                pageResult.offset(),
                pageResult.returned(),
                pageResult.nextOffset(),
                pageResult.items()
        );
    }
}
