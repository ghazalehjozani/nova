package ir.dotin.loan.trade.core.application.ports.driven.command;

import java.math.BigDecimal;
import java.time.Period;
import java.util.UUID;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import ir.dotin.platform.commons.domain.vo.CurrencyType;
import ir.dotin.platform.dispatcher.api.command.Command;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.enums.DisbursementMethod;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.SanctionType;

public record ApproveFacilityCommand(
        @NotNull UUID uid,
        @NotNull Long version,
        @NotNull UUID loanFacilityId,
        @NotNull SanctionedLoanIdDto id,
        @NotNull SanctionSerialDto sanctionSerial,
        @NotNull MoneyDto approvedAmount,
        @NotNull CurrencyType currency,
        @NotNull GracePeriodDto gracePeriod,
        @NotNull InstallmentCountDto installmentCount,
        @NotNull LoanDurationDto loanDuration,
        @NotNull DisbursementMethod disbursementMethod,
        @Nullable LifeInsuranceIdDto lifeInsuranceId,
        @Nullable CollateralSerialDto collateralSerial,
        @Nullable RevocationReasonDto revocationReason)
        implements Command {

    public record SanctionedLoanIdDto(@NotNull UUID value) {}

    public record SanctionSerialDto(@NotBlank String value, @NotNull SanctionType type) {}

    public record MoneyDto(@NotNull BigDecimal value, @NotNull CurrencyType currency) {}

    public record GracePeriodDto(@NotNull Period value) {}

    public record InstallmentCountDto(@NotNull Integer value) {}

    public record LoanDurationDto(@NotNull Period value) {}

    public record LifeInsuranceIdDto(@NotNull String value) {}

    public record CollateralSerialDto(@NotBlank String value) {}

    public record RevocationReasonDto(@NotBlank String text) {}
}
