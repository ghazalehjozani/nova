package ir.dotin.loan.trade.core.application.ports.driven.command;

import java.util.UUID;
import jakarta.validation.constraints.NotNull;

import ir.dotin.platform.dispatcher.api.command.Command;
import ir.dotin.loan.trade.core.application.ports.driven.dto.MoneyDto;

public record RegularDisbursementCommand(
        @NotNull UUID uid, @NotNull Long version, UUID loanFacilityId, MoneyDto trancheAmount, Integer trancheNumber)
        implements Command {}
