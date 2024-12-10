package com.fetisman.currencymodel.utils;

import com.fetisman.currencymodel.dto.CurrencyAggregateDto;
import com.fetisman.currencymodel.model.CurrencyAggregate;

import java.util.OptionalDouble;

public class Calculator {

    public static double calculateDynamics (double actualVal, double previousVal) {
        return round(((actualVal - previousVal) / previousVal) * 100);
    }
    public static Double round (OptionalDouble average){
        return average.isPresent() ? round(average.getAsDouble()) : 0.0;
    }
    public static Double round (Double average){
        return Math.round(average * 1000.0) / 1000.0;
    }

    public static CurrencyAggregateDto aggregateCurrency(CurrencyAggregate latestAggregate, CurrencyAggregate previousAggregate) {
        double latest = latestAggregate.getAverageBuy();
        double previous = previousAggregate.getAverageBuy();
        CurrencyAggregateDto aggregateDto = new CurrencyAggregateDto();
        aggregateDto.setAverageBuy(calculateDynamics(latest, previous));

        latest = latestAggregate.getAverageSale();
        previous = previousAggregate.getAverageSale();
        aggregateDto.setAverageSale(calculateDynamics(latest, previous));

        return aggregateDto;
    }
}
