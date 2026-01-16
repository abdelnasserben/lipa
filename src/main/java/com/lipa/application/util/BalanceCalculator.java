package com.lipa.application.util;

import com.lipa.application.port.out.AccountBalancePort;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Centralized balance computation: credits - debits.
 */
public final class BalanceCalculator {

    private BalanceCalculator() {
    }

    public static BigDecimal balanceOf(AccountBalancePort port, UUID accountId) {
        BigDecimal credits = MoneyRules.zeroIfNull(port.sumCredits(accountId));
        BigDecimal debits = MoneyRules.zeroIfNull(port.sumDebits(accountId));
        return credits.subtract(debits);
    }
}
