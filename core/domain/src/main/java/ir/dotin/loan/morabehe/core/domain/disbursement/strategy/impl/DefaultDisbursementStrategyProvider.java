package ir.dotin.loan.morabehe.core.domain.disbursement.strategy.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ir.dotin.platform.domain.common.annotation.DomainService;
import ir.dotin.loan.baseloan.core.domain.shared.strategy.DocumentCalculationStrategy;
import ir.dotin.loan.morabehe.core.domain.loanfacility.aggregate.MorabeheLoanFacility;
import ir.dotin.loan.morabehe.core.domain.disbursement.strategy.DisbursementStrategyProvider;

@DomainService
public final class DefaultDisbursementStrategyProvider implements DisbursementStrategyProvider {

    private final BankCommitmentTransactionStrategy bankCommitmentStrategy;
    private final PaymentAmountTransactionStrategy paymentAmountStrategy;
    private final DisbursedInterestTransactionStrategy disbursedFacilitiesStrategy;
    // Inject other optional strategies...
    // private final CashInsuranceTransactionStrategy cashInsuranceStrategy;

    public DefaultDisbursementStrategyProvider(
            BankCommitmentTransactionStrategy bankCommitmentStrategy,
            PaymentAmountTransactionStrategy paymentAmountStrategy,
            DisbursedInterestTransactionStrategy disbursedFacilitiesStrategy
            /*, CashInsuranceTransactionStrategy cashInsuranceStrategy */ ) {
        this.bankCommitmentStrategy = Objects.requireNonNull(bankCommitmentStrategy);
        this.paymentAmountStrategy = Objects.requireNonNull(paymentAmountStrategy);
        this.disbursedFacilitiesStrategy = Objects.requireNonNull(disbursedFacilitiesStrategy);
        // this.cashInsuranceStrategy = Objects.requireNonNull(cashInsuranceStrategy);
    }

    @Override
    public List<DocumentCalculationStrategy<MorabeheLoanFacility>> getStrategies(MorabeheLoanFacility facility) {
        Objects.requireNonNull(facility);
        List<DocumentCalculationStrategy<MorabeheLoanFacility>> applicableStrategies = new ArrayList<>();

        applicableStrategies.add(bankCommitmentStrategy);
        applicableStrategies.add(paymentAmountStrategy);
        applicableStrategies.add(disbursedFacilitiesStrategy);

        // if (shouldApplyCashInsurance(facility)) {
        //     applicableStrategies.add(cashInsuranceStrategy);
        // }
        // Add other conditionals...

        return applicableStrategies;
    }

    // private boolean shouldApplyCashInsurance(MorabeheLoanFacility facility) { ... }
    // Other condition checks...
}
