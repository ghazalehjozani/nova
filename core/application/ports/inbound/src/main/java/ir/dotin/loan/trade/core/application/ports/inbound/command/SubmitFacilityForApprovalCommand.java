package ir.dotin.loan.trade.core.application.ports.inbound.command;

import java.util.UUID;
import jakarta.validation.constraints.NotNull;

import org.jspecify.annotations.Nullable;

import ir.dotin.platform.pangaea.dispatcher.api.command.Command;

import lombok.Builder;

@Builder(toBuilder = true)
public record SubmitFacilityForApprovalCommand(
        @NotNull UUID uid,
        @NotNull Long version,
        @NotNull UUID loanFacilityId,
        @Nullable String branchCode) implements Command {}
