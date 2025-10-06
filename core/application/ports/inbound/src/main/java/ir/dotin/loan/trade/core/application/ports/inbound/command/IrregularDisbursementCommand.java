package ir.dotin.loan.trade.core.application.ports.inbound.command;

import java.util.UUID;
import jakarta.validation.constraints.NotNull;

import ir.dotin.platform.dispatcher.api.command.Command;
import ir.dotin.loan.trade.core.application.ports.inbound.dto.MoneyDto;

public record IrregularDisbursementCommand(
        @NotNull UUID uid, @NotNull Long version, UUID loanFacilityId, MoneyDto requestedAmount) implements Command {}
