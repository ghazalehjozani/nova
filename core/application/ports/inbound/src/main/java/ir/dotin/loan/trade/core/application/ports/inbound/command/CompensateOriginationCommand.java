package ir.dotin.loan.trade.core.application.ports.inbound.command;

import java.util.UUID;

import ir.dotin.platform.pangaea.servicelayer.api.command.Command;

import lombok.Builder;

/** Compensates origination, cancelling the facility. Valid from: APPLICATION_SUBMITTED */
@Builder
public record CompensateOriginationCommand(UUID uid, Long version, UUID loanFacilityId, String reason)
        implements Command {}
