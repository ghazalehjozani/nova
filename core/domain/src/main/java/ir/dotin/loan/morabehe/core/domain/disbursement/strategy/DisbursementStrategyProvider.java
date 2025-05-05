package ir.dotin.loan.morabehe.core.domain.disbursement.strategy;

import java.util.List;

import ir.dotin.loan.baseloan.core.domain.shared.strategy.DocumentCalculationStrategy;
import ir.dotin.loan.morabehe.core.domain.loanfacility.aggregate.MorabeheLoanFacility;

public interface DisbursementStrategyProvider {
    List<DocumentCalculationStrategy<MorabeheLoanFacility>> getStrategies(MorabeheLoanFacility facility);
}
