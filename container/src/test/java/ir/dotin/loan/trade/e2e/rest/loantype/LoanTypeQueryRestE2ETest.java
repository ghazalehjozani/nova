package ir.dotin.loan.trade.e2e.rest.loantype;

import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import ir.dotin.loan.trade.adapters.driven.persistence.loantype.entity.TradeLoanTypeEntity;
import ir.dotin.loan.trade.e2e.AbstractRestE2E;
import ir.dotin.loan.trade.e2e.orchestrator.PrerequisiteOrchestrator.MinimalChain;

import static ir.dotin.loan.trade.e2e.assertion.BaseResponseAssertions.assertSuccess;
import static ir.dotin.loan.trade.e2e.assertion.BaseResponseAssertions.extractData;
import static org.assertj.core.api.Assertions.assertThat;

@Disabled
class LoanTypeQueryRestE2ETest extends AbstractRestE2E {

    private TradeLoanTypeEntity loanType;

    @BeforeAll
    void setupFixtures() {
        MinimalChain chain = prerequisiteOrchestrator.createMinimalChain();
        loanType = chain.loanType();
    }

    @Test
    void shouldGetLoanTypeById() {
        ResponseEntity<String> response = getJson("/loan-types/" + loanType.getId());

        assertSuccess(response, objectMapper);
        JsonNode data = extractData(response, objectMapper);
        assertThat(data).isNotNull();
    }

    @Test
    void shouldReturn404ForNonExistentLoanType() {
        ResponseEntity<String> response = getJson("/loan-types/" + UUID.randomUUID());

        assertThat(response.getStatusCode().value()).isIn(404, 500);
    }

    @Test
    void shouldSearchLoanTypesByCode() {
        String code = loanType.getCode().getValue();

        ResponseEntity<String> response = getJson("/loan-types/search?code=" + code);

        assertSuccess(response, objectMapper);
    }
}
