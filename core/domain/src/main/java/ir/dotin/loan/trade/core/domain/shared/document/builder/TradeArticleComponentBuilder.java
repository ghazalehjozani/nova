package ir.dotin.loan.trade.core.domain.shared.document.builder;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.google.common.collect.ImmutableSetMultimap;
import org.jspecify.annotations.Nullable;

import ir.dotin.platform.accounting.document.api.enumeration.ArticleTargetType;
import ir.dotin.platform.accounting.document.api.enumeration.RelationType;
import ir.dotin.platform.accounting.document.api.error.DocumentErrors;
import ir.dotin.platform.accounting.document.api.model.AccountArticleComponent;
import ir.dotin.platform.accounting.document.api.model.AccountId;
import ir.dotin.platform.accounting.document.api.model.ArticleComponent;
import ir.dotin.platform.accounting.document.api.model.ArticleType;
import ir.dotin.platform.accounting.document.api.model.PostingTopic;
import ir.dotin.platform.accounting.document.api.model.TargetArticleComponent;
import ir.dotin.platform.pangaea.commons.core.Notification;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.domain.vo.Money;
import ir.dotin.loan.baseloan.core.domain.shared.vo.ContextualArticleDestination;
import ir.dotin.loan.baseloan.core.domain.shared.vo.EconomicSector;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTopic;
import ir.dotin.loan.baseloan.core.domain.shared.vo.ResolvedAccounts;
import ir.dotin.loan.trade.core.domain.loantype.enums.TradeRelationType;

import static java.util.Objects.requireNonNull;

/**
 * Builds typed maps of {@link ArticleComponent} for trade-loan document generation.
 *
 * <p>Bridges loan domain ({@link LoanTopic}, {@link EconomicSector}, {@link ResolvedAccounts}) with the platform
 * accounting-document API ({@link PostingTopic}, {@link AccountId}). Resolves accounts, looks up loan topics by
 * economic sector, and converts them to {@link PostingTopic} instances.
 */
public class TradeArticleComponentBuilder {

    private final ImmutableSetMultimap<RelationType<TradeRelationType>, LoanTopic> relationTopics;
    private final EconomicSector economicSector;

    public TradeArticleComponentBuilder(
            ImmutableSetMultimap<RelationType<TradeRelationType>, LoanTopic> relationTopics,
            EconomicSector economicSector) {
        this.relationTopics = requireNonNull(relationTopics);
        this.economicSector = requireNonNull(economicSector);
    }

    /**
     * Builds a debit/credit pair for non-contextual article types. Both legs use the same amount and are resolved via
     * loan-topic lookup.
     */
    public <K extends Enum<K> & ArticleType<K, TradeRelationType>> Result<Map<K, ArticleComponent>> buildSinglePair(
            K debitType, K creditType, Money amount, ResolvedAccounts resolvedAccounts) {
        return buildSinglePairWithContext(debitType, creditType, amount, null, resolvedAccounts);
    }

    /**
     * Builds a debit/credit pair where one or both legs may be contextual (e.g. disbursement to a deposit or external
     * account). Contextual article types are resolved via the destination; non-contextual types use loan-topic +
     * account lookup.
     */
    public <K extends Enum<K> & ArticleType<K, TradeRelationType>>
            Result<Map<K, ArticleComponent>> buildSinglePairWithContext(
                    K debitType,
                    K creditType,
                    Money amount,
                    @Nullable ContextualArticleDestination destination,
                    ResolvedAccounts resolvedAccounts) {

        Result<ArticleComponent> debitResult = resolveComponent(debitType, amount, destination, resolvedAccounts);
        if (debitResult.isFailure()) {
            return Result.failure(debitResult.err().orElseThrow());
        }

        Result<ArticleComponent> creditResult = resolveComponent(creditType, amount, destination, resolvedAccounts);
        if (creditResult.isFailure()) {
            return Result.failure(creditResult.err().orElseThrow());
        }

        Map<K, ArticleComponent> components = new HashMap<>();
        components.put(debitType, debitResult.unwrap());
        components.put(creditType, creditResult.unwrap());
        return Result.success(components);
    }

    // ── Internal resolution ──────────────────────────────────────

    private <K extends Enum<K> & ArticleType<K, TradeRelationType>> Result<ArticleComponent> resolveComponent(
            K articleType,
            Money amount,
            @Nullable ContextualArticleDestination destination,
            ResolvedAccounts resolvedAccounts) {

        if (articleType.isContextual()) {
            if (destination == null) {
                return Result.failure(Notification.ofError(
                        DocumentErrors.FIELD_REQUIRED, "destination (contextual article type: " + articleType + ")"));
            }
            return resolveContextualComponent(amount, destination);
        }
        return resolveAccountComponent(articleType, amount, resolvedAccounts);
    }

    /**
     * Resolves an account-based component: finds the matching {@link LoanTopic} for the article's relation type and
     * economic sector, converts it to a {@link PostingTopic}, then creates an {@link AccountArticleComponent}.
     */
    private <K extends Enum<K> & ArticleType<K, TradeRelationType>> Result<ArticleComponent> resolveAccountComponent(
            K articleType, Money amount, ResolvedAccounts resolvedAccounts) {

        RelationType<TradeRelationType> relationType = articleType.getRelationType();
        AccountId accountId = resolvedAccounts.getAccount(relationType);
        if (accountId == null) {
            return Result.failure(DocumentErrors.CONTEXT_ACCOUNT_NOT_RESOLVED, relationType);
        }

        Optional<LoanTopic> loanTopicOpt = relationTopics.get(relationType).stream()
                .filter(topic -> topic.economicSectors().contains(economicSector))
                .findFirst();

        return loanTopicOpt
                .map(loanTopic -> {
                    PostingTopic postingTopic = toPostingTopic(loanTopic);
                    return AccountArticleComponent.of(amount, postingTopic, accountId)
                            .map(c -> (ArticleComponent) c);
                })
                .orElseGet(() -> Result.failure(
                        DocumentErrors.ARTICLE_COMPONENT_TOPIC_REQUIRED,
                        "No topic for relation=" + articleType + ", sector=" + economicSector));
    }

    /** Resolves a contextual component from the disbursement destination. */
    private Result<ArticleComponent> resolveContextualComponent(
            Money amount, ContextualArticleDestination destination) {

        ArticleTargetType targetType = destination.type().getTargetType();
        return switch (targetType) {
            case BOX -> TargetArticleComponent.forBox(amount).map(c -> (ArticleComponent) c);
            case DEPOSIT ->
                destination
                        .depositNumber()
                        .map(dn -> TargetArticleComponent.forDeposit(amount, dn).map(c -> (ArticleComponent) c))
                        .orElseGet(() -> Result.failure(
                                Notification.ofError(DocumentErrors.ARTICLE_DEPOSIT_NUMBER_REQUIRED_FOR_DEPOSIT)));
            case ACCOUNT ->
                destination
                        .accountNumber()
                        .map(an -> TargetArticleComponent.forAccount(amount, an).map(c -> (ArticleComponent) c))
                        .orElseGet(() -> Result.failure(
                                Notification.ofError(DocumentErrors.ARTICLE_ACCOUNT_NUMBER_REQUIRED_FOR_ACCOUNT)));
        };
    }

    /** Converts a loan-domain {@link LoanTopic} to a platform {@link PostingTopic}. */
    private static PostingTopic toPostingTopic(LoanTopic loanTopic) {
        return PostingTopic.of(loanTopic.code(), loanTopic.name());
    }
}
