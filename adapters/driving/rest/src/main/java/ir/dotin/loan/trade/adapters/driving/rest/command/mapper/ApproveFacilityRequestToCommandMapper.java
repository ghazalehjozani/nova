package ir.dotin.loan.trade.adapters.driving.rest.command.mapper;

import java.util.Objects;
import java.util.UUID;

import org.mapstruct.*;

import ir.dotin.loan.trade.adapters.driving.rest.command.dto.ApproveFacilityRequest;
import ir.dotin.loan.trade.core.application.ports.inbound.command.ApproveFacilityCommand;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedSourcePolicy = ReportingPolicy.WARN,
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ApproveFacilityRequestToCommandMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "approvedAmount", ignore = true)
    @Mapping(target = "currency", ignore = true)
    @Mapping(target = "gracePeriod", ignore = true)
    @Mapping(target = "installmentCount", ignore = true)
    @Mapping(target = "loanDuration", ignore = true)
    @Mapping(target = "disbursementMethod", ignore = true)
    @Mapping(target = "lifeInsuranceId", ignore = true)
    @Mapping(target = "collateralSerial", ignore = true)
    @Mapping(target = "revocationReason", ignore = true)
    ApproveFacilityCommand toCommand(UUID loanFacilityId, String sanctionSerial, ApproveFacilityRequest request);

    default ApproveFacilityCommand.SanctionSerialDto mapSanctionSerialDto(String sanctionSerial) {
        return Objects.isNull(sanctionSerial)
                ? null
                : new ApproveFacilityCommand.SanctionSerialDto(sanctionSerial, null);
    }
}
