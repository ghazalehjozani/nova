package ir.dotin.loan.trade.core.application.ports.driven.command;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import ir.dotin.platform.dispatcher.api.command.Command;

public record PlanEqualInstallmentScheduleCommand(
        @NotNull UUID uid,
        @NotNull Long version,
        @NotNull UUID loanFacilityId,
        @NotNull @Positive BigDecimal totalLoanAmount,
        @NotBlank String currency,
        @NotNull Integer numberOfInstallments,
        @NotNull Integer installmentPeriodDays,
        @NotNull LocalDate firstInstallmentDate,
        @NotNull BigDecimal interestRate,
        Integer gracePeriodDays)
        implements Command {}
