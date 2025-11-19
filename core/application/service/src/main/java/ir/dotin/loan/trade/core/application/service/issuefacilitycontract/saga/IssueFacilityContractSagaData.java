package ir.dotin.loan.trade.core.application.service.issuefacilitycontract.saga;

import java.util.Map;
import java.util.UUID;

import ir.dotin.loan.baseloan.core.domain.shared.enums.RelationType;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTransaction;
import ir.dotin.loan.baseloan.core.domain.shared.vo.TrackedTransactionNumber;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.AccountId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.TransactionConfig;

public record IssueFacilityContractInput(
        LoanFacilityId facilityId,
        String branchCode,
        TransactionConfig transactionConfig,
        LoanTransaction preparedTransaction,
        TrackedTransactionNumber postedTransactionNumber,
        Map<RelationType<?>, AccountId> accountIds) {

    public static IssueFacilityContractInput initial(
            UUID facilityId, String branchCode, TransactionConfig transactionConfig) {
        return new IssueFacilityContractInput(
                LoanFacilityId.of(facilityId), branchCode, transactionConfig, null, null, null);
    }

    public IssueFacilityContractInput withPreparedTransaction(
            LoanTransaction transaction, Map<RelationType<?>, AccountId> accountIds) {
        return new IssueFacilityContractInput(
                facilityId, branchCode, transactionConfig, transaction, postedTransactionNumber, accountIds);
    }

    public IssueFacilityContractInput withPostedTransaction(TrackedTransactionNumber transactionNumber) {
        return new IssueFacilityContractInput(
                facilityId, branchCode, transactionConfig, preparedTransaction, transactionNumber, accountIds);
    }
}
