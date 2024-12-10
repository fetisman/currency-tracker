package com.fetisman.currencyapi.controller;

import com.fetisman.currencyapi.service.CurrencyService;
import com.fetisman.currencymodel.dto.CurrencyAggregateDto;
import com.fetisman.currencymodel.dto.CurrencyDto;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/currency")
@RequiredArgsConstructor
@Validated
public class CurrencyApiController {

    private final CurrencyService currencyService;
//            @Valid @ModelAttribute final CurrencyRequest currency) {
//    @RequestParam @Pattern(regexp = "USD|EUR", message = "Currency must be USD or EUR") String currency) {
//    @RequestBody @Valid final CurrencyRequest currency) {
    @GetMapping("/last")
    public ResponseEntity<CurrencyDto> getLastCurrency(
            @RequestParam @Pattern(regexp = "USD|EUR", message = "Currency must be USD or EUR") String currency) {
        CurrencyDto lastCurrency = currencyService.getLastCurrency(currency);
        return ResponseEntity.ok(lastCurrency);
    }

    @GetMapping("/hourly-dynamics")
    public ResponseEntity<CurrencyAggregateDto> getHourlyDynamics(
            @RequestParam @Pattern(regexp = "USD|EUR", message = "Currency must be USD or EUR") String currency) {
        CurrencyAggregateDto hourlyDynamics = currencyService.getHourlyDynamics(currency);
//        Double hourlyDynamics = currencyService.calculateHourlyDynamics(currency);
        return ResponseEntity.ok(hourlyDynamics);
    }

    @GetMapping("/daily-dynamics")
    public ResponseEntity<List<CurrencyAggregateDto>> getDailyDynamics(
            @RequestParam @Pattern(regexp = "USD|EUR", message = "Currency must be USD or EUR") String currency) {
        List<CurrencyAggregateDto> dailyDynamics = currencyService.getDailyDynamics(currency);
        return ResponseEntity.ok(dailyDynamics);
    }

}

