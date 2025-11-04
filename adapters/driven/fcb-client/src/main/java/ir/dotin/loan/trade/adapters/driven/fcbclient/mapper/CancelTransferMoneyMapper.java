package ir.dotin.loan.trade.adapters.driven.fcbclient.mapper;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.loan.baseloan.core.domain.shared.enums.transaction.Direction;
import ir.dotin.loan.baseloan.core.domain.shared.vo.BranchCode;
import ir.dotin.loan.baseloan.core.domain.shared.vo.FailureReason;
import ir.dotin.loan.baseloan.core.domain.shared.vo.TransactionNumber;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.AccountTarget;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.Article;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.ArticleTarget;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.BoxTarget;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.DepositTarget;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.request.ExtraInfoVO;
import ir.dotin.loan.trade.adapters.driven.fcbclient.i18n.FcbBusinessLocalizedMessageCodes;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class CancelTransferMoneyMapper {

    public static Result<ExtraInfoVO> mapToExtraInfo(
            String transactionId,
            TransactionNumber transactionNumber,
            List<Article> articles,
            BranchCode branchCode,
            FailureReason failureReason) {
        try {

            Map<String, Object> systemMetaData =
                    buildSystemMetadata(transactionId, transactionNumber, articles, branchCode, failureReason);

            List<Map<String, Object>> userMetaData = buildUserMetadata(articles);

            ExtraInfoVO extraInfo = ExtraInfoVO.builder()
                    .type("cancellation")
                    .scope("loan-transfer")
                    .token(transactionId)
                    .systemMetaData(systemMetaData)
                    .userMetaData(userMetaData)
                    .build();

            return Result.success(extraInfo);

        } catch (Exception e) {
            log.error("Error mapping CancelTransferMoneyRequest to ExtraInfo", e);
            return Result.failure(Notification.ofError(
                    FcbBusinessLocalizedMessageCodes.FCB_INVALID_RESPONSE,
                    "Failed to map cancellation request: " + e.getMessage()));
        }
    }

    private static Map<String, Object> buildSystemMetadata(
            String transactionId,
            TransactionNumber transactionNumber,
            List<Article> articles,
            BranchCode branchCode,
            FailureReason failureReason) {
        Map<String, Object> systemMetaData = new HashMap<>();

        systemMetaData.put("transactionId", transactionId);
        systemMetaData.put("transactionNumber", transactionNumber.value());
        systemMetaData.put("cancelTimestamp", Instant.now().toString());
        systemMetaData.put("branchCode", branchCode != null ? branchCode : "");
        systemMetaData.put("operatorId", "loanSystem");

        Map<String, Object> articlesSummary = buildArticlesSummary(articles);
        systemMetaData.put("articles", articlesSummary);

        if (failureReason != null) {
            systemMetaData.put("cancellationReason", failureReason);
        }

        log.debug("Built system metadata with {} entries", systemMetaData.size());
        return systemMetaData;
    }

    private static Map<String, Object> buildArticlesSummary(List<Article> articles) {
        Map<String, Object> summary = new HashMap<>();

        summary.put("totalArticles", articles.size());
        summary.put("debitCount", countArticlesByDirection(articles, Direction.DEBIT));
        summary.put("creditCount", countArticlesByDirection(articles, Direction.CREDIT));

        Map<String, String> amounts = new HashMap<>();
        amounts.put("totalDebitAmount", calculateTotalAmount(articles, Direction.DEBIT));
        amounts.put("totalCreditAmount", calculateTotalAmount(articles, Direction.CREDIT));
        summary.put("amounts", amounts);

        Map<String, Integer> articleTypes = new HashMap<>();
        articleTypes.put("accounts", countArticlesByTargetType(articles, AccountTarget.class));
        articleTypes.put("deposits", countArticlesByTargetType(articles, DepositTarget.class));
        articleTypes.put("boxes", countArticlesByTargetType(articles, BoxTarget.class));
        summary.put("articleTypes", articleTypes);

        return summary;
    }

    private static List<Map<String, Object>> buildUserMetadata(List<Article> articles) {
        List<Map<String, Object>> userMetaData = new ArrayList<>();

        Map<String, Object> cancellationEvent = new HashMap<>();
        cancellationEvent.put("action", "CANCEL_TRANSFER_MONEY");
        cancellationEvent.put("timestamp", Instant.now().toString());
        userMetaData.add(cancellationEvent);

        for (int i = 0; i < articles.size(); i++) {
            Article article = articles.get(i);
            Map<String, Object> articleMetadata = buildArticleMetadata(article, i);
            userMetaData.add(articleMetadata);
        }

        log.debug("Built user metadata with {} entries", userMetaData.size());
        return userMetaData;
    }

    private static Map<String, Object> buildArticleMetadata(Article article, int index) {
        Map<String, Object> metadata = new HashMap<>();

        metadata.put("articleIndex", index);
        metadata.put("direction", article.direction().name());
        metadata.put("amount", article.amount().value().toPlainString());
        metadata.put("currency", article.amount().currency().getCode());
        metadata.put("comment", article.comment());

        Map<String, Object> targetInfo = extractTargetInfo(article.target());
        metadata.put("target", targetInfo);

        metadata.put("metadata", article.articleMetadata().toString());

        return metadata;
    }

    private static Map<String, Object> extractTargetInfo(ArticleTarget target) {
        Map<String, Object> targetInfo = new HashMap<>();

        switch (target) {
            case AccountTarget accountTarget -> {
                targetInfo.put("type", "ACCOUNT");
                targetInfo.put("identifier", accountTarget.accountId().value());
            }
            case DepositTarget depositTarget -> {
                targetInfo.put("type", "DEPOSIT");
                targetInfo.put("identifier", depositTarget.depositNumber().value());
            }
            case BoxTarget boxTarget -> {
                targetInfo.put("type", "BOX");
                targetInfo.put("identifier", "CASH");
            }
            default -> {
                targetInfo.put("type", "UNKNOWN");
                targetInfo.put("identifier", "");
            }
        }

        return targetInfo;
    }

    private static long countArticlesByDirection(List<Article> articles, Direction direction) {
        return articles.stream()
                .filter(article -> article.direction() == direction)
                .count();
    }

    private static String calculateTotalAmount(List<Article> articles, Direction direction) {
        return articles.stream()
                .filter(article -> article.direction() == direction)
                .map(article -> article.amount().value())
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add)
                .toPlainString();
    }

    private static int countArticlesByTargetType(List<Article> articles, Class<?> targetType) {
        return (int) articles.stream()
                .filter(article -> targetType.isInstance(article.target()))
                .count();
    }

    public static List<String> extractTargetIdentifiers(List<Article> articles) {
        return articles.stream()
                .map(article -> {
                    return switch (article.target()) {
                        case AccountTarget accountTarget ->
                            accountTarget.accountId().value();
                        case DepositTarget depositTarget ->
                            depositTarget.depositNumber().value();
                        case BoxTarget boxTarget -> "BOX";
                        default -> "UNKNOWN";
                    };
                })
                .collect(Collectors.toList());
    }

    public static String createCancellationSummary(TransactionNumber transactionNumber, List<Article> articles) {

        long debitCount = countArticlesByDirection(articles, Direction.DEBIT);
        long creditCount = countArticlesByDirection(articles, Direction.CREDIT);
        String totalDebit = calculateTotalAmount(articles, Direction.DEBIT);
        String totalCredit = calculateTotalAmount(articles, Direction.CREDIT);

        return String.format(
                "Cancelling transaction %s: %d debits (%s), %d credits (%s), %d total articles",
                transactionNumber.value(), debitCount, totalDebit, creditCount, totalCredit, articles.size());
    }
}
