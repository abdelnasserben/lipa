package com.lipa.application.usecase;

import com.lipa.application.exception.BusinessRuleException;
import com.lipa.application.port.in.UpdateFeeConfigurationUseCase;
import com.lipa.application.port.out.FeeConfigurationRepositoryPort;
import com.lipa.application.port.out.TimeProviderPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateFeeConfigurationServiceTest {

    @Mock FeeConfigurationRepositoryPort repo;
    @Mock TimeProviderPort time;

    @Captor ArgumentCaptor<com.lipa.domain.fees.FeeConfiguration> cfgCaptor;

    private UpdateFeeConfigurationService service;

    @BeforeEach
    void setUp() {
        service = new UpdateFeeConfigurationService(repo, time);
    }

    @Test
    void update_rejects_negative_percentage() {
        when(time.now()).thenReturn(Instant.parse("2026-01-17T10:00:00Z"));

        var ex = assertThrows(BusinessRuleException.class, () ->
                service.update(new UpdateFeeConfigurationUseCase.Command(
                        new BigDecimal("-0.01"),
                        new BigDecimal("1.00"),
                        new BigDecimal("2.00"),
                        "KMF"
                ))
        );

        assertEquals("Invalid percentage", ex.getMessage());
        verify(repo, never()).upsertActive(any());
    }

    @Test
    void update_rejects_min_greater_than_max() {
        when(time.now()).thenReturn(Instant.parse("2026-01-17T10:00:00Z"));

        var ex = assertThrows(BusinessRuleException.class, () ->
                service.update(new UpdateFeeConfigurationUseCase.Command(
                        new BigDecimal("1.00"),
                        new BigDecimal("10.00"),
                        new BigDecimal("2.00"),
                        "KMF"
                ))
        );

        assertEquals("minAmount must be <= maxAmount", ex.getMessage());
        verify(repo, never()).upsertActive(any());
    }

    @Test
    void update_persists_configuration_and_returns_updated() {
        Instant now = Instant.parse("2026-01-17T10:00:00Z");
        when(time.now()).thenReturn(now);

        var res = service.update(new UpdateFeeConfigurationUseCase.Command(
                new BigDecimal("2.50"),
                new BigDecimal("100.00"),
                new BigDecimal("500.00"),
                "KMF"
        ));

        assertEquals(new BigDecimal("2.50"), res.percentage());
        assertEquals(new BigDecimal("100.00"), res.minAmount());
        assertEquals(new BigDecimal("500.00"), res.maxAmount());
        assertEquals("KMF", res.currency());
        assertEquals(now, res.updatedAt());

        verify(repo).upsertActive(cfgCaptor.capture());
        var cfg = cfgCaptor.getValue();
        assertEquals(new BigDecimal("2.50"), cfg.percentage());
        assertEquals(new BigDecimal("100.00"), cfg.minAmount());
        assertEquals(new BigDecimal("500.00"), cfg.maxAmount());
        assertEquals("KMF", cfg.currency());
        assertEquals(now, cfg.updatedAt());
    }
}
