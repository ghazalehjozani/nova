package ir.dotin.loan.trade.adapters.driving.contract.mapper;

import java.util.UUID;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import ir.dotin.loan.baseloan.core.domain.loanarrangement.enums.CollateralType;
import ir.dotin.loan.trade.adapters.driving.contract.dto.DeleteFacilityCollateralRequest;
import ir.dotin.loan.trade.core.application.ports.inbound.command.DeleteFacilityCollateralCommand;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedSourcePolicy = ReportingPolicy.WARN,
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DeleteFacilityCollateralRequestToCommandMapper {

    @Mapping(target = "uid", ignore = true)
    @Mapping(target = "version", source = "request.version")
    DeleteFacilityCollateralCommand toDeleteCommand(UUID loanFacilityId, DeleteFacilityCollateralRequest request);

    default CollateralType mapCollateralType(String code) {
        return CollateralType.valueOf(code);
    }
}
