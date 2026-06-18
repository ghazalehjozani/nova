package ir.dotin.loan.trade.adapters.driven.fcbmessaging.artemis.client;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbBaseRequest;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request.EvaluateFormulaViaFcbRequest;

import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;

class EvaluateFormulaRequestWireTest {

    private final JsonMapper mapper = JsonMapper.builder().build();

    @Test
    void routesOnEvaluateFormulaAndEmitsFcbCodedAliasKeysWithStringValues() {
        Map<String, String> aliasValues = new LinkedHashMap<>();
        aliasValues.put("LOAN_APPROVED_AMOUNT", "1000000");
        aliasValues.put("LOAN_RATE", "0.18");

        FcbBaseRequest req = EvaluateFormulaViaFcbRequest.builder()
                .code("INSTALLMENT_AMOUNT")
                .aliasValues(aliasValues)
                .build();

        assertThat(req.getOperationName())
                .as("operationName routes the request to the FCB evaluate-formula handler (sent as the broker "
                        + "PROP_OPERATION_TYPE header, @JsonIgnore on the body)")
                .isEqualTo("evaluate-formula");

        String json = mapper.writeValueAsString(req);

        assertThat(json)
                .as("wire json: %s", json)
                .doesNotContain("operationName")
                .contains("\"code\":\"INSTALLMENT_AMOUNT\"")
                .contains("\"LOAN_APPROVED_AMOUNT\":\"1000000\"")
                .contains("\"LOAN_RATE\":\"0.18\"");
    }
}
