package ir.dotin.loan.trade.core.application.ports.inbound.command;

import java.util.UUID;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import ir.dotin.platform.pangaea.servicelayer.api.command.Command;

import lombok.Builder;

@Builder(toBuilder = true)
public record RenameLoanTypeGroupCommand(
        @NotNull UUID uid,
        @NotNull Long version,
        @NotNull UUID groupId,
        @NotBlank String title) implements Command {}
