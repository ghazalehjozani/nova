package ir.dotin.loan.trade.e2e.rest.arrangement;

import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import ir.dotin.loan.trade.adapters.driving.rest.command.dto.DefineTradeLoanArrangementRequest;
import ir.dotin.loan.trade.e2e.AbstractRestE2E;
import ir.dotin.loan.trade.e2e.fixture.builder.ArrangementRequestBuilder;

import static ir.dotin.loan.trade.e2e.assertion.BaseResponseAssertions.assertSuccess;
import static ir.dotin.loan.trade.e2e.assertion.BaseResponseAssertions.extractData;
import static org.assertj.core.api.Assertions.assertThat;

@Disabled
class DefineLoanArrangementRestE2ETest extends AbstractRestE2E {

    @BeforeAll
    void setupFixtures() {
        prerequisiteOrchestrator.ensureFormulas();
    }

    @Test
    void shouldDefineLoanArrangementSuccessfully() {
        DefineTradeLoanArrangementRequest request =
                ArrangementRequestBuilder.defaults().build();

        ResponseEntity<String> response = postJson("/loan-arrangements/define", request);

        assertSuccess(response, objectMapper);
        JsonNode data = extractData(response, objectMapper);
        assertThat(data).isNotNull();
    }

    @Test
    void shouldRejectArrangementWithMissingRequiredFields() {
        // Send an empty JSON object - missing all required fields
        ResponseEntity<String> response =
                postJson("/loan-arrangements/define", new java.util.HashMap<String, Object>());

        assertThat(response.getStatusCode().is4xxClientError()).isTrue();
    }

    @Test
    void shouldRejectDuplicateArrangementCode() {
        String code = "E2E-DUP-" + UUID.randomUUID().toString().substring(0, 8);
        DefineTradeLoanArrangementRequest request =
                ArrangementRequestBuilder.defaults().withCode(code).build();

        ResponseEntity<String> first = postJson("/loan-arrangements/define", request);
        assertSuccess(first, objectMapper);

        DefineTradeLoanArrangementRequest duplicateRequest =
                ArrangementRequestBuilder.defaults().withCode(code).build();
        ResponseEntity<String> second = postJson("/loan-arrangements/define", duplicateRequest);

        assertThat(second.getStatusCode().is4xxClientError()
                        || second.getStatusCode().is5xxServerError())
                .as(
                        "Duplicate arrangement code should be rejected, got: %s %s",
                        second.getStatusCode(), second.getBody())
                .isTrue();
    }

    @Test
    void shouldEnforceIdempotencyOnDefine() {
        String idempotencyKey = UUID.randomUUID().toString();
        DefineTradeLoanArrangementRequest request =
                ArrangementRequestBuilder.defaults().build();

        HttpHeaders headers = defaultHeadersWithIdempotencyKey(idempotencyKey);

        ResponseEntity<String> first = postJsonWithHeaders("/loan-arrangements/define", request, headers);
        assertSuccess(first, objectMapper);

        ResponseEntity<String> second = postJsonWithHeaders("/loan-arrangements/define", request, headers);
        assertSuccess(second, objectMapper);

        // Both should succeed with same data (idempotency replay)
        assertThat(first.getBody()).isEqualTo(second.getBody());
    }
}
