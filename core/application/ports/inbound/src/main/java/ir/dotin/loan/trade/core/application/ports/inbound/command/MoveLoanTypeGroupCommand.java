package ir.dotin.loan.trade.core.application.ports.inbound.command;

import java.util.UUID;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;

import ir.dotin.platform.pangaea.servicelayer.api.command.Command;

import lombok.Builder;

@Builder(toBuilder = true)
public record MoveLoanTypeGroupCommand(
        @NotNull UUID uid,
        @NotNull Long version,
        @NotNull UUID groupId,
        @Nullable UUID newParentGroupId) implements Command {}
