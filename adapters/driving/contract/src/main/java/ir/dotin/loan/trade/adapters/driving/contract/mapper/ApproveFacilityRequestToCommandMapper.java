package ir.dotin.loan.trade.adapters.driving.contract.mapper;

import java.util.UUID;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import ir.dotin.loan.trade.adapters.driving.contract.dto.ApproveFacilityRequest;
import ir.dotin.loan.trade.core.application.ports.inbound.command.ApproveFacilityCommand;

@Component
public class ApproveFacilityRequestToCommandMapper {

    public ApproveFacilityCommand toCommand(
            UUID facilityId, @Nullable String sanctionSerial, ApproveFacilityRequest request) {
        // uid assigned by the dispatcher upon dispatch; sanctionDetails is system-populated post pre-flight
        // (PrepareFacilityApprovalQuery) and threaded onto the command by the controller — null here intentionally.
        return new ApproveFacilityCommand(
                UUID.randomUUID(), request.version(), facilityId, sanctionSerial, request.confirmType(), null);
    }
}
