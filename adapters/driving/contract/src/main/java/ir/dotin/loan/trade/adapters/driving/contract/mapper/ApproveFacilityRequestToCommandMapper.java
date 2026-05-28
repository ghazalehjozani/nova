package ir.dotin.loan.trade.adapters.driving.contract.mapper;

import java.util.UUID;
import jakarta.annotation.Nullable;

import org.springframework.stereotype.Component;

import ir.dotin.loan.trade.adapters.driving.contract.dto.ApproveFacilityRequest;
import ir.dotin.loan.trade.core.application.ports.inbound.command.ApproveFacilityCommand;

@Component
public class ApproveFacilityRequestToCommandMapper {

    public ApproveFacilityCommand toCommand(
            UUID facilityId, @Nullable String sanctionSerial, ApproveFacilityRequest request) {
        // sanctionDetails is system-populated post pre-flight (PrepareFacilityApprovalQuery); never sourced from the
        // request — built null here and threaded onto the command by the controller after the manual pre-flight.
        return new ApproveFacilityCommand(
                null, request.version(), facilityId, sanctionSerial, request.confirmType(), null);
    }
}
