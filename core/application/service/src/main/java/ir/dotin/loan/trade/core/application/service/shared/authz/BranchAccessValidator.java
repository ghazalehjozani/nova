package ir.dotin.loan.trade.core.application.service.shared.authz;

import java.util.List;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import ir.dotin.platform.accounting.document.api.model.BranchCode;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.Unit;
import ir.dotin.loan.trade.core.application.ports.outbound.client.error.CoreBankingErrors;
import ir.dotin.loan.trade.core.application.ports.outbound.client.loanservice.LoanServicePort;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class BranchAccessValidator {

    private final LoanServicePort loanServicePort;

    public Result<Unit> verifyCallerCoversFacility(@Nullable String callerBranchCode, TradeLoanFacility facility) {
        return verifyCallerCoversBranch(
                callerBranchCode, facility.getLoanApplication().getBranch().code());
    }

    public Result<Unit> verifyCallerCoversBranch(@Nullable String callerBranchCode, BranchCode facilityBranchCode) {
        if (callerBranchCode == null) {
            return Result.success();
        }
        Result<List<BranchCode>> coveredResult = loanServicePort.loadCoveredBranches(facilityBranchCode);
        if (coveredResult.isFailure()) {
            return Result.failure(coveredResult.err().orElseThrow());
        }
        boolean covered = coveredResult.unwrap().stream()
                .anyMatch(branch -> branch.value().equals(callerBranchCode));
        return covered ? Result.success() : Result.failure(CoreBankingErrors.BRANCH_NOT_COVERED, callerBranchCode);
    }
}
