package ir.dotin.loan.trade.adapters.driving.rest.command.mapper;

import java.util.Objects;
import java.util.UUID;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import ir.dotin.loan.trade.adapters.driving.rest.command.dto.AddFacilityCollateralRequest;
import ir.dotin.loan.trade.core.application.ports.inbound.command.AddFacilityCollateralCommand;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedSourcePolicy = ReportingPolicy.WARN,
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AddFacilityCollateralRequestToCommandMapper {

    @Mapping(target = "usedAmount", source = "request.usedAmount")
    @Mapping(target = "version", source = "request.version")
    @Mapping(target = "uid", source = "request.uid")
    @Mapping(target = "uid", ignore = true)
    AddFacilityCollateralCommand toCommand(
            UUID loanFacilityId, String collateralSerialDto, AddFacilityCollateralRequest request);

    default AddFacilityCollateralCommand.CollateralSerialDto mapCollateralSerialDto(String collateralSerialDto) {
        return Objects.isNull(collateralSerialDto)
                ? null
                : new AddFacilityCollateralCommand.CollateralSerialDto(collateralSerialDto);
    }

    AddFacilityCollateralCommand.MoneyDto mapMoneyDto(AddFacilityCollateralRequest.MoneyDto dto);
}
