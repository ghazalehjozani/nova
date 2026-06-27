package ir.dotin.loan.trade.core.application.ports.inbound.command;

import java.util.UUID;
import jakarta.validation.constraints.NotNull;

import ir.dotin.platform.pangaea.servicelayer.api.command.Command;

import lombok.Builder;

@Builder(toBuilder = true)
public record RemoveLoanTypeFromGroupCommand(
        @NotNull UUID uid, @NotNull Long version, @NotNull UUID loanTypeId) implements Command {}
