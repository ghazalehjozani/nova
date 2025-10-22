package ir.dotin.loan.trade.adapters.driving.rest.command.mapper;

import java.util.UUID;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import ir.dotin.loan.trade.adapters.driving.rest.command.dto.IssueFacilityContractRequest;
import ir.dotin.loan.trade.core.application.ports.inbound.command.IssueFacilityContractCommand;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedSourcePolicy = ReportingPolicy.WARN,
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface IssueFacilityContractRequestToCommandMapper {

    IssueFacilityContractCommand toCommand(UUID loanFacilityId, IssueFacilityContractRequest request);
}
