package ir.dotin.loan.trade.core.application.service.establishloanarrangement.commandhandler;

import java.time.Clock;
import java.util.List;

import org.springframework.stereotype.Service;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.platform.commons.domain.event.DomainEvent;
import ir.dotin.platform.dispatcher.api.command.CommandHandler;
import ir.dotin.loan.trade.core.application.ports.driven.command.EstablishTradeLoanArrangementCommand;
import ir.dotin.loan.trade.core.application.ports.driven.repository.TradeLoanArrangementRepository;
import ir.dotin.loan.trade.core.application.service.establishloanarrangement.i18n.LoanArrangementErrorCodes;
import ir.dotin.loan.trade.core.application.service.establishloanarrangement.mapper.EstablishTradeLoanArrangementCommandMapper;
import ir.dotin.loan.trade.core.domain.loanarrangement.entity.TradeLoanArrangement;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EstablishTradeLoanArrangementCommandHandler
        implements CommandHandler<EstablishTradeLoanArrangementCommand> {

    private final EstablishTradeLoanArrangementCommandMapper mapper;
    private final TradeLoanArrangementRepository repository;
    private final Clock clock;

    @Override
    public Result<List<DomainEvent<?, ?>>> handle(EstablishTradeLoanArrangementCommand command) {
        return Result.requireFalse(
                        repository.existsByCode(command.code().value()),
                        Notification.ofError(LoanArrangementErrorCodes.DUPLICATE_CODE, command.code()))
                .mapNonNull(ignored -> mapper.toBuilder(command))
                .flatMap(builder -> TradeLoanArrangement.create(builder, clock))
                .peekValue(arrangement -> {
                    repository.save(arrangement);
                    log.info("Successfully established trade loan arrangement with ID: {}", arrangement.getId());
                })
                .mapNonNull(TradeLoanArrangement::domainEvents);
    }
}
