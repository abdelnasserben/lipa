package com.lipa.application.usecase;

import com.lipa.application.exception.BusinessRuleException;
import com.lipa.application.exception.NotFoundException;
import com.lipa.application.port.in.ListAccountLedgerUseCase;
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
class ListAccountLedgerServiceTest {

    @Mock AccountReadPort accounts;

    private ListAccountLedgerService service;

    @BeforeEach
    void setUp() {
        service = new ListAccountLedgerService(accounts);
    }

    @Test
    void list_requires_accountId() {
        var ex = assertThrows(BusinessRuleException.class, () -> service.list(null, 50, 0));
        assertEquals("accountId is required", ex.getMessage());
    }

    @Test
    void list_throws_not_found_when_account_missing() {
        UUID id = UUID.randomUUID();
        when(accounts.existsById(id)).thenReturn(false);

        var ex = assertThrows(NotFoundException.class, () -> service.list(id, 50, 0));
        assertEquals("Account not found id=" + id, ex.getMessage());
    }

    @Test
    void list_returns_items_and_next_offset() {
        UUID accountId = UUID.randomUUID();
        when(accounts.existsById(accountId)).thenReturn(true);

        var rows = List.of(
                new AccountReadPort.LedgerEntryRow(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        "DEBIT",
                        new BigDecimal("100.00"),
                        Instant.parse("2026-01-18T09:00:00Z")
                )
        );

        when(accounts.findAccountLedger(accountId, 50, 0)).thenReturn(rows);

        ListAccountLedgerUseCase.Result res = service.list(accountId, 50, 0);

        assertEquals(accountId, res.accountId());
        assertEquals(50, res.limit());
        assertEquals(0, res.offset());
        assertEquals(1, res.returned());
        assertEquals(1, res.nextOffset());
        assertEquals(1, res.items().size());
    }
}
