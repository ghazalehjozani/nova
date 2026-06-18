package ir.dotin.loan.trade.core.application.query.formula.handler;

import org.springframework.stereotype.Service;

import ir.dotin.platform.formula.service.cqrs.query.FormulaView;
import ir.dotin.platform.formula.service.cqrs.query.GetFormulaQuery;
import ir.dotin.platform.formula.service.query.FormulaQueryService;
import ir.dotin.platform.pangaea.commons.core.Notification;
import ir.dotin.platform.pangaea.commons.core.error.FailureCause;
import ir.dotin.platform.pangaea.commons.core.exception.FailureCauseException;
import ir.dotin.platform.pangaea.servicelayer.api.query.QueryHandler;
import ir.dotin.loan.trade.core.application.query.formula.i18n.FormulaQueryErrorCodes;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetFormulaQueryHandler implements QueryHandler<GetFormulaQuery, FormulaView> {

    private final FormulaQueryService formulaQueryService;

    @Override
    public FormulaView handle(GetFormulaQuery query) {
        return formulaQueryService
                .findByCode(query.code())
                .map(FormulaView::new)
                .orElseThrow(() -> new FailureCauseException(FailureCause.notFound(
                        Notification.ofError(FormulaQueryErrorCodes.FORMULA_NOT_FOUND, query.code()))));
    }
}
