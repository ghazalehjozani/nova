package ir.dotin.loan.trade.e2e.rest.facility;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import ir.dotin.loan.baseloan.core.domain.installmentschedule.enums.InstallmentScheduleStatus;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.enums.InstallmentScheduleType;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.enums.DisbursementType;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.ApplicantChannel;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.DisburseDestinationType;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.DisbursementMethod;
import ir.dotin.loan.baseloan.core.domain.shared.enums.PartyRole;
import ir.dotin.loan.trade.adapters.driven.persistence.installmentschedule.entity.InstallmentScheduleEntity;
import ir.dotin.loan.trade.adapters.driven.persistence.installmentschedule.repository.InstallmentScheduleJpaRepository;
import ir.dotin.loan.trade.adapters.driven.persistence.loanarrangement.entity.TradeLoanArrangementEntity;
import ir.dotin.loan.trade.adapters.driven.persistence.loantype.entity.TradeLoanTypeEntity;
import ir.dotin.loan.trade.adapters.driving.contract.dto.DisburseDestinationRequestDto;
import ir.dotin.loan.trade.adapters.driving.contract.dto.LoanApplicationRequestDto;
import ir.dotin.loan.trade.adapters.driving.contract.dto.OriginateEqualInstallmentFacilityRequest;
import ir.dotin.loan.trade.adapters.driving.contract.dto.PartyRequestDto;
import ir.dotin.loan.trade.adapters.driving.contract.dto.SamatRequestDto;
import ir.dotin.loan.trade.e2e.AbstractRestE2E;
import ir.dotin.loan.trade.e2e.fixture.LoanArrangementTestFixture;
import ir.dotin.loan.trade.e2e.fixture.LoanTypeTestFixture;

import static ir.dotin.loan.trade.e2e.assertion.BaseResponseAssertions.assertCreated;
import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Closes the end-to-end half of task 6.10: a system-sourced product originates through {@code POST
 * /v1/loan-facilities/origination/equal-installment} with NO instalment table in the body, and the schedule the system
 * generates is persisted as {@code DRAFT} and referenced by the new facility.
 */
class OriginateEqualInstallmentFacilityRestE2ETest extends AbstractRestE2E {

    @Autowired
    private LoanArrangementTestFixture arrangementFixture;

    @Autowired
    private LoanTypeTestFixture loanTypeFixture;

    @Autowired
    private InstallmentScheduleJpaRepository scheduleRepository;

    @SuppressWarnings("NullAway.Init") // resolved in @BeforeAll
    private String loanTypeCode;

    @SuppressWarnings("NullAway.Init") // resolved in @BeforeAll
    private String arrangementCode;

    @BeforeAll
    void setupFixtures() {
        prerequisiteOrchestrator.ensureFormulas();
        // LUMP_SUM + SCHEDULED is the system-sourced profile: the caller sends an instalment COUNT and the
        // system generates the table. The default fixture arrangement is PROGRESSIVE + GRADUAL, i.e. the
        // user-sourced product that the sibling unequal-instalment test drives.
        TradeLoanArrangementEntity arrangement = arrangementFixture.createArrangement(
                "E2E-EQ-" + UUID.randomUUID().toString().substring(0, 8), entity -> {
                    entity.setDisbursementType(DisbursementType.LUMP_SUM);
                    requireNonNull(entity.getInstallmentPolicy()).setInstallmentPaymentType("SCHEDULED");
                });
        TradeLoanTypeEntity loanType = loanTypeFixture.createLoanType(
                "E2E-EQ-LT-" + UUID.randomUUID().toString().substring(0, 8),
                requireNonNull(arrangement.getId(), "arrangement id after save"),
                entity -> {});

        arrangementCode = requireNonNull(arrangement.getCode(), "arrangement code after save");
        loanTypeCode = requireNonNull(
                requireNonNull(loanType.getCode(), "loan type code after save").getValue(),
                "loan type code value after save");
    }

    @Test
    void systemSourcedOriginationPersistsADraftScheduleForTheNewFacility() {
        ResponseEntity<String> response =
                postJson("/loan-facilities/origination/equal-installment", buildRequest(new BigDecimal("50000000")));

        assertCreated(response);

        UUID facilityId = facilityIdFrom(requireNonNull(response.getHeaders().getFirst("Location")));
        InstallmentScheduleEntity schedule = scheduleRepository.findAll().stream()
                .filter(candidate -> facilityId.equals(candidate.getLoanFacilityId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No installment schedule persisted for facility " + facilityId));

        assertThat(schedule.getStatus())
                .as("a system-generated schedule starts as DRAFT — it is not collectable until confirmed")
                .isEqualTo(InstallmentScheduleStatus.DRAFT);
        assertThat(schedule.getScheduleType()).isEqualTo(InstallmentScheduleType.EQUAL_INSTALLMENTS);
        assertThat(schedule.getInstallments())
                .as("the system generates one row per requested instalment")
                .hasSize(12);
    }

    private static UUID facilityIdFrom(String location) {
        return UUID.fromString(location.substring(location.lastIndexOf('/') + 1));
    }

    private OriginateEqualInstallmentFacilityRequest buildRequest(BigDecimal amount) {
        return new OriginateEqualInstallmentFacilityRequest(
                loanTypeCode,
                arrangementCode,
                new LoanApplicationRequestDto(
                        Instant.now(),
                        Set.of(new PartyRequestDto.ApplicantDto("12345678", PartyRole.PRIMARY_APPLICANT)),
                        amount,
                        DisbursementMethod.LUMP_SUM,
                        "IRR",
                        12,
                        ApplicantChannel.DIGITAL_BANK,
                        10,
                        3,
                        new DisburseDestinationRequestDto.DepositDestinationDto(
                                "1.10.1357.60", DisburseDestinationType.DEPOSIT),
                        "2-1",
                        "0",
                        "03",
                        "E2E equal-instalment facility via REST",
                        null,
                        "A",
                        new SamatRequestDto("1234567899876543", null, null, null, null, null)),
                Map.of());
    }
}
