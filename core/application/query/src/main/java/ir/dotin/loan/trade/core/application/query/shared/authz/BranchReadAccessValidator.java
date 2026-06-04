package ir.dotin.loan.trade.core.application.query.shared.authz;

import java.util.List;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import ir.dotin.platform.accounting.document.api.model.BranchCode;
import ir.dotin.platform.pangaea.commons.core.Notification;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.error.FailureCause;
import ir.dotin.platform.pangaea.commons.core.exception.FailureCauseException;
import ir.dotin.loan.trade.core.application.ports.outbound.client.error.CoreBankingErrors;
import ir.dotin.loan.trade.core.application.ports.outbound.client.loanservice.LoanServicePort;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class BranchReadAccessValidator {

    private final LoanServicePort loanServicePort;

    public void verifyCallerCoversBranch(@Nullable String callerBranchCode, @Nullable String facilityBranchCode) {
        if (callerBranchCode == null || facilityBranchCode == null) {
            return;
        }
        BranchCode facilityBranch = BranchCode.of(facilityBranchCode).unwrap();
        Result<List<BranchCode>> covered = loanServicePort.loadCoveredBranches(facilityBranch);
        if (covered.isFailure()) {
            throw new FailureCauseException(covered.err().orElseThrow());
        }
        boolean allowed =
                covered.unwrap().stream().anyMatch(branch -> branch.value().equals(callerBranchCode));
        if (!allowed) {
            throw new FailureCauseException(FailureCause.businessRule(
                    Notification.ofError(CoreBankingErrors.BRANCH_NOT_COVERED, callerBranchCode)));
        }
    }
}
