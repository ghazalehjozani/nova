package ir.dotin.loan.trade.adapters.driven.fcbmessaging.mapper;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import ir.dotin.platform.accounting.document.api.model.BranchCode;
import ir.dotin.platform.accounting.document.api.model.Document;
import ir.dotin.platform.accounting.document.api.model.IsoCode;
import ir.dotin.platform.pangaea.commons.core.Notification;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.loan.baseloan.core.domain.shared.error.LoanValidationErrors;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTransaction;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class LoanTransactionMerger {

    public static Result<LoanTransaction> merge(
            @NonNull Instant createdAt,
            @NonNull LoanFacilityId loanFacilityId,
            @NonNull String description,
            @NonNull BranchCode branchCode,
            @Nullable IsoCode isoCode,
            @NonNull List<LoanTransaction> transactions) {

        Notification notification = Notification.create();

        if (transactions.isEmpty()) {
            return Result.failure(
                    notification.addError(LoanValidationErrors.VALIDATION_FIELD_REQUIRED, "transactions"));
        }

        List<Document> documents =
                transactions.stream().map(LoanTransaction::document).toList();

        Result<Document> mergedDocResult = DocumentMerger.merge(description, branchCode, isoCode, documents);
        if (mergedDocResult.isFailure()) {
            return Result.failure(mergedDocResult.err().orElseThrow());
        }

        return LoanTransaction.ofWithDetails(createdAt, loanFacilityId, mergedDocResult.unwrap());
    }

    public static Result<LoanTransaction> merge(
            @NonNull Clock clock,
            @NonNull LoanFacilityId loanFacilityId,
            @NonNull String description,
            @NonNull List<LoanTransaction> transactions) {

        if (transactions.isEmpty()) {
            return Result.failure(
                    Notification.create().addError(LoanValidationErrors.VALIDATION_FIELD_REQUIRED, "transactions"));
        }

        LoanTransaction first = transactions.getFirst();
        Document firstDoc = first.document();

        return merge(
                clock.instant(),
                loanFacilityId,
                description,
                firstDoc.branchCode(),
                firstDoc.isoCode().orElse(null),
                transactions);
    }

    public static Result<LoanTransaction> mergeWithValidation(
            @NonNull String description, @NonNull List<LoanTransaction> transactions) {
        Notification notification = Notification.create();

        if (transactions.isEmpty()) {
            return Result.failure(
                    notification.addError(LoanValidationErrors.VALIDATION_FIELD_REQUIRED, "transactions"));
        }

        LoanFacilityId firstLoanId = transactions.getFirst().loanFacilityId();
        boolean allSameLoanId =
                transactions.stream().map(LoanTransaction::loanFacilityId).allMatch(firstLoanId::equals);

        if (!allSameLoanId) {
            return Result.failure(notification.addError(
                    LoanValidationErrors.VALIDATION_FIELD_REQUIRED,
                    "All transactions must have the same loanFacilityId"));
        }

        Instant earliestTime = transactions.stream()
                .map(LoanTransaction::createdAt)
                .min(Instant::compareTo)
                .orElseThrow();

        LoanTransaction first = transactions.getFirst();
        Document firstDoc = first.document();

        return merge(
                earliestTime,
                firstLoanId,
                description,
                firstDoc.branchCode(),
                firstDoc.isoCode().orElse(null),
                transactions);
    }
}
