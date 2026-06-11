package ir.dotin.loan.trade.core.application.service.definetradeloanarrangement.component;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.springframework.stereotype.Component;

import ir.dotin.platform.formula.service.query.FormulaQueryService;
import ir.dotin.platform.pangaea.commons.core.Notification;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.Unit;
import ir.dotin.platform.pangaea.commons.core.concurrent.ParallelFanout;
import ir.dotin.platform.pangaea.commons.core.context.ContextSnapshot;
import ir.dotin.platform.pangaea.commons.core.error.FailureCause;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.vo.LoanArrangementCode;
import ir.dotin.loan.baseloan.core.domain.shared.vo.EconomicSector;
import ir.dotin.loan.trade.core.application.ports.inbound.command.DefineTradeLoanArrangementCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanArrangementRepository;
import ir.dotin.loan.trade.core.application.service.shared.error.TradeLoanApplicationServiceErrors;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ArrangementPreparer {

    private static final ExecutorService VIRTUAL_EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();

    private final TradeLoanArrangementRepository repository;
    private final FormulaQueryService formulaQueryService;
    private final EconomicalSectorLoader economicalSectorLoader;

    public Result<ArrangementPreparation> prepare(DefineTradeLoanArrangementCommand command) {
        List<String> formulasToValidate = Stream.of(
                        command.interestPolicy().interestFormula(),
                        command.interestPolicy().refundFormula(),
                        command.penaltyPolicy().formula(),
                        command.installmentPolicy().installmentFormula(),
                        command.installmentPolicy().interestComponentFormula(),
                        command.gracePeriodPolicy().formula())
                .filter(Objects::nonNull)
                .toList();

        List<Supplier<Result<Unit>>> formulaTasks = formulasToValidate.stream()
                .map(formulaId -> (Supplier<Result<Unit>>) () -> checkFormulaExistence(formulaId))
                .toList();
        CompletableFuture<Result<Unit>> validationsFuture = CompletableFuture.supplyAsync(
                () -> ParallelFanout.allVoid(formulaTasks), ContextSnapshot.wrap(VIRTUAL_EXECUTOR));

        CompletableFuture<Result<EconomicSector>> economicSectorFuture = CompletableFuture.supplyAsync(
                () -> economicalSectorLoader.loadForArrangement(command), ContextSnapshot.wrap(VIRTUAL_EXECUTOR));

        Result<EconomicSector> combinedValidation =
                Result.combine(validationsFuture.join(), economicSectorFuture.join(), (voids, sector) -> sector);

        Result<Unit> duplicateCodeResult = Result.requireFalse(
                repository.existsByCode(
                        LoanArrangementCode.valueOf(command.code().value()).unwrap()),
                FailureCause.businessRule(Notification.ofError(
                        TradeLoanApplicationServiceErrors.DUPLICATE_CODE,
                        command.code().value())));

        return Result.combine(duplicateCodeResult, combinedValidation, (ignored, sector) -> sector)
                .map(ArrangementPreparation::new);
    }

    private Result<Unit> checkFormulaExistence(String formulaId) {
        return Result.requireTrue(
                formulaQueryService.exists(formulaId),
                FailureCause.businessRule(
                        Notification.ofError(TradeLoanApplicationServiceErrors.FORMULA_NOT_EXIST, formulaId)));
    }
}
