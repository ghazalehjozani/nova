package ir.dotin.loan.trade.core.application.service.originateloanfacility.component.validation;

import java.util.function.Predicate;
import java.util.function.Supplier;

import org.springframework.stereotype.Component;

import ir.dotin.platform.pangaea.commons.core.Notification;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.Unit;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.LoanTypeCode;
import ir.dotin.loan.baseloan.core.domain.shared.vo.EconomicSector;
import ir.dotin.loan.trade.core.application.ports.inbound.command.OriginateFacilityCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.client.loanservice.LoanServicePort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.EconomicalSectorResponse;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.EconomicalSectorValidation;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.i18n.OriginateLoanFacilityErrorCodes;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class EconomicSectorValidationRules {

    private final LoanServicePort loanServicePort;

    public Result<Unit> validateEconomicalSector(OriginateFacilityCommand command) {
        String code = command.loanApplication().economicSector().code();

        return loadEconomicalSectorByCode(code).flatMap(this::validateNotParent);
    }

    public Result<Unit> validateEconomicalSectionForLoanType(OriginateFacilityCommand command) {
        String sectorCode = command.loanApplication().economicSector().code();
        String typeCodeRaw = command.loanTypeCode();
        LoanTypeCode loanTypeCode = LoanTypeCode.of(typeCodeRaw).unwrap();

        Result<EconomicalSectorValidation> result = validateEconomicalSectorForLoanType(sectorCode, loanTypeCode);

        return validateBusinessRule(
                result,
                EconomicalSectorValidation::isValid,
                () -> Notification.ofError(
                        OriginateLoanFacilityErrorCodes.INVALID_ECONOMIC_SECTOR_FOR_LOAN_TYPE,
                        sectorCode,
                        loanTypeCode));
    }

    private Result<Unit> validateNotParent(EconomicalSectorResponse sector) {
        if (sector.hasChild()) {
            return Result.failure(
                    Notification.ofError(OriginateLoanFacilityErrorCodes.ECONOMIC_SECTOR_IS_PARENT, sector.code()));
        }
        return Result.success();
    }

    private Result<EconomicalSectorResponse> loadEconomicalSectorByCode(String economicSectorCode) {
        return loanServicePort.loadEconomicalSector(
                EconomicSector.of(economicSectorCode).unwrap());
    }

    private Result<EconomicalSectorValidation> validateEconomicalSectorForLoanType(
            String economicSectorCode, LoanTypeCode loanTypeCode) {
        return loanServicePort.validateEconomicalSectorForLoanType(
                EconomicSector.of(economicSectorCode).unwrap(), loanTypeCode);
    }

    private <T> Result<Unit> validateBusinessRule(
            Result<T> result, Predicate<T> isValid, Supplier<Notification> errorSupplier) {

        if (result.isFailure()) {
            return Result.failure(result.err().orElseThrow());
        }

        if (!isValid.test(result.unwrap())) {
            return Result.failure(errorSupplier.get());
        }

        return Result.success();
    }
}
