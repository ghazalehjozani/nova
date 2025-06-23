package ir.dotin.loan.trade.core.domain.loanfacility.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ir.dotin.loan.trade.core.domain.loantype.enums.TradeRelationType;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DisburseBankCommitmentArticleType Enum")
final class DisburseBankCommitmentArticleTypeTest {

    @Test
    @DisplayName("should have expected enum values")
    void shouldHaveExpectedValues() {
        var values = DisburseBankCommitmentArticleType.values();
        assertThat(values)
                .hasSize(2)
                .containsExactly(
                        DisburseBankCommitmentArticleType.BANK_COMMITMENT_DEBIT_LEG,
                        DisburseBankCommitmentArticleType.BANK_COMMITMENT_CREDIT_LEG);
    }

    @Test
    @DisplayName("should support enum valueOf")
    void shouldSupportValueOf() {
        var values = DisburseBankCommitmentArticleType.values();
        for (DisburseBankCommitmentArticleType value : values) {
            var found = DisburseBankCommitmentArticleType.valueOf(value.name());
            assertThat(found).isEqualTo(value);
        }
    }

    @Test
    @DisplayName("should have consistent toString and name")
    void shouldHaveConsistentToStringAndName() {
        var values = DisburseBankCommitmentArticleType.values();
        for (DisburseBankCommitmentArticleType value : values) {
            assertThat(value).hasToString(value.name());
        }
    }

    @Test
    @DisplayName("should return correct relation types")
    void shouldReturnCorrectRelationTypes() {
        assertThat(DisburseBankCommitmentArticleType.BANK_COMMITMENT_DEBIT_LEG.getRelationType())
                .isEqualTo(TradeRelationType.BANK_COMMITMENTS);
        assertThat(DisburseBankCommitmentArticleType.BANK_COMMITMENT_CREDIT_LEG.getRelationType())
                .isEqualTo(TradeRelationType.BANK_COMMITMENTS_CONTRA);
    }

    @Test
    @DisplayName("should implement ArticleType interface correctly")
    void shouldImplementArticleTypeInterfaceCorrectly() {
        var values = DisburseBankCommitmentArticleType.values();
        for (DisburseBankCommitmentArticleType value : values) {
            assertThat(value.getRelationType()).isNotNull();
            assertThat(value.getRelationType()).isInstanceOf(TradeRelationType.class);
        }
    }
}
