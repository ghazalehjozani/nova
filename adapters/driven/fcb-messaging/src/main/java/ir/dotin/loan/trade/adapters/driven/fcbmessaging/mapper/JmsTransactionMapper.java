package ir.dotin.loan.trade.adapters.driven.fcbmessaging.mapper;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.loan.baseloan.core.domain.shared.enums.transaction.Direction;
import ir.dotin.loan.baseloan.core.domain.shared.enums.transaction.TransactionStatus;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTransaction;
import ir.dotin.loan.baseloan.core.domain.shared.vo.TrackedTransactionNumber;
import ir.dotin.loan.baseloan.core.domain.shared.vo.TransactionNumber;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.AccountNumberTarget;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.AccountTarget;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.Article;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.BoxTarget;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.DepositTarget;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbJmsParameter;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.i18n.FcbJmsLocalizedMessageCodes;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

/** Maps domain objects to/from JMS parameters for the transaction queue. Covers: TransactionPostingPort. */
@Slf4j
@UtilityClass
public class JmsTransactionMapper {

    // ── postTransaction ──

    public List<FcbJmsParameter> mapPostTransactionParams(LoanTransaction transaction, UUID trackingId) {
        List<FcbJmsParameter> params = new ArrayList<>();
        params.add(new FcbJmsParameter("trackingId", trackingId.toString()));
        params.add(new FcbJmsParameter(
                "facilityId", transaction.loanFacilityId().value().toString()));
        params.add(new FcbJmsParameter(
                "branchCode", transaction.document().branchCode().value()));
        params.add(new FcbJmsParameter("documentComment", transaction.document().description()));

        List<Article> articles = transaction.document().articles();
        for (int i = 0; i < articles.size(); i++) {
            Article article = articles.get(i);
            String prefix = "article[" + i + "].";

            // Extract target identifier via pattern matching on sealed ArticleTarget
            switch (article.target()) {
                case AccountTarget accountTarget -> {
                    params.add(new FcbJmsParameter(prefix + "targetType", "ACCOUNT"));
                    params.add(new FcbJmsParameter(
                            prefix + "targetId", accountTarget.accountId().value()));
                }
                case AccountNumberTarget accountNumberTarget -> {
                    params.add(new FcbJmsParameter(prefix + "targetType", "ACCOUNT"));
                    params.add(new FcbJmsParameter(
                            prefix + "targetId",
                            accountNumberTarget.accountNumber().accountNumber()));
                }
                case DepositTarget depositTarget -> {
                    params.add(new FcbJmsParameter(prefix + "targetType", "DEPOSIT"));
                    params.add(new FcbJmsParameter(
                            prefix + "targetId", depositTarget.depositNumber().value()));
                }
                case BoxTarget ignored -> {
                    params.add(new FcbJmsParameter(prefix + "targetType", "BOX"));
                    params.add(new FcbJmsParameter(prefix + "targetId", ""));
                }
            }

            params.add(new FcbJmsParameter(
                    prefix + "amount", article.amount().value().toString()));
            params.add(new FcbJmsParameter(
                    prefix + "currency", article.amount().currency().getCode()));
            params.add(
                    new FcbJmsParameter(prefix + "isDebtor", String.valueOf(article.direction() == Direction.DEBIT)));
        }

        return params;
    }

    public Result<TrackedTransactionNumber> mapToTrackedTransactionNumber(
            Map<String, Object> payload, UUID trackingId, Clock clock) {

        String transactionCode = asString(payload.get("transactionCode"));
        if (transactionCode == null || transactionCode.isBlank()) {
            return Result.failure(
                    Notification.ofError(FcbJmsLocalizedMessageCodes.JMS_INVALID_RESPONSE, "postTransaction"));
        }

        Result<TransactionNumber> txnResult = TransactionNumber.of(transactionCode);
        if (txnResult.isFailure()) {
            return Result.failure(txnResult.notification());
        }

        return Result.success(TrackedTransactionNumber.create(
                txnResult.orElseThrow().value(), trackingId.toString(), TransactionStatus.POSTED, clock));
    }

    // ── reverseTransaction ──

    public List<FcbJmsParameter> mapReverseTransactionParams(TrackedTransactionNumber transactionNumber) {
        return List.of(new FcbJmsParameter("transactionNumber", transactionNumber.value()));
    }

    public Result<Void> mapToReverseResult(Map<String, Object> payload) {
        String transactionNumber = asString(payload.get("transactionNumber"));
        if (transactionNumber == null) {
            return Result.failure(
                    Notification.ofError(FcbJmsLocalizedMessageCodes.JMS_INVALID_RESPONSE, "reverseTransaction"));
        }
        return Result.success();
    }

    // ── Helper ──

    private String asString(Object value) {
        return value != null ? value.toString() : null;
    }
}
