package ir.dotin.loan.trade.core.application.service.originateloanfacility.component.validation;

import org.springframework.stereotype.Component;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.Unit;
import ir.dotin.loan.trade.core.application.ports.inbound.command.OriginateLoanFacilityCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.client.loanservice.LoanServicePort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.ReasonType;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RequestReasonValidationRule {

    private final LoanServicePort loanServicePort;

    public Result<Unit> validateRequestReason(OriginateLoanFacilityCommand command) {
        String code = command.loanApplication().requestReason().code();
        Result<ReasonType> result = loadRequestReasonByCode(code);

        if (result.isFailure()) {
            return Result.failure(result.err().orElseThrow());
        }
        return Result.success();
    }

    private Result<ReasonType> loadRequestReasonByCode(String requestReasonCode) {
        return loanServicePort.loadReasonTypeForCreate(requestReasonCode);
    }
}
