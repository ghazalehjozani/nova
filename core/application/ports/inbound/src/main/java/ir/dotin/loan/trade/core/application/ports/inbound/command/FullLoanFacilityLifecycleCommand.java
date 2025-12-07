package ir.dotin.loan.trade.core.application.ports.inbound.command;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import ir.dotin.platform.dispatcher.api.command.Command;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.ApplicantChannel;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.DisburseDestinationType;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.DisbursementMethod;
import ir.dotin.loan.baseloan.core.domain.shared.enums.PartyRole;
import ir.dotin.loan.trade.core.application.ports.inbound.dto.*;

import lombok.Builder;

@Builder(toBuilder = true)
public record FullLoanFacilityLifecycleCommand(
        @NotNull UUID uid,
        @Nullable Long version,
        @NotNull String loanTypeCode,
        @NotNull String loanArrangementCode,
        @NotNull @Valid LoanApplicationDto loanApplication,
        @Nullable @Valid InstallmentSchedulePlanDto installmentSchedulePlan,
        @NotNull @Valid DisbursementDto disbursement,
        @NotNull @Valid TransactionMetadataDto transactionMetadata)
        implements Command {

    @Builder(toBuilder = true)
    public record DisbursementDto(@NotNull @Valid AmountDto trancheAmount) {}

    @Builder(toBuilder = true)
    public record TransactionMetadataDto(
            @NotBlank String branchCode,
            @NotBlank String userId,
            @NotBlank String terminalId,
            @NotBlank String terminalIp,
            @NotBlank String terminalType,
            @NotBlank String channel,
            @NotBlank String toolSource,
            @NotBlank String productCode,
            @NotBlank String networkType) {}

    @Builder(toBuilder = true)
    public record LoanApplicationDto(
            @NotNull Instant requestDate,
            @Valid @NotNull @NotEmpty Set<PartyDto> parties,
            @Valid @NotNull AmountDto requestedAmount,
            @Valid @NotNull CurrencyTypeDto currency,
            @Valid @NotNull LoanDurationDto requestedLoanDuration,
            @Valid @NotNull ApplicantChannel applicantChannel,
            @Valid @NotNull GracePeriodDto gracePeriod,
            @Valid @NotNull InstallmentCountDto installmentCount,
            @Valid @NotNull DisburseDestinationDto disburseDestination,
            @Valid @NotNull EconomicSectorDto economicSector,
            @Valid @NotNull BranchDto branch,
            @Valid @NotNull RequestReasonDto requestReason,
            @Valid @Nullable SubSourceDto subSource,
            @Valid @Nullable DescriptionDto description,
            @Valid @NotNull DisbursementMethod disbursementMethod,
            Set<CertificateDto> certificates,
            @Valid @Nullable ApplicationNumberDto applicationNumber,
            @Valid @Nullable CredibilityRankDto credibilityRank) {}

    @Builder(toBuilder = true)
    public record InstallmentSchedulePlanDto(
            @NotEmpty @Valid List<InstallmentSpecDto> installments) {}

    @Builder(toBuilder = true)
    public record InstallmentSpecDto(
            @NotNull Integer sequenceNumber,
            @NotNull LocalDate dueDate,
            @Valid @NotNull AmountDto principalAmount,
            @Valid @NotNull AmountDto interestAmount,
            @Nullable AmountDto penaltyAmount,
            @Nullable AmountDto feeAmount) {}

    @Builder(toBuilder = true)
    public record PartyDto(
            @NotBlank String customerNumber, @NotNull PartyRole role) {}

    @Builder(toBuilder = true)
    public record BranchDto(@Nullable String code) {}

    @Builder(toBuilder = true)
    public record CertificateDto(@NotBlank String serial) {}

    @Builder(toBuilder = true)
    public record CredibilityRankDto(@NotBlank String value) {}

    @Builder(toBuilder = true)
    public record DescriptionDto(@NotBlank String value) {}

    @Builder(toBuilder = true)
    public record DisburseDestinationDto(
            @Nullable String depositNumber, @NotNull DisburseDestinationType type) {}

    @Builder(toBuilder = true)
    public record RequestReasonDto(@NotBlank String code) {}

    @Builder(toBuilder = true)
    public record ApplicationNumberDto(
            @Valid @Nullable BranchDto branch,
            @Valid @Nullable LoanTypeCodeDto loanTypeCode,
            @Valid @Nullable PartyDto party,
            @Nullable String derivedValue) {}

    @Builder(toBuilder = true)
    public record SubSourceDto(@NotBlank String code) {}

    @Builder(toBuilder = true)
    public record LoanDurationDto(@NotNull Period value) {}

    @Builder(toBuilder = true)
    public record GracePeriodDto(@NotNull Period value) {}

    @Builder(toBuilder = true)
    public record EconomicSectorDto(@NotBlank String code) {}

    @Builder(toBuilder = true)
    public record InstallmentCountDto(@NotNull Integer value) {}
}
