package ir.dotin.loan.trade.core.application.service.issuefacilitycontract.component;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.stereotype.Component;

import ir.dotin.platform.commons.core.Result;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanArrangementId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.trade.core.application.ports.inbound.command.IssueFacilityContractCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanArrangementRepository;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanFacilityRepository;
import ir.dotin.loan.trade.core.application.service.issuefacilitycontract.strategy.FacilityContractContext;
import ir.dotin.loan.trade.core.application.service.shared.error.TradeLoanApplicationServiceErrors;
import ir.dotin.loan.trade.core.domain.loanarrangement.entity.TradeLoanArrangement;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class FacilityContractDependencyLoader {

    private final TradeLoanFacilityRepository facilityRepository;
    private final TradeLoanArrangementRepository loanArrangementRepository;

    private static final ExecutorService VIRTUAL_EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();

    public Result<FacilityContractContext> loadDependencies(IssueFacilityContractCommand command) {
        log.debug("Loading dependencies for facility contract issuance: {}", command.loanFacilityId());
        var facilityFuture =
                CompletableFuture.supplyAsync(() -> safeLoadFacility(command.loanFacilityId()), VIRTUAL_EXECUTOR);
        return facilityFuture
                .thenCompose(facilityResult -> {
                    if (facilityResult.isFailure()) {
                        return CompletableFuture.completedFuture(Result.<FacilityContractContext>failure(
                                facilityResult.err().orElseThrow()));
                    }
                    var facility = facilityResult.unwrap();
                    var arrangementFuture = CompletableFuture.supplyAsync(
                            () -> safeLoadArrangement(facility.getLoanArrangementId()), VIRTUAL_EXECUTOR);
                    return arrangementFuture.thenApply((Result<TradeLoanArrangement> arrangementResult) -> {
                        if (arrangementResult.isFailure()) {
                            return Result.<FacilityContractContext>failure(
                                    arrangementResult.err().orElseThrow());
                        }
                        var context = new FacilityContractContext(facility, arrangementResult.unwrap());
                        return Result.success(context);
                    });
                })
                .join();
    }

    private Result<TradeLoanFacility> safeLoadFacility(UUID facilityId) {
        return facilityRepository
                .findById(LoanFacilityId.of(facilityId))
                .map(Result::success)
                .orElseGet(() ->
                        Result.failure(TradeLoanApplicationServiceErrors.FACILITY_NOT_FOUND, facilityId.toString()));
    }

    private Result<TradeLoanArrangement> safeLoadArrangement(LoanArrangementId arrangementId) {
        return loanArrangementRepository
                .findById(arrangementId)
                .map(Result::success)
                .orElseGet(() ->
                        Result.failure(TradeLoanApplicationServiceErrors.LOAN_ARRANGEMENT_NOT_FOUND, arrangementId));
    }
}
