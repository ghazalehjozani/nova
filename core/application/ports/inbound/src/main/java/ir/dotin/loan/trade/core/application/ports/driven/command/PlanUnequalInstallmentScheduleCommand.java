package ir.dotin.loan.trade.core.application.ports.driven.command;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import ir.dotin.platform.dispatcher.api.command.Command;

public record PlanUnequalInstallmentScheduleCommand(
        @NotNull UUID uid,
        @NotNull Long version,
        @NotNull UUID loanFacilityId,
        @NotNull @Positive BigDecimal totalLoanAmount,
        @NotBlank String currency,
        @NotNull BigDecimal interestRate,
        Integer gracePeriodDays,
        @NotEmpty @Valid List<InstallmentSpecDto> installments)
        implements Command {

    public record InstallmentSpecDto(@NotNull LocalDate dueDate, @NotNull @Positive BigDecimal principalAmount) {}
}
