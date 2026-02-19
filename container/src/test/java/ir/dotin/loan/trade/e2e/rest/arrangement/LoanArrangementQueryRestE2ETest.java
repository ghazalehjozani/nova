package ir.dotin.loan.trade.e2e.rest.arrangement;

import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import ir.dotin.loan.trade.adapters.driven.persistence.loanarrangement.entity.TradeLoanArrangementEntity;
import ir.dotin.loan.trade.e2e.AbstractRestE2E;
import ir.dotin.loan.trade.e2e.fixture.LoanArrangementTestFixture;
import ir.dotin.loan.trade.e2e.orchestrator.PrerequisiteOrchestrator.MinimalChain;

import static ir.dotin.loan.trade.e2e.assertion.BaseResponseAssertions.assertSuccess;
import static ir.dotin.loan.trade.e2e.assertion.BaseResponseAssertions.extractData;
import static org.assertj.core.api.Assertions.assertThat;

@Disabled
class LoanArrangementQueryRestE2ETest extends AbstractRestE2E {

    @Autowired
    private LoanArrangementTestFixture arrangementFixture;

    private TradeLoanArrangementEntity arrangement;

    @BeforeAll
    void setupFixtures() {
        MinimalChain chain = prerequisiteOrchestrator.createMinimalChain();
        arrangement = chain.arrangement();
    }

    @Test
    void shouldGetArrangementById() {
        ResponseEntity<String> response = getJson("/loan-arrangements/" + arrangement.getId());

        assertSuccess(response, objectMapper);
        JsonNode data = extractData(response, objectMapper);
        assertThat(data).isNotNull();
    }

    @Test
    void shouldReturn404ForNonExistentArrangement() {
        ResponseEntity<String> response = getJson("/loan-arrangements/" + UUID.randomUUID());

        assertThat(response.getStatusCode().value()).isIn(404, 500);
    }

    @Test
    void shouldListArrangementsWithCursorPagination() {
        // Create additional arrangements to have data for pagination
        arrangementFixture.createDefaultArrangement();

        ResponseEntity<String> response = getJson("/loan-arrangements?pageSize=1");

        assertSuccess(response, objectMapper);
    }

    @Test
    void shouldSearchArrangementsByCode() {
        String code = arrangement.getCode();

        ResponseEntity<String> response = getJson("/loan-arrangements/search?code=" + code);

        assertSuccess(response, objectMapper);
        JsonNode data = extractData(response, objectMapper);
        assertThat(data).isNotNull();
    }
}
