package com.fetisman.currencyapi.service;


import com.fetisman.currencymodel.dto.CurrencyAggregateDto;
import com.fetisman.currencymodel.dto.CurrencyDto;

import java.util.List;

public interface CurrencyService {
    CurrencyDto getLastCurrency(String currency);
    CurrencyAggregateDto getHourlyDynamics(String currency);
    List<CurrencyAggregateDto> getDailyDynamics(String currency);

    void clearHourlyDynamicsCache(String currency);
    void clearAllHourlyDynamicsCache();

}
