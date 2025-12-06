package ir.dotin.loan.trade.adapters.driving.rest.command.mapper;

import java.util.stream.Collectors;

import org.mapstruct.*;

import ir.dotin.loan.trade.adapters.driving.rest.command.dto.DefineLoanTypeRequest;
import ir.dotin.loan.trade.core.application.ports.inbound.command.DefineLoanTypeCommand;
import ir.dotin.loan.trade.core.application.ports.inbound.dto.*;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedSourcePolicy = ReportingPolicy.WARN,
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DefineLoanTypeRequestToCommandMapper {

    @Mapping(target = "version", ignore = true)
    @Mapping(target = "uid", ignore = true)
    @Mapping(source = "code", target = "code.value")
    @Mapping(source = "title", target = "title.value")
    @Mapping(source = "loanApplicationAllowed", target = "loanApplicationAllowed.isAllowed")
    DefineLoanTypeCommand toCommand(DefineLoanTypeRequest request);

    default DefineLoanTypeCommand.EconomicSectorCurrencyDto mapEconomicSectorCurrency(
            DefineLoanTypeRequest.EconomicSectorCurrencyDto economicSectorCurrency) {
        if (economicSectorCurrency == null) {
            return null;
        }
        return new DefineLoanTypeCommand.EconomicSectorCurrencyDto(
                new EconomicSectorDto(economicSectorCurrency.economicSectorCode()),
                economicSectorCurrency.currencyTypes().stream()
                        .map(CurrencyTypeDto::new)
                        .collect(Collectors.toSet()));
    }

    default LoanArrangementCodeDto mapLoanArrangementCode(String loanArrangementCode) {
        return loanArrangementCode != null ? new LoanArrangementCodeDto(loanArrangementCode) : null;
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
                        .map(EconomicSectorDto::new)
                        .collect(Collectors.toSet()));
    }
}
