package ir.dotin.loan.trade.core.application.ports.inbound.command;

import java.util.UUID;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import ir.dotin.platform.pangaea.servicelayer.api.command.Command;

import lombok.Builder;

@Builder(toBuilder = true)
public record CreateLoanTypeGroupCommand(
        @NotNull UUID uid,
        @Nullable Long version,
        @NotBlank String code,
        @NotBlank String title,
        @Nullable UUID parentGroupId)
        implements Command {}
