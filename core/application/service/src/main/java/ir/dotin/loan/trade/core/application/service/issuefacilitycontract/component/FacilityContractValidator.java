package ir.dotin.loan.trade.core.application.service.issuefacilitycontract.component;

import java.util.List;
import java.util.function.Supplier;

import org.springframework.stereotype.Component;

import ir.dotin.platform.accounting.document.api.model.BranchCode;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.Unit;
import ir.dotin.platform.pangaea.commons.core.concurrent.ParallelFanout;
import ir.dotin.loan.trade.core.application.ports.inbound.command.IssueFacilityContractCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.client.error.CoreBankingErrors;
import ir.dotin.loan.trade.core.application.ports.outbound.client.loanservice.BranchCoveragePort;
import ir.dotin.loan.trade.core.application.service.issuefacilitycontract.strategy.FacilityContractContext;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class FacilityContractValidator {

    private final BranchCoveragePort branchCoveragePort;

    public Result<Unit> callAndValidateServices(IssueFacilityContractCommand command, FacilityContractContext context) {
        log.debug("Call and validate services for facility contract issuance");

        // ParallelFanout fans the validations over virtual threads, propagates the caller's ambient context, and
        // error-accumulates every failure into the aggregate Result.
        List<Supplier<Result<Unit>>> tasks = List.of(() -> validateBranch(command, context));
        return ParallelFanout.allVoid(tasks);
    }

    private Result<Unit> validateBranch(IssueFacilityContractCommand command, FacilityContractContext context) {
        String branchCode = command.branchCode();
        BranchCode facilityBranchCode =
                context.facility().getLoanApplication().getBranch().code();
        Result<List<BranchCode>> result = branchCoveragePort.coveredBranches(facilityBranchCode);

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
