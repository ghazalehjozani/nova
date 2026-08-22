package ir.dotin.loan.trade.core.application.ports.inbound.command;

import java.util.UUID;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import lombok.Builder;

@Builder(toBuilder = true)
public record OriginateEqualInstallmentFacilityCommand(
        @NotNull UUID uid,
        @Nullable Long version,
        @NotNull String loanTypeCode,
        @NotNull String loanArrangementCode,
        @NotNull @Valid LoanApplicationDto loanApplication)
        implements OriginateFacilityCommand {}
