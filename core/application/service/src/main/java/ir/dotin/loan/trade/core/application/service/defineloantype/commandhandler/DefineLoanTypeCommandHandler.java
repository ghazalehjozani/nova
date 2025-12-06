package ir.dotin.loan.trade.core.application.service.defineloantype.commandhandler;

import java.time.Clock;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.platform.commons.domain.event.DomainEvent;
import ir.dotin.platform.dispatcher.api.command.CommandHandler;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.vo.LoanArrangementCode;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.LoanTypeCode;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanArrangementId;
import ir.dotin.loan.trade.core.application.ports.inbound.command.DefineLoanTypeCommand;
import ir.dotin.loan.trade.core.application.ports.inbound.dto.LoanArrangementCodeDto;
import ir.dotin.loan.trade.core.application.ports.outbound.client.loanservice.LoanServicePort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.EconomicalSectorValidation;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.TopicInfo;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanArrangementRepository;
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
    private final TradeLoanTypeRepository loanTypeRepository;
    private final TradeLoanArrangementRepository loanArrangementRepository;
    private final TradeLoanTypeValidationService loanTypeValidationService;
    private final Clock clock;
    private final LoanServicePort loanServicePort;

    private static final ExecutorService VIRTUAL_EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();

    @Override
    public Result<List<DomainEvent<?>>> handle(DefineLoanTypeCommand command) {
        return joinAsync(gatherPrerequisitesAsync(command))
                .flatMap(prereqs -> buildLoanType(command, prereqs))
                .flatMap(this::validateBusinessRules)
                .peekValue(loanTypeRepository::save)
                .peekValue(loanType -> log.debug(
                        "Loan type defined successfully: {}", loanType.getId().value()))
                .mapNonNull(TradeLoanType::domainEvents);
    }

    private CompletableFuture<Result<Prerequisites>> gatherPrerequisitesAsync(DefineLoanTypeCommand command) {
        var codeValue = command.code().value();

        var uniquenessFuture =
                CompletableFuture.supplyAsync(() -> checkLoanTypeDoesNotExist(codeValue), VIRTUAL_EXECUTOR);

        var sectorValidationFuture = validateEconomicSectorsAsync(command, codeValue);

        var arrangementIdsFuture = resolveArrangementIdsAsync(command.loanArrangementCodes());

        var topicInfoFuture = CompletableFuture.supplyAsync(() -> loadTopics(command), VIRTUAL_EXECUTOR);

        return CompletableFuture.allOf(uniquenessFuture, sectorValidationFuture, arrangementIdsFuture, topicInfoFuture)
                .thenApply(ignored -> {
                    Result<Void> uniqueCheck = uniquenessFuture.join();
                    Result<Void> sectorsCheck = sectorValidationFuture.join();
                    Result<Set<LoanArrangementId>> idsResult = arrangementIdsFuture.join();
                    Result<List<TopicInfo>> topicsResult = topicInfoFuture.join();

                    Result<Void> validations = Result.combine(uniqueCheck, sectorsCheck, (a, b) -> null);

                    return Result.combine(validations, idsResult, (v, ids) -> ids)
                            .flatMap(ids -> Result.combine(Result.success(ids), topicsResult, Prerequisites::new));
                });
    }

    private Result<Void> checkLoanTypeDoesNotExist(String codeValue) {
        boolean exists =
                loanTypeRepository.existsByCode(LoanTypeCode.of(codeValue).getValue());
        return Result.requireFalse(
                exists, Notification.ofError(DefineLoanTypeErrorCodes.LOAN_TYPE_ALREADY_EXISTS, codeValue));
    }

    private CompletableFuture<Result<Set<LoanArrangementId>>> resolveArrangementIdsAsync(
            Set<LoanArrangementCodeDto> dtos) {
        List<CompletableFuture<Result<LoanArrangementId>>> futures = dtos.stream()
                .map(LoanArrangementCodeDto::value)
                .map(LoanArrangementCode::valueOf)
                .map(code -> CompletableFuture.supplyAsync(() -> findArrangementId(code.getValue()), VIRTUAL_EXECUTOR))
                .toList();

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new))
                .thenApply(v -> Result.traverse(futures, CompletableFuture::join))
                .thenApply(resultList -> resultList.map(HashSet::new));
    }

    private Result<LoanArrangementId> findArrangementId(LoanArrangementCode code) {
        return Result.fromOptional(
                loanArrangementRepository.getIdByCode(code).map(LoanArrangementId::of),
                () -> Notification.ofError(DefineLoanTypeErrorCodes.LOAN_ARRANGEMENT_NOT_FOUND, code.value()));
    }

    private CompletableFuture<Result<Void>> validateEconomicSectorsAsync(
            DefineLoanTypeCommand command, String loanTypeCode) {
        List<CompletableFuture<Result<EconomicalSectorValidation>>> futures =
                command.economicSectorCurrencies().stream()
                        .map(sectorCurrency -> CompletableFuture.supplyAsync(
                                () -> loanServicePort.validateEconomicalSectorForLoanType(
                                        mapper.map(sectorCurrency.economicSector()),
                                        LoanTypeCode.of(loanTypeCode).getValue()),
                                VIRTUAL_EXECUTOR))
                        .toList();

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new))
                .thenApply(v -> collectSectorValidations(futures));
    }

    private Result<Void> collectSectorValidations(List<CompletableFuture<Result<EconomicalSectorValidation>>> futures) {
        Notification aggregatedNotification = Notification.create();
        for (var future : futures) {
            Result<EconomicalSectorValidation> result = future.join();
            aggregatedNotification.merge(result.notification());
            if (result.hasValue() && !result.getValue().isValid()) {
                aggregatedNotification.addError(
                        DefineLoanTypeErrorCodes.INVALID_ECONOMIC_SECTOR_FOR_LOAN_TYPE,
                        result.getValue().message());
            }
        }
        return aggregatedNotification.hasErrors() ? Result.failure(aggregatedNotification) : Result.success();
    }

    private Result<List<TopicInfo>> loadTopics(DefineLoanTypeCommand command) {
        List<String> topicCodes = command.relationTypeLoanTopics().stream()
                .map(DefineLoanTypeCommand.RelationTypeLoanTopicDto::topicCode)
                .toList();
        return loanServicePort.loadTopicByCode(topicCodes).peekValue(info -> log.debug("Topic info loaded: {}", info));
    }

    private Result<TradeLoanType> buildLoanType(DefineLoanTypeCommand command, Prerequisites prereqs) {
        return TradeLoanType.create(mapper.toBuilder(command).loanArrangementIds(prereqs.arrangementIds), clock);
    }

    private Result<TradeLoanType> validateBusinessRules(TradeLoanType loanType) {
        return loanTypeValidationService
                .validateMandatoryRelationTypeLoanTopics(loanType)
                .map(ignored -> loanType);
    }

    private <T> Result<T> joinAsync(CompletableFuture<Result<T>> future) {
        return future.join();
    }

    private record Prerequisites(Set<LoanArrangementId> arrangementIds, List<TopicInfo> topicInfos) {}
}
