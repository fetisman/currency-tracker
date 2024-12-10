package com.fetisman.currencyaggregator.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fetisman.currencyaggregator.repo.CurrencyAggregateRepository;
import com.fetisman.currencymodel.model.BankCurrency;
import com.fetisman.currencymodel.model.CurrencyAggregate;
import com.fetisman.currencymodel.utils.Calculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.OptionalDouble;

import static com.fetisman.currencymodel.utils.Constants.DAILY_DYNAMICS_CACHE;
import static com.fetisman.currencymodel.utils.Constants.EUR;
import static com.fetisman.currencymodel.utils.Constants.HOURLY_DYNAMICS_CACHE;
import static com.fetisman.currencymodel.utils.Constants.LAST_CURRENCY;
import static com.fetisman.currencymodel.utils.Constants.USD;

@Service
@RequiredArgsConstructor
@Slf4j
public class CurrencyAggregatorServiceImpl implements CurrencyAggregatorService{

    private final CurrencyAggregateRepository repository;

    @Override
    @Transactional
    public void processCurrencyRates(String message) {
        ObjectMapper objectMapper = new ObjectMapper();
        List<BankCurrency> currencies;
        try {
            currencies = objectMapper.readValue(message, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            log.error("Failed to parse message from Kafka", e);
            throw new RuntimeException("Failed to parse message from Kafka", e);
        }

        calculateAndSaveAverage(currencies, USD);
        calculateAndSaveAverage(currencies, EUR);
    }

    @CacheEvict(cacheNames = {LAST_CURRENCY, HOURLY_DYNAMICS_CACHE, DAILY_DYNAMICS_CACHE}, key = "#currency")
    public void calculateAndSaveAverage(List<BankCurrency> currencies, String currency) {
        List<BankCurrency> filteredCurrencies = currencies.stream()
                .filter(currencyCode -> currencyCode.getCcy().equalsIgnoreCase(currency))
                .toList();

        if (!filteredCurrencies.isEmpty()) {
            OptionalDouble average = filteredCurrencies.stream()
                    .mapToDouble(BankCurrency::getBuy)
                    .average();
            double averageBuy = Calculator.round(average);

            average = filteredCurrencies.stream()
                    .mapToDouble(BankCurrency::getSale)
                    .average();
            double averageSale = Calculator.round(average);

            CurrencyAggregate aggregate = CurrencyAggregate.builder()
                    .currencyCode(currency)
                    .averageBuy(averageBuy)
                    .averageSale(averageSale)
                    .calculatedAt(LocalDateTime.now())
                    .build();
            repository.save(aggregate);
            log.info("Saved Average Currency: {}", aggregate);
        }
    }
}

