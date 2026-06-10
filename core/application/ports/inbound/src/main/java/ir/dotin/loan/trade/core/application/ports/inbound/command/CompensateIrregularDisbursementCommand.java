package ir.dotin.loan.trade.core.application.ports.inbound.command;

import java.util.UUID;

import org.jspecify.annotations.NonNull;

import ir.dotin.platform.pangaea.servicelayer.api.command.Command;

import lombok.Builder;

@Builder(toBuilder = true)
public record CompensateIrregularDisbursementCommand(
        UUID uid,
        Long version,
        @NonNull UUID loanFacilityId,
        @NonNull UUID installmentScheduleId,
        @NonNull UUID previousInstallmentScheduleId)
        implements Command {

    public static CompensateIrregularDisbursementCommand of(
            UUID uid,
            Long version,
            @NonNull UUID loanFacilityId,
            @NonNull UUID installmentScheduleId,
            @NonNull UUID previousInstallmentScheduleId) {
        return new CompensateIrregularDisbursementCommand(
                uid, version, loanFacilityId, installmentScheduleId, previousInstallmentScheduleId);
    }
}
