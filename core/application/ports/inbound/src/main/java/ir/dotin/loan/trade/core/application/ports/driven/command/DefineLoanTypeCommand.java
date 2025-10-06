package ir.dotin.loan.trade.core.application.ports.driven.command;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import ir.dotin.platform.commons.domain.vo.CurrencyType;
import ir.dotin.platform.commons.domain.vo.ValueType;
import ir.dotin.platform.dispatcher.api.command.Command;
import ir.dotin.loan.baseloan.core.domain.loantype.enums.SegmentType;
import ir.dotin.loan.baseloan.core.domain.shared.enums.GatewayType;
import ir.dotin.loan.trade.core.domain.loantype.enums.TradeRelationType;

public record DefineLoanTypeCommand(
        @NotNull UUID uid,
        @NotNull Long version,
        @NotNull LoanTypeCodeDto code,
        @NotNull TitleDto title,
        @NotNull GatewayType gatewayType,
        @NotNull LoanApplicationStatusDto loanApplicationAllowed,
        @NotNull SegmentType segmentType,
        @NotNull Set<EconomicSectorCurrencyDto> economicSectorCurrencies,
        @NotNull Set<LoanArrangementIdDto> loanArrangementIds,
        @NotNull Set<IncomeIdDto> incomeIds,
        @NotNull LoanTypeGroupIdDto groupId,
        @NotNull List<AttributeDto> attributes,
        @NotNull List<RelationTypeLoanTopicDto> relationTypeLoanTopics)
        implements Command {

    public record LoanTypeCodeDto(@NotBlank String value) {}

    public record TitleDto(@NotBlank String value) {}

    public record LoanApplicationStatusDto(boolean isAllowed) {}

    public record EconomicSectorCurrencyDto(
            @NotNull EconomicSectorDto economicSector,
            @NotNull CurrencyType currencyType) {} // TODO: use CurrencyTypeDto

    public record EconomicSectorDto(@NotBlank String code, @NotBlank String name) {}

    public record AttributeDto(
            @NotBlank String name, @NotBlank String code, @NotNull ValueType dataType, boolean mandatory) {}

    public record LoanArrangementIdDto(@NotNull UUID value) {}

    public record IncomeIdDto(@NotNull UUID value) {}

    public record LoanTypeGroupIdDto(@NotNull UUID value) {}

    public record RelationTypeLoanTopicDto(
            @NotNull TradeRelationType relationTypeKey,
            @NotNull RelationTypeDto relationType,
            @NotBlank String topicName,
            @NotBlank String topicCode,
            @NotNull EconomicSectorDto economicSector) {}

    public record RelationTypeDto(@NotBlank String code, @NotBlank String name) {}
}
