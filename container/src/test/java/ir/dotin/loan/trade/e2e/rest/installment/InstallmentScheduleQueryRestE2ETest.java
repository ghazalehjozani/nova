package ir.dotin.loan.trade.e2e.rest.installment;

import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import ir.dotin.loan.trade.e2e.AbstractRestE2E;
import ir.dotin.loan.trade.e2e.fixture.LoanFacilityTestFixture.DisbursedFacilityResult;
import ir.dotin.loan.trade.e2e.orchestrator.PrerequisiteOrchestrator.FullChain;

import static ir.dotin.loan.trade.e2e.assertion.BaseResponseAssertions.assertSuccess;
import static ir.dotin.loan.trade.e2e.assertion.BaseResponseAssertions.extractData;
import static org.assertj.core.api.Assertions.assertThat;

class InstallmentScheduleQueryRestE2ETest extends AbstractRestE2E {

    private DisbursedFacilityResult facilityResult;

    @BeforeAll
    void setupFixtures() {
        FullChain chain = prerequisiteOrchestrator.createFullChain();
        facilityResult = chain.facility();
    }

    @Test
    void shouldGetInstallmentScheduleById() {
        ResponseEntity<String> response = getJson("/installment-schedules/" + facilityResult.installmentScheduleId());

        assertSuccess(response, objectMapper);
        JsonNode data = extractData(response, objectMapper);
        assertThat(data).isNotNull();
    }

    @Test
    void shouldReturn404ForNonExistentSchedule() {
        ResponseEntity<String> response = getJson("/installment-schedules/" + UUID.randomUUID());

        assertThat(response.getStatusCode().value()).isIn(404, 500);
    }
}
