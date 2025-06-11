package ir.dotin.loan.trade.core.domain.shared.formula;

import ir.dotin.loan.baseloan.core.domain.shared.formula.BaseFormulaField;
import ir.dotin.loan.baseloan.core.domain.shared.formula.FieldValueResolver;
import ir.dotin.loan.trade.core.domain.loanarrangement.entity.TradeLoanArrangement;
import ir.dotin.loan.trade.core.domain.loanarrangement.vo.TradeLoanArrangementId;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;

public interface TradeFieldValueResolver
        extends FieldValueResolver<BaseFormulaField, TradeLoanFacility, TradeLoanArrangementId, TradeLoanArrangement> {}
