package ir.dotin.loan.trade.core.application.service.issuefacilitycontract.component;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

import org.springframework.stereotype.Component;

import ir.dotin.platform.accounting.document.api.model.BranchCode;
import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.platform.commons.core.Unit;
import ir.dotin.loan.trade.core.application.ports.inbound.command.IssueFacilityContractCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.client.error.CoreBankingErrors;
import ir.dotin.loan.trade.core.application.ports.outbound.client.loanservice.LoanServicePort;
import ir.dotin.loan.trade.core.application.service.issuefacilitycontract.strategy.FacilityContractContext;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class FacilityContractValidator {

    private final LoanServicePort loanServicePort;
    private static final ExecutorService VIRTUAL_EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();

    public Result<Unit> callAndValidateServices(IssueFacilityContractCommand command, FacilityContractContext context) {
        log.debug("Call and validate services for facility contract issuance");

        var futures = List.of(runAsync(() -> validateBranch(command, context)));

        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();
        Notification aggregatedNotification = Notification.create();
        for (var future : futures) {
            Result<Unit> r = future.join();
            if (r.isFailure()) {
                aggregatedNotification.merge(r.err().orElseThrow().notification());
            }
        }
        return aggregatedNotification.hasErrors() ? Result.failure(aggregatedNotification) : Result.success();
    }

    private CompletableFuture<Result<Unit>> runAsync(Supplier<Result<Unit>> supplier) {
        return CompletableFuture.supplyAsync(supplier, VIRTUAL_EXECUTOR);
    }

    private Result<Unit> validateBranch(IssueFacilityContractCommand command, FacilityContractContext context) {
        String branchCode = command.branchCode();
        BranchCode facilityBranchCode =
                context.facility().getLoanApplication().getBranch().code();
        Result<List<BranchCode>> result = loanServicePort.loadCoveredBranches(facilityBranchCode);

        if (result.isFailure()) {
            return Result.failure(result.err().orElseThrow());
        }

        List<BranchCode> coveredBranches = result.unwrap();
        boolean isBranchCovered = coveredBranches.stream()
                .anyMatch(coveredBranch -> coveredBranch.value().equals(branchCode));

        if (!isBranchCovered) {
            return Result.failure(CoreBankingErrors.BRANCH_NOT_COVERED, branchCode);
        }

        return Result.success();
    }
}
