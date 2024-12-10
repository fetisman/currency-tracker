package com.fetisman.currencycollector.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fetisman.currencymodel.model.BankCurrency;
import com.fetisman.currencymodel.utils.Constants;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.fetisman.currencymodel.utils.Constants.currencyCodes;
import static com.fetisman.currencymodel.utils.Constants.bankMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class CurrencyCollectorService {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final WebClient webClient;
    private final ObjectMapper mapper;

    @Scheduled(cron = "${collect.currency_rates.schedule:0 0 * * * *}") // Виконувати кожну годину
    public void collectCurrencyRates() throws JsonProcessingException {
        List<Map<String, Object>> currencyBankData =
                Flux.fromIterable(bankMap.keySet())
                        .flatMap(key -> fetchData(bankMap.get(key), key))
                        .flatMap(Flux::fromIterable)
                        .collectList().block();

        log.info("currencyBankData" + currencyBankData);

        assert currencyBankData != null;
        List<BankCurrency> baseBankCurrencies = deserializeJson(currencyBankData, BankCurrency.class)
                .stream().filter(this::validateCurrency).toList();
        publishToKafka(mapper.writeValueAsString(baseBankCurrencies));
    }

    public <T> Mono<List<Map<String, Object>>> fetchData(String url, String typeValue) {
        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                .map(list -> list.stream()
                        .filter(json -> {
                            if (Constants.MONO.equals(typeValue)) {
                                Integer currencyCodeA = (Integer) json.get("currencyCodeA");
                                Integer currencyCodeB = (Integer) json.get("currencyCodeB");
                                return (currencyCodeA != null && currencyCodeB != null) &&
                                        (currencyCodeA == 840 || currencyCodeA == 978 && currencyCodeB != 840);
                            }
                            return true;
                        })
                        .map(json -> convertCurrency(json, typeValue))
                        .map(json -> addTypeField(json, typeValue))
                        .toList())
                .doOnError(e -> log.error("Error fetching data from: {}", typeValue, e));
    }

    private Map<String, Object> convertCurrency(Map<String, Object> node, String typeValue) {
        if (typeValue.equals(Constants.MONO)) {
            Map<String, Object> resultNode = new HashMap<>();
            resultNode.put("sale", node.get("rateSell").toString());
            resultNode.put("buy", node.get("rateBuy").toString());
            resultNode.put("base_ccy", currencyCodes.get((Integer) node.get("currencyCodeB")));
            resultNode.put("ccy", currencyCodes.get((Integer) node.get("currencyCodeA")));
            return resultNode;
        }
        return node;
    }


    private Map<String, Object> addTypeField(Map<String, Object> json, String typeValue) {
        json.put("type", typeValue);
        return json;
    }

    private <T> List<T> deserializeJson(List<Map<String, Object>> json, Class<T> targetType) {
        try {
            JavaType type = mapper.getTypeFactory().constructCollectionType(List.class, targetType);
            return mapper.readValue(mapper.writeValueAsString(json), type);
        } catch (JsonProcessingException e) {
            log.error("Error deserializing JSON", e);
            throw new RuntimeException("Error deserializing JSON", e);
        }
    }

    public  <T> boolean validateCurrency(T currency){
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<T>> violations = validator.validate(currency);
        if (!violations.isEmpty()) {
            String errors = violations.stream()
                    .map(v -> v.getPropertyPath() + " " + v.getMessage())
                    .collect(Collectors.joining(", "));
            log.warn("Currency validation failed: {}", errors);
            return false;
        }
        return true;
    }

    public <T> void publishToKafka(String data) {
        try {
            kafkaTemplate.sendDefault(data);
            log.info("Published to Kafka: {}", data);
        } catch (Exception e) {
            log.error("Error sending message to Kafka", e);
        }
    }
}

