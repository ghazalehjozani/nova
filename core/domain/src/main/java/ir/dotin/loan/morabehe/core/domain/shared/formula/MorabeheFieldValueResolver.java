package ir.dotin.loan.morabehe.core.domain.shared.formula;

import ir.dotin.loan.baseloan.core.domain.shared.formula.FieldValueResolver;
import ir.dotin.loan.baseloan.core.domain.shared.formula.BaseFormulaField;
import ir.dotin.loan.morabehe.core.domain.loanarrangement.aggregate.MorabeheLoanArrangement;
import ir.dotin.loan.morabehe.core.domain.loanarrangement.vo.MorabeheLoanArrangementId;
import ir.dotin.loan.morabehe.core.domain.loanfacility.aggregate.MorabeheLoanFacility;

public interface MorabeheFieldValueResolver
        extends FieldValueResolver<
                BaseFormulaField, MorabeheLoanFacility, MorabeheLoanArrangementId, MorabeheLoanArrangement> {}
