package ir.dotin.loan.trade.core.domain.disbursement.strategy.config;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import ir.dotin.platform.domain.common.annotation.DomainService;
import ir.dotin.loan.baseloan.core.domain.shared.enums.transaction.MetadataSection;
import ir.dotin.loan.baseloan.core.domain.shared.strategy.config.MetadataConfig;
import ir.dotin.loan.trade.core.domain.disbursement.enums.DisbursedInterestArticleType;
import ir.dotin.loan.trade.core.domain.loantype.enums.TradeRelationType;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toUnmodifiableSet;

@DomainService
public class DisbursedInterestMetadataConfig
        implements MetadataConfig<DisbursedInterestArticleType, TradeRelationType> {

    private static final List<MetadataSection> DEBIT_SPECIFIC_SECTIONS = ImmutableList.of(
            MetadataSection.SOURCE_ORIGINATOR_INFO,
            MetadataSection.SOURCE_TOOL_INFO,
            MetadataSection.SOURCE_PRODUCT_INFO);

    private static final List<MetadataSection> CREDIT_SPECIFIC_SECTIONS = ImmutableList.of(
            MetadataSection.DESTINATION_PRODUCT_INFO,
            MetadataSection.DESTINATION_RECEIVER_INFO,
            MetadataSection.DESTINATION_TOOL_INFO);

    private static final Map<DisbursedInterestArticleType, List<MetadataSection>> TYPE_SPECIFIC_SECTIONS =
            ImmutableMap.of(
                    DisbursedInterestArticleType.INTEREST_DEBIT_LEG, DEBIT_SPECIFIC_SECTIONS,
                    DisbursedInterestArticleType.INTEREST_CREDIT_LEG, CREDIT_SPECIFIC_SECTIONS);

    @Override
    public List<MetadataSection> getMetadataSections(DisbursedInterestArticleType articleType) {
        requireNonNull(articleType, "Article type cannot be null");

        var specificSections = TYPE_SPECIFIC_SECTIONS.getOrDefault(articleType, ImmutableList.of());
        var commonSections = getCommonSections();

        return Stream.concat(commonSections.stream(), specificSections.stream())
                .distinct()
                .toList();
    }

    @Override
    public Set<MetadataSection> getAllPossibleSections() {
        return Stream.concat(
                        getCommonSections().stream(),
                        TYPE_SPECIFIC_SECTIONS.values().stream().flatMap(List::stream))
                .collect(toUnmodifiableSet());
    }
}
