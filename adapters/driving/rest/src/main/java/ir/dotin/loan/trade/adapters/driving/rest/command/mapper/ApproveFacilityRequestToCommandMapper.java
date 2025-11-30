package ir.dotin.loan.trade.adapters.driving.rest.command.mapper;

import java.util.UUID;
import jakarta.annotation.Nullable;

import org.springframework.stereotype.Component;

import ir.dotin.loan.trade.adapters.driving.rest.command.dto.ApproveFacilityRequest;
import ir.dotin.loan.trade.core.application.ports.inbound.command.ApproveFacilityCommand;

@Component
public class ApproveFacilityRequestToCommandMapper {

    public ApproveFacilityCommand toCommand(
            UUID facilityId, @Nullable String sanctionSerial, ApproveFacilityRequest request) {
        return new ApproveFacilityCommand(null, request.version(), facilityId, sanctionSerial);
    }
}
