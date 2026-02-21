package ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.response;

import org.jspecify.annotations.Nullable;

public record TopicInfoDto(
        @Nullable Long id,
        @Nullable String title,
        @Nullable String code,
        @Nullable Boolean isDebtor,
        @Nullable Boolean isUnderLine,
        @Nullable String type,
        @Nullable Boolean hasOppositeAccount,
        @Nullable String numOfOpenableAccounts,
        @Nullable Boolean isPermanent) {}
