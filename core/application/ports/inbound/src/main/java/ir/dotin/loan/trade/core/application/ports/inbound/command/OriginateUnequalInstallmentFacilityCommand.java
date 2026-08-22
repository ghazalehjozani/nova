package ir.dotin.loan.trade.core.application.ports.inbound.command;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import ir.dotin.loan.trade.core.application.ports.inbound.dto.AmountDto;

import lombok.Builder;

@Builder(toBuilder = true)
public record OriginateUnequalInstallmentFacilityCommand(
        @NotNull UUID uid,
        @Nullable Long version,
        @NotNull String loanTypeCode,
        @NotNull String loanArrangementCode,
        @NotNull @Valid LoanApplicationDto loanApplication,
        @NotNull @Valid InstallmentSchedulePlanDto installmentSchedulePlan)
        implements OriginateFacilityCommand {

    @Builder(toBuilder = true)
    public record InstallmentSchedulePlanDto(@NotEmpty List<@Valid InstallmentSpecDto> installments) {}

    @Builder(toBuilder = true)
    public record InstallmentSpecDto(
            @NotNull Integer sequenceNumber,
            @NotNull LocalDate dueDate,
            @Valid @NotNull AmountDto principalAmount,
            @Valid @NotNull AmountDto interestAmount) {}
}
