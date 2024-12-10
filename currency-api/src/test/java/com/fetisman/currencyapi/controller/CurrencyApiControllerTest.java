package com.fetisman.currencyapi.controller;

import com.fetisman.currencyapi.service.CurrencyService;
import com.fetisman.currencymodel.dto.CurrencyDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static com.fetisman.currencymodel.utils.Constants.USD;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CurrencyApiController.class)
class CurrencyApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CurrencyService currencyService;

    private String requestBody = """
                {
                    "currency": "USD"
                }
                """;

    @Test
    void testGetLastCurrency() throws Exception {
        CurrencyDto mockAggregate = CurrencyDto.builder()
                .currencyCode(USD)
                .averageBuy(41.5D)
                .averageSale(42.0D)
                .calculatedAt(LocalDateTime.now())
                .build();

        when(currencyService.getLastCurrency("USD")).thenReturn(mockAggregate);

        mockMvc.perform(get("/api/currency/last")
                        .param("currency", USD))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currencyCode").value(USD))
                .andExpect(jsonPath("$.averageBuy").value(41.5))
                .andExpect(jsonPath("$.averageSale").value(42.0));
    }


    @Test
    void getHourlyDynamics() {
    }

    @Test
    void getDailyDynamics() {
    }
}