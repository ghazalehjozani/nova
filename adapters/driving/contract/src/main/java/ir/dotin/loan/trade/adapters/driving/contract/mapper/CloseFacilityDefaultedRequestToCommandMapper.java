package ir.dotin.loan.trade.adapters.driving.contract.mapper;

import java.util.UUID;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import ir.dotin.loan.trade.adapters.driving.contract.dto.CloseFacilityDefaultedRequest;
import ir.dotin.loan.trade.core.application.ports.inbound.command.CloseFacilityDefaultedCommand;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedSourcePolicy = ReportingPolicy.WARN,
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CloseFacilityDefaultedRequestToCommandMapper {

    @Mapping(target = "branchCode", ignore = true)
    CloseFacilityDefaultedCommand toCommand(UUID loanFacilityId, CloseFacilityDefaultedRequest request);
}
