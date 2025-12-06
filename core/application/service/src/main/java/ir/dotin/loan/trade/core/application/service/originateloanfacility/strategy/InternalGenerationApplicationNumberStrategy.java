package ir.dotin.loan.trade.core.application.service.originateloanfacility.strategy;

import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.ApplicationNumber;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.Branch;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.LoanTypeCode;
import ir.dotin.loan.baseloan.core.domain.shared.vo.BranchCode;
import ir.dotin.loan.baseloan.core.domain.shared.vo.customer.Party;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanFacilityRepository;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.i18n.OriginateLoanFacilityErrorCodes;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class InternalGenerationApplicationNumberStrategy implements ApplicationNumberStrategy {

    private final TradeLoanFacilityRepository loanFacilityRepository;
    private final TradeLoanFacilityRepository facilityRepository;

    @Override
    public @NonNull Result<ApplicationNumber> generateOrValidateApplicationNumber(
            @NonNull Branch branch, @NonNull LoanTypeCode loanTypeCode, @NonNull Party primaryApplicant) {
        String derivedValue = String.valueOf(
                generateApplicationSequence(branch.code(), loanTypeCode, primaryApplicant.customerNumber()));
        ApplicationNumber applicationNumber =
                new ApplicationNumber(branch, loanTypeCode, primaryApplicant, derivedValue);

        boolean exists = facilityRepository.existsByApplicationNumber(applicationNumber);

        if (exists) {
            return Result.failure(Notification.ofError(
                    OriginateLoanFacilityErrorCodes.DUPLICATE_APPLICATION_NUMBER,
                    applicationNumber.formattedApplicationNumber()));
        }
        return Result.success(applicationNumber);
    }

    @Override
    public @NonNull ApplicationNumberGenerationType getType() {
        return ApplicationNumberGenerationType.INTERNAL_GENERATION;
    }

    private Long generateApplicationSequence(BranchCode branchCode, LoanTypeCode loanTypeCode, String customerNumber) {
        return loanFacilityRepository.countByBranchCodeAndLoanTypeCodeAndCustomerNumber(
                branchCode, loanTypeCode, customerNumber);
    }
}
