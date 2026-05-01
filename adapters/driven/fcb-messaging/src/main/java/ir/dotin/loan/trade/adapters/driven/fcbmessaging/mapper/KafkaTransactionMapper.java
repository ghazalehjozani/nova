package ir.dotin.loan.trade.adapters.driven.fcbmessaging.mapper;

import java.time.Clock;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import ir.dotin.platform.accounting.document.api.enumeration.Direction;
import ir.dotin.platform.accounting.document.api.enumeration.RelationType;
import ir.dotin.platform.accounting.document.api.enumeration.TransactionStatus;
import ir.dotin.platform.accounting.document.api.model.Article;
import ir.dotin.platform.accounting.document.api.model.Document;
import ir.dotin.platform.accounting.document.api.model.target.AccountNumberTarget;
import ir.dotin.platform.accounting.document.api.model.target.AccountTarget;
import ir.dotin.platform.accounting.document.api.model.target.ArticleTarget;
import ir.dotin.platform.accounting.document.api.model.target.BoxTarget;
import ir.dotin.platform.accounting.document.api.model.target.DepositTarget;
import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.platform.commons.core.error.CoreCommonErrors;
import ir.dotin.platform.commons.domain.vo.CurrencyType;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTransaction;
import ir.dotin.loan.baseloan.core.domain.shared.vo.TrackedTransactionNumber;
import ir.dotin.loan.baseloan.core.domain.shared.vo.TransactionNumber;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request.ExtraInfoVO;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request.PostTransactionRequest;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.response.TransactionResultKafkaResponse;
import ir.dotin.loan.trade.core.application.ports.outbound.client.error.CoreBankingErrors;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@UtilityClass
public class KafkaTransactionMapper {

    private static final String ITEM_FORMAT_ACCOUNT = "ACCOUNT,%s,%s,%s";
    private static final String ITEM_FORMAT_DEPOSIT = "DEPOSIT,%s,%s,%s";
    private static final String ITEM_FORMAT_BOX = "BOX,%s,%s";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static Result<PostTransactionRequest> mapToIssueDocumentRequest(
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
            notification.addError(CoreCommonErrors.GENERAL_FIELD_REQUIRED, "Branch code");
            return Result.failure(notification);
        }

        String isoCode = extractIsoCode(loanTransaction);
        if (isoCode == null || isoCode.isBlank()) {
            notification.addError(CoreCommonErrors.GENERAL_FIELD_REQUIRED, "ISO code (currency)");
            return Result.failure(notification);
        }

        String comment = document.description();
        if (comment.isBlank()) {
            notification.addError(CoreCommonErrors.GENERAL_FIELD_REQUIRED, "Document description");
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

        PostTransactionRequest request = PostTransactionRequest.builder()
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
            notification.addError(CoreCommonErrors.GENERAL_FIELD_REQUIRED, "Articles list");
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
        ArticleTarget target = article.target();
        boolean isDebtor = article.direction() == Direction.DEBIT;
        String amount = article.amount().value().toPlainString();

        String item =
                switch (target) {
                    case AccountTarget(var accountId, RelationType<?> relationType) ->
                        String.format(ITEM_FORMAT_ACCOUNT, accountId.value(), isDebtor, amount);
                    case DepositTarget(var depositNumber) ->
                        String.format(ITEM_FORMAT_DEPOSIT, depositNumber.value(), isDebtor, amount);
                    case BoxTarget() -> String.format(ITEM_FORMAT_BOX, isDebtor, amount);
                    case AccountNumberTarget(var accountNumber) ->
                        String.format(ITEM_FORMAT_ACCOUNT, accountNumber.accountNumber(), isDebtor, amount);
                };

        log.debug("Mapped article {} to item: {}", index, item);
        return Result.success(item);
    }

    public static Result<List<String>> mapArticlesToItemComments(List<Article> articles) {
        Notification notification = Notification.create();
        List<String> itemComments = new ArrayList<>();

        if (articles == null || articles.isEmpty()) {
            notification.addError(CoreCommonErrors.GENERAL_FIELD_REQUIRED, "Articles list");
            return Result.failure(notification);
        }

        for (Article article : articles) {
            String comment = buildItemComment(article);
            itemComments.add(comment);
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
        Map<String, Object> systemMetaData = new LinkedHashMap<>();
        systemMetaData.put("loanFacilityId", loanTransaction.loanFacilityId().value());
        systemMetaData.put("createdAt", loanTransaction.createdAt().toString());
        systemMetaData.put("branchCode", loanTransaction.document().branchCode().value());
        systemMetaData.put("totalDebit", loanTransaction.getTotalDebit().value().toPlainString());
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

        String json = OBJECT_MAPPER.writeValueAsString(extraInfo);
        log.debug("Created document extra info JSON: {}", json);
        return json;
    }

    public Result<TrackedTransactionNumber> mapToTrackedTransactionNumber(
            TransactionResultKafkaResponse response, UUID trackingId, Clock clock) {
        if (response.getTransactionCode() == null
                || response.getTransactionCode().isBlank()) {
            return Result.failure(Notification.ofError(CoreBankingErrors.KAFKA_INVALID_RESPONSE, "postTransaction"));
        }
        Result<TransactionNumber> txnResult = TransactionNumber.of(response.getTransactionCode());
        if (txnResult.isFailure()) {
            return Result.failure(txnResult.notification());
        }
        return Result.success(TrackedTransactionNumber.create(
                txnResult.orElseThrow().value(), trackingId.toString(), TransactionStatus.POSTED, clock));
    }
}
