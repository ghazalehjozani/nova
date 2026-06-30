package ir.dotin.loan.trade.adapters.driving.contract.mapper;

import java.util.UUID;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import ir.dotin.loan.trade.adapters.driving.contract.dto.UpdateFacilityCollateralRequest;
import ir.dotin.loan.trade.core.application.ports.inbound.command.UpdateCollateralCommand;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedSourcePolicy = ReportingPolicy.WARN,
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UpdateFacilityCollateralRequestToCommandMapper {

    @Mapping(target = "version", source = "request.version")
    @Mapping(target = "uid", ignore = true)
    UpdateCollateralCommand toCommand(UUID loanFacilityId, UpdateFacilityCollateralRequest request);

    UpdateCollateralCommand.CollateralDto mapCollateralDto(UpdateFacilityCollateralRequest.CollateralDto dto);

    UpdateCollateralCommand.MoneyDto mapMoneyDto(UpdateFacilityCollateralRequest.MoneyDto dto);
}
