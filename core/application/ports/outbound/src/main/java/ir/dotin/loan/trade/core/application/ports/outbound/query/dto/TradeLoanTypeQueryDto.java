package ir.dotin.loan.trade.core.application.ports.outbound.query.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import ir.dotin.platform.commons.domain.vo.ValueType;
import ir.dotin.loan.baseloan.core.domain.loantype.enums.SegmentType;
import ir.dotin.loan.baseloan.core.domain.shared.enums.GatewayType;
import ir.dotin.loan.trade.core.domain.loantype.enums.TradeRelationType;

public record TradeLoanTypeQueryDto(
        UUID id,
        Long version,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt,
        String createdBy,
        String modifiedBy,
        LoanTypeCodeEmbDto code,
        TitleEmbDto title,
        EditReasonEmbDto editReason,
        GatewayType gatewayType,
        Boolean loanApplicationAllowed,
        SegmentType segmentType,
        Set<EconomicSectorCurrencyEmbDto> economicSectorCurrencies,
        Set<UUID> incomeIds,
        Set<UUID> loanArrangementIds,
        Set<RelationTypeLoanTopicEmbDto> relationTypeLoanTopics,
        List<AttributeEmbDto> attributes,
        UUID groupId,
        boolean active,
        boolean disable,
        UUID previousVersion)
        implements Serializable {

    public record LoanTypeCodeEmbDto(String value) implements Serializable {}

    public record TitleEmbDto(String value) implements Serializable {}

    public record EditReasonEmbDto(String editReason) implements Serializable {}

    public record EconomicSectorCurrencyEmbDto(
            String economicSectorCode, String economicSectorName, CurrencyTypeEmbDto currencyType)
            implements Serializable {

        public record CurrencyTypeEmbDto(String value) implements Serializable {}
    }

    public record RelationTypeLoanTopicEmbDto(
            TradeRelationType tradeRelationType,
            String topicName,
            String topicCode,
            String relationTypeCode,
            String relationTypeName,
            EconomicSectorEmbDto economicSector)
            implements Serializable {

        public record EconomicSectorEmbDto(String code, String name) implements Serializable {}
    }

    public record AttributeEmbDto(String name, String code, ValueType dataType, boolean mandatory)
            implements Serializable {}
}
