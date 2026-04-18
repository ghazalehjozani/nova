package ir.dotin.loan.trade.adapters.driving.contract.mapper;

import java.util.UUID;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import ir.dotin.loan.trade.adapters.driving.contract.dto.AddFacilityCollateralRequest;
import ir.dotin.loan.trade.core.application.ports.inbound.command.AddFacilityCollateralCommand;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedSourcePolicy = ReportingPolicy.WARN,
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AddFacilityCollateralRequestToCommandMapper {

    @Mapping(target = "version", source = "request.version")
    @Mapping(target = "uid", ignore = true)
    AddFacilityCollateralCommand toCommand(UUID loanFacilityId, AddFacilityCollateralRequest request);

    AddFacilityCollateralCommand.CollateralDto mapCollateralDto(AddFacilityCollateralRequest.CollateralDto dto);

    AddFacilityCollateralCommand.MoneyDto mapMoneyDto(AddFacilityCollateralRequest.MoneyDto dto);
}
