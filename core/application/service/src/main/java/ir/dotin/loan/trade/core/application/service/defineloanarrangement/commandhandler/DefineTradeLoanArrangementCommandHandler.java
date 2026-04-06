package ir.dotin.loan.trade.core.application.service.defineloanarrangement.commandhandler;

import java.time.Clock;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.platform.commons.domain.event.DomainEvent;
import ir.dotin.platform.dispatcher.api.command.CommandHandler;
import ir.dotin.platform.formula.service.query.FormulaQueryService;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.vo.LoanArrangementCode;
import ir.dotin.loan.baseloan.core.domain.shared.vo.EconomicSector;
import ir.dotin.loan.trade.core.application.ports.inbound.command.DefineTradeLoanArrangementCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.client.loanservice.LoanServicePort;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanArrangementRepository;
import ir.dotin.loan.trade.core.application.service.defineloanarrangement.mapper.DefineTradeLoanArrangementCommandMapper;
import ir.dotin.loan.trade.core.application.service.shared.error.TradeLoanApplicationServiceErrors;
import ir.dotin.loan.trade.core.domain.loanarrangement.entity.TradeLoanArrangement;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefineTradeLoanArrangementCommandHandler implements CommandHandler<DefineTradeLoanArrangementCommand> {

    private final DefineTradeLoanArrangementCommandMapper mapper;
    private final TradeLoanArrangementRepository repository;
    private final Clock clock;
    private final LoanServicePort loanServicePort;
    private final FormulaQueryService formulaQueryService;

    private static final ExecutorService VIRTUAL_EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();

    @Override
    public Result<List<DomainEvent<?>>> handle(DefineTradeLoanArrangementCommand command) {

        List<String> formulasToValidate = Stream.of(
                        command.interestPolicy().interestFormula(),
                        command.interestPolicy().refundFormula(),
                        command.penaltyPolicy().formula(),
                        command.installmentPolicy().installmentFormula(),
                        command.installmentPolicy().interestComponentFormula(),
                        command.gracePeriodPolicy().formula())
                .filter(Objects::nonNull)
                .toList();

        List<CompletableFuture<Result<Void>>> validationFutures = formulasToValidate.stream()
                .map(formulaId ->
                        CompletableFuture.supplyAsync(() -> checkFormulaExistence(formulaId), VIRTUAL_EXECUTOR))
                .toList();

        CompletableFuture<Result<EconomicSector>> economicSectorFuture = CompletableFuture.supplyAsync(
                () -> loadEconomicSector(mapper.map(command.economicSector())), VIRTUAL_EXECUTOR);

        CompletableFuture<Result<EconomicSector>> combinedValidationFuture = CompletableFuture.allOf(
                        validationFutures.toArray(CompletableFuture[]::new))
                .thenCombineAsync(
                        economicSectorFuture,
                        (v, sectorResult) -> Result.combine(
                                Result.traverseAll(
                                        validationFutures.stream()
                                                .map(CompletableFuture::join)
                                                .toList(),
                                        res -> res),
                                sectorResult,
                                (voids, sector) -> sector),
                        VIRTUAL_EXECUTOR);

        Result<Void> duplicateCodeResult = Result.requireFalse(
                repository.existsByCode(
                        LoanArrangementCode.valueOf(command.code().value()).getValue()),
                Notification.ofError(
                        TradeLoanApplicationServiceErrors.DUPLICATE_CODE,
                        command.code().value()));

        return Result.combine(duplicateCodeResult, combinedValidationFuture.join(), (ignored, sector) -> sector)
                .flatMap(validatedSector -> Result.success(mapper.toBuilder(command))
                        .flatMap(builder -> TradeLoanArrangement.create(builder, clock)))
                .peekValue(arrangement -> {
                    repository.save(arrangement);
                    log.info("Successfully established trade loan arrangement with ID: {}", arrangement.getId());
                })
                .mapNonNull(TradeLoanArrangement::domainEvents);
    }

    private Result<EconomicSector> loadEconomicSector(EconomicSector economicSector) {
        return loanServicePort.loadEconomicalSectorByCode(economicSector);
    }

    private Result<Void> checkFormulaExistence(String formulaId) {
        return Result.requireTrue(
                formulaQueryService.exists(formulaId),
                Notification.ofError(TradeLoanApplicationServiceErrors.FORMULA_NOT_EXIST, formulaId));
    }
}
