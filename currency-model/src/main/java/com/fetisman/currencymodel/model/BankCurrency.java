package com.fetisman.currencymodel.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BankCurrency {
    private String type;
    @NotNull(message = "can't be empty")
    @Size(min = 3, max = 3, message = "should be made up of 3 letters")
    private String ccy;

    @NotNull(message = "can't be empty")
    @Size(min = 3, max = 3, message = "should be made up of 3 letters")
    private String base_ccy;

    @NotNull(message = "can't be empty")
    @DecimalMin(value = "0.0", inclusive = false, message = "must be more than zero")
    private Double buy;

    @NotNull(message = "can't be empty")
    @DecimalMin(value = "0.0", inclusive = false, message = "must be more than zero")
    private Double sale;
}

