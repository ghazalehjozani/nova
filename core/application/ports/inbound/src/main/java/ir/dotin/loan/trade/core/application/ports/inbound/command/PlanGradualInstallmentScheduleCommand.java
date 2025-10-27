package ir.dotin.loan.trade.core.application.ports.inbound.command;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import ir.dotin.platform.dispatcher.api.command.Command;

import lombok.Builder;

@Builder(toBuilder = true)
public record PlanGradualInstallmentScheduleCommand(
        @NotNull UUID uid,
        @Nullable Long version,
        @Nullable UUID loanFacilityId,
        @NotNull MoneyDto totalLoanAmount,
        @NotNull BigDecimal interestRate,
        Integer gracePeriodDays,
        @NotEmpty @Valid List<InstallmentSpecDto> installments)
        implements Command {

    public record MoneyDto(@NotNull BigDecimal value) {}

    public record InstallmentSpecDto(
            @NotNull LocalDate dueDate, @NotNull MoneyDto principalAmount, @NotNull MoneyDto interestAmount) {}
}
