package com.fetisman.currencyapi.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fetisman.currencyaggregator.repo.CurrencyAggregateRepository;
import com.fetisman.currencymodel.dto.CurrencyAggregateDto;
import com.fetisman.currencymodel.dto.CurrencyDto;
import com.fetisman.currencymodel.model.CurrencyAggregate;
import com.fetisman.currencymodel.utils.Calculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static com.fetisman.currencymodel.utils.Constants.DAILY_DYNAMICS_CACHE;
import static com.fetisman.currencymodel.utils.Constants.HOURLY_DYNAMICS_CACHE;
import static com.fetisman.currencymodel.utils.Constants.LAST_CURRENCY;

@Service
@RequiredArgsConstructor
@Slf4j
public class CurrencyServiceImpl implements CurrencyService{
    private final CurrencyAggregateRepository currencyAggregateRepository;
    private final ObjectMapper mapper;

    @Cacheable(value = LAST_CURRENCY, key = "#currency")
    public CurrencyDto getLastCurrency(String currency) {
        CurrencyDto currencyDto = mapper.convertValue(findCurrencyAggregateOrElseThrow(currency), CurrencyDto.class);
        log.info("Last Currency: {}", currencyDto);
        return currencyDto;
    }

//    @Cacheable(value = HOURLY_DYNAMICS_CACHE, key = "#currency")
//    public Double calculateHourlyDynamics(String currency) {
//        return currencyAggregateRepository.computeHourlyDynamics(currency)
//                .orElseThrow(() -> new ResponseStatusException(
//                        HttpStatus.NOT_FOUND, "Not enough data for hourly dynamics for %s".formatted(currency)));
//    }

    @Cacheable(value = HOURLY_DYNAMICS_CACHE, key = "#currency")
    public CurrencyAggregateDto getHourlyDynamics(String currency) {
        List<CurrencyAggregate> records = currencyAggregateRepository
                .findTop2ByCurrencyCodeOrderByCalculatedAtDesc(currency);

        if (records.size() < 2) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Not enough data for hourly dynamics for %s".formatted(currency));
        }
        CurrencyAggregateDto hourlyDynamic = Calculator.aggregateCurrency(records.get(0), records.get(1));
        log.info("Hourly Dynamic: {}", hourlyDynamic);
        return hourlyDynamic;
    }

    @Cacheable(value = DAILY_DYNAMICS_CACHE, key = "#currency")
    public List<CurrencyAggregateDto> getDailyDynamics(String currency) {
        List<CurrencyAggregate> records = currencyAggregateRepository.findByCurrencyCodeAndCalculatedAtAfter(
                currency, LocalDateTime.now().with(LocalTime.MIN));

        if (records.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No data found for daily dynamics");
        }

        List<CurrencyAggregateDto> dynamics = new ArrayList<>();
        for (int i = 1; i < records.size(); i++) {
            dynamics.add(Calculator.aggregateCurrency(records.get(i), records.get(i - 1)));
        }
        log.info("Daily Dynamic: {}", dynamics);
        return dynamics;
    }

    private CurrencyAggregate findCurrencyAggregateOrElseThrow(final String currency) {
        return currencyAggregateRepository.findTopByCurrencyCodeOrderByCalculatedAtDesc(currency)
                .orElseThrow(() -> {
                    log.warn("Currency {} was not found", currency);
                    return new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Currency %s was not found".formatted(currency));
                });
    }

    @CacheEvict(value = HOURLY_DYNAMICS_CACHE, key = "#currency")
    public void clearHourlyDynamicsCache(String currency) {
        log.info("Hourly Dynamics Cache cleaned for {}", currency);
    }
    @CacheEvict(value = DAILY_DYNAMICS_CACHE, key = "#currency")
    public void clearDailyDynamicsCache(String currency) {
        log.info("Daily Dynamics Cache cleaned for {}", currency);
    }

    @CacheEvict(value = LAST_CURRENCY, allEntries = true)
    public void clearAllLastCurrencyCache() {
        log.info("Last Currency Cache cleaned for all currencies");
    }
    @CacheEvict(value = HOURLY_DYNAMICS_CACHE, allEntries = true)
    public void clearAllHourlyDynamicsCache() {
        log.info("Hourly Dynamics Cache cleaned for all currencies");
    }
    @CacheEvict(value = DAILY_DYNAMICS_CACHE, allEntries = true)
    public void clearAllDailyDynamicsCache() {
        log.info("Daily Dynamics Cache cleaned for all currencies");
    }

}
