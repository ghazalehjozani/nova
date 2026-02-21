package ir.dotin.loan.trade.adapters.driven.fcbmessaging.mapper;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
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
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request.ArticleDto;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request.PostTransactionRequest;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.response.TransactionResultKafkaResponse;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.i18n.FcbKafkaLocalizedMessageCodes;

import lombok.experimental.UtilityClass;

@UtilityClass
public class KafkaTransactionMapper {

    public PostTransactionRequest mapToPostTransactionRequest(LoanTransaction transaction, UUID trackingId) {
        List<ArticleDto> articleDtos = new ArrayList<>();
        for (Article article : transaction.document().articles()) {
            String targetType;
            String targetId;
            switch (article.target()) {
                case AccountTarget accountTarget -> {
                    targetType = "ACCOUNT";
                    targetId = accountTarget.accountId().value();
                }
                case AccountNumberTarget accountNumberTarget -> {
                    targetType = "ACCOUNT";
                    targetId = accountNumberTarget.accountNumber().accountNumber();
                }
                case DepositTarget depositTarget -> {
                    targetType = "DEPOSIT";
                    targetId = depositTarget.depositNumber().value();
                }
                case BoxTarget ignored -> {
                    targetType = "BOX";
                    targetId = "";
                }
            }
            articleDtos.add(new ArticleDto(
                    targetType,
                    targetId,
                    article.amount().value(),
                    article.amount().currency().getCode(),
                    article.direction() == Direction.DEBIT));
        }

        return new PostTransactionRequest(
                trackingId.toString(),
                transaction.loanFacilityId().value().toString(),
                transaction.document().branchCode().value(),
                transaction.document().description(),
                articleDtos);
    }

    public Result<TrackedTransactionNumber> mapToTrackedTransactionNumber(
            TransactionResultKafkaResponse response, UUID trackingId, Clock clock) {
        if (response.getTransactionCode() == null || response.getTransactionCode().isBlank()) {
            return Result.failure(
                    Notification.ofError(FcbKafkaLocalizedMessageCodes.KAFKA_INVALID_RESPONSE, "postTransaction"));
        }
        Result<TransactionNumber> txnResult = TransactionNumber.of(response.getTransactionCode());
        if (txnResult.isFailure()) {
            return Result.failure(txnResult.notification());
        }
        return Result.success(TrackedTransactionNumber.create(
                txnResult.orElseThrow().value(), trackingId.toString(), TransactionStatus.POSTED, clock));
    }
}
