package ir.dotin.loan.trade.core.application.ports.driven.command;

import java.util.UUID;
import jakarta.validation.constraints.NotNull;

import ir.dotin.platform.dispatcher.api.command.Command;

public record SubmitFacilityForApprovalCommand(@NotNull UUID uid, @NotNull Long version, @NotNull UUID loanFacilityId)
        implements Command {}
