package ir.dotin.loan.trade.core.domain.shared.interaction;

import ir.dotin.loan.baseloan.core.domain.shared.formula.BaseFormulaField;
import ir.dotin.loan.baseloan.core.domain.shared.interaction.LoanArrangementDataProvider;
import ir.dotin.loan.trade.core.domain.loanarrangement.aggregate.TradeLoanArrangement;
import ir.dotin.loan.trade.core.domain.loanarrangement.vo.TradeLoanArrangementId;

public interface TradeLoanArrangementDataProvider
        extends LoanArrangementDataProvider<BaseFormulaField, TradeLoanArrangementId, TradeLoanArrangement> {}
