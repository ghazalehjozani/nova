package ir.dotin.loan.trade.core.application.ports.inbound.command;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.Period;
import java.util.Set;
import java.util.UUID;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.jspecify.annotations.Nullable;

import ir.dotin.platform.commons.domain.vo.CurrencyType;
import ir.dotin.platform.dispatcher.api.command.Command;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.enums.DisbursementMethod;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.ApplicantChannel;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.DisburseDestinationType;
import ir.dotin.loan.baseloan.core.domain.shared.enums.PartyType;

public record OpenFacilityCaseCommand(
        @NotNull UUID uid,
        @NotNull Long version,
        @NotNull UUID loanTypeId,
        @NotNull UUID loanArrangementId,
        @Nullable UUID loanFacilityId,
        @NotNull LoanApplicationDto loanApplication,
        @NotNull String customerId)
        implements Command {

    public record LoanApplicationDto(
            @NotNull Instant requestDate,
            @NotNull PartyDto customer,
            @NotNull MoneyDto requestedAmount,
            @NotNull CurrencyType currency,
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
            @Nullable CredibilityRankDto credibilityRank,
            @NotNull DisbursementMethod disbursementMethod) {}

    public record PartyDto(@NotBlank String customerNumber, @NotNull PartyType type, @NotNull PersonNameDto name) {}

    public record PersonNameDto(@NotBlank String firstName, @NotBlank String lastName) {}

    public record BranchDto(@NotBlank String code, @NotBlank String name) {}

    public record CertificateDto(@NotBlank String serial) {}

    public record CredibilityRankDto(@NotBlank String value) {}

    public record DescriptionDto(@NotBlank String value) {}

    public record DisburseDestinationDto(@Nullable String depositNumber, @NotNull DisburseDestinationType type) {}

    public record RequestReasonDto(@NotBlank String code, @NotBlank String name) {}

    public record SubSourceDto(@NotBlank String code, @NotBlank String name) {}

    public record ApplicationNumberDto(
            @NotNull BranchDto branch,
            @NotNull LoanTypeCodeDto loanTypeCode,
            @NotNull PartyDto party,
            @Nullable String respiteSerial,
            @NotBlank String derivedValue) {}

    public record LoanTypeCodeDto(@NotBlank String value) {}

    public record MoneyDto(@NotNull BigDecimal value, @NotNull CurrencyType currency) {}

    public record LoanDurationDto(@NotNull Period value) {}

    public record GracePeriodDto(@NotNull Period value) {}

    public record InstallmentCountDto(@NotNull Integer value) {}

    public record EconomicSectorDto(@NotBlank String code, @NotBlank String name) {}
}
