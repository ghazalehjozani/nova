package ir.dotin.loan.trade.adapters.driving.contract.mapper;

import java.util.UUID;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import ir.dotin.loan.trade.adapters.driving.contract.dto.ApproveFacilityRequest;
import ir.dotin.loan.trade.core.application.ports.inbound.command.ApproveFacilityCommand;

@Component
public class ApproveFacilityRequestToCommandMapper {

    public ApproveFacilityCommand toCommand(
            UUID facilityId, UUID idempotencyKey, @Nullable String sanctionSerial, ApproveFacilityRequest request) {
        // uid is the request's idempotency key (never randomly generated); branchCode is stamped by the controller from
        // the auth context; sanctionDetails is system-populated post pre-flight (PrepareFacilityApprovalQuery).
        return new ApproveFacilityCommand(
                idempotencyKey, request.version(), facilityId, null, sanctionSerial, request.confirmType(), null);
    }
}
