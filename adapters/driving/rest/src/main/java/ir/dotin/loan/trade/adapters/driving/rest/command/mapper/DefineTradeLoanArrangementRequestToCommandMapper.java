package ir.dotin.loan.trade.adapters.driving.rest.command.mapper;

import org.mapstruct.*;

import ir.dotin.loan.trade.adapters.driving.rest.command.dto.DefineTradeLoanArrangementRequest;
import ir.dotin.loan.trade.core.application.ports.inbound.command.DefineTradeLoanArrangementCommand;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedSourcePolicy = ReportingPolicy.WARN,
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DefineTradeLoanArrangementRequestToCommandMapper {

    @Mapping(target = "version", ignore = true)
    DefineTradeLoanArrangementCommand toCommand(DefineTradeLoanArrangementRequest request);
}
