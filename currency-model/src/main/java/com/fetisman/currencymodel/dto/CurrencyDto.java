package com.fetisman.currencymodel.dto;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class CurrencyDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String currencyCode;

    private Double averageBuy;

    private Double averageSale;

    private LocalDateTime calculatedAt;
}
