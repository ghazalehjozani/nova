package ir.dotin.loan.trade.core.domain.loanfacility.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ir.dotin.loan.trade.core.domain.loantype.enums.TradeRelationType;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DisbursedInterestArticleType Enum")
final class DisbursedInterestArticleTypeTest {

    @Test
    @DisplayName("should have expected enum values")
    void shouldHaveExpectedValues() {
        var values = DisbursedInterestArticleType.values();
        assertThat(values)
                .hasSize(2)
                .containsExactly(
                        DisbursedInterestArticleType.INTEREST_DEBIT_LEG,
                        DisbursedInterestArticleType.INTEREST_CREDIT_LEG);
    }

    @Test
    @DisplayName("should support enum valueOf")
    void shouldSupportValueOf() {
        var values = DisbursedInterestArticleType.values();
        for (DisbursedInterestArticleType value : values) {
            var found = DisbursedInterestArticleType.valueOf(value.name());
            assertThat(found).isEqualTo(value);
        }
    }

    @Test
    @DisplayName("should have consistent toString and name")
    void shouldHaveConsistentToStringAndName() {
        var values = DisbursedInterestArticleType.values();
        for (DisbursedInterestArticleType value : values) {
            assertThat(value).hasToString(value.name());
        }
    }

    @Test
    @DisplayName("should return correct relation types")
    void shouldReturnCorrectRelationTypes() {
        assertThat(DisbursedInterestArticleType.INTEREST_DEBIT_LEG.getRelationType())
                .isEqualTo(TradeRelationType.PRINCIPAL);
        assertThat(DisbursedInterestArticleType.INTEREST_CREDIT_LEG.getRelationType())
                .isEqualTo(TradeRelationType.FUTURE_INTEREST);
    }

    @Test
    @DisplayName("should implement ArticleType interface correctly")
    void shouldImplementArticleTypeInterfaceCorrectly() {
        var values = DisbursedInterestArticleType.values();
        for (DisbursedInterestArticleType value : values) {
            assertThat(value.getRelationType()).isNotNull();
            assertThat(value.getRelationType()).isInstanceOf(TradeRelationType.class);
        }
    }
}
