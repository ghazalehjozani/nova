package ir.dotin.loan.trade.core.application.ports.inbound.command;

import java.util.UUID;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;

import ir.dotin.platform.pangaea.dispatcher.api.command.Command;
import ir.dotin.loan.trade.core.application.ports.inbound.dto.SanctionDetailsDto;

import lombok.Builder;

@Builder(toBuilder = true)
public record ApproveFacilityCommand(
        @NotNull UUID uid,
        @NotNull Long version,
        @NotNull UUID loanFacilityId,
        @Nullable String sanctionSerial,
        @NotNull String confirmType,

        /*
         * System-populated post pre-flight: the FCB-resolved sanction details produced by
         * PrepareFacilityApprovalQuery (manual path only). Intentionally carries NO @NotNull / @Valid — it is null on
         * the client-facing query-dispatch path and on the auto path, and is only filled in by the controller / saga
         * before the manual command is dispatched. The transactional command handler's ManualApprovalStrategy rebuilds
         * SanctionDetails from this DTO instead of re-calling FCB.
         */
        @Nullable SanctionDetailsDto sanctionDetails)
        implements Command {}
