package com.lipa.application.usecase;

import com.lipa.application.exception.NotFoundException;
import com.lipa.application.port.out.FeeConfigurationRepositoryPort;
import com.lipa.domain.fees.FeeConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetCurrentFeeConfigurationServiceTest {

    @Mock FeeConfigurationRepositoryPort repo;

    private GetCurrentFeeConfigurationService service;

    @BeforeEach
    void setUp() {
        service = new GetCurrentFeeConfigurationService(repo);
    }

    @Test
    void get_throws_not_found_when_no_active_configuration() {
        when(repo.findActive()).thenReturn(Optional.empty());

        var ex = assertThrows(NotFoundException.class, () -> service.get());
        assertEquals("Fee configuration not found", ex.getMessage());
    }

    @Test
    void get_returns_current_configuration() {
        Instant now = Instant.parse("2026-01-17T10:00:00Z");

        FeeConfiguration cfg = FeeConfiguration.of(
                new BigDecimal("2.50"),
                new BigDecimal("100.00"),
                new BigDecimal("500.00"),
                "KMF",
                now
        );

        when(repo.findActive()).thenReturn(Optional.of(cfg));

        var res = service.get();

        assertEquals(new BigDecimal("2.50"), res.percentage());
        assertEquals(new BigDecimal("100.00"), res.minAmount());
        assertEquals(new BigDecimal("500.00"), res.maxAmount());
        assertEquals("KMF", res.currency());
        assertEquals(now, res.updatedAt());
    }
}
