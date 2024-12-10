package com.fetisman.currencyapi.service;

import com.fetisman.currencyaggregator.repo.CurrencyAggregateRepository;
import com.fetisman.currencymodel.dto.CurrencyAggregateDto;
import com.fetisman.currencymodel.model.CurrencyAggregate;
import com.fetisman.currencymodel.utils.Calculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

import static com.fetisman.currencymodel.utils.Constants.USD;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
class CurrencyServiceImplTest {

    @MockBean
    private CurrencyAggregateRepository currencyAggregateRepository;

    @Autowired
    private CurrencyService currencyService;

    @BeforeEach
    void clearCache(){
        currencyService.clearAllHourlyDynamicsCache();
    }

    @Test
    void testGetHourlyDynamics_Success() {

        List<CurrencyAggregate> mockRecords = List.of(
                new CurrencyAggregate(1L, USD, 42.0, 43.0, LocalDateTime.now()),
                new CurrencyAggregate(2L, USD, 40.0, 41.0, LocalDateTime.now().minusHours(1))
        );

        when(currencyAggregateRepository.findTop2ByCurrencyCodeOrderByCalculatedAtDesc(USD))
                .thenReturn(mockRecords);

        CurrencyAggregateDto dynamics = currencyService.getHourlyDynamics(USD);

        assertNotNull(dynamics, "Dynamics should not be null");
        assertEquals(5.0, dynamics.getAverageBuy(), 0.01, "Dynamics should be calculated correctly");

        verify(currencyAggregateRepository, times(1))
                .findTop2ByCurrencyCodeOrderByCalculatedAtDesc(USD);

        dynamics = currencyService.getHourlyDynamics(USD);

        verify(currencyAggregateRepository, times(1))
                .findTop2ByCurrencyCodeOrderByCalculatedAtDesc(USD);
    }

    @Test
    void testGetHourlyDynamics_NotEnoughData() {
        List<CurrencyAggregate> mockRecords = List.of(
                new CurrencyAggregate(1L, USD, 42.0, 43.0, LocalDateTime.now())
        );

        when(currencyAggregateRepository.findTop2ByCurrencyCodeOrderByCalculatedAtDesc(USD))
                .thenReturn(mockRecords);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> currencyService.getHourlyDynamics(USD),
                "Expected exception when not enough data for %s".formatted(USD)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode(), "Exception status should be NOT_FOUND");
        assertEquals("Not enough data for hourly dynamics for %s".formatted(USD), exception.getReason(), "Exception message should match");

        verify(currencyAggregateRepository, times(1))
                .findTop2ByCurrencyCodeOrderByCalculatedAtDesc(USD);
    }

    @Test
    @Disabled("Need to add test DB")
    void testClearHourlyDynamicsCache() {
        currencyService.getHourlyDynamics(USD);

        currencyService.clearHourlyDynamicsCache(USD);

        currencyService.getHourlyDynamics(USD);
        verify(currencyAggregateRepository, times(1))
                .findTop2ByCurrencyCodeOrderByCalculatedAtDesc(USD);
    }

}
