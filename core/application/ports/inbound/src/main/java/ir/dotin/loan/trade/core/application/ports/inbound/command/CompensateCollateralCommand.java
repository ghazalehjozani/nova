package ir.dotin.loan.trade.core.application.ports.inbound.command;

import java.util.List;
import java.util.UUID;

import ir.dotin.platform.pangaea.servicelayer.api.command.Command;

import lombok.Builder;

@Builder
public record CompensateCollateralCommand(UUID uid, Long version, UUID loanFacilityId, List<String> collateralSerials)
        implements Command {}
