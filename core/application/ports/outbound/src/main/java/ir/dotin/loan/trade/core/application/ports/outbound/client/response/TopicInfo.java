package ir.dotin.loan.trade.core.application.ports.outbound.client.response;

import org.jspecify.annotations.NonNull;

import ir.dotin.platform.pangaea.commons.core.Notification;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.domain.vo.ValueObject;

import static java.util.Objects.requireNonNull;

public record TopicInfo(
        Long id,
        String title,
        String code,
        Boolean isDebtor,
        Boolean isUnderLine,
        String type,
        Boolean hasOppositeAccount,
        String numOfOpenableAccounts,
        Boolean isPermanent)
        implements ValueObject<TopicInfo> {

    public TopicInfo {
        requireNonNull(code, "TopicInfo code cannot be null in constructor");
        requireNonNull(title, "TopicInfo title cannot be null in constructor");
    }

    public static Result<TopicInfo> of(
            Long id,
            @NonNull String title,
            @NonNull String code,
            Boolean isDebtor,
            Boolean isUnderLine,
            String type,
            Boolean hasOppositeAccount,
            String numOfOpenableAccounts,
            Boolean isPermanent) {

        TopicInfo topicInfo = new TopicInfo(
                id, title, code, isDebtor, isUnderLine, type, hasOppositeAccount, numOfOpenableAccounts, isPermanent);

        Notification internalValidation = topicInfo.validate();
        if (internalValidation.hasErrors()) {
            return Result.failure(internalValidation);
        }

        return Result.success(topicInfo);
    }
}
