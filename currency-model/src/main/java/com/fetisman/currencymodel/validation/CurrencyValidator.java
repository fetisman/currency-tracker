package com.fetisman.currencymodel.validation;

import com.fetisman.currencymodel.utils.Constants;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CurrencyValidator implements ConstraintValidator<ValidCurrency, String> {

    @Override
    public boolean isValid(final String currency, final ConstraintValidatorContext cxt) {
        if (currency == null || currency.isEmpty()) {
            return false;
        } else {
            return !Constants.UAH.equals(currency) && Constants.currencyCodes.containsValue(currency);
        }
    }
}
