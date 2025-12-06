package ir.dotin.loan.trade.core.application.ports.inbound.command;

import java.time.LocalDate;
import java.util.UUID;
import jakarta.validation.constraints.NotNull;

import ir.dotin.platform.dispatcher.api.command.Command;

import lombok.Builder;

@Builder(toBuilder = true)
public record LumpSumDisbursementCommand(
        @NotNull Long version,
        @NotNull UUID loanFacilityId,
        @NotNull String branchCode,
        @NotNull String terminalType,
        @NotNull String terminalIp,
        @NotNull String terminalId,
        @NotNull String productCode,
        @NotNull String userId,
        @NotNull String toolSource,
        @NotNull String networkType,
        @NotNull String channel,
        @NotNull LocalDate disbursementDate)
        implements Command {}
