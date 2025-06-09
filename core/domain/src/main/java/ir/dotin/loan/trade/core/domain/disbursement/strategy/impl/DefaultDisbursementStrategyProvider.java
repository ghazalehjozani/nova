package ir.dotin.loan.trade.core.domain.disbursement.strategy.impl;

import java.util.ArrayList;
import java.util.List;

import ir.dotin.platform.domain.common.annotation.DomainService;
import ir.dotin.loan.baseloan.core.domain.shared.strategy.DocumentCalculationStrategy;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.ArticleType;
import ir.dotin.loan.trade.core.domain.disbursement.strategy.DisbursementStrategyProvider;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;
import ir.dotin.loan.trade.core.domain.loantype.enums.TradeRelationType;

import static java.util.Objects.requireNonNull;

@DomainService
public final class DefaultDisbursementStrategyProvider implements DisbursementStrategyProvider {

    private final PaymentAmountTransactionStrategy paymentAmountStrategy;
    private final DisbursedInterestTransactionStrategy disbursedInterestStrategy;

    public DefaultDisbursementStrategyProvider(
            PaymentAmountTransactionStrategy paymentAmountStrategy,
            DisbursedInterestTransactionStrategy disbursedInterestStrategy) {
        this.paymentAmountStrategy = requireNonNull(paymentAmountStrategy);
        this.disbursedInterestStrategy = requireNonNull(disbursedInterestStrategy);
    }

    @Override
    public List<
                    DocumentCalculationStrategy<
                            TradeLoanFacility, TradeRelationType, ? extends ArticleType<?, TradeRelationType>>>
            getStrategies(TradeLoanFacility facility) {
        requireNonNull(facility);
        List<
                        DocumentCalculationStrategy<
                                TradeLoanFacility, TradeRelationType, ? extends ArticleType<?, TradeRelationType>>>
                applicableStrategies = new ArrayList<>();

        applicableStrategies.add(paymentAmountStrategy);
        applicableStrategies.add(disbursedInterestStrategy);

        return applicableStrategies;
    }
}
