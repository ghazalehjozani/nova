package ir.dotin.loan.trade.core.application.ports.inbound.command;

import java.time.Instant;
import java.time.Period;
import java.util.Set;
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

/**
 * What every origination shares. The two implementations differ only in whether they carry a caller-supplied instalment
 * table, which is what makes the table-forbidden and table-required rules unrepresentable rather than runtime checks.
 */
// why: deliberately not sealed. The guarantee that matters — a payload either carries an instalment table or has
// nowhere to put one — comes from the two record types, not from sealing, and nothing switches exhaustively over
// this hierarchy. Sealing would only block Mockito, which the existing validation-rule tests rely on.
public interface OriginateFacilityCommand extends Command {

    String loanTypeCode();

    String loanArrangementCode();

    LoanApplicationDto loanApplication();

    @Builder(toBuilder = true)
    record LoanApplicationDto(
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
    record BranchDto(@Nullable String code) {}

    @Builder(toBuilder = true)
    record CredibilityRankDto(@NotBlank String value) {}

    @Builder(toBuilder = true)
    record DescriptionDto(@NotBlank String value) {}

    @Builder(toBuilder = true)
    record RequestReasonDto(@NotBlank String code) {}

    @Builder(toBuilder = true)
    record SubSourceDto(@NotBlank String code) {}

    @Builder(toBuilder = true)
    record LoanDurationDto(@NotNull Period value) {}

    @Builder(toBuilder = true)
    record GracePeriodDto(@NotNull Period value) {}

    @Builder(toBuilder = true)
    record InstallmentCountDto(@Nullable Integer value) {}
}
