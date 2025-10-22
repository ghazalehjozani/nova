package ir.dotin.loan.trade.adapters.driving.rest.command.mapper;

import org.mapstruct.*;

import ir.dotin.loan.trade.adapters.driving.rest.command.dto.OpenFacilityCaseRequest;
import ir.dotin.loan.trade.core.application.ports.inbound.command.OpenFacilityCaseCommand;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedSourcePolicy = ReportingPolicy.WARN,
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface OpenFacilityCaseRequestToCommandMapper {

    @Mapping(target = "installmentSchedule", ignore = true)
    OpenFacilityCaseCommand toCommand(OpenFacilityCaseRequest request);
}
