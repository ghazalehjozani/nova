package ir.dotin.loan.trade.core.application.service.originateloanfacility.strategy;

import org.springframework.stereotype.Component;

import ir.dotin.platform.accounting.document.api.model.BranchCode;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.ApplicationNumber;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.Branch;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.LoanTypeCode;
import ir.dotin.loan.baseloan.core.domain.shared.vo.customer.Party;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanFacilityRepository;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.i18n.OriginateLoanFacilityErrorCodes;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class InternalApplicationNumberGenerationStrategy implements ApplicationNumberGenerationStrategy {

    private final TradeLoanFacilityRepository loanFacilityRepository;
    private final TradeLoanFacilityRepository facilityRepository;

    @Override
    public Result<ApplicationNumber> generateApplicationNumber(
            Branch branch, LoanTypeCode loanTypeCode, Party primaryApplicant) {
        String derivedValue = String.valueOf(
                generateApplicationSequence(branch.code(), loanTypeCode, primaryApplicant.customerNumber()));
        ApplicationNumber applicationNumber =
                new ApplicationNumber(branch, loanTypeCode, primaryApplicant, derivedValue);

        boolean exists = facilityRepository.existsByApplicationNumber(applicationNumber);

        if (exists) {
            return Result.failure(
                    OriginateLoanFacilityErrorCodes.DUPLICATE_APPLICATION_NUMBER,
                    applicationNumber.formattedApplicationNumber());
        }
        return Result.success(applicationNumber);
    }

    @Override
    public ApplicationNumberGenerationType getType() {
        return ApplicationNumberGenerationType.INTERNAL_GENERATION;
    }

    private Long generateApplicationSequence(BranchCode branchCode, LoanTypeCode loanTypeCode, String customerNumber) {
        return loanFacilityRepository.countByBranchCodeAndLoanTypeCodeAndCustomerNumber(
                branchCode, loanTypeCode, customerNumber);
    }
}
