package ir.dotin.loan.trade.core.domain.shared.document.config;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import ir.dotin.platform.accounting.document.api.enumeration.MetadataSection;
import ir.dotin.platform.accounting.document.api.strategy.config.MetadataConfig;
import ir.dotin.platform.commons.domain.annotation.DomainComponent;
import ir.dotin.loan.trade.core.domain.loantype.enums.TradeRelationType;
import ir.dotin.loan.trade.core.domain.shared.document.enums.DisburseBankCommitmentArticleType;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toUnmodifiableSet;

@DomainComponent
public class BankCommitmentMetadataConfig
        implements MetadataConfig<DisburseBankCommitmentArticleType, TradeRelationType> {

    private static final List<MetadataSection> DEBIT_SPECIFIC_SECTIONS = List.of(
            MetadataSection.SOURCE_ORIGINATOR_INFO,
            MetadataSection.SOURCE_TOOL_INFO,
            MetadataSection.SOURCE_PRODUCT_INFO);

    private static final List<MetadataSection> CREDIT_SPECIFIC_SECTIONS = List.of(
            MetadataSection.DESTINATION_PRODUCT_INFO,
            MetadataSection.DESTINATION_RECEIVER_INFO,
            MetadataSection.DESTINATION_TOOL_INFO);

    private static final Map<DisburseBankCommitmentArticleType, List<MetadataSection>> TYPE_SPECIFIC_SECTIONS = Map.of(
            DisburseBankCommitmentArticleType.BANK_COMMITMENT_DEBIT_LEG, DEBIT_SPECIFIC_SECTIONS,
            DisburseBankCommitmentArticleType.BANK_COMMITMENT_CREDIT_LEG, CREDIT_SPECIFIC_SECTIONS);

    @Override
    public List<MetadataSection> getMetadataSections(DisburseBankCommitmentArticleType articleType) {
        requireNonNull(articleType, "Article type cannot be null");
        var specificSections = TYPE_SPECIFIC_SECTIONS.getOrDefault(articleType, List.of());
        return Stream.concat(getCommonSections().stream(), specificSections.stream())
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
