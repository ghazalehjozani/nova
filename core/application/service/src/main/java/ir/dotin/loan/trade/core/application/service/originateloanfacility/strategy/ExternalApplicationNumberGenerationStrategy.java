package ir.dotin.loan.trade.core.application.service.originateloanfacility.strategy;

import org.springframework.stereotype.Component;

import ir.dotin.platform.pangaea.commons.core.Notification;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.ApplicationNumber;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.Branch;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.LoanTypeCode;
import ir.dotin.loan.baseloan.core.domain.shared.vo.customer.Party;
import ir.dotin.loan.trade.core.application.ports.outbound.client.loanservice.LoanServicePort;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanFacilityRepository;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.i18n.OriginateLoanFacilityErrorCodes;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExternalApplicationNumberGenerationStrategy implements ApplicationNumberGenerationStrategy {

    private final LoanServicePort loanServicePort;
    private final TradeLoanFacilityRepository facilityRepository;

    @Override
    public Result<ApplicationNumber> generateApplicationNumber(
            Branch branch, LoanTypeCode loanTypeCode, Party primaryApplicant) {

        Result<ApplicationNumber> fcbResult =
                loanServicePort.getApplicationNumber(branch, loanTypeCode, primaryApplicant);

        if (fcbResult.isFailure()) {
            return Result.failure(fcbResult.err().orElseThrow());
        }

        ApplicationNumber fcbApplicationNumber = fcbResult.unwrap();

        if (fcbApplicationNumber == null) {
            return Result.failure(
                    Notification.ofError(OriginateLoanFacilityErrorCodes.APPLICATION_NUMBER_CREATION_FAILED));
        }

        // Classify a clashing application number: an active facility is a true conflict; a facility that
        // exists only in a terminal/cancelled state is a ghost/stale allocation (compensated origination,
        // FCB loan file never materialised or already cancelled) and must surface a distinct, recoverable
        // error rather than a misleading duplicate.
        if (facilityRepository.existsActiveByApplicationNumber(fcbApplicationNumber)) {
            return Result.failure(
                    OriginateLoanFacilityErrorCodes.DUPLICATE_APPLICATION_NUMBER,
                    fcbApplicationNumber.formattedApplicationNumber());
        }

        if (facilityRepository.existsByApplicationNumber(fcbApplicationNumber)) {
            return Result.failure(
                    OriginateLoanFacilityErrorCodes.STALE_APPLICATION_NUMBER_ALLOCATION,
                    fcbApplicationNumber.formattedApplicationNumber());
        }

        return Result.success(fcbApplicationNumber);
    }

    @Override
    public ApplicationNumberGenerationType getType() {
        return ApplicationNumberGenerationType.FCB_VALIDATION;
    }
}
