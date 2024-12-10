package com.fetisman.currencymodel.validation;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target(ElementType.FIELD)
@Constraint(validatedBy = CurrencyValidator.class)
@Retention(RUNTIME)
public @interface ValidCurrency {
    String message() default "{invalid.currency}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
