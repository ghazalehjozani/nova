package ir.dotin.loan.trade.core.application.ports.inbound.command;

import java.util.UUID;
import jakarta.validation.constraints.NotNull;

import ir.dotin.platform.dispatcher.api.command.Command;

import lombok.Builder;

@Builder(toBuilder = true)
public record IssueFacilityContractCommand(
        @NotNull UUID uid,
        @NotNull Long version,
        @NotNull UUID loanFacilityId,
        @NotNull String branchCode,
        @NotNull String userId,
        @NotNull String terminalId,
        @NotNull String terminalIp,
        @NotNull String terminalType,
        @NotNull String channel,
        @NotNull String toolSource,
        @NotNull String productCode,
        @NotNull String networkType)
        implements Command {}
