package ir.dotin.loan.trade.core.domain.shared.document.strategy.impl;

import java.util.List;

import ir.dotin.platform.accounting.document.api.model.ArticleType;
import ir.dotin.platform.accounting.document.api.strategy.DocumentCalculationStrategy;
import ir.dotin.platform.pangaea.commons.domain.annotation.DomainComponent;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;
import ir.dotin.loan.trade.core.domain.loantype.enums.TradeRelationType;
import ir.dotin.loan.trade.core.domain.shared.document.strategy.DisbursementStrategyProvider;

import static java.util.Objects.requireNonNull;

@DomainComponent
public final class DefaultDisbursementStrategyProvider implements DisbursementStrategyProvider {

    private final BankCommitmentTransactionStrategy bankCommitmentTransactionStrategy;
    private final PaymentAmountTransactionStrategy paymentAmountStrategy;
    private final DisbursedInterestTransactionStrategy disbursedInterestStrategy;

    public DefaultDisbursementStrategyProvider(
            BankCommitmentTransactionStrategy bankCommitmentTransactionStrategy,
            PaymentAmountTransactionStrategy paymentAmountStrategy,
            DisbursedInterestTransactionStrategy disbursedInterestStrategy) {
        this.paymentAmountStrategy = requireNonNull(paymentAmountStrategy);
        this.disbursedInterestStrategy = requireNonNull(disbursedInterestStrategy);
        this.bankCommitmentTransactionStrategy = bankCommitmentTransactionStrategy;
    }

    @Override
    public List<
                    DocumentCalculationStrategy<
                            TradeLoanFacility, TradeRelationType, ? extends ArticleType<?, TradeRelationType>>>
            getStrategies(TradeLoanFacility facility) {
        requireNonNull(facility);
        return List.of(bankCommitmentTransactionStrategy, paymentAmountStrategy, disbursedInterestStrategy);
    }
}
