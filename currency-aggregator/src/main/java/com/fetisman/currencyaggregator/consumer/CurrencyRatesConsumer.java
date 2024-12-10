package com.fetisman.currencyaggregator.consumer;

import com.fetisman.currencyaggregator.service.CurrencyAggregatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CurrencyRatesConsumer {

    private final CurrencyAggregatorService aggregatorService;

    @KafkaListener(topics = "currency_rates", groupId = "currency-aggregator-group")
    public void listen(String message) {
        aggregatorService.processCurrencyRates(message);
    }
}

