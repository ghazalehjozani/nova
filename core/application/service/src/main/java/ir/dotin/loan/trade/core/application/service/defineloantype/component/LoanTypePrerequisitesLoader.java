package ir.dotin.loan.trade.core.application.service.defineloantype.component;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import ir.dotin.platform.pangaea.commons.core.Notification;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.Unit;
import ir.dotin.platform.pangaea.commons.core.concurrent.ParallelFanout;
import ir.dotin.platform.pangaea.commons.core.context.ContextSnapshot;
import ir.dotin.platform.pangaea.commons.core.error.FailureCause;
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
import ir.dotin.loan.trade.core.application.service.defineloantype.mapper.DefineLoanTypeCommandMapper;
import ir.dotin.loan.trade.core.application.service.shared.error.TradeLoanApplicationServiceErrors;

@Component
public class LoanTypePrerequisitesLoader {

    private static final Logger log = LoggerFactory.getLogger(LoanTypePrerequisitesLoader.class);

    private final DefineLoanTypeCommandMapper mapper;
    private final TradeLoanTypeRepository loanTypeRepository;
    private final TradeLoanArrangementRepository loanArrangementRepository;
    private final LoanServicePort loanServicePort;

    private static final ExecutorService VIRTUAL_EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();

    public LoanTypePrerequisitesLoader(
            DefineLoanTypeCommandMapper mapper,
            TradeLoanTypeRepository loanTypeRepository,
            TradeLoanArrangementRepository loanArrangementRepository,
            LoanServicePort loanServicePort) {
        this.mapper = mapper;
        this.loanTypeRepository = loanTypeRepository;
        this.loanArrangementRepository = loanArrangementRepository;
        this.loanServicePort = loanServicePort;
    }

    public Result<Prerequisites> gather(DefineLoanTypeCommand command) {
        return gatherPrerequisitesAsync(command).join();
    }

    private CompletableFuture<Result<Prerequisites>> gatherPrerequisitesAsync(DefineLoanTypeCommand command) {
        var codeValue = command.code().value();

        var uniquenessFuture = CompletableFuture.supplyAsync(
                () -> checkLoanTypeDoesNotExist(codeValue), ContextSnapshot.wrap(VIRTUAL_EXECUTOR));

        var sectorValidationFuture = CompletableFuture.supplyAsync(
                () -> validateEconomicSectors(command, codeValue), ContextSnapshot.wrap(VIRTUAL_EXECUTOR));

        var arrangementIdsFuture = CompletableFuture.supplyAsync(
                () -> resolveArrangementIds(command.loanArrangementCodes()), ContextSnapshot.wrap(VIRTUAL_EXECUTOR));

        var topicInfoFuture =
                CompletableFuture.supplyAsync(() -> loadTopics(command), ContextSnapshot.wrap(VIRTUAL_EXECUTOR));

        return CompletableFuture.allOf(uniquenessFuture, sectorValidationFuture, arrangementIdsFuture, topicInfoFuture)
                .thenApply(ignored -> {
                    Result<Unit> uniqueCheck = uniquenessFuture.join();
                    Result<Unit> sectorsCheck = sectorValidationFuture.join();
                    Result<Set<LoanArrangementId>> idsResult = arrangementIdsFuture.join();
                    Result<List<TopicInfo>> topicsResult = topicInfoFuture.join();

                    Result<Unit> validations = Result.combine(uniqueCheck, sectorsCheck, (a, b) -> Unit.INSTANCE);

                    return Result.combine(validations, idsResult, (v, ids) -> ids)
                            .flatMap(ids -> Result.combine(Result.success(ids), topicsResult, Prerequisites::new));
                });
    }

    private Result<Unit> checkLoanTypeDoesNotExist(String codeValue) {
        boolean exists =
                loanTypeRepository.existsByCode(LoanTypeCode.of(codeValue).unwrap());
        return Result.requireFalse(
                exists,
                FailureCause.businessRule(
                        Notification.ofError(TradeLoanApplicationServiceErrors.DUPLICATE_LOAN_TYPE, codeValue)));
    }

    private Result<Set<LoanArrangementId>> resolveArrangementIds(Set<LoanArrangementCodeDto> dtos) {
        List<Supplier<Result<LoanArrangementId>>> tasks = dtos.stream()
                .map(LoanArrangementCodeDto::value)
                .map(LoanArrangementCode::valueOf)
                .map(code -> (Supplier<Result<LoanArrangementId>>) () -> findArrangementId(code.unwrap()))
                .toList();
        return ParallelFanout.allOf(tasks).map(HashSet::new);
    }

    private Result<LoanArrangementId> findArrangementId(LoanArrangementCode code) {
        return Result.fromOptional(
                loanArrangementRepository.getIdByCode(code).map(LoanArrangementId::of),
                () -> FailureCause.notFound(Notification.ofError(
                        TradeLoanApplicationServiceErrors.LOAN_ARRANGEMENT_NOT_FOUND, code.value())));
    }

    private Result<Unit> validateEconomicSectors(DefineLoanTypeCommand command, String loanTypeCode) {
        List<Supplier<Result<EconomicalSectorValidation>>> tasks = command.economicSectorCurrencies().stream()
                .map(sectorCurrency -> (Supplier<Result<EconomicalSectorValidation>>)
                        () -> loanServicePort.validateEconomicalSectorForLoanType(
                                mapper.map(sectorCurrency.economicSector()),
                                LoanTypeCode.of(loanTypeCode).unwrap()))
                .toList();

        return ParallelFanout.allOf(tasks).flatMap(this::ensureAllSectorsValid);
    }

    private Result<Unit> ensureAllSectorsValid(List<EconomicalSectorValidation> validations) {
        Notification aggregatedNotification = Notification.create();
        for (EconomicalSectorValidation validation : validations) {
            if (!validation.isValid()) {
                aggregatedNotification.addError(
                        TradeLoanApplicationServiceErrors.INVALID_ECONOMIC_SECTOR_FOR_LOAN_TYPE,
                        Objects.requireNonNullElse(validation.message(), ""));
            }
        }
        return aggregatedNotification.hasErrors() ? Result.failure(aggregatedNotification) : Result.success();
    }

    private Result<List<TopicInfo>> loadTopics(DefineLoanTypeCommand command) {
        List<String> topicCodes = command.relationTypeLoanTopics().stream()
                .map(DefineLoanTypeCommand.RelationTypeLoanTopicDto::topicCode)
                .toList();
        return loanServicePort.loadTopicByCode(topicCodes).onSuccess(info -> log.debug("Topic info loaded: {}", info));
    }

    public record Prerequisites(Set<LoanArrangementId> arrangementIds, List<TopicInfo> topicInfos) {}
}
