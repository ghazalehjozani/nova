package ir.dotin.loan.trade.adapters.driven.fcbmessaging.artemis.client;

import org.junit.jupiter.api.Test;

import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbBaseResponse;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.reply.EvaluateFormulaViaFcbResponse;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.reply.ValidateFormulaResponse;

import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;

class EvaluateFormulaReplyWireTest {

    private final JsonMapper mapper = JsonMapper.builder().build();

    @Test
    void deserializesEvaluateFormulaResultViaSubtypeDiscriminator() {
        String json = "{\"operationName\":\"evaluate-formula\",\"success\":true,\"result\":\"123456.78\"}";

        FcbBaseResponse resp = mapper.readValue(json, FcbBaseResponse.class);

        assertThat(resp).as("reply parsed type").isInstanceOf(EvaluateFormulaViaFcbResponse.class);
        EvaluateFormulaViaFcbResponse r = (EvaluateFormulaViaFcbResponse) resp;
        assertThat(r.getResult()).isEqualTo("123456.78");
    }

    @Test
    void deserializesValidateFormulaReplyViaSubtypeDiscriminator() {
        String json = "{\"operationName\":\"validate-formula\",\"success\":true,\"valid\":false,"
                + "\"violations\":[\"unknown variable foo\",\"divide by zero\"]}";

        FcbBaseResponse resp = mapper.readValue(json, FcbBaseResponse.class);

        assertThat(resp).as("reply parsed type").isInstanceOf(ValidateFormulaResponse.class);
        ValidateFormulaResponse r = (ValidateFormulaResponse) resp;
        assertThat(r.isValid()).isFalse();
        assertThat(r.getViolations()).containsExactly("unknown variable foo", "divide by zero");
    }
}
