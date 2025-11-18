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

import ir.dotin.platform.dispatcher.api.command.Command;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.ApplicantChannel;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.DisburseDestinationType;
import ir.dotin.loan.baseloan.core.domain.shared.enums.PartyType;

import lombok.Builder;

@Builder(toBuilder = true)
public record OriginateLoanFacilityCommand(
        @NotNull UUID uid,
        @Nullable Long version,
        @NotNull UUID loanTypeId,
        @NotNull UUID loanArrangementId,
        @NotNull @Valid LoanApplicationDto loanApplication,
        @Nullable @Valid InstallmentSchedulePlanDto installmentSchedulePlan)
        implements Command {

    @Builder(toBuilder = true)
    public record LoanApplicationDto(
            @NotNull Instant requestDate,
            @NotNull PartyDto customer,
            @NotNull MoneyDto requestedAmount,
            @NotNull CurrencyTypeDto currency,
            @NotNull LoanDurationDto requestedLoanDuration,
            @NotNull ApplicantChannel applicantChannel,
            @NotNull GracePeriodDto gracePeriod,
            @NotNull InstallmentCountDto installmentCount,
            @NotNull DisburseDestinationDto disburseDestination,
            @NotNull EconomicSectorDto economicSector,
            @NotNull BranchDto branch,
            @NotNull RequestReasonDto requestReason,
            @Nullable SubSourceDto subSource,
            @Nullable DescriptionDto description,
            @NotNull Set<PartyDto> guarantors,
            @NotNull Set<CertificateDto> certificates,
            @Nullable ApplicationNumberDto applicationNumber,
            @Nullable CredibilityRankDto credibilityRank) {}

    @Builder(toBuilder = true)
    public record InstallmentSchedulePlanDto(@NotEmpty @Valid List<InstallmentSpecDto> installments) {}

    @Builder(toBuilder = true)
    public record InstallmentSpecDto(
            @NotNull Integer sequenceNumber,
            @NotNull LocalDate dueDate,
            @NotNull MoneyDto principalAmount,
            @NotNull MoneyDto interestAmount,
            @Nullable MoneyDto penaltyAmount,
            @Nullable MoneyDto feeAmount) {}

    public record PartyDto(@Nullable String customerNumber, @Nullable PartyType type, @Nullable PersonNameDto name) {}

    public record PersonNameDto(@Nullable String firstName, @Nullable String lastName) {}

    public record BranchDto(@Nullable String code) {}

    public record CertificateDto(@NotBlank String serial) {}

    public record CredibilityRankDto(@NotBlank String value) {}

    public record DescriptionDto(@NotBlank String value) {}

    public record DisburseDestinationDto(@Nullable String depositNumber, @NotNull DisburseDestinationType type) {}

    public record RequestReasonDto(@NotBlank String code) {}

    public record ApplicationNumberDto(
            @Nullable BranchDto branch,
            @Nullable LoanTypeCodeDto loanTypeCode,
            @Nullable PartyDto party,
            @Nullable String respiteSerial,
            @Nullable String derivedValue) {}

    public record LoanTypeCodeDto(@NotBlank String value) {}

    public record SubSourceDto(@NotBlank String code) {}

    public record MoneyDto(@NotNull BigDecimal value) {}

    public record LoanDurationDto(@NotNull Period value) {}

    public record GracePeriodDto(@NotNull Period value) {}

    public record InstallmentCountDto(@NotNull Integer value) {}

    public record EconomicSectorDto(@NotBlank String code) {}

    public record CurrencyTypeDto(@NotBlank String value) {}
}
