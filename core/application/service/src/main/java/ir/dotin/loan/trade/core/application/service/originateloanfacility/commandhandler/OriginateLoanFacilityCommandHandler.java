package ir.dotin.loan.trade.core.application.service.originateloanfacility.commandhandler;

import java.util.List;

import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import ir.dotin.platform.commons.core.Result;
import ir.dotin.platform.commons.domain.event.DomainEvent;
import ir.dotin.platform.dispatcher.api.command.CommandHandler;
import ir.dotin.loan.trade.core.application.ports.inbound.command.OriginateLoanFacilityCommand;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.orchestrator.FacilityOriginationOrchestrator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OriginateLoanFacilityCommandHandler implements CommandHandler<OriginateLoanFacilityCommand> {

    private final FacilityOriginationOrchestrator orchestrator;

    @Override
    public Result<List<DomainEvent<?>>> handle(@NonNull OriginateLoanFacilityCommand command) {
        return orchestrator.originate(command);
    }
}
