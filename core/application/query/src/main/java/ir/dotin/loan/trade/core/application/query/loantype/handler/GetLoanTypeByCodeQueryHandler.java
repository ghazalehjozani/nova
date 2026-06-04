package ir.dotin.loan.trade.core.application.query.loantype.handler;

import org.springframework.stereotype.Service;

import ir.dotin.platform.pangaea.commons.core.Notification;
import ir.dotin.platform.pangaea.commons.core.error.FailureCause;
import ir.dotin.platform.pangaea.commons.core.exception.FailureCauseException;
import ir.dotin.platform.pangaea.dispatcher.api.query.QueryHandler;
import ir.dotin.loan.trade.core.application.query.loantype.dto.TradeLoanTypeQueryDto;
import ir.dotin.loan.trade.core.application.query.loantype.i18n.LoanTypeQueryErrorCodes;
import ir.dotin.loan.trade.core.application.query.loantype.repository.TradeLoanTypeQueryRepository;
import ir.dotin.loan.trade.core.application.query.loantype.request.GetLoanTypeByCodeQuery;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetLoanTypeByCodeQueryHandler implements QueryHandler<GetLoanTypeByCodeQuery, TradeLoanTypeQueryDto> {

    private final TradeLoanTypeQueryRepository tradeLoanTypeRepository;

    public TradeLoanTypeQueryDto handle(GetLoanTypeByCodeQuery query) {
        return tradeLoanTypeRepository.findByCode(query.code()).orElseThrow(() -> {
            var notification = Notification.ofError(LoanTypeQueryErrorCodes.LOAN_TYPE_NOT_FOUND, query.code());
            return new FailureCauseException(FailureCause.notFound(notification));
        });
    }
}
