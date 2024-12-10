package com.fetisman.currencymodel.request;

import com.fetisman.currencymodel.validation.ValidCurrency;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CurrencyRequest {

    @ValidCurrency(message = "Currency must be USD or EUR")
    private String currency;
}
