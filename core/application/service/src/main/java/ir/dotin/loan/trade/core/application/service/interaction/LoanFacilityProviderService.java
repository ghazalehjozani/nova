package ir.dotin.loan.trade.core.application.service.interaction;

import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import ir.dotin.platform.pangaea.commons.core.Notification;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.error.FailureCause;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanFacilityRepository;
import ir.dotin.loan.trade.core.application.service.shared.error.TradeLoanApplicationServiceErrors;
import ir.dotin.loan.trade.core.domain.installmentschedule.intraction.LoanFacilityProvider;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LoanFacilityProviderService implements LoanFacilityProvider {

    private final TradeLoanFacilityRepository tradeLoanFacilityRepository;

    @Override
    public Result<TradeLoanFacility> findLoanFacilityById(@NonNull LoanFacilityId id) {
        return Result.fromOptional(
                tradeLoanFacilityRepository.findById(id),
                () -> FailureCause.notFound(
                        Notification.ofError(TradeLoanApplicationServiceErrors.FACILITY_NOT_FOUND, id)));
    }
}
