package ir.dotin.loan.trade.core.application.service.defineloanarrangement.commandhandler;

import java.time.Clock;
import java.util.List;

import org.springframework.stereotype.Service;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.platform.commons.domain.event.DomainEvent;
import ir.dotin.platform.dispatcher.api.command.CommandHandler;
import ir.dotin.loan.trade.core.application.ports.inbound.command.EstablishTradeLoanArrangementCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanArrangementRepository;
import ir.dotin.loan.trade.core.application.service.defineloanarrangement.i18n.DefineLoanArrangementErrorCodes;
import ir.dotin.loan.trade.core.application.service.defineloanarrangement.mapper.DefineTradeLoanArrangementCommandMapper;
import ir.dotin.loan.trade.core.domain.loanarrangement.entity.TradeLoanArrangement;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefineTradeLoanArrangementCommandHandler implements CommandHandler<EstablishTradeLoanArrangementCommand> {

    private final DefineTradeLoanArrangementCommandMapper mapper;
    private final TradeLoanArrangementRepository repository;
    private final Clock clock;

    @Override
    public Result<List<DomainEvent<?, ?>>> handle(EstablishTradeLoanArrangementCommand command) {
        return Result.requireFalse(
                        repository.existsByCode(command.code().value()),
                        Notification.ofError(DefineLoanArrangementErrorCodes.DUPLICATE_CODE, command.code()))
                .map(ignored -> mapper.toBuilder(command))
                .flatMap(builder -> TradeLoanArrangement.create(builder, clock))
                .peekValue(arrangement -> {
                    repository.save(arrangement);
                    log.info("Successfully established trade loan arrangement with ID: {}", arrangement.getId());
                })
                .mapNonNull(TradeLoanArrangement::domainEvents);
    }
}
