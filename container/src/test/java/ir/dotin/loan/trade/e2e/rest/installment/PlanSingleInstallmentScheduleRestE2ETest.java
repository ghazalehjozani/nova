package ir.dotin.loan.trade.e2e.rest.installment;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import ir.dotin.loan.baseloan.core.domain.loanarrangement.enums.DisbursementType;
import ir.dotin.loan.trade.adapters.driven.persistence.installmentschedule.repository.InstallmentScheduleJpaRepository;
import ir.dotin.loan.trade.adapters.driven.persistence.loanarrangement.entity.TradeLoanArrangementEntity;
import ir.dotin.loan.trade.adapters.driven.persistence.loantype.entity.TradeLoanTypeEntity;
import ir.dotin.loan.trade.adapters.driving.contract.dto.PlanSingleInstallmentScheduleRequest;
import ir.dotin.loan.trade.e2e.AbstractRestE2E;
import ir.dotin.loan.trade.e2e.fixture.LoanArrangementTestFixture;
import ir.dotin.loan.trade.e2e.fixture.LoanFacilityTestFixture;
import ir.dotin.loan.trade.e2e.fixture.LoanFacilityTestFixture.DisbursedFacilityResult;
import ir.dotin.loan.trade.e2e.fixture.LoanTypeTestFixture;
import ir.dotin.loan.trade.e2e.orchestrator.PrerequisiteOrchestrator.FullChain;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Covers the single-instalment schedule-planning endpoint end to end: the happy path, an idempotent replay, and a
 * product whose profile is not single-instalment being rejected as a business-rule violation rather than served.
 */
@DisplayName("POST /loan-facilities/{id}/installment-schedules/single-installment")
class PlanSingleInstallmentScheduleRestE2ETest extends AbstractRestE2E {

    @Autowired
    private LoanArrangementTestFixture arrangementFixture;

    @Autowired
    private LoanTypeTestFixture loanTypeFixture;

    @Autowired
    private LoanFacilityTestFixture facilityFixture;

    @Autowired
    private InstallmentScheduleJpaRepository scheduleRepository;

    @SuppressWarnings("NullAway.Init") // resolved in @BeforeAll
    private UUID singleInstallmentFacilityId;

    @SuppressWarnings("NullAway.Init") // resolved in @BeforeAll
    private UUID gradualFacilityId;

    @BeforeAll
    void setupFixtures() {
        prerequisiteOrchestrator.ensureFormulas();
        singleInstallmentFacilityId = createFacilityOnSingleInstallmentProduct();

        FullChain gradualChain = prerequisiteOrchestrator.createFullChain();
        gradualFacilityId = gradualChain.facility().facilityId();
    }

    @Test
    @DisplayName("plans a schedule for a lump-sum, one-time-repayment product")
    void plansScheduleForSingleInstallmentProduct() {
        long before = scheduleRepository.count();

        ResponseEntity<String> response =
                postJson(planPath(singleInstallmentFacilityId), request(new BigDecimal("50000000")));

        assertThat(response.getStatusCode().is2xxSuccessful())
                .as("expected 2xx but got %s: %s", response.getStatusCode(), response.getBody())
                .isTrue();
        assertThat(scheduleRepository.count()).isEqualTo(before + 1);
    }

    @Test
    @DisplayName("replaying the same Idempotency-Key does not plan a second schedule")
    void replayIsIdempotent() {
        UUID facilityId = createFacilityOnSingleInstallmentProduct();
        String idempotencyKey = UUID.randomUUID().toString();
        HttpHeaders headers = defaultHeadersWithIdempotencyKey(idempotencyKey);

        ResponseEntity<String> first = postJsonWithHeaders(planPath(facilityId), request(new BigDecimal("1000000")), headers);
        assertThat(first.getStatusCode().is2xxSuccessful())
                .as("first call: %s — %s", first.getStatusCode(), first.getBody())
                .isTrue();

        long afterFirst = scheduleRepository.count();

        ResponseEntity<String> replay = postJsonWithHeaders(
                planPath(facilityId), request(new BigDecimal("1000000")), defaultHeadersWithIdempotencyKey(idempotencyKey));

        assertThat(replay.getStatusCode().is2xxSuccessful())
                .as("replay: %s — %s", replay.getStatusCode(), replay.getBody())
                .isTrue();
        assertThat(scheduleRepository.count()).isEqualTo(afterFirst);
    }

    @Test
    @DisplayName("a gradual product is rejected — the profile, not the caller, decides the schedule type")
    void rejectsProductThatIsNotSingleInstallment() {
        ResponseEntity<String> response = postJson(planPath(gradualFacilityId), request(new BigDecimal("50000000")));

        assertThat(response.getStatusCode().is4xxClientError())
                .as("expected 4xx but got %s: %s", response.getStatusCode(), response.getBody())
                .isTrue();
    }

    private UUID createFacilityOnSingleInstallmentProduct() {
        TradeLoanArrangementEntity arrangement = arrangementFixture.createArrangement(
                "E2E-ARR-SINGLE-" + UUID.randomUUID().toString().substring(0, 8), entity -> {
                    entity.setDisbursementType(DisbursementType.LUMP_SUM);
                    requireNonNull(entity.getInstallmentPolicy()).setInstallmentPaymentType("ONE_TIME");
                });
        UUID arrangementId = requireNonNull(arrangement.getId(), "arrangement id after save");

        TradeLoanTypeEntity loanType = loanTypeFixture.createLoanType(
                "E2E-LT-SINGLE-" + UUID.randomUUID().toString().substring(0, 8), arrangementId, entity -> {});
        UUID loanTypeId = requireNonNull(loanType.getId(), "loan type id after save");
        String loanTypeCode = requireNonNull(
                requireNonNull(loanType.getCode(), "loan type code after save").getValue(),
                "loan type code value after save");

        DisbursedFacilityResult facility =
                facilityFixture.createDisbursedFacilityForCollection(loanTypeId, loanTypeCode, arrangementId);
        return facility.facilityId();
    }

    private String planPath(UUID facilityId) {
        return "/loan-facilities/" + facilityId + "/installment-schedules/single-installment";
    }

    private PlanSingleInstallmentScheduleRequest request(BigDecimal totalLoanAmount) {
        return new PlanSingleInstallmentScheduleRequest(
                1L, totalLoanAmount, "IRR", new BigDecimal("18"), 30, Map.of());
    }
}
