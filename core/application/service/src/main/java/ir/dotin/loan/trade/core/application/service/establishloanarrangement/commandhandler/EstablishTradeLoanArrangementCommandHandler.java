package ir.dotin.loan.trade.core.application.service.establishloanarrangement.commandhandler;

import java.time.Clock;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ResolvableType;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.stereotype.Service;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.platform.commons.domain.event.DomainEvent;
import ir.dotin.platform.dispatcher.api.command.CommandHandler;
import ir.dotin.loan.trade.core.application.ports.driven.command.EstablishTradeLoanArrangementCommand;
import ir.dotin.loan.trade.core.application.ports.driven.repository.TradeLoanArrangementRepository;
import ir.dotin.loan.trade.core.application.ports.driven.usecase.EstablishTradeLoanArrangementUseCase;
import ir.dotin.loan.trade.core.application.service.establishloanarrangement.i18n.LoanArrangementErrorCodes;
import ir.dotin.loan.trade.core.domain.loanarrangement.entity.TradeLoanArrangement;

import static java.util.Objects.requireNonNull;

@Service
public class EstablishTradeLoanArrangementCommandHandler
        implements EstablishTradeLoanArrangementUseCase, CommandHandler<EstablishTradeLoanArrangementCommand> {

    private static final Logger log = LoggerFactory.getLogger(EstablishTradeLoanArrangementCommandHandler.class);

    private final ConversionService mapper;
    private final TradeLoanArrangementRepository repository;
    private final Clock clock;

    public EstablishTradeLoanArrangementCommandHandler(
            ConversionService mapper, TradeLoanArrangementRepository repository, Clock clock) {
        this.mapper = mapper;
        this.repository = repository;
        this.clock = clock;
    }

    @Override
    public Result<List<DomainEvent<?, ?>>> handle(EstablishTradeLoanArrangementCommand command) {
        log.debug("Establishing trade loan arrangement with code: {}", command.code());
        try {
            // Step 1: Check for duplicate code
            if (repository.existsByCode(command.code())) {
                var notification = Notification.ofError(LoanArrangementErrorCodes.DUPLICATE_CODE, command.code());
                return Result.failure(notification);
            }

            // Step 2: Build domain model
            ResolvableType rt = ResolvableType.forClassWithGenerics(Result.class, TradeLoanArrangement.Builder.class);
            @SuppressWarnings("unchecked")
            Result<TradeLoanArrangement.Builder> builderResult =
                    (Result<TradeLoanArrangement.Builder>) mapper.convert(command, new TypeDescriptor(rt, null, null));
            requireNonNull(builderResult);
            if (builderResult.isFailure()) {
                return Result.failure(builderResult.notification());
            }
            var builder = builderResult.orElseThrow();

            // Step 3: Create aggregate
            var arrangementResult = TradeLoanArrangement.create(builder, clock);
            if (arrangementResult.isFailure()) {
                return Result.failure(arrangementResult.notification());
            }
            var arrangement = arrangementResult.orElseThrow();

            // Step 4: Save to repository
            var saved = repository.save(arrangement);

            log.info("Successfully established trade loan arrangement with ID: {}", saved.getId());
            return Result.success(arrangement.domainEvents());
        } catch (Exception e) {
            log.error("Failed to establish loan arrangement", e);
            throw new RuntimeException("Failed to establish loan arrangement", e);
        }
    }
}
