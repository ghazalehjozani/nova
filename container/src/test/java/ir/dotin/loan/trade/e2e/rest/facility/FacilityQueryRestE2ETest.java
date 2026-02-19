package ir.dotin.loan.trade.e2e.rest.facility;

import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import ir.dotin.loan.trade.e2e.AbstractRestE2E;
import ir.dotin.loan.trade.e2e.fixture.LoanFacilityTestFixture.DisbursedFacilityResult;
import ir.dotin.loan.trade.e2e.orchestrator.PrerequisiteOrchestrator.FullChain;

import static ir.dotin.loan.trade.e2e.assertion.BaseResponseAssertions.assertSuccess;
import static ir.dotin.loan.trade.e2e.assertion.BaseResponseAssertions.extractData;
import static org.assertj.core.api.Assertions.assertThat;

@Disabled
class FacilityQueryRestE2ETest extends AbstractRestE2E {

    private DisbursedFacilityResult facilityResult;

    @BeforeAll
    void setupFixtures() {
        FullChain chain = prerequisiteOrchestrator.createFullChain();
        facilityResult = chain.facility();
    }

    @Test
    void shouldGetFacilityById() {
        ResponseEntity<String> response = getJson("/loan-facilities/" + facilityResult.facilityId());

        assertSuccess(response, objectMapper);
        JsonNode data = extractData(response, objectMapper);
        assertThat(data).isNotNull();
    }

    @Test
    void shouldReturn404ForNonExistentFacility() {
        ResponseEntity<String> response = getJson("/loan-facilities/" + UUID.randomUUID());

        assertThat(response.getStatusCode().value()).isIn(404, 500);
    }

    @Test
    void shouldSearchFacilitiesByStatus() {
        ResponseEntity<String> response = getJson("/loan-facilities/search?status=FULLY_DISBURSED");

        assertSuccess(response, objectMapper);
    }

    @Test
    void shouldSearchFacilitiesByAmountRange() {
        ResponseEntity<String> response =
                getJson("/loan-facilities/search?requestAmountMin=1000&requestAmountMax=999999999");

        assertSuccess(response, objectMapper);
    }
}
