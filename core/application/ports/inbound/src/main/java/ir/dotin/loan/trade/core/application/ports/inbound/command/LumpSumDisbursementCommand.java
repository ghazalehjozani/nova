package ir.dotin.loan.trade.core.application.ports.inbound.command;

import java.time.LocalDate;
import java.util.UUID;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;

import ir.dotin.platform.pangaea.dispatcher.api.command.Command;

import lombok.Builder;

@Builder(toBuilder = true)
public record LumpSumDisbursementCommand(
        @NotNull UUID uid,
        @NotNull Long version,
        @NotNull UUID loanFacilityId,
        @NotNull String branchCode,
        @Nullable String terminalType,
        @Nullable String terminalIp,
        @Nullable String terminalId,
        @Nullable String productCode,
        @NotNull String userId,
        @Nullable String toolSource,
        @Nullable String networkType,
        @Nullable String channel,
        @NotNull LocalDate disbursementDate)
        implements Command {}
