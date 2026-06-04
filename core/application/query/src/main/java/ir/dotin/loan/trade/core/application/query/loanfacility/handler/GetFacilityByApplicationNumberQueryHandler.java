package ir.dotin.loan.trade.core.application.query.loanfacility.handler;

import org.springframework.stereotype.Service;

import ir.dotin.platform.pangaea.commons.core.Notification;
import ir.dotin.platform.pangaea.commons.core.error.FailureCause;
import ir.dotin.platform.pangaea.commons.core.exception.FailureCauseException;
import ir.dotin.platform.pangaea.dispatcher.api.query.QueryHandler;
import ir.dotin.loan.trade.core.application.query.loanfacility.dto.TradeFacilityQueryDto;
import ir.dotin.loan.trade.core.application.query.loanfacility.i18n.LoanFacilityQueryErrorCodes;
import ir.dotin.loan.trade.core.application.query.loanfacility.repository.TradeLoanFacilityQueryRepository;
import ir.dotin.loan.trade.core.application.query.loanfacility.request.GetFacilityByApplicationNumberQuery;
import ir.dotin.loan.trade.core.application.query.shared.authz.BranchReadAccessValidator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetFacilityByApplicationNumberQueryHandler
        implements QueryHandler<GetFacilityByApplicationNumberQuery, TradeFacilityQueryDto> {

    private final TradeLoanFacilityQueryRepository queryRepository;
    private final BranchReadAccessValidator branchReadAccessValidator;

    @Override
    public TradeFacilityQueryDto handle(GetFacilityByApplicationNumberQuery query) {
        TradeFacilityQueryDto facility = queryRepository
                .findByApplicationNumber(query.applicationNumber())
                .orElseThrow(() -> {
                    var notification = Notification.ofError(
                            LoanFacilityQueryErrorCodes.FACILITY_NOT_FOUND, query.applicationNumber());
                    return new FailureCauseException(FailureCause.notFound(notification));
                });
        branchReadAccessValidator.verifyCallerCoversBranch(
                query.callerBranchCode(), facility.loanApplication().branchCode());
        return facility;
    }
}
