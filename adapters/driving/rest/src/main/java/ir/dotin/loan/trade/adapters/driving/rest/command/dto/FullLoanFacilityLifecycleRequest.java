package ir.dotin.loan.trade.adapters.driving.rest.command.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.ApplicantChannel;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.DisburseDestinationType;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.DisbursementMethod;
import ir.dotin.loan.baseloan.core.domain.shared.enums.PartyRole;

public record FullLoanFacilityLifecycleRequest(
        Long version,
        String loanTypeCode,
        String loanArrangementCode,
        LoanApplicationDto loanApplication,
        DisbursementDto disbursement,
        InstallmentSchedulePlanDto installmentSchedulePlan) {

    public record LoanApplicationDto(
            Instant requestDate,
            Set<PartyDto> parties,
            BigDecimal requestedAmount,
            String currency,
            Integer requestedLoanDurationMonths,
            ApplicantChannel applicantChannel,
            Integer gracePeriodDays,
            Integer installmentCount,
            DisburseDestinationDto disburseDestination,
            String economicSectorCode,
            String branchCode,
            RequestReasonDto requestReason,
            String subSourceCode,
            String description,
            String applicationNumber,
            DisbursementMethod disbursementMethod,
            SamatDto samat,
            String credibilityRank) {}

    public record SamatDto(Integer trackingNumber) {}

    public record PartyDto(String customerNumber, PartyRole role) {}

    public record DisburseDestinationDto(DisburseDestinationType type, String depositNumber) {}

    public record RequestReasonDto(String code) {}

    public record InstallmentSchedulePlanDto(List<InstallmentSpecDto> installments) {}

    public record InstallmentSpecDto(Integer sequenceNumber, Instant dueDate, BigDecimal principalAmount) {}

    public record DisbursementDto(BigDecimal trancheAmount, LocalDate disbursementDate) {}
}
