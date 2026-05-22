package ir.dotin.loan.trade.core.application.ports.inbound.command;

import java.util.UUID;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import ir.dotin.platform.pangaea.dispatcher.api.command.Command;
import ir.dotin.loan.trade.core.application.ports.inbound.dto.MoneyDto;

public record RegularDisbursementCommand(
        @NotNull UUID uid,
        @NotNull Long version,
        UUID loanFacilityId,
        @Valid MoneyDto trancheAmount,
        Integer trancheNumber)
        implements Command {}
