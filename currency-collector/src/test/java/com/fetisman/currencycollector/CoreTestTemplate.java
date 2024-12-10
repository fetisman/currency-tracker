package com.fetisman.currencycollector;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.fetisman.currencymodel.utils.Constants.EUR;
import static com.fetisman.currencymodel.utils.Constants.USD;

public class CoreTestTemplate {
    protected static final String MONO_URL = "https://api.monobank.ua/bank/currency";
    protected static final String PRIVATBANK_URL = "https://api.privatbank.ua/p24api/pubinfo?json&exchange&coursid=5";
    protected static final String DEFAULT_TOPIC = "currency_rates";

    protected final ObjectMapper mapper = new ObjectMapper();

    String mockResponseMonoString = """
                    [
                        {"currencyCodeA": 840, "currencyCodeB": 980, "date":1733297773, "rateBuy": 41.5, "rateSell": 41.99},
                        {"currencyCodeA": 978, "currencyCodeB": 980, "date":1733297775, "rateBuy": 43.0, "rateSell": 44.0}
                    ]
                """;
    String mockToPublishString = """
                    [{"type":"PrivatBank","ccy":"EUR","base_ccy":"UAH","buy":43.35,"sale":44.35},
                    {"type":"PrivatBank","ccy":"USD","base_ccy":"UAH","buy":41.15,"sale":41.75},
                    {"type":"MonoBank","ccy":"USD","base_ccy":"UAH","buy":41.33,"sale":41.8305},
                    {"type":"MonoBank","ccy":"EUR","base_ccy":"UAH","buy":43.73,"sale":44.4306}]
                """;



    @Test
    @Disabled
    void shouldConvertDifferentFormat() throws JsonProcessingException {
        Map<Integer, String> currencyCodes = Map.of(
                840, USD,
                978, EUR,
                980, "UAH"
        );
        String json1 = """
                [{"type":"PrivatBank","ccy":"EUR","base_ccy":"UAH","buy":"43.37000","sale":"44.24779"},
                 {"type":"PrivatBank","ccy":"USD","base_ccy":"UAH","buy":"41.40000","sale":"41.84100"}]
                """;
//              [{"type":"PrivatBank","ccy":"EUR","base_ccy":"UAH","buy":"43.35000","sale":"44.35000"},
//               {"type":"PrivatBank","ccy":USD,"base_ccy":"UAH","buy":"41.15000","sale":"41.75000"}]

        String json2 = """
               [{"type":"MonoBank","currencyCodeA":840,"currencyCodeB":980,"date":1733349673,"rateBuy":41.44,"rateSell":41.8393},
               {"type":"MonoBank","currencyCodeA":978,"currencyCodeB":980,"date":1733349673,"rateBuy":43.6,"rateSell":44.3007}]
                """;

        ObjectMapper mapper = new ObjectMapper();

        List<Map<String, Object>> jsonList1 = mapper.readValue(json1, new TypeReference<List<Map<String, Object>>>() {});
        List<Map<String, Object>> jsonList2 = mapper.readValue(json2, new TypeReference<List<Map<String, Object>>>() {});


        // Читання JSON
//        JsonNode privatBankArray = mapper.readTree(json1);
        JsonNode monoBankArray = mapper.readTree(json2);

        List<JsonNode> resultList = new ArrayList<>();

        // Обробка PrivatBank
//        for (JsonNode node : privatBankArray) {
//            ObjectNode resultNode = mapper.createObjectNode();
//            resultNode.put("type", node.get("type").asText());
//            resultNode.put("ccy", node.get("ccy").asText());
//            resultNode.put("base_ccy", node.get("base_ccy").asText());
//            resultNode.put("buy", node.get("buy").asText());
//            resultNode.put("sale", node.get("sale").asText());
//            resultList.add(resultNode);
//        }

        // Обробка MonoBank
        for (JsonNode node : monoBankArray) {
            int currencyCodeA = node.get("currencyCodeA").asInt();
            int currencyCodeB = node.get("currencyCodeB").asInt();

            if (currencyCodes.containsKey(currencyCodeB) && currencyCodeB == 980) { // base_ccy == UAH
                String ccy = currencyCodes.get(currencyCodeA);

                if (ccy != null) {
                    ObjectNode resultNode = mapper.createObjectNode();
                    resultNode.put("type", node.get("type").asText());
                    resultNode.put("ccy", ccy);
                    resultNode.put("base_ccy", currencyCodes.get(currencyCodeB));
                    resultNode.put("buy", node.get("rateBuy").asDouble());
                    resultNode.put("sale", node.get("rateSell").asDouble());
                    resultList.add(resultNode);
                }
            }
        }

        // Конвертуємо List<JsonNode> у List<Map<String, Object>>
        for (Map<String, Object> node : jsonList2) {
            Map<String, Object> resultNode = new HashMap<>();
            resultNode.put("sale", node.get("rateSell").toString());
            resultNode.put("buy", node.get("rateBuy").toString());
            resultNode.put("base_ccy", currencyCodes.get((Integer) node.get("currencyCodeB")));
            resultNode.put("ccy", currencyCodes.get((Integer) node.get("currencyCodeA")));
            resultNode.put("type", node.get("type"));
            jsonList1.add(resultNode);
        }

        // Конвертуємо результат у JSON
        String resultJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonList1);
        System.out.println(resultJson);

    }
}
