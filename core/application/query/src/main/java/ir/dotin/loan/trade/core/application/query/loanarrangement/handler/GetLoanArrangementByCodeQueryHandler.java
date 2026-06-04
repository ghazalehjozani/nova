package ir.dotin.loan.trade.core.application.query.loanarrangement.handler;

import org.springframework.stereotype.Service;

import ir.dotin.platform.pangaea.commons.core.Notification;
import ir.dotin.platform.pangaea.commons.core.error.FailureCause;
import ir.dotin.platform.pangaea.commons.core.exception.FailureCauseException;
import ir.dotin.platform.pangaea.dispatcher.api.query.QueryHandler;
import ir.dotin.loan.trade.core.application.query.loanarrangement.dto.TradeLoanArrangementQueryDto;
import ir.dotin.loan.trade.core.application.query.loanarrangement.i18n.LoanArrangementQueryErrorCodes;
import ir.dotin.loan.trade.core.application.query.loanarrangement.repository.TradeLoanArrangementQueryRepository;
import ir.dotin.loan.trade.core.application.query.loanarrangement.request.GetLoanArrangementByCodeQuery;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetLoanArrangementByCodeQueryHandler
        implements QueryHandler<GetLoanArrangementByCodeQuery, TradeLoanArrangementQueryDto> {

    private final TradeLoanArrangementQueryRepository queryRepository;

    @Override
    public TradeLoanArrangementQueryDto handle(GetLoanArrangementByCodeQuery query) {
        return queryRepository.findByCode(query.code()).orElseThrow(() -> {
            var notification =
                    Notification.ofError(LoanArrangementQueryErrorCodes.LOAN_ARRANGEMENT_NOT_FOUND, query.code());
            return new FailureCauseException(FailureCause.notFound(notification));
        });
    }
}
