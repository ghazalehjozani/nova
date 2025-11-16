package ir.dotin.loan.trade.core.application.service.defineloantype.commandhandler;

import java.time.Clock;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.platform.commons.domain.event.DomainEvent;
import ir.dotin.platform.dispatcher.api.command.CommandHandler;
import ir.dotin.loan.trade.core.application.ports.inbound.command.DefineLoanTypeCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanTypeRepository;
import ir.dotin.loan.trade.core.application.service.defineloantype.i18n.DefineLoanTypeErrorCodes;
import ir.dotin.loan.trade.core.application.service.defineloantype.mapper.DefineLoanTypeCommandMapper;
import ir.dotin.loan.trade.core.domain.loantype.entity.TradeLoanType;
import ir.dotin.loan.trade.core.domain.loantype.service.TradeLoanTypeValidationService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DefineLoanTypeCommandHandler implements CommandHandler<DefineLoanTypeCommand> {

    private static final Logger log = LoggerFactory.getLogger(DefineLoanTypeCommandHandler.class);

    private final DefineLoanTypeCommandMapper mapper;
    private final TradeLoanTypeRepository repository;
    private final TradeLoanTypeValidationService loanTypeValidationService;
    private final Clock clock;

    @Override
    public Result<List<DomainEvent<?>>> handle(DefineLoanTypeCommand command) {
        return TradeLoanType.create(mapper.toBuilder(command), clock)
                .flatMap(loanType -> repository
                        .existsByCode(loanType.getCode())
                        .flatMap(exists -> Result.requireFalse(
                                exists,
                                Notification.ofError(
                                        DefineLoanTypeErrorCodes.LOAN_TYPE_ALREADY_EXISTS,
                                        loanType.getCode().value())))
                        .map(ignored -> loanType))
                .flatMap(loanType -> loanTypeValidationService
                        .validateMandatoryRelationTypeLoanTopics(loanType)
                        .map(ignored -> loanType))
                .peekValue(loanType -> {
                    repository.save(loanType);
                    log.debug(
                            "Loan type defined successfully: {}",
                            loanType.getId().value());
                })
                .mapNonNull(TradeLoanType::domainEvents);
    }
}
