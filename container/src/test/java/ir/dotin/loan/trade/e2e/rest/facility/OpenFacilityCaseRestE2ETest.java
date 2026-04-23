package ir.dotin.loan.trade.e2e.rest.facility;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.DisburseDestinationType;
import ir.dotin.loan.baseloan.core.domain.shared.enums.PartyRole;
import ir.dotin.loan.trade.adapters.driving.contract.dto.DisburseDestinationRequestDto;
import ir.dotin.loan.trade.adapters.driving.contract.dto.PartyRequestDto;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.ApplicantChannel;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.DisbursementMethod;
import ir.dotin.loan.trade.adapters.driven.persistence.loanarrangement.entity.TradeLoanArrangementEntity;
import ir.dotin.loan.trade.adapters.driven.persistence.loantype.entity.TradeLoanTypeEntity;
import ir.dotin.loan.trade.adapters.driving.contract.dto.OriginateLoanFacilityRequest;
import ir.dotin.loan.trade.adapters.driving.contract.dto.OriginateLoanFacilityRequest.InstallmentSchedulePlanDto;
import ir.dotin.loan.trade.adapters.driving.contract.dto.OriginateLoanFacilityRequest.InstallmentSpecDto;
import ir.dotin.loan.trade.adapters.driving.contract.dto.OriginateLoanFacilityRequest.LoanApplicationDto;
import ir.dotin.loan.trade.adapters.driving.contract.dto.OriginateLoanFacilityRequest.SamatDto;
import ir.dotin.loan.trade.e2e.AbstractRestE2E;
import ir.dotin.loan.trade.e2e.orchestrator.PrerequisiteOrchestrator.MinimalChain;

import static ir.dotin.loan.trade.e2e.assertion.BaseResponseAssertions.assertSuccess;
import static org.assertj.core.api.Assertions.assertThat;

@Disabled
class OpenFacilityCaseRestE2ETest extends AbstractRestE2E {

    private TradeLoanArrangementEntity arrangement;
    private TradeLoanTypeEntity loanType;

    @BeforeAll
    void setupFixtures() {
        MinimalChain chain = prerequisiteOrchestrator.createMinimalChain();
        arrangement = chain.arrangement();
        loanType = chain.loanType();
    }

    @Test
    void shouldOpenFacilityCaseSuccessfully() {
        OriginateLoanFacilityRequest request =
                buildOpenCaseRequest(loanType.getCode().getValue(), arrangement.getCode(), new BigDecimal("50000000"));

        ResponseEntity<String> response = postJson("/facilities/open-case", request);

        assertSuccess(response, objectMapper);
    }

    @Test
    void shouldRejectFacilityWithInvalidLoanType() {
        OriginateLoanFacilityRequest request =
                buildOpenCaseRequest("NON_EXISTENT_TYPE", arrangement.getCode(), new BigDecimal("50000000"));

        ResponseEntity<String> response = postJson("/facilities/open-case", request);

        assertThat(response.getStatusCode().is4xxClientError()
                        || response.getStatusCode().is5xxServerError())
                .as("Invalid loan type should be rejected, got: %s %s", response.getStatusCode(), response.getBody())
                .isTrue();
    }

    @Test
    void shouldRejectFacilityExceedingAmountRange() {
        // The arrangement has max 100,000,000 IRR
        OriginateLoanFacilityRequest request = buildOpenCaseRequest(
                loanType.getCode().getValue(), arrangement.getCode(), new BigDecimal("999999999999"));

        ResponseEntity<String> response = postJson("/facilities/open-case", request);

        assertThat(response.getStatusCode().is4xxClientError()
                        || response.getStatusCode().is5xxServerError())
                .as(
                        "Amount exceeding range should be rejected, got: %s %s",
                        response.getStatusCode(), response.getBody())
                .isTrue();
    }

    private OriginateLoanFacilityRequest buildOpenCaseRequest(
            String loanTypeCode, String arrangementCode, BigDecimal amount) {

        return new OriginateLoanFacilityRequest(
                loanTypeCode,
                arrangementCode,
                new LoanApplicationDto(
                        Instant.now(),
                        Set.of(new PartyRequestDto.ApplicantDto("12345678", PartyRole.PRIMARY_APPLICANT)),
                        amount,
                        DisbursementMethod.IRREGULAR_PROGRESSIVE,
                        "IRR",
                        12,
                        ApplicantChannel.DIGITAL_BANK,
                        10,
                        3,
                        new DisburseDestinationRequestDto.DepositDestinationDto("1.10.1357.60", DisburseDestinationType.DEPOSIT),
                        "2-1",
                        "0",
                        "03",
                        "E2E test facility via REST",
                        null,
                        "A",
                        new SamatDto("1234567899876543", null, null, null, null, null)),
                new InstallmentSchedulePlanDto(List.of(
                        new InstallmentSpecDto(
                                1,
                                LocalDate.now().plusMonths(1),
                                new BigDecimal("16666667"),
                                new BigDecimal("750000"),
                                null,
                                null),
                        new InstallmentSpecDto(
                                2,
                                LocalDate.now().plusMonths(2),
                                new BigDecimal("16666667"),
                                new BigDecimal("625000"),
                                null,
                                null),
                        new InstallmentSpecDto(
                                3,
                                LocalDate.now().plusMonths(3),
                                new BigDecimal("16666666"),
                                new BigDecimal("500000"),
                                null,
                                null))),
                Map.of());
    }
}
