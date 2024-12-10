package com.fetisman.currencymodel.utils;

import java.util.Map;

public class Constants {
    public static final String USD = "USD";
    public static final String EUR = "EUR";
    public static final String UAH = "UAH";
    public static final String PRIVAT = "PrivatBank";
    public static final  String MONO = "MonoBank";

    public static final String PRIVATBANK_URL = "https://api.privatbank.ua/p24api/pubinfo?json&exchange&coursid=5";
    public static final String MONOBANK_URL = "https://api.monobank.ua/bank/currency";

    public static final Map<Integer, String> currencyCodes = Map.of(
            840, USD,
            978, EUR,
            980, UAH
    );

    public static final Map<String, String> bankMap = Map.of(
            Constants.PRIVAT, PRIVATBANK_URL,
            Constants.MONO, MONOBANK_URL
    );

    public static final  String LAST_CURRENCY = "lastCurrency";
    public static final  String HOURLY_DYNAMICS_CACHE = "hourlyDynamics";
    public static final  String DAILY_DYNAMICS_CACHE = "dailyDynamics";
}
