package ir.dotin.loan.trade.adapters.driving.contract.mapper;

import java.util.UUID;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import ir.dotin.loan.trade.adapters.driving.contract.dto.CloseFacilityPaidOffRequest;
import ir.dotin.loan.trade.core.application.ports.inbound.command.CloseFacilityPaidOffCommand;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedSourcePolicy = ReportingPolicy.WARN,
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CloseFacilityPaidOffRequestToCommandMapper {

    @Mapping(target = "uid", source = "request.uid")
    @Mapping(target = "version", source = "request.version")
    @Mapping(target = "applicationNumber", ignore = true)
    @Mapping(target = "transactionReference", ignore = true)
    @Mapping(target = "currency", ignore = true)
    @Mapping(target = "payments", ignore = true)
    @Mapping(target = "channel", ignore = true)
    @Mapping(target = "legacyTransactionReference", ignore = true)
    @Mapping(target = "transactionMetadata", ignore = true)
    CloseFacilityPaidOffCommand toCommand(UUID loanFacilityId, CloseFacilityPaidOffRequest request);
}
