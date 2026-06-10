package ir.dotin.loan.trade.core.application.query.loantype.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ir.dotin.platform.pangaea.servicelayer.api.query.QueryResult;
import ir.dotin.loan.baseloan.core.domain.shared.enums.GatewayType;
import ir.dotin.loan.trade.core.domain.loantype.enums.TradeRelationType;

public record TradeLoanTypeQueryDto(
        UUID id,
        Long version,
        @JsonIgnore LocalDateTime createdAt,
        @JsonIgnore LocalDateTime modifiedAt,
        @JsonIgnore String createdBy,
        @JsonIgnore String modifiedBy,
        LoanTypeCodeEmbDto code,
        TitleEmbDto title,
        EditReasonEmbDto editReason,
        GatewayType gatewayType,
        Boolean loanApplicationAllowed,
        Set<EconomicSectorCurrencyEmbDto> economicSectorCurrencies,
        Set<UUID> incomeIds,
        Set<UUID> loanArrangementIds,
        Set<RelationTypeLoanTopicEmbDto> relationTypeLoanTopics,
        UUID groupId,
        boolean active,
        boolean disable,
        UUID previousVersion)
        implements QueryResult {

    public record LoanTypeCodeEmbDto(String value) implements Serializable {}

    public record TitleEmbDto(String value) implements Serializable {}

    public record EditReasonEmbDto(String editReason) implements Serializable {}

    public record EconomicSectorCurrencyEmbDto(String economicSectorCode, Set<CurrencyTypeEmbDto> currencyTypes)
            implements Serializable {}

    public record CurrencyTypeEmbDto(String value) implements Serializable {}

    public record RelationTypeLoanTopicEmbDto(
            TradeRelationType tradeRelationType,
            String topicName,
            String topicCode,
            Set<EconomicSectorEmbDto> economicSectors)
            implements Serializable {

        public record EconomicSectorEmbDto(String code) implements Serializable {}
    }
}
