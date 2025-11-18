package ir.dotin.loan.trade.core.application.service.defineloantype.commandhandler;

import java.time.Clock;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.platform.commons.domain.event.DomainEvent;
import ir.dotin.platform.dispatcher.api.command.CommandHandler;
import ir.dotin.loan.trade.core.application.ports.inbound.command.DefineLoanTypeCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.client.loanservice.LoanServicePort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.EconomicalSectorValidation;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.TopicInfo;
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
    private final LoanServicePort loanServicePort;

    private static final ExecutorService VIRTUAL_EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();

    @Override
    public Result<List<DomainEvent<?>>> handle(DefineLoanTypeCommand command) {
        return TradeLoanType.create(mapper.toBuilder(command), clock)
                .flatMap(loanType -> validateLoanTypeAsync(loanType, command))
                .peekValue(loanType -> {
                    repository.save(loanType);
                    log.debug(
                            "Loan type defined successfully: {}",
                            loanType.getId().value());
                })
                .mapNonNull(TradeLoanType::domainEvents);
    }

    private Result<TradeLoanType> validateLoanTypeAsync(TradeLoanType loanType, DefineLoanTypeCommand command) {
        CompletableFuture<Result<Boolean>> existsCheckFuture =
                CompletableFuture.supplyAsync(() -> repository.existsByCode(loanType.getCode()), VIRTUAL_EXECUTOR);

        List<CompletableFuture<Result<EconomicalSectorValidation>>> sectorValidationFutures =
                command.economicSectorCurrencies().stream()
                        .map(sectorCurrency -> CompletableFuture.supplyAsync(
                                () -> loanServicePort.validateEconomicalSectorForLoanType(
                                        mapper.map(sectorCurrency.economicSector()), loanType.getCode()),
                                VIRTUAL_EXECUTOR))
                        .toList();

        List<String> topicCodes = command.relationTypeLoanTopics().stream()
                .map(DefineLoanTypeCommand.RelationTypeLoanTopicDto::topicCode)
                .toList();

        CompletableFuture<Result<List<TopicInfo>>> topicInfoFuture =
                CompletableFuture.supplyAsync(() -> loanServicePort.loadTopicByCode(topicCodes), VIRTUAL_EXECUTOR);

        CompletableFuture.allOf(
                        Stream.concat(Stream.of(existsCheckFuture, topicInfoFuture), sectorValidationFutures.stream())
                                .toArray(CompletableFuture[]::new))
                .join();

        return existsCheckFuture
                .join()
                .flatMap(exists -> Result.requireFalse(
                        exists,
                        Notification.ofError(
                                DefineLoanTypeErrorCodes.LOAN_TYPE_ALREADY_EXISTS,
                                loanType.getCode().value())))
                .flatMap(ignored -> collectSectorValidations(sectorValidationFutures))
                .flatMap(ignored -> topicInfoFuture.join().map(topicInfo -> {
                    log.debug("Topic info loaded: {}", topicInfo);
                    return ignored;
                }))
                .flatMap(ignored -> loanTypeValidationService.validateMandatoryRelationTypeLoanTopics(loanType))
                .map(ignored -> loanType);
    }

    private Result<Void> collectSectorValidations(List<CompletableFuture<Result<EconomicalSectorValidation>>> futures) {

        Notification aggregatedNotification = Notification.create();

        for (CompletableFuture<Result<EconomicalSectorValidation>> future : futures) {
            Result<EconomicalSectorValidation> result = future.join();
            aggregatedNotification.merge(result.notification());

            if (result.hasValue() && !Objects.requireNonNull(result.value()).isValid()) {
                aggregatedNotification.addError(
                        DefineLoanTypeErrorCodes.INVALID_ECONOMIC_SECTOR_FOR_LOAN_TYPE,
                        result.value().message());
            }
        }

        return aggregatedNotification.hasErrors() ? Result.failure(aggregatedNotification) : Result.success();
    }
}
