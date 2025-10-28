package ir.dotin.loan.trade.adapters.driving.rest.command.mapper;

import java.util.UUID;
import java.util.stream.Collectors;

import org.mapstruct.*;

import ir.dotin.loan.trade.adapters.driving.rest.command.dto.DefineLoanTypeRequest;
import ir.dotin.loan.trade.core.application.ports.inbound.command.DefineLoanTypeCommand;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedSourcePolicy = ReportingPolicy.WARN,
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DefineLoanTypeRequestToCommandMapper {

    @Mapping(target = "version", ignore = true)
    @Mapping(source = "code", target = "code.value")
    @Mapping(source = "title", target = "title.value")
    @Mapping(source = "loanApplicationAllowed", target = "loanApplicationAllowed.isAllowed")
    @Mapping(source = "economicSectorCurrencies", target = "economicSectorCurrencies")
    @Mapping(source = "loanArrangementIds", target = "loanArrangementIds")
    @Mapping(source = "incomeIds", target = "incomeIds")
    @Mapping(source = "groupId", target = "groupId.value")
    @Mapping(source = "relationTypeLoanTopics", target = "relationTypeLoanTopics")
    DefineLoanTypeCommand toCommand(DefineLoanTypeRequest request);

    default DefineLoanTypeCommand.EconomicSectorCurrencyDto mapEconomicSectorCurrency(
            DefineLoanTypeRequest.EconomicSectorCurrencyDto economicSectorCurrency) {
        if (economicSectorCurrency == null) {
            return null;
        }
        return new DefineLoanTypeCommand.EconomicSectorCurrencyDto(
                new DefineLoanTypeCommand.EconomicSectorDto(economicSectorCurrency.economicSectorCode()),
                economicSectorCurrency.currencyTypes().stream()
                        .map(DefineLoanTypeCommand.CurrencyTypeDto::new)
                        .collect(Collectors.toSet()));
    }

    default DefineLoanTypeCommand.LoanArrangementIdDto mapLoanArrangementId(UUID loanArrangementId) {
        return loanArrangementId != null ? new DefineLoanTypeCommand.LoanArrangementIdDto(loanArrangementId) : null;
    }

    default DefineLoanTypeCommand.IncomeIdDto mapIncomeId(UUID incomeId) {
        return incomeId != null ? new DefineLoanTypeCommand.IncomeIdDto(incomeId) : null;
    }

    default DefineLoanTypeCommand.LoanTypeGroupIdDto mapGroupId(UUID groupId) {
        return groupId != null ? new DefineLoanTypeCommand.LoanTypeGroupIdDto(groupId) : null;
    }

    default DefineLoanTypeCommand.RelationTypeLoanTopicDto mapRelationTypeLoanTopic(
            DefineLoanTypeRequest.RelationTypeLoanTopicDto relationTypeLoanTopic) {
        if (relationTypeLoanTopic == null) {
            return null;
        }
        return new DefineLoanTypeCommand.RelationTypeLoanTopicDto(
                relationTypeLoanTopic.relationType(),
                relationTypeLoanTopic.topicName(),
                relationTypeLoanTopic.topicCode(),
                relationTypeLoanTopic.economicSectorCodes().stream()
                        .map(DefineLoanTypeCommand.EconomicSectorDto::new)
                        .collect(Collectors.toSet()));
    }
}
