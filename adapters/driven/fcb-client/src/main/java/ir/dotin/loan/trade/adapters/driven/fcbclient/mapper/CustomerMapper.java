package ir.dotin.loan.trade.adapters.driven.fcbclient.mapper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.platform.commons.domain.vo.NationalCode;
import ir.dotin.loan.baseloan.core.domain.shared.enums.PartyType;
import ir.dotin.loan.baseloan.core.domain.shared.enums.transaction.Direction;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTransaction;
import ir.dotin.loan.baseloan.core.domain.shared.vo.customer.Party;
import ir.dotin.loan.baseloan.core.domain.shared.vo.customer.PersonName;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.AccountId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.AccountTarget;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.Article;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.BoxTarget;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.DepositNumber;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.DepositTarget;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.CustomerInfoResponse;
import ir.dotin.loan.trade.adapters.driven.fcbclient.i18n.FcbBusinessLocalizedMessageCodes;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class CustomerMapper {

    public static Result<List<String>> mapToFcbItems(LoanTransaction loanTransaction) {
        Notification notification = Notification.create();
        List<String> items = new ArrayList<>();

        List<Article> articles = loanTransaction.document().articles();

        for (int i = 0; i < articles.size(); i++) {
            Article article = articles.get(i);

            Result<String> itemResult = mapArticleToFcbItem(article, i);
            if (itemResult.isFailure()) {
                notification.merge(itemResult.notification());
            } else {
                items.add(itemResult.orElseThrow());
            }
        }

        if (notification.hasErrors()) {
            return Result.failure(notification);
        }

        return Result.success(items);
    }

    public static List<String> mapToFcbItemComments(LoanTransaction loanTransaction) {
        List<String> itemComments = new ArrayList<>();

        List<Article> articles = loanTransaction.document().articles();

        for (Article article : articles) {
            String direction = article.direction() == Direction.DEBIT ? "بدهکاری" : "بستانکاری";
            String targetType = getTargetTypeInPersian(article);
            String targetIdentifier = extractTargetIdentifier(article);
            String comment = String.format("بند سند %s %s - %s", direction, targetType, targetIdentifier);

            itemComments.add(comment);
        }

        return itemComments;
    }

    private static String extractTargetIdentifier(Article article) {
        return switch (article.target()) {
            case AccountTarget(AccountId accountId) -> accountId.value();
            case DepositTarget depositTarget -> depositTarget.depositNumber().value();
            case BoxTarget boxTarget -> "صندوق";
            default -> "";
        };
    }

    private static Result<String> mapArticleToFcbItem(Article article, int index) {
        String targetType;
        String targetIdentifier;

        switch (article.target()) {
            case AccountTarget(AccountId accountId) -> {
                targetType = "ACCOUNT";
                targetIdentifier = accountId.value();
            }
            case DepositTarget(DepositNumber depositNumber) -> {
                targetType = "DEPOSIT";
                targetIdentifier = depositNumber.value();
            }
            case BoxTarget boxTarget -> {
                targetType = "BOX";
                targetIdentifier = "";
            }
            default -> {
                log.error(
                        "Unknown article target type at index {}: {}",
                        index,
                        article.target().getClass());
                return Result.failure(Notification.ofError(
                        FcbBusinessLocalizedMessageCodes.FCB_INVALID_RESPONSE,
                        "Unknown article target type: "
                                + article.target().getClass().getSimpleName()));
            }
        }

        boolean isDebtor = article.direction() == Direction.DEBIT;

        BigDecimal amountValue = article.amount().value();

        String fcbItem;
        if (targetType.equals("BOX")) {
            fcbItem = String.format("%s,%s,%s", targetType, isDebtor, amountValue);
        } else {
            fcbItem = String.format("%s,%s,%s,%s", targetType, targetIdentifier, isDebtor, amountValue);
        }

        log.debug("Mapped article {} to FCB item: {}", index, fcbItem);
        return Result.success(fcbItem);
    }

    private static String getTargetTypeInPersian(Article article) {
        return switch (article.target()) {
            case AccountTarget accountTarget -> "حساب";
            case DepositTarget depositTarget -> "سپرده";
            case BoxTarget boxTarget -> "صندوق";
            default -> "نامشخص";
        };
    }

    public static String extractBranchCode(LoanTransaction loanTransaction) {
        return loanTransaction.document().branchCode().value();
    }

    public static String extractDocumentComment(LoanTransaction loanTransaction) {
        return loanTransaction.document().description();
    }

    public Result<Party> mapToDomainCustomerInfo(CustomerInfoResponse fcbResponse) {

        try {
            String firstName = fcbResponse.getFirstName();
            String lastName = fcbResponse.getLastName();

            PersonName personName = new PersonName(firstName, lastName);
            PartyType partyType = fcbResponse.getReal() ? PartyType.REAL : PartyType.LEGAL;
            Result<NationalCode> nationalCode = NationalCode.valueOf(fcbResponse.getNationalCode());

            if (fcbResponse.getCustomerNumber() == null) {
                log.error("FCB response missing customer number");
                return Result.failure(Notification.ofError(
                        FcbBusinessLocalizedMessageCodes.FCB_INVALID_RESPONSE,
                        "Customer number is missing in FCB response"));
            }

            Result<Party> result = Party.of(
                    String.valueOf(fcbResponse.getCustomerNumber()),
                    partyType,
                    personName,
                    nationalCode.getValue(),
                    fcbResponse.getIsInBlackList(),
                    fcbResponse.getIsIncapable(),
                    fcbResponse.getIsInGrayList());

            if (result.isFailure()) {
                log.error(
                        "Failed to create CustomerInfo: {}",
                        result.notification().getErrorMessages());
            }

            return result;

        } catch (Exception e) {
            log.error("Failed to map FCB response to domain CustomerInfo", e);
            return Result.failure(Notification.ofError(
                    FcbBusinessLocalizedMessageCodes.FCB_INVALID_RESPONSE,
                    "Failed to parse customer info: " + e.getMessage()));
        }
    }
}
