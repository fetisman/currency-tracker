package com.fetisman.currencycollector;

import com.fetisman.currencycollector.service.CurrencyCollectorService;
import com.fetisman.currencymodel.model.BankCurrency;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static com.fetisman.currencycollector.CoreTestTemplate.DEFAULT_TOPIC;
import static com.fetisman.currencymodel.utils.Constants.EUR;
import static com.fetisman.currencymodel.utils.Constants.MONO;
import static com.fetisman.currencymodel.utils.Constants.UAH;
import static com.fetisman.currencymodel.utils.Constants.USD;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(partitions = 1, topics = {DEFAULT_TOPIC})
@Slf4j
class CurrencyCollectorTest extends CoreTestTemplate {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private CurrencyCollectorService service;

    @Test
    void shouldValidateCurrencySuccessfully() {
        BankCurrency validCurrency = new BankCurrency(MONO, USD, UAH, 41.5D, 42.0D);
        assertTrue(service.validateCurrency(validCurrency), "Currency should pass validation");
    }

    @Test
    @Disabled
    void shouldFailValidationForInvalidCurrency() {
        BankCurrency validCurrency = new BankCurrency(MONO, USD + "D", UAH, 0.0D, 42.0D);
        assertFalse(service.validateCurrency(validCurrency), "Currency should fail validation");
    }

    @Test
    void shouldFetchCurrencyDataAndSendToKafka() {
        assertNotNull(embeddedKafkaBroker, "EmbeddedKafkaBroker should be injected!");

        Map<String, Object> consumerProps = Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, embeddedKafkaBroker.getBrokersAsString(),
                ConsumerConfig.GROUP_ID_CONFIG, "test-group",
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class
        );

        Consumer<String, String> consumer = new DefaultKafkaConsumerFactory<String, String>(consumerProps)
                .createConsumer();
        consumer.subscribe(List.of(DEFAULT_TOPIC));

        service.publishToKafka(mockToPublishString);

        // Очікування повідомлення у Kafka
//        Awaitility.await()
//                .atMost(10, TimeUnit.SECONDS) // Збільшено тайм-аут
//                .untilAsserted(() -> {
        ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
        assertFalse(records.isEmpty(), "Expected at least one Kafka message");

        records.forEach(record -> {
            log.info("Received message: {}", record.value());
            assertNotNull(record.value(), "Message value should not be null");
            assertTrue(record.value().contains("ccy"), "Message should contain ccy field");
            assertTrue(record.value().contains("base_ccy"), "Message should contain base_ccy field");
            assertTrue(record.value().contains("buy"), "Message should contain buy field");
            assertTrue(record.value().contains("sale"), "Message should contain sale field");
            assertTrue(record.value().contains(USD), "Message should contain USD value");
            assertTrue(record.value().contains(EUR), "Message should contain EUR value");
            assertTrue(record.value().contains(UAH), "Message should contain UAH value");
        });

        assertEquals(1, records.count(), "Kafka should contain one message");
        assertEquals(1, consumer.subscription().size(), "Consumer should have 1 topic");
        assertTrue(consumer.subscription().contains(DEFAULT_TOPIC), "Consumer should contain default topic");

//                });
    }



    @Test
    void shouldFetchCurrencyData() {
        webTestClient.get()
                .uri(PRIVATBANK_URL)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.size()").isEqualTo(2)
                .jsonPath("$[0].ccy").isEqualTo(EUR)
                .jsonPath("$[0].base_ccy").isEqualTo(UAH)
                .jsonPath("$[1].ccy").isEqualTo(USD)
                .jsonPath("$[1].base_ccy").isEqualTo(UAH);
    }

}
