package ir.dotin.loan.trade.core.application.ports.inbound.command;

import java.util.UUID;
import jakarta.validation.constraints.NotNull;

import ir.dotin.platform.dispatcher.api.command.Command;

import lombok.Builder;

@Builder(toBuilder = true)
public record IssueFacilityContractCommand(@NotNull Long version, @NotNull UUID loanFacilityId, String branchCode)
        implements Command {}
