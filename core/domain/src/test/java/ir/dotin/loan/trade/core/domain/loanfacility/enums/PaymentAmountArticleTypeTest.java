package ir.dotin.loan.trade.core.domain.loanfacility.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ir.dotin.loan.trade.core.domain.loantype.enums.TradeRelationType;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PaymentAmountArticleType Enum")
final class PaymentAmountArticleTypeTest {

    @Test
    @DisplayName("should have expected enum values")
    void shouldHaveExpectedValues() {
        var values = PaymentAmountArticleType.values();
        assertThat(values)
                .hasSize(2)
                .containsExactly(
                        PaymentAmountArticleType.PRINCIPAL_DEBIT_LEG, PaymentAmountArticleType.DISBURSEMENT_CREDIT);
    }

    @Test
    @DisplayName("should support enum valueOf")
    void shouldSupportValueOf() {
        var values = PaymentAmountArticleType.values();
        for (PaymentAmountArticleType value : values) {
            var found = PaymentAmountArticleType.valueOf(value.name());
            assertThat(found).isEqualTo(value);
        }
    }

    @Test
    @DisplayName("should have consistent toString and name")
    void shouldHaveConsistentToStringAndName() {
        var values = PaymentAmountArticleType.values();
        for (PaymentAmountArticleType value : values) {
            assertThat(value).hasToString(value.name());
        }
    }

    @Test
    @DisplayName("should return correct relation types")
    void shouldReturnCorrectRelationTypes() {
        assertThat(PaymentAmountArticleType.PRINCIPAL_DEBIT_LEG.getRelationType())
                .isEqualTo(TradeRelationType.PRINCIPAL);
        assertThat(PaymentAmountArticleType.DISBURSEMENT_CREDIT.getRelationType())
                .isEqualTo(TradeRelationType.DISBURSEMENT_TRANSACTION_CONTEXT);
    }

    @Test
    @DisplayName("should implement ArticleType interface correctly")
    void shouldImplementArticleTypeInterfaceCorrectly() {
        var values = PaymentAmountArticleType.values();
        for (PaymentAmountArticleType value : values) {
            assertThat(value.getRelationType()).isNotNull();
            assertThat(value.getRelationType()).isInstanceOf(TradeRelationType.class);
        }
    }
}
