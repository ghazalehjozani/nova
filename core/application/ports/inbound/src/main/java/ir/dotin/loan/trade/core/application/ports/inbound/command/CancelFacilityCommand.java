package ir.dotin.loan.trade.core.application.ports.inbound.command;

import java.time.LocalDate;
import java.util.UUID;
import jakarta.validation.constraints.NotNull;

import org.jspecify.annotations.Nullable;

import ir.dotin.platform.dispatcher.api.command.Command;

public record CancelFacilityCommand(
        @NotNull UUID uid,
        @NotNull Long version,
        UUID loanFacilityId,
        String applicationNumber,
        LocalDate cancelDate,
        String cancelDescription,
        String cancelReason,
        @Nullable String cancelLoanTransactionNumber)
        implements Command {}
