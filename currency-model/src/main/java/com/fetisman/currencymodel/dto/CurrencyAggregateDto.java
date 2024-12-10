package com.fetisman.currencymodel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class CurrencyAggregateDto  implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Double averageBuy;

    private Double averageSale;
}
