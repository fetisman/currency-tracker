package com.fetisman.currencycollector;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"com.fetisman.currencymodel", "com.fetisman.currencycollector"},
        exclude = {DataSourceAutoConfiguration.class})
@EnableScheduling
public class CurrencyCollectorApplication {

    public static void main(String[] args) {
        SpringApplication.run(CurrencyCollectorApplication.class, args);
    }

}
