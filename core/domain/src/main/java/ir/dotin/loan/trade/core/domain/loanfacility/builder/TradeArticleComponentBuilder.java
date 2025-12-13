package ir.dotin.loan.trade.core.domain.loanfacility.builder;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ImmutableSetMultimap;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.platform.commons.domain.vo.Money;
import ir.dotin.loan.baseloan.core.domain.shared.enums.RelationType;
import ir.dotin.loan.baseloan.core.domain.shared.strategy.factory.ArticleComponentFactory;
import ir.dotin.loan.baseloan.core.domain.shared.vo.EconomicSector;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTopic;
import ir.dotin.loan.baseloan.core.domain.shared.vo.ResolvedAccounts;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.ArticleComponent;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.ArticleType;
import ir.dotin.loan.trade.core.domain.loanfacility.i18n.TradeLoanFacilityLocalizedMessageCodes;
import ir.dotin.loan.trade.core.domain.loantype.enums.TradeRelationType;

import static java.util.Objects.requireNonNull;

public class TradeArticleComponentBuilder {

    private final ImmutableSetMultimap<RelationType<TradeRelationType>, LoanTopic> relationTopics;
    private final EconomicSector economicSector;
    private final Map<ArticleType<?, TradeRelationType>, ArticleComponent> components = new HashMap<>();

    public TradeArticleComponentBuilder(
            ImmutableSetMultimap<RelationType<TradeRelationType>, LoanTopic> relationTopics,
            EconomicSector economicSector) {
        this.relationTopics = requireNonNull(relationTopics);
        this.economicSector = requireNonNull(economicSector);
    }

    public <K extends Enum<K> & ArticleType<K, TradeRelationType>> TradeArticleComponentBuilder addComponent(
            K articleType, Money amount, ResolvedAccounts resolvedAccounts) {
        Result<ArticleComponent> result = ArticleComponentFactory.createWithLoanTopicLookup(
                relationTopics, articleType, economicSector, amount, resolvedAccounts);

        if (result.isSuccessWithValue()) {
            components.put(articleType, result.getValue());
        }
        return this;
    }

    public <K extends Enum<K> & ArticleType<K, TradeRelationType>> TradeArticleComponentBuilder addDebitCreditPair(
            K debitType, K creditType, Money amount, ResolvedAccounts resolvedAccounts) {
        return addComponent(debitType, amount, resolvedAccounts).addComponent(creditType, amount, resolvedAccounts);
    }

    @SuppressWarnings("unchecked")
    public <K extends Enum<K> & ArticleType<K, TradeRelationType>> Result<Map<K, ArticleComponent>> build(
            Class<K> articleTypeClass) {
        if (components.isEmpty()) {
            return Result.failure(
                    Notification.ofError(TradeLoanFacilityLocalizedMessageCodes.NO_ARTICLE_COMPONENTS_ADDED));
        }

        Map<K, ArticleComponent> typedMap = new HashMap<>();
        for (Map.Entry<ArticleType<?, TradeRelationType>, ArticleComponent> entry : components.entrySet()) {
            if (articleTypeClass.isInstance(entry.getKey())) {
                typedMap.put((K) entry.getKey(), entry.getValue());
            }
        }

        return Result.success(typedMap);
    }
}
