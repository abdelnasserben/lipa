package com.lipa.application.usecase;

import com.lipa.application.exception.NotFoundException;
import com.lipa.application.port.out.AccountReadPort;
import com.lipa.application.port.out.TimeProviderPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetAccountBalanceServiceTest {

    @Mock AccountReadPort accounts;
    @Mock TimeProviderPort time;

    private GetAccountBalanceService service;

    @BeforeEach
    void setUp() {
        service = new GetAccountBalanceService(accounts, time);
    }

    @Test
    void getBalance_throws_not_found_when_id_is_null() {
        var ex = assertThrows(NotFoundException.class, () -> service.getBalance(null));
        assertEquals("Account not found id=null", ex.getMessage());
    }

    @Test
    void getBalance_throws_not_found_when_account_does_not_exist() {
        UUID id = UUID.randomUUID();
        when(accounts.existsById(id)).thenReturn(false);

        var ex = assertThrows(NotFoundException.class, () -> service.getBalance(id));
        assertEquals("Account not found id=" + id, ex.getMessage());
    }

    @Test
    void getBalance_returns_balance_from_ledger_sums() {
        UUID id = UUID.randomUUID();
        when(accounts.existsById(id)).thenReturn(true);
        when(accounts.sumCredits(id)).thenReturn(new BigDecimal("1500.00"));
        when(accounts.sumDebits(id)).thenReturn(new BigDecimal("400.00"));
        Instant now = Instant.parse("2026-01-18T10:00:00Z");
        when(time.now()).thenReturn(now);

        var res = service.getBalance(id);

        assertEquals(id, res.accountId());
        assertEquals(new BigDecimal("1100.00"), res.balance());
        assertEquals("KMF", res.currency());
        assertEquals(now, res.asOf());
    }
}
