package ir.dotin.loan.trade.adapters.driving.rest.command.mapper;

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
    DefineLoanTypeCommand toCommand(DefineLoanTypeRequest request);
}
