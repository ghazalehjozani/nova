package ir.dotin.loan.trade.core.application.ports.inbound.command;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import ir.dotin.platform.dispatcher.api.command.Command;
import ir.dotin.loan.baseloan.core.domain.shared.enums.GatewayType;
import ir.dotin.loan.trade.core.domain.loantype.enums.TradeRelationType;

import lombok.Builder;

@Builder(toBuilder = true)
public record DefineLoanTypeCommand(
        @NotNull UUID uid,
        @Nullable Long version,
        @NotNull @Valid LoanTypeCodeDto code,
        @NotNull @Valid TitleDto title,
        @NotNull @Valid GatewayType gatewayType,
        @NotNull @Valid LoanApplicationStatusDto loanApplicationAllowed,
        @NotNull @Valid Set<EconomicSectorCurrencyDto> economicSectorCurrencies,
        @NotNull @Valid Set<LoanArrangementCodeDto> loanArrangementCodes,
        @NotNull @Valid List<RelationTypeLoanTopicDto> relationTypeLoanTopics)
        implements Command {

    public record LoanTypeCodeDto(@NotBlank @Pattern(regexp = "^\\d+$") String value) {}

    public record TitleDto(@NotBlank String value) {}

    public record LoanApplicationStatusDto(boolean isAllowed) {}

    public record EconomicSectorCurrencyDto(
            @NotNull @Valid EconomicSectorDto economicSector,
            @Valid @NotNull Set<@NotNull CurrencyTypeDto> currencyTypes) {}

    public record EconomicSectorDto(@NotBlank String code) {}

    public record LoanArrangementCodeDto(@NotNull @Pattern(regexp = "^\\d+$") String value) {}

    public record CurrencyTypeDto(@NotBlank String value) {}

    public record RelationTypeLoanTopicDto(
            @NotNull TradeRelationType relationType,
            @NotBlank String topicName,
            @NotBlank String topicCode,
            @NotNull @Valid Set<@NotNull EconomicSectorDto> economicSectors) {}
}
