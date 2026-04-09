package ir.dotin.loan.trade.adapters.driven.fcbclient.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import ir.dotin.platform.accounting.document.api.model.Article;
import ir.dotin.platform.accounting.document.api.model.BranchCode;
import ir.dotin.platform.accounting.document.api.model.Document;
import ir.dotin.platform.accounting.document.api.model.IsoCode;
import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.platform.commons.domain.vo.CurrencyType;
import ir.dotin.loan.baseloan.core.domain.shared.error.LoanValidationErrors;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class DocumentMerger {

    public static Result<Document> merge(
            @NonNull String description,
            @NonNull BranchCode branchCode,
            @Nullable IsoCode isoCode,
            @NonNull List<Document> documents) {

        Notification notification = Notification.create();

        if (documents.isEmpty()) {
            return Result.failure(notification.addError(LoanValidationErrors.VALIDATION_FIELD_REQUIRED, "documents"));
        }

        Optional<CurrencyType> firstCurrency = documents.getFirst().getDocumentCurrency();
        if (firstCurrency.isEmpty()) {
            return Result.failure(notification.addError(
                    LoanValidationErrors.VALIDATION_FIELD_REQUIRED, "currency in first document"));
        }

        for (int i = 1; i < documents.size(); i++) {
            Optional<CurrencyType> docCurrency = documents.get(i).getDocumentCurrency();
            if (docCurrency.isEmpty() || !docCurrency.get().equals(firstCurrency.get())) {
                return Result.failure(notification.addError(
                        LoanValidationErrors.DOCUMENT_INCONSISTENT_CURRENCIES,
                        documents.stream()
                                .map(Document::getDocumentCurrency)
                                .filter(Optional::isPresent)
                                .map(Optional::get)
                                .map(CurrencyType::getCode)
                                .distinct()
                                .collect(Collectors.joining(", "))));
            }
        }

        List<Article> mergedArticles = documents.stream()
                .flatMap(doc -> doc.articles().stream())
                .collect(Collectors.toCollection(ArrayList::new));

        return Document.of(description, branchCode, isoCode, mergedArticles);
    }

    public static Result<Document> merge(@NonNull String description, @NonNull List<Document> documents) {
        if (documents.isEmpty()) {
            return Result.failure(
                    Notification.create().addError(LoanValidationErrors.VALIDATION_FIELD_REQUIRED, "documents"));
        }

        Document first = documents.getFirst();
        return merge(description, first.branchCode(), first.isoCode().orElse(null), documents);
    }

    public static Result<Document> merge(@NonNull List<Document> documents) {
        if (documents.isEmpty()) {
            return Result.failure(
                    Notification.create().addError(LoanValidationErrors.VALIDATION_FIELD_REQUIRED, "documents"));
        }

        return merge("Merged Document", documents);
    }
}
