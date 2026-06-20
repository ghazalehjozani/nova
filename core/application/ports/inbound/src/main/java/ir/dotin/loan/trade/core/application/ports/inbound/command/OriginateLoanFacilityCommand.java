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
import jakarta.validation.constraints.Pattern;

import ir.dotin.platform.pangaea.servicelayer.api.command.Command;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.ApplicantChannel;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.DisbursementMethod;
import ir.dotin.loan.trade.core.application.ports.inbound.dto.*;

import lombok.Builder;

@Builder(toBuilder = true)
public record OriginateLoanFacilityCommand(
        @NotNull UUID uid,
        @Nullable Long version,
        @NotNull String loanTypeCode,
        @NotNull String loanArrangementCode,
        @NotNull @Valid LoanApplicationDto loanApplication,
        @Nullable @Valid InstallmentSchedulePlanDto installmentSchedulePlan)
        implements Command {

    @Builder(toBuilder = true)
    public record LoanApplicationDto(
            @NotNull Instant requestDate,
            @NotNull @NotEmpty Set<@Valid PartyDto> parties,
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
            @Valid @NotNull SamatDto samat,

            @Nullable @Pattern(regexp = "^\\d+-\\d+-\\d+-\\d+$")
            String applicationNumber,

            @Valid @Nullable CredibilityRankDto credibilityRank) {}

    @Builder(toBuilder = true)
    public record InstallmentSchedulePlanDto(@NotEmpty List<@Valid InstallmentSpecDto> installments) {}

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
    public record InstallmentCountDto(@Nullable Integer value) {}
}
