package ir.dotin.loan.trade.adapters.driven.fcbmessaging.artemis.client;

import org.junit.jupiter.api.Test;

import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbBaseRequest;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request.ValidateFormulaRequest;

import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;

class ValidateFormulaRequestWireTest {

    private final JsonMapper mapper = JsonMapper.builder().build();

    @Test
    void routesOnValidateFormulaOperationAndEmitsBodyFields() {
        FcbBaseRequest req = ValidateFormulaRequest.builder()
                .code("INSTALLMENT_AMOUNT")
                .expression("approvedAmount * interestRate")
                .build();

        assertThat(req.getOperationName())
                .as("operationName routes the request to the FCB validate-formula handler (sent as the broker "
                        + "PROP_OPERATION_TYPE header, @JsonIgnore on the body)")
                .isEqualTo("validate-formula");

        String json = mapper.writeValueAsString(req);

        assertThat(json)
                .as("wire json: %s", json)
                .doesNotContain("operationName")
                .contains("\"code\":\"INSTALLMENT_AMOUNT\"")
                .contains("\"expression\":\"approvedAmount * interestRate\"");
    }
}
