package ir.dotin.loan.trade.core.application.ports.inbound.command;

import java.math.BigDecimal;
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
import jakarta.validation.constraints.Pattern;

import ir.dotin.platform.pangaea.dispatcher.api.command.Command;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.enums.CollateralType;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.ApplicantChannel;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.DisbursementMethod;
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
        @NotNull @Valid TransactionMetadataDto transactionMetadata,
        @NotNull @Valid List<CollateralDto> collaterals,
        @NotNull String confirmType)
        implements Command {

    @Builder(toBuilder = true)
    public record DisbursementDto(
            @NotNull @Valid AmountDto trancheAmount,
            @Nullable LocalDate disbursementDate) {}

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
            @Nullable InstallmentCountDto installmentCount,
            @Valid @NotNull DisburseDestinationDto disburseDestination,
            @Valid @NotNull EconomicSectorDto economicSector,
            @Valid @NotNull BranchDto branch,
            @Valid @NotNull RequestReasonDto requestReason,
            @Valid @Nullable SubSourceDto subSource,
            @Valid @Nullable DescriptionDto description,
            @Valid @NotNull DisbursementMethod disbursementMethod,

            @Nullable @Pattern(regexp = "^\\d+-\\d+-\\d+-\\d+$")
            String applicationNumber,

            @Nullable SamatDto samatDto,
            @Valid @Nullable CredibilityRankDto credibilityRank) {}

    @Builder(toBuilder = true)
    public record InstallmentSchedulePlanDto(
            @NotEmpty @Valid List<InstallmentSpecDto> installments) {}

    @Builder(toBuilder = true)
    public record InstallmentSpecDto(
            @NotNull Integer sequenceNumber,
            @NotNull LocalDate dueDate,
            @Valid @NotNull AmountDto principalAmount,
            @Valid @NotNull AmountDto interestAmount) {}

    @Builder(toBuilder = true)
    public record BranchDto(@Nullable String code) {}

    @Builder(toBuilder = true)
    public record CredibilityRankDto(@NotBlank String value) {}

    @Builder(toBuilder = true)
    public record DescriptionDto(@NotBlank String value) {}

    @Builder(toBuilder = true)
    public record RequestReasonDto(@NotBlank String code) {}

    @Builder(toBuilder = true)
    public record SubSourceDto(@NotBlank String code) {}

    @Builder(toBuilder = true)
    public record LoanDurationDto(@NotNull Period value) {}

    @Builder(toBuilder = true)
    public record GracePeriodDto(@NotNull Period value) {}

    @Builder(toBuilder = true)
    public record EconomicSectorDto(@NotBlank String code) {}

    @Builder(toBuilder = true)
    public record InstallmentCountDto(@Nullable Integer value) {}

    @Builder(toBuilder = true)
    public record CollateralDto(
            @NotNull CollateralType collateralTypeCode,
            @NotNull Integer percent,
            @NotNull String description,
            @NotNull String collateralSerial,
            @NotNull MoneyDto usedAmount) {}

    public record MoneyDto(
            @NotNull BigDecimal value, @NotBlank String currency) {}
}
