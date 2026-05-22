package ir.dotin.loan.trade.core.application.ports.inbound.command;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import ir.dotin.platform.pangaea.dispatcher.api.command.Command;
import ir.dotin.loan.trade.core.application.ports.inbound.dto.AmountDto;

import lombok.Builder;

@Builder(toBuilder = true)
public record IrregularProgressiveDisbursementCommand(
        @NotNull UUID uid,
        @NotNull UUID loanFacilityId,
        @NotNull BigDecimal trancheAmount,
        @NotNull Long version,
        @NotBlank String branchCode,
        @NotBlank String terminalType,
        @NotBlank String terminalIp,
        @NotBlank String terminalId,
        @NotBlank String productCode,
        @NotBlank String userId,
        @NotBlank String toolSource,
        @NotBlank String networkType,
        @NotBlank String channel,
        @Nullable @Valid InstallmentSchedulePlanDto installmentSchedulePlan,
        @Nullable LocalDate disbursementDate)
        implements Command {

    @Builder(toBuilder = true)
    public record InstallmentSchedulePlanDto(@NotNull @Valid List<@Valid InstallmentSpecDto> installments) {}

    @Builder(toBuilder = true)
    public record InstallmentSpecDto(
            @NotNull Integer sequenceNumber,
            @NotNull LocalDate dueDate,
            @NotNull AmountDto principalAmount,
            @NotNull AmountDto interestAmount) {}
}
