package com.fetisman.currencycollector.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fetisman.currencycollector.CoreTestTemplate;
import com.fetisman.currencymodel.model.BankCurrency;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Map;

import static com.fetisman.currencymodel.utils.Constants.EUR;
import static com.fetisman.currencymodel.utils.Constants.MONO;
import static com.fetisman.currencymodel.utils.Constants.PRIVAT;
import static com.fetisman.currencymodel.utils.Constants.UAH;
import static com.fetisman.currencymodel.utils.Constants.USD;
import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CurrencyCollectorServiceTest extends CoreTestTemplate {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private WebClient webClient;

    @InjectMocks
    private CurrencyCollectorService service;

    @Test
    void shouldFetchAndConvertMonoBankData() {

        List<Map<String, Object>> mockResponse = List.of(
                Map.of("currencyCodeA", 840, "currencyCodeB", 980, "date", 1733297773, "rateBuy", 41.5, "rateSell", 41.99),
                Map.of("currencyCodeA", 978, "currencyCodeB", 980, "date", 1733297775, "rateBuy", 43.0, "rateSell", 44.0)
        );

        WebClient.RequestHeadersUriSpec uriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec headersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        when(webClient.get()).thenReturn(uriSpec);
        when(uriSpec.uri(MONO_URL)).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {}))
                .thenReturn(Mono.just(mockResponse));

        StepVerifier.create(service.fetchData(MONO_URL, MONO))
                .assertNext(data -> {
                    assertNotNull(data);
                    assertEquals(2, data.size());


                    BankCurrency currency = mapper.convertValue(data.get(0), BankCurrency.class);
                    assert currency != null;
                    assertEquals(MONO, currency.getType());
                    assertEquals(USD, currency.getCcy());
                    assertEquals(UAH, currency.getBase_ccy());
                    assertEquals(41.5, currency.getBuy());
                    assertEquals(41.99, currency.getSale());

                    currency = mapper.convertValue(data.get(1), BankCurrency.class);
                    assertEquals(MONO, currency.getType());
                    assertEquals(EUR, currency.getCcy());
                    assertEquals(UAH, currency.getBase_ccy());
                    assertEquals(43.0, currency.getBuy());
                    assertEquals(44.0, currency.getSale());
                })
                .verifyComplete();

        verify(webClient).get();
        verify(uriSpec).uri(anyString());
        verify(headersSpec).retrieve();
        verify(responseSpec).bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {});
    }

    @Test
    void shouldSendMessageToKafka() throws JsonProcessingException {
        BankCurrency usd = new BankCurrency( MONO, USD, "UAH", Double.parseDouble("41.5"), Double.parseDouble("42.0"));
        BankCurrency eur = new BankCurrency( MONO, EUR, "UAH", Double.parseDouble("42.5"), Double.parseDouble("43.0"));

        List<BankCurrency> data = List.of(usd,eur);

        service.publishToKafka(mapper.writeValueAsString(data));

        verify(kafkaTemplate).sendDefault(eq(mapper.writeValueAsString(data)));
    }

    @Test
    void shouldHandleKafkaSendErrorGracefully() {
        BankCurrency usd = new BankCurrency( PRIVAT, USD, "UAH", Double.parseDouble("41.5"), Double.parseDouble("42.0"));
        BankCurrency eur = new BankCurrency( PRIVAT, EUR, "UAH", Double.parseDouble("42.5"), Double.parseDouble("43.0"));

        List<BankCurrency> data = List.of(usd,eur);

        doThrow(new RuntimeException("Kafka error"))
                .when(kafkaTemplate).sendDefault(any());

        Assertions.assertDoesNotThrow(() -> service.publishToKafka(mapper.writeValueAsString(data)));
    }

}