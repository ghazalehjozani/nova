package ir.dotin.loan.trade.adapters.driving.rest.command.mapper;

import java.util.UUID;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import ir.dotin.loan.trade.adapters.driving.rest.command.dto.RegularDisbursementRequest;
import ir.dotin.loan.trade.core.application.ports.inbound.command.IssueFacilityContractCommand;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedSourcePolicy = ReportingPolicy.WARN,
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface RegularDisbursementRequestToCommandMapper {

    @Mapping(target = "branchCode", ignore = true)
    IssueFacilityContractCommand toCommand(
            UUID loanFacilityId, RegularDisbursementRequest request); // TODO: no need mapper
}
