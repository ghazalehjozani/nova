package ir.dotin.loan.trade.core.application.ports.inbound.command;

import java.util.List;
import java.util.UUID;
import jakarta.validation.constraints.NotNull;

import ir.dotin.platform.pangaea.dispatcher.api.command.Command;

import lombok.Builder;

@Builder(toBuilder = true)
public record FullLifecycleRevertCommand(
        @NotNull UUID uid,
        @NotNull Long version,
        @NotNull UUID loanFacilityId,
        String reason,
        String contractTransactionNumberToReverse,
        String disbursementTransactionNumberToReverse,
        List<String> collateralSerialsToRevert)
        implements Command {}
