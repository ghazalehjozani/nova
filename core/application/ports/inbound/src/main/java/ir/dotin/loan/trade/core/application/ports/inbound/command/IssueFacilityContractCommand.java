package ir.dotin.loan.trade.core.application.ports.inbound.command;

import java.util.UUID;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;

import ir.dotin.platform.pangaea.servicelayer.api.command.Command;

import lombok.Builder;

@Builder(toBuilder = true)
public record IssueFacilityContractCommand(
        @NotNull UUID uid,
        @NotNull Long version,
        @NotNull UUID loanFacilityId,
        @NotNull String branchCode,
        @NotNull String userId,
        @Nullable String terminalId,
        @Nullable String terminalIp,
        @Nullable String terminalType,
        @Nullable String channel,
        @Nullable String toolSource,
        @Nullable String productCode,
        @Nullable String networkType)
        implements Command {}
