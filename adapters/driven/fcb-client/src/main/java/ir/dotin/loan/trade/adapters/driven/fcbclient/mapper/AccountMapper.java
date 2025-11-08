package ir.dotin.loan.trade.adapters.driven.fcbclient.mapper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.platform.commons.domain.vo.CurrencyType;
import ir.dotin.loan.baseloan.core.domain.shared.enums.transaction.Direction;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTransaction;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.AccountTarget;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.Article;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.ArticleTarget;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.BoxTarget;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.DepositTarget;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.Document;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.request.ExtraInfoVO;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.request.IssueDocumentRequest;
import ir.dotin.loan.trade.adapters.driven.fcbclient.i18n.FcbBusinessLocalizedMessageCodes;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class AccountMapper {

    private static final String ITEM_FORMAT_ACCOUNT = "ACCOUNT,%s,%s,%s";
    private static final String ITEM_FORMAT_DEPOSIT = "DEPOSIT,%s,%s,%s";
    private static final String ITEM_FORMAT_BOX = "BOX,%s,%s";

    public static Result<IssueDocumentRequest> mapToIssueDocumentRequest(
            LoanTransaction loanTransaction, UUID trackingId) {
        log.debug(
                "Mapping LoanTransaction to IssueDocumentRequest - facilityId: {}",
                loanTransaction.loanFacilityId().value());

        Notification notification = Notification.create();

        Notification validationResult = loanTransaction.validate();
        if (validationResult.hasErrors()) {
            log.error("LoanTransaction validation failed: {}", validationResult.getErrorMessages());
            notification.merge(validationResult);
            return Result.failure(notification);
        }

        Document document = loanTransaction.document();

        String branchCode = document.branchCode().value();
        if (branchCode.isBlank()) {
            notification.addError(FcbBusinessLocalizedMessageCodes.FCB_BAD_REQUEST, "Branch code is required");
            return Result.failure(notification);
        }

        String isoCode = extractIsoCode(loanTransaction);
        if (isoCode == null || isoCode.isBlank()) {
            notification.addError(FcbBusinessLocalizedMessageCodes.FCB_BAD_REQUEST, "ISO code (currency) is required");
            return Result.failure(notification);
        }

        String comment = document.description();
        if (comment.isBlank()) {
            notification.addError(FcbBusinessLocalizedMessageCodes.FCB_BAD_REQUEST, "Document description is required");
            return Result.failure(notification);
        }

        Result<List<String>> itemsResult = mapArticlesToItems(document.articles());
        if (itemsResult.isFailure()) {
            notification.merge(itemsResult.notification());
            return Result.failure(notification);
        }

        Result<List<String>> itemCommentsResult = mapArticlesToItemComments(document.articles());
        if (itemCommentsResult.isFailure()) {
            notification.merge(itemCommentsResult.notification());
            return Result.failure(notification);
        }

        String documentExtraInfoJson = createDocumentExtraInfoJson(loanTransaction);

        IssueDocumentRequest request = IssueDocumentRequest.builder()
                .transactionId(String.valueOf(trackingId))
                .comment(comment)
                .isoCode(isoCode)
                .branchCode(branchCode)
                .skipTransferMoneyBillNumber(false)
                .items(itemsResult.orElseThrow())
                .itemComments(itemCommentsResult.orElseThrow())
                .documentExtraInfo(documentExtraInfoJson)
                .build();

        log.info(
                "Successfully mapped LoanTransaction to IssueDocumentRequest - items count: {}",
                request.getItems().size());

        return Result.success(request);
    }

    private static String extractIsoCode(LoanTransaction loanTransaction) {
        if (loanTransaction.document().isoCode().isPresent()) {
            return loanTransaction.document().isoCode().get().value();
        }
        try {
            CurrencyType currency = loanTransaction.getTransactionCurrency();
            return String.valueOf(currency.getNumericCode());
        } catch (Exception e) {
            log.error("Failed to extract ISO code from transaction", e);
            return null;
        }
    }

    public static Result<List<String>> mapArticlesToItems(List<Article> articles) {
        Notification notification = Notification.create();
        List<String> items = new ArrayList<>();

        if (articles == null || articles.isEmpty()) {
            notification.addError(FcbBusinessLocalizedMessageCodes.FCB_BAD_REQUEST, "Articles list cannot be empty");
            return Result.failure(notification);
        }

        for (int i = 0; i < articles.size(); i++) {
            Article article = articles.get(i);
            Result<String> itemResult = mapArticleToItem(article, i);

            if (itemResult.isFailure()) {
                notification.merge(itemResult.notification());
            } else {
                items.add(itemResult.orElseThrow());
            }
        }

        if (notification.hasErrors()) {
            return Result.failure(notification);
        }

        log.debug("Mapped {} articles to items", items.size());
        return Result.success(items);
    }

    private static Result<String> mapArticleToItem(Article article, int index) {
        try {
            ArticleTarget target = article.target();
            boolean isDebtor = article.direction() == Direction.DEBIT;
            String amount = article.amount().value().toPlainString();

            String item =
                    switch (target) {
                        case AccountTarget accountTarget ->
                            String.format(
                                    ITEM_FORMAT_ACCOUNT,
                                    accountTarget.accountId().value(),
                                    isDebtor,
                                    amount);

                        case DepositTarget depositTarget ->
                            String.format(
                                    ITEM_FORMAT_DEPOSIT,
                                    depositTarget.depositNumber().value(),
                                    isDebtor,
                                    amount);

                        case BoxTarget boxTarget -> String.format(ITEM_FORMAT_BOX, isDebtor, amount);

                        default -> {
                            log.error(
                                    "Unknown article target type at index {}: {}",
                                    index,
                                    target.getClass().getName());
                            yield null;
                        }
                    };

            if (item == null) {
                return Result.failure(Notification.ofError(
                        FcbBusinessLocalizedMessageCodes.FCB_INVALID_RESPONSE,
                        "Unknown article target type at index " + index));
            }

            log.debug("Mapped article {} to item: {}", index, item);
            return Result.success(item);

        } catch (Exception e) {
            log.error("Error mapping article at index {} to item", index, e);
            return Result.failure(Notification.ofError(
                    FcbBusinessLocalizedMessageCodes.FCB_INVALID_RESPONSE,
                    "Failed to map article at index " + index + ": " + e.getMessage()));
        }
    }

    public static Result<List<String>> mapArticlesToItemComments(List<Article> articles) {
        Notification notification = Notification.create();
        List<String> itemComments = new ArrayList<>();

        if (articles == null || articles.isEmpty()) {
            notification.addError(FcbBusinessLocalizedMessageCodes.FCB_BAD_REQUEST, "Articles list cannot be empty");
            return Result.failure(notification);
        }

        for (Article article : articles) {
            try {
                String comment = buildItemComment(article);
                itemComments.add(comment);
            } catch (Exception e) {
                log.error("Error creating item comment for article", e);
                notification.addError(
                        FcbBusinessLocalizedMessageCodes.FCB_INVALID_RESPONSE,
                        "Failed to create item comment: " + e.getMessage());
            }
        }

        if (notification.hasErrors()) {
            return Result.failure(notification);
        }

        log.debug("Created {} item comments", itemComments.size());
        return Result.success(itemComments);
    }

    private static String buildItemComment(Article article) {
        String direction = article.direction() == Direction.DEBIT ? "بدهکاری" : "بستانکاری";
        String targetType = getTargetTypeInPersian(article.target());
        String identifier = extractTargetIdentifier(article.target());

        return String.format("بند سند %s %s - %s", direction, targetType, identifier);
    }

    private static String getTargetTypeInPersian(ArticleTarget target) {
        return switch (target) {
            case AccountTarget accountTarget -> "حساب";
            case DepositTarget depositTarget -> "سپرده";
            case BoxTarget boxTarget -> "صندوق";
            default -> "نامشخص";
        };
    }

    private static String extractTargetIdentifier(ArticleTarget target) {
        return switch (target) {
            case AccountTarget accountTarget -> accountTarget.accountId().value();
            case DepositTarget depositTarget -> depositTarget.depositNumber().value();
            case BoxTarget boxTarget -> "صندوق";
            default -> "";
        };
    }

    private static String createDocumentExtraInfoJson(LoanTransaction loanTransaction) {
        try {
            Map<String, Object> systemMetaData = new LinkedHashMap<>();
            systemMetaData.put(
                    "loanFacilityId", loanTransaction.loanFacilityId().value());
            systemMetaData.put("createdAt", loanTransaction.createdAt().toString());
            systemMetaData.put(
                    "branchCode", loanTransaction.document().branchCode().value());
            systemMetaData.put(
                    "totalDebit", loanTransaction.getTotalDebit().value().toPlainString());
            systemMetaData.put(
                    "totalCredit", loanTransaction.getTotalCredit().value().toPlainString());

            Map<String, Object> userData = new LinkedHashMap<>();
            userData.put("transactionType", "loan-transaction");
            userData.put("description", loanTransaction.document().description());

            ExtraInfoVO extraInfo = ExtraInfoVO.builder()
                    .type("document")
                    .scope("loan-facility")
                    .token(String.valueOf(loanTransaction.loanFacilityId().value()))
                    .systemMetaData(systemMetaData)
                    .userMetaData(List.of(userData))
                    .build();

            String json = extraInfo.toJsonString();
            log.debug("Created document extra info JSON: {}", json);
            return json;

        } catch (Exception e) {
            log.error("Failed to create document extra info JSON", e);
            return "{}";
        }
    }
}
