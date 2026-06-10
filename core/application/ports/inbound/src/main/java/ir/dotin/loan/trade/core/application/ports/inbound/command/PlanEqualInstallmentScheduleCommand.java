package ir.dotin.loan.trade.core.application.ports.inbound.command;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import ir.dotin.platform.pangaea.servicelayer.api.command.Command;

public record PlanEqualInstallmentScheduleCommand(
        @NotNull UUID uid,
        @NotNull Long version,
        @NotNull UUID loanFacilityId,
        @NotNull @DecimalMin(value = "0") BigDecimal totalLoanAmount,
        @NotBlank String currency,
        @NotNull Integer numberOfInstallments,
        @NotNull Integer installmentPeriodDays,
        @NotNull LocalDate firstInstallmentDate,
        @NotNull BigDecimal interestRate,
        Integer gracePeriodDays)
        implements Command {}
