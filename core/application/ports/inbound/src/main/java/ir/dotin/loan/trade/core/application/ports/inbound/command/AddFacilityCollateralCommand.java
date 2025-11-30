package ir.dotin.loan.trade.core.application.ports.inbound.command;

import java.util.UUID;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import ir.dotin.platform.dispatcher.api.command.Command;

import lombok.Builder;

@Builder(toBuilder = true)
public record AddFacilityCollateralCommand(
        @NotNull UUID uid,
        @NotNull Long version,
        @NotBlank UUID loanFacilityId,
        @NotNull CollateralSerialDto collateralSerialDto)
        implements Command {

    public record CollateralSerialDto(@NotBlank String value) {}
}
