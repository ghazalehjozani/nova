package ir.dotin.loan.trade.core.application.service.irregularprogressivedisbursement.workflow;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
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

public record IrregularDisbursementData(
        UUID facilityId,
        String branchCode,
        TransactionConfig transactionConfig,
        @Nullable LocalDate disbursementDate,
        long expectedVersion,
        BigDecimal trancheAmount,
        String currencyCode,
        int trancheNumber,
        @Nullable List<InstallmentSpecData> customPlanSpecs,
        List<InstallmentSpecData> approvedPlanSpecs,
        @Nullable Map<String, String> resolvedAccounts,
        @Nullable List<PostedTransactionData> postedTransactions,
        @Nullable Map<String, String> postedAccountIds) {

    public static IrregularDisbursementData initial(
            UUID facilityId,
            String branchCode,
            TransactionConfig transactionConfig,
            @Nullable LocalDate disbursementDate,
            long expectedVersion,
            BigDecimal trancheAmount,
            String currencyCode,
            int trancheNumber,
            @Nullable List<InstallmentSpecData> customPlanSpecs,
            List<InstallmentSpecData> approvedPlanSpecs) {
        return new IrregularDisbursementData(
                facilityId,
                branchCode,
                transactionConfig,
                disbursementDate,
                expectedVersion,
                trancheAmount,
                currencyCode,
                trancheNumber,
                customPlanSpecs,
                approvedPlanSpecs,
                null,
                null,
                null);
    }

    public IrregularDisbursementData withResolvedAccounts(Map<String, String> accounts) {
        return new IrregularDisbursementData(
                facilityId,
                branchCode,
                transactionConfig,
                disbursementDate,
                expectedVersion,
                trancheAmount,
                currencyCode,
                trancheNumber,
                customPlanSpecs,
                approvedPlanSpecs,
                accounts,
                postedTransactions,
                postedAccountIds);
    }

    public IrregularDisbursementData withPostedTransactions(
            List<PostedTransactionData> transactions, Map<String, String> accountIds) {
        return new IrregularDisbursementData(
                facilityId,
                branchCode,
                transactionConfig,
                disbursementDate,
                expectedVersion,
                trancheAmount,
                currencyCode,
                trancheNumber,
                customPlanSpecs,
                approvedPlanSpecs,
                resolvedAccounts,
                transactions,
                accountIds);
    }

    public ResolvedAccounts getResolvedAccounts() {
        if (resolvedAccounts == null || resolvedAccounts.isEmpty()) {
            return new ResolvedAccounts(Collections.emptyMap());
        }
        return new ResolvedAccounts(toAccountIdMap(resolvedAccounts));
    }

    public Map<RelationType<?>, AccountId> getPostedAccountIds() {
        if (postedAccountIds == null || postedAccountIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return toAccountIdMap(postedAccountIds);
    }

    private static Map<RelationType<?>, AccountId> toAccountIdMap(Map<String, String> serialized) {
        return serialized.entrySet().stream()
                .collect(Collectors.toMap(e -> TradeRelationType.valueOf(e.getKey()), e -> new AccountId(e.getValue())));
    }

    public record InstallmentSpecData(
            int sequenceNumber, LocalDate dueDate, BigDecimal principalAmount, BigDecimal interestAmount) {}

    public record PostedTransactionData(String transactionNumber, String trackingId, TransactionStatus status) {}
}
