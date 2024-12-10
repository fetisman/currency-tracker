package com.fetisman.currencymodel.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "currency_aggregates")
public class CurrencyAggregate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "currency_code", nullable = false)
    private String currencyCode;

    @Column(name = "average_buy", nullable = false)
    private Double averageBuy;

    @Column(name = "average_sale", nullable = false)
    private Double averageSale;

    @Column(name = "calculated_at", nullable = false)
    private LocalDateTime calculatedAt;
}
