package com.fetisman.currencyaggregator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication(scanBasePackages = {"com.fetisman.currencymodel", "com.fetisman.currencyaggregator"})
@EntityScan(basePackages = "com.fetisman.currencymodel.model")
public class CurrencyAggregatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(CurrencyAggregatorApplication.class, args);
    }

}
