package com.fetisman.currencyapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication(scanBasePackages = {"com.fetisman.currencyaggregator", "com.fetisman.currencymodel", "com.fetisman.currencyapi"})
@EnableCaching
public class CurrencyApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(CurrencyApiApplication.class, args);
    }
}
