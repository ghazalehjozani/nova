package ir.dotin.loan.trade.core.application.service.originateloanfacility.component.validation;

import org.springframework.stereotype.Component;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.Unit;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.SubSource;
import ir.dotin.loan.trade.core.application.ports.inbound.command.OriginateFacilityCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.client.loanservice.LoanServicePort;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SubSourceValidationRule {

    private final LoanServicePort loanServicePort;

    public Result<Unit> validateSubSource(OriginateFacilityCommand command) {
        var subSource = command.loanApplication().subSource();
        if (subSource == null) {
            return Result.success();
        }
        String code = subSource.code();
        Result<SubSource> result = loadResourceByCode(code);

        if (result.isFailure()) {
            return Result.failure(result.err().orElseThrow());
        }
        return Result.success();
    }

    private Result<SubSource> loadResourceByCode(String subSourceCode) {
        return loanServicePort.loadResourceByCode(subSourceCode);
    }
}
