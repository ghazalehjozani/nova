package ir.dotin.loan.trade.e2e.rest.facility;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.ApplicantChannel;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.DisburseDestinationType;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.DisbursementMethod;
import ir.dotin.loan.baseloan.core.domain.shared.enums.PartyRole;
import ir.dotin.loan.trade.adapters.driven.persistence.loanarrangement.entity.TradeLoanArrangementEntity;
import ir.dotin.loan.trade.adapters.driven.persistence.loantype.entity.TradeLoanTypeEntity;
import ir.dotin.loan.trade.adapters.driving.contract.dto.DisburseDestinationRequestDto;
import ir.dotin.loan.trade.adapters.driving.contract.dto.InstallmentSchedulePlanRequestDto;
import ir.dotin.loan.trade.adapters.driving.contract.dto.InstallmentSpecRequestDto;
import ir.dotin.loan.trade.adapters.driving.contract.dto.LoanApplicationRequestDto;
import ir.dotin.loan.trade.adapters.driving.contract.dto.OriginateUnequalInstallmentFacilityRequest;
import ir.dotin.loan.trade.adapters.driving.contract.dto.PartyRequestDto;
import ir.dotin.loan.trade.adapters.driving.contract.dto.SamatRequestDto;
import ir.dotin.loan.trade.e2e.AbstractRestE2E;
import ir.dotin.loan.trade.e2e.orchestrator.PrerequisiteOrchestrator.MinimalChain;

import static ir.dotin.loan.trade.e2e.assertion.BaseResponseAssertions.assertSuccess;
import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;

@Disabled
class OpenFacilityCaseRestE2ETest extends AbstractRestE2E {

    private TradeLoanArrangementEntity arrangement;
    private TradeLoanTypeEntity loanType;

    @SuppressWarnings("NullAway.Init") // resolved in @BeforeAll setupFixtures
    private String loanTypeCode;

    @SuppressWarnings("NullAway.Init") // resolved in @BeforeAll setupFixtures
    private String arrangementCode;

    @BeforeAll
    void setupFixtures() {
        MinimalChain chain = prerequisiteOrchestrator.createMinimalChain();
        arrangement = chain.arrangement();
        loanType = chain.loanType();
        // persisted fixtures: code is non-null by contract after save
        loanTypeCode = requireNonNull(
                requireNonNull(loanType.getCode(), "loan type code after save").getValue(),
                "loan type code value after save");
        arrangementCode = requireNonNull(arrangement.getCode(), "arrangement code after save");
    }

    @Test
    void shouldOpenFacilityCaseSuccessfully() {
        OriginateUnequalInstallmentFacilityRequest request =
                buildOpenCaseRequest(loanTypeCode, arrangementCode, new BigDecimal("50000000"));

        ResponseEntity<String> response = postJson("/loan-facilities/unequal-installments", request);

        assertSuccess(response, objectMapper);
    }

    @Test
    void shouldRejectFacilityWithInvalidLoanType() {
        OriginateUnequalInstallmentFacilityRequest request =
                buildOpenCaseRequest("NON_EXISTENT_TYPE", arrangementCode, new BigDecimal("50000000"));

        ResponseEntity<String> response = postJson("/loan-facilities/unequal-installments", request);

        assertThat(response.getStatusCode().is4xxClientError()
                        || response.getStatusCode().is5xxServerError())
                .as("Invalid loan type should be rejected, got: %s %s", response.getStatusCode(), response.getBody())
                .isTrue();
    }

    @Test
    void shouldRejectFacilityExceedingAmountRange() {
        // The arrangement has max 100,000,000 IRR
        OriginateUnequalInstallmentFacilityRequest request =
                buildOpenCaseRequest(loanTypeCode, arrangementCode, new BigDecimal("999999999999"));

        ResponseEntity<String> response = postJson("/loan-facilities/unequal-installments", request);

        assertThat(response.getStatusCode().is4xxClientError()
                        || response.getStatusCode().is5xxServerError())
                .as(
                        "Amount exceeding range should be rejected, got: %s %s",
                        response.getStatusCode(), response.getBody())
                .isTrue();
    }

    private OriginateUnequalInstallmentFacilityRequest buildOpenCaseRequest(
            String loanTypeCode, String arrangementCode, BigDecimal amount) {

        return new OriginateUnequalInstallmentFacilityRequest(
                loanTypeCode,
                arrangementCode,
                new LoanApplicationRequestDto(
                        Instant.now(),
                        Set.of(new PartyRequestDto.ApplicantDto("12345678", PartyRole.PRIMARY_APPLICANT)),
                        amount,
                        DisbursementMethod.IRREGULAR_PROGRESSIVE,
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
                        "E2E test facility via REST",
                        null,
                        "A",
                        new SamatRequestDto("1234567899876543", null, null, null, null, null)),
                new InstallmentSchedulePlanRequestDto(List.of(
                        new InstallmentSpecRequestDto(
                                1,
                                LocalDate.now(ZoneOffset.UTC).plusMonths(1),
                                new BigDecimal("16666667"),
                                new BigDecimal("750000"),
                                null,
                                null),
                        new InstallmentSpecRequestDto(
                                2,
                                LocalDate.now(ZoneOffset.UTC).plusMonths(2),
                                new BigDecimal("16666667"),
                                new BigDecimal("625000"),
                                null,
                                null),
                        new InstallmentSpecRequestDto(
                                3,
                                LocalDate.now(ZoneOffset.UTC).plusMonths(3),
                                new BigDecimal("16666666"),
                                new BigDecimal("500000"),
                                null,
                                null))),
                Map.of());
    }
}
