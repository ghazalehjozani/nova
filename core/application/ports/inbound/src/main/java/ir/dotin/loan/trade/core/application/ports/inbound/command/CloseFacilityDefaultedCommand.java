package ir.dotin.loan.trade.core.application.ports.inbound.command;

import java.util.UUID;
import jakarta.validation.constraints.NotNull;

import ir.dotin.platform.dispatcher.api.command.Command;

public record CloseFacilityDefaultedCommand(@NotNull UUID uid, @NotNull Long version, @NotNull UUID loanFacilityId)
        implements Command {}
