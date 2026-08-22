package ir.dotin.loan.trade.core.application.service.originateloanfacility.component.validation;

import org.springframework.stereotype.Component;

import ir.dotin.platform.accounting.document.api.model.AccountNumber;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.Unit;
import ir.dotin.loan.trade.core.application.ports.inbound.command.OriginateFacilityCommand;
import ir.dotin.loan.trade.core.application.ports.inbound.dto.DisburseDestinationDto;
import ir.dotin.loan.trade.core.application.ports.outbound.client.accountservice.AccountValidationPort;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.i18n.OriginateLoanFacilityErrorCodes;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AccountNumberValidationRule {

    private final AccountValidationPort accountValidationPort;

    public Result<Unit> validateAccountNumber(OriginateFacilityCommand command) {
        DisburseDestinationDto disburseDestination = command.loanApplication().disburseDestination();

        return switch (disburseDestination) {
            case DisburseDestinationDto.AccountDestinationDto(var accountNumber) -> {
                Result<AccountNumber> result = accountValidationPort.validateAccountNumber(accountNumber);

                if (result.isFailure()) {
                    yield Result.failure(OriginateLoanFacilityErrorCodes.INVALID_ACCOUNT_NUMBER, accountNumber);
                }
                yield Result.success();
            }
            case DisburseDestinationDto.DepositDestinationDto(var ignored) -> Result.success();
        };
    }
}
