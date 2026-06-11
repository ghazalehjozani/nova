package ir.dotin.loan.trade.core.application.service.issuefacilitycontract.workflow;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;

import ir.dotin.platform.accounting.document.api.enumeration.RelationType;
import ir.dotin.platform.accounting.document.api.enumeration.TransactionStatus;
import ir.dotin.platform.accounting.document.api.model.AccountId;
import ir.dotin.platform.accounting.document.api.model.TransactionConfig;
import ir.dotin.loan.baseloan.core.domain.shared.vo.ResolvedAccounts;
import ir.dotin.loan.trade.core.domain.loantype.enums.TradeRelationType;

public record ContractData(
        UUID facilityId,
        String branchCode,
        TransactionConfig transactionConfig,
        long expectedVersion,
        @Nullable String postedTransactionNumber,
        @Nullable String postedTrackingId,
        @Nullable Instant postedAt,
        @Nullable TransactionStatus transactionStatus,
        @Nullable Map<String, String> resolvedAccounts) {

    public static ContractData initial(
            UUID facilityId, String branchCode, TransactionConfig transactionConfig, long expectedVersion) {
        return new ContractData(
                facilityId, branchCode, transactionConfig, expectedVersion, null, null, null, null, null);
    }

    public ContractData withResolvedAccounts(Map<String, String> accounts) {
        return new ContractData(
                facilityId,
                branchCode,
                transactionConfig,
                expectedVersion,
                postedTransactionNumber,
                postedTrackingId,
                postedAt,
                transactionStatus,
                accounts);
    }

    public ContractData withPostedTransaction(
            String transactionNumber, String trackingId, TransactionStatus status, Instant posted) {
        return new ContractData(
                facilityId,
                branchCode,
                transactionConfig,
                expectedVersion,
                transactionNumber,
                trackingId,
                posted,
                status,
                resolvedAccounts);
    }

    public ResolvedAccounts getResolvedAccounts() {
        if (resolvedAccounts == null || resolvedAccounts.isEmpty()) {
            return new ResolvedAccounts(Collections.emptyMap());
        }
        Map<RelationType<?>, AccountId> accounts = resolvedAccounts.entrySet().stream()
                .collect(Collectors.toMap(e -> TradeRelationType.valueOf(e.getKey()), e -> new AccountId(e.getValue())));
        return new ResolvedAccounts(accounts);
    }

    public Map<RelationType<?>, AccountId> getAccountIdsByRelationType() {
        return getResolvedAccounts().accountsByRelationType();
    }
}
