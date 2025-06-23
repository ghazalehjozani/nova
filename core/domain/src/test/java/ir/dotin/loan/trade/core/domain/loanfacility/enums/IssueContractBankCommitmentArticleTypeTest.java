package ir.dotin.loan.trade.core.domain.loanfacility.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ir.dotin.loan.trade.core.domain.loantype.enums.TradeRelationType;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("IssueContractBankCommitmentArticleType Enum")
final class IssueContractBankCommitmentArticleTypeTest {

    @Test
    @DisplayName("should have expected enum values")
    void shouldHaveExpectedValues() {
        var values = IssueContractBankCommitmentArticleType.values();
        assertThat(values)
                .hasSize(2)
                .containsExactly(
                        IssueContractBankCommitmentArticleType.BANK_COMMITMENT_DEBIT_LEG,
                        IssueContractBankCommitmentArticleType.BANK_COMMITMENT_CREDIT_LEG);
    }

    @Test
    @DisplayName("should support enum valueOf")
    void shouldSupportValueOf() {
        var values = IssueContractBankCommitmentArticleType.values();
        for (IssueContractBankCommitmentArticleType value : values) {
            var found = IssueContractBankCommitmentArticleType.valueOf(value.name());
            assertThat(found).isEqualTo(value);
        }
    }

    @Test
    @DisplayName("should have consistent toString and name")
    void shouldHaveConsistentToStringAndName() {
        var values = IssueContractBankCommitmentArticleType.values();
        for (IssueContractBankCommitmentArticleType value : values) {
            assertThat(value).hasToString(value.name());
        }
    }

    @Test
    @DisplayName("should return correct relation types")
    void shouldReturnCorrectRelationTypes() {
        assertThat(IssueContractBankCommitmentArticleType.BANK_COMMITMENT_DEBIT_LEG.getRelationType())
                .isEqualTo(TradeRelationType.BANK_COMMITMENTS_CONTRA);
        assertThat(IssueContractBankCommitmentArticleType.BANK_COMMITMENT_CREDIT_LEG.getRelationType())
                .isEqualTo(TradeRelationType.BANK_COMMITMENTS);
    }

    @Test
    @DisplayName("should implement ArticleType interface correctly")
    void shouldImplementArticleTypeInterfaceCorrectly() {
        var values = IssueContractBankCommitmentArticleType.values();
        for (IssueContractBankCommitmentArticleType value : values) {
            assertThat(value.getRelationType()).isNotNull();
            assertThat(value.getRelationType()).isInstanceOf(TradeRelationType.class);
        }
    }
}
