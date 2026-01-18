package com.lipa.application.usecase;

import com.lipa.application.exception.BusinessRuleException;
import com.lipa.application.exception.NotFoundException;
import com.lipa.application.port.in.ListAccountTransactionsUseCase;
import com.lipa.application.port.out.AccountReadPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListAccountTransactionsServiceTest {

    @Mock AccountReadPort accounts;

    private ListAccountTransactionsService service;

    @BeforeEach
    void setUp() {
        service = new ListAccountTransactionsService(accounts);
    }

    @Test
    void list_requires_accountId() {
        var ex = assertThrows(BusinessRuleException.class, () -> service.list(null, 20, 0));
        assertEquals("accountId is required", ex.getMessage());
    }

    @Test
    void list_throws_not_found_when_account_missing() {
        UUID id = UUID.randomUUID();
        when(accounts.existsById(id)).thenReturn(false);

        var ex = assertThrows(NotFoundException.class, () -> service.list(id, 20, 0));
        assertEquals("Account not found id=" + id, ex.getMessage());
    }

    @Test
    void list_maps_rows_to_items_and_paginates() {
        UUID accountId = UUID.randomUUID();
        when(accounts.existsById(accountId)).thenReturn(true);

        UUID tx1 = UUID.randomUUID();
        UUID tx2 = UUID.randomUUID();

        var rows = List.of(
                new AccountReadPort.AccountTransactionRow(
                        tx1, "PAYMENT", "SUCCESS", new BigDecimal("100.00"), "KMF", "DEBIT",
                        Instant.parse("2026-01-18T09:00:00Z"), "desc1"
                ),
                new AccountReadPort.AccountTransactionRow(
                        tx2, "CASH_IN", "SUCCESS", new BigDecimal("200.00"), "KMF", "CREDIT",
                        Instant.parse("2026-01-18T08:00:00Z"), "desc2"
                )
        );

        when(accounts.findAccountTransactions(accountId, 20, 0)).thenReturn(rows);

        ListAccountTransactionsUseCase.Result res = service.list(accountId, 20, 0);

        assertEquals(accountId, res.accountId());
        assertEquals(20, res.limit());
        assertEquals(0, res.offset());
        assertEquals(2, res.returned());
        assertEquals(2, res.nextOffset());
        assertEquals(2, res.items().size());
        assertEquals(tx1, res.items().get(0).transactionId());
    }
}
