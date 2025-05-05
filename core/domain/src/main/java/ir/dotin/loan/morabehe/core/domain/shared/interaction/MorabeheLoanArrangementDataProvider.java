package ir.dotin.loan.morabehe.core.domain.shared.interaction;

import ir.dotin.loan.baseloan.core.domain.shared.formula.BaseFormulaField;
import ir.dotin.loan.baseloan.core.domain.shared.interaction.LoanArrangementDataProvider;
import ir.dotin.loan.morabehe.core.domain.loanarrangement.aggregate.MorabeheLoanArrangement;
import ir.dotin.loan.morabehe.core.domain.loanarrangement.vo.MorabeheLoanArrangementId;

public interface MorabeheLoanArrangementDataProvider
        extends LoanArrangementDataProvider<BaseFormulaField, MorabeheLoanArrangementId, MorabeheLoanArrangement> {}
