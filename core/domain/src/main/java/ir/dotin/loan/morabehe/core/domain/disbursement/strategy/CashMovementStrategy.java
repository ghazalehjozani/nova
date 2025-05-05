package ir.dotin.loan.morabehe.core.domain.disbursement.strategy;

import ir.dotin.loan.baseloan.core.domain.shared.strategy.DocumentCalculationStrategy;
import ir.dotin.loan.morabehe.core.domain.loanfacility.aggregate.MorabeheLoanFacility;

public interface CashMovementStrategy extends DocumentCalculationStrategy<MorabeheLoanFacility> {}
