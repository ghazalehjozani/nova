package ir.dotin.loan.trade.core.application.ports.inbound.command;

import java.util.List;
import java.util.UUID;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import ir.dotin.platform.pangaea.servicelayer.api.command.Command;

import lombok.Builder;

@Builder(toBuilder = true)
public record DeleteFacilityCollateralCommand(
        @NotNull UUID uid,
        @NotNull Long version,
        @NotNull UUID loanFacilityId,
        @NotEmpty List<String> collateralSerials)
        implements Command {}
