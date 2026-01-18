package com.lipa.infrastructure.persistence.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "fee_configuration")
@Getter @Setter
public class FeeConfigurationEntity {

    @Id
    private UUID id;

    private BigDecimal percentage;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private String currency;
    private boolean active;
    private Instant updatedAt;

}
