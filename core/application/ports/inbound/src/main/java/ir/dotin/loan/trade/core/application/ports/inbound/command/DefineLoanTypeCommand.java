package ir.dotin.loan.trade.core.application.ports.inbound.command;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import ir.dotin.platform.dispatcher.api.command.Command;
import ir.dotin.loan.baseloan.core.domain.loantype.enums.SegmentType;
import ir.dotin.loan.baseloan.core.domain.shared.enums.GatewayType;
import ir.dotin.loan.trade.core.domain.loantype.enums.TradeRelationType;

public record DefineLoanTypeCommand(
        @NotNull UUID uid,
        @Nullable Long version,
        @NotNull LoanTypeCodeDto code,
        @NotNull TitleDto title,
        @NotNull GatewayType gatewayType,
        @NotNull LoanApplicationStatusDto loanApplicationAllowed,
        @NotNull SegmentType segmentType,
        @NotNull Set<EconomicSectorCurrencyDto> economicSectorCurrencies,
        @NotNull Set<LoanArrangementIdDto> loanArrangementIds,
        @Nullable Set<IncomeIdDto> incomeIds,
        @Nullable LoanTypeGroupIdDto groupId,
        @NotNull List<RelationTypeLoanTopicDto> relationTypeLoanTopics)
        implements Command {

    public record LoanTypeCodeDto(@NotBlank String value) {}

    public record TitleDto(@NotBlank String value) {}

    public record LoanApplicationStatusDto(boolean isAllowed) {}

    public record EconomicSectorCurrencyDto(
            @NotNull EconomicSectorDto economicSector, @NotNull Set<CurrencyTypeDto> currencyTypes) {}

    public record EconomicSectorDto(@NotBlank String code) {}

    public record LoanArrangementIdDto(@NotNull UUID value) {}

    public record IncomeIdDto(@NotNull UUID value) {}

    public record LoanTypeGroupIdDto(@NotNull UUID value) {}

    public record CurrencyTypeDto(@NotBlank String value) {}

    public record RelationTypeLoanTopicDto(
            @NotNull TradeRelationType relationType,
            @NotBlank String topicName,
            @NotBlank String topicCode,
            @NotNull Set<EconomicSectorDto> economicSectors) {}
}
