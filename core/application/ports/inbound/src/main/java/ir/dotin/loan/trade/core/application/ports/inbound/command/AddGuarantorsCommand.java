package ir.dotin.loan.trade.core.application.ports.inbound.command;

import java.util.List;
import java.util.UUID;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import org.jspecify.annotations.Nullable;

import ir.dotin.platform.pangaea.servicelayer.api.command.Command;

import lombok.Builder;

@Builder(toBuilder = true)
public record AddGuarantorsCommand(
        @NotNull UUID uid,
        @NotNull Long version,
        @NotNull UUID loanFacilityId,
        @Nullable String branchCode,
        @NotEmpty List<@Valid GuarantorInput> guarantors)
        implements Command {

    @Builder
    public record GuarantorInput(
            @NotBlank String customerNumber, @NotNull Integer guaranteePercentage) {}
}
