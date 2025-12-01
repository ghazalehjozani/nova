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
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import ir.dotin.platform.dispatcher.api.command.Command;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.enums.DisbursementMethod;
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
            @Valid @NotNull PartyDto customer,
            @Valid @NotNull MoneyDto requestedAmount,
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
            @Valid @NotNull Set<PartyDto> guarantors,
            @Valid @NotNull Set<CertificateDto> certificates,
            @Valid @Nullable ApplicationNumberDto applicationNumber,
            @Valid @Nullable CredibilityRankDto credibilityRank) {}

    @Builder(toBuilder = true)
    public record InstallmentSchedulePlanDto(@NotEmpty @Valid List<InstallmentSpecDto> installments) {}

    @Builder(toBuilder = true)
    public record InstallmentSpecDto(
            @NotNull Integer sequenceNumber,
            @NotNull LocalDate dueDate,
            @Valid @NotNull MoneyDto principalAmount,
            @Valid @NotNull MoneyDto interestAmount,
            @Valid @Nullable MoneyDto penaltyAmount,
            @Valid @Nullable MoneyDto feeAmount) {}

    public record PartyDto(
            @Nullable String customerNumber, @Nullable PartyType type, @Valid @Nullable PersonNameDto name) {}

    public record PersonNameDto(@Nullable String firstName, @Nullable String lastName) {}

    public record BranchDto(@Nullable String code) {}

    public record CertificateDto(@NotBlank String serial) {}

    public record CredibilityRankDto(@NotBlank String value) {}

    public record DescriptionDto(@NotBlank String value) {}

    public record DisburseDestinationDto(@Nullable String depositNumber, @NotNull DisburseDestinationType type) {}

    public record RequestReasonDto(@NotBlank String code) {}

    public record ApplicationNumberDto(
            @Valid @Nullable BranchDto branch,
            @Valid @Nullable LoanTypeCodeDto loanTypeCode,
            @Valid @Nullable PartyDto party,
            @Nullable String respiteSerial,
            @Nullable String derivedValue) {}

    public record LoanTypeCodeDto(@NotBlank String value) {}

    public record SubSourceDto(@NotBlank String code) {}

    public record MoneyDto(@NotNull @DecimalMin(value = "0") BigDecimal value) {}

    public record LoanDurationDto(@NotNull Period value) {}

    public record GracePeriodDto(@NotNull Period value) {}

    public record InstallmentCountDto(@NotNull Integer value) {}

    public record EconomicSectorDto(@NotBlank String code) {}

    public record CurrencyTypeDto(@NotBlank @Pattern(regexp = "^[A-Z]{3}$") String value) {}
}
