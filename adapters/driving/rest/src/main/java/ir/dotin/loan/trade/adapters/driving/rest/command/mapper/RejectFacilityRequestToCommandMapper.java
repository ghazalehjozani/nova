package ir.dotin.loan.trade.adapters.driving.rest.command.mapper;

import java.util.UUID;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import ir.dotin.loan.trade.adapters.driving.rest.command.dto.RejectFacilityRequest;
import ir.dotin.loan.trade.core.application.ports.inbound.command.RejectFacilityCommand;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedSourcePolicy = ReportingPolicy.WARN,
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface RejectFacilityRequestToCommandMapper {

    RejectFacilityCommand toCommand(UUID loanFacilityId, RejectFacilityRequest request);
}
