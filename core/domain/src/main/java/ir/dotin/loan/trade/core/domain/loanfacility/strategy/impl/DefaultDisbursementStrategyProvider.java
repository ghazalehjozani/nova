package ir.dotin.loan.trade.core.domain.loanfacility.strategy.impl;

import java.util.List;

import com.google.common.collect.ImmutableList;

import ir.dotin.platform.commons.domain.annotation.DomainComponent;
import ir.dotin.loan.baseloan.core.domain.shared.strategy.DocumentCalculationStrategy;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.ArticleType;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;
import ir.dotin.loan.trade.core.domain.loanfacility.strategy.DisbursementStrategyProvider;
import ir.dotin.loan.trade.core.domain.loantype.enums.TradeRelationType;

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
        return ImmutableList.of(bankCommitmentTransactionStrategy, paymentAmountStrategy, disbursedInterestStrategy);
    }
}
