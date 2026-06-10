package ir.dotin.loan.trade.core.application.query.loantype.handler;

import org.springframework.stereotype.Service;

import ir.dotin.platform.pangaea.commons.core.Notification;
import ir.dotin.platform.pangaea.commons.core.error.FailureCause;
import ir.dotin.platform.pangaea.commons.core.exception.FailureCauseException;
import ir.dotin.platform.pangaea.servicelayer.api.query.QueryHandler;
import ir.dotin.loan.trade.core.application.query.loantype.dto.TradeLoanTypeQueryDto;
import ir.dotin.loan.trade.core.application.query.loantype.i18n.LoanTypeQueryErrorCodes;
import ir.dotin.loan.trade.core.application.query.loantype.repository.TradeLoanTypeQueryRepository;
import ir.dotin.loan.trade.core.application.query.loantype.request.GetLoanTypeByIdQuery;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetLoanTypeByIdQueryHandler implements QueryHandler<GetLoanTypeByIdQuery, TradeLoanTypeQueryDto> {

    private final TradeLoanTypeQueryRepository tradeLoanTypeRepository;

    public TradeLoanTypeQueryDto handle(GetLoanTypeByIdQuery query) {
        return tradeLoanTypeRepository.findById(query.loanTypeId()).orElseThrow(() -> {
            var notification = Notification.ofError(LoanTypeQueryErrorCodes.LOAN_TYPE_NOT_FOUND, query.loanTypeId());
            return new FailureCauseException(FailureCause.notFound(notification));
        });
    }
}
