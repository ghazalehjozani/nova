package ir.dotin.loan.trade.core.application.service.issuefacilitycontract.saga;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import ir.dotin.loan.baseloan.core.domain.shared.enums.RelationType;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTransaction;
import ir.dotin.loan.baseloan.core.domain.shared.vo.TrackedTransactionNumber;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.AccountId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.TransactionConfig;
import ir.dotin.loan.trade.core.domain.loantype.enums.TradeRelationType;

public record IssueFacilityContractSagaData(
        LoanFacilityId facilityId,
        String branchCode,
        TransactionConfig transactionConfig,
        LoanTransaction preparedTransaction,
        TrackedTransactionNumber postedTransactionNumber,
        Map<String, AccountId> accountIds) {

    public static IssueFacilityContractSagaData initial(
            UUID facilityId, String branchCode, TransactionConfig transactionConfig) {
        return new IssueFacilityContractSagaData(
                LoanFacilityId.of(facilityId), branchCode, transactionConfig, null, null, null);
    }

    public IssueFacilityContractSagaData withPreparedTransaction(
            LoanTransaction transaction, Map<RelationType<?>, AccountId> accountIds) {

        var stringKeyMap = accountIds.entrySet().stream()
                .collect(Collectors.toMap(e -> ((Enum<?>) e.getKey()).name(), Map.Entry::getValue));

        return new IssueFacilityContractSagaData(
                facilityId, branchCode, transactionConfig, transaction, postedTransactionNumber, stringKeyMap);
    }

    public IssueFacilityContractSagaData withPostedTransaction(TrackedTransactionNumber transactionNumber) {
        return new IssueFacilityContractSagaData(
                facilityId, branchCode, transactionConfig, preparedTransaction, transactionNumber, accountIds);
    }

    public Map<RelationType<?>, AccountId> getAccountIdsByRelationType() {
        if (accountIds == null) return Collections.emptyMap();

        return accountIds.entrySet().stream()
                .collect(Collectors.toMap(e -> resolveRelationType(e.getKey()), Map.Entry::getValue));
    }

    private RelationType<?> resolveRelationType(String key) {
        try {
            return TradeRelationType.valueOf(key);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Unknown RelationType in Saga Data: " + key, e);
        }
    }
}
