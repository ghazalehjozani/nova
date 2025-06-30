package ir.dotin.loan.trade.core.domain.loanfacility.strategy.impl;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ir.dotin.loan.baseloan.core.domain.shared.strategy.DocumentCalculationStrategy;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.ArticleType;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;
import ir.dotin.loan.trade.core.domain.loantype.enums.TradeRelationType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("DefaultDisbursementStrategyProvider Test")
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NullAway")
final class DefaultDisbursementStrategyProviderTest {

    @Mock
    private BankCommitmentTransactionStrategy mockBankCommitmentStrategy;

    @Mock
    private PaymentAmountTransactionStrategy mockPaymentAmountStrategy;

    @Mock
    private DisbursedInterestTransactionStrategy mockDisbursedInterestStrategy;

    @Mock
    private TradeLoanFacility mockTradeLoanFacility;

    private DefaultDisbursementStrategyProvider provider;

    @BeforeEach
    void setUp() {
        provider = new DefaultDisbursementStrategyProvider(
                mockBankCommitmentStrategy, mockPaymentAmountStrategy, mockDisbursedInterestStrategy);
    }

    @Nested
    @DisplayName("Constructor Tests")
    final class ConstructorTests {

        @Test
        @DisplayName("should create provider with valid parameters")
        void shouldCreateProviderWithValidParameters() {
            var newProvider = new DefaultDisbursementStrategyProvider(
                    mockBankCommitmentStrategy, mockPaymentAmountStrategy, mockDisbursedInterestStrategy);

            assertThat(newProvider).isNotNull();
        }

        @Test
        @DisplayName("should throw exception when payment amount strategy is null")
        void shouldThrowExceptionWhenPaymentAmountStrategyIsNull() {
            assertThatThrownBy(() -> new DefaultDisbursementStrategyProvider(
                            mockBankCommitmentStrategy, null, mockDisbursedInterestStrategy))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("should throw exception when disbursed interest strategy is null")
        void shouldThrowExceptionWhenDisbursedInterestStrategyIsNull() {
            assertThatThrownBy(() -> new DefaultDisbursementStrategyProvider(
                            mockBankCommitmentStrategy, mockPaymentAmountStrategy, null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("should allow null bank commitment strategy")
        void shouldAllowNullBankCommitmentStrategy() {
            var newProvider = new DefaultDisbursementStrategyProvider(
                    null, mockPaymentAmountStrategy, mockDisbursedInterestStrategy);

            assertThat(newProvider).isNotNull();
        }
    }

    @Nested
    @DisplayName("GetStrategies Tests")
    final class GetStrategiesTests {

        @Test
        @DisplayName("should return all three strategies when facility is provided")
        void shouldReturnAllThreeStrategiesWhenFacilityIsProvided() {
            List<
                            DocumentCalculationStrategy<
                                    TradeLoanFacility, TradeRelationType, ? extends ArticleType<?, TradeRelationType>>>
                    strategies = provider.getStrategies(mockTradeLoanFacility);

            assertThat(strategies)
                    .hasSize(3)
                    .containsExactly(
                            mockBankCommitmentStrategy, mockPaymentAmountStrategy, mockDisbursedInterestStrategy);
        }

        @Test
        @DisplayName("should return strategies in correct order")
        void shouldReturnStrategiesInCorrectOrder() {
            List<
                            DocumentCalculationStrategy<
                                    TradeLoanFacility, TradeRelationType, ? extends ArticleType<?, TradeRelationType>>>
                    strategies = provider.getStrategies(mockTradeLoanFacility);

            assertThat(strategies.get(0)).isSameAs(mockBankCommitmentStrategy);
            assertThat(strategies.get(1)).isSameAs(mockPaymentAmountStrategy);
            assertThat(strategies.get(2)).isSameAs(mockDisbursedInterestStrategy);
        }

        @Test
        @DisplayName("should throw exception when facility is null")
        void shouldThrowExceptionWhenFacilityIsNull() {
            assertThatThrownBy(() -> provider.getStrategies(null)).isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("should return immutable list")
        void shouldReturnImmutableList() {
            List<
                            DocumentCalculationStrategy<
                                    TradeLoanFacility, TradeRelationType, ? extends ArticleType<?, TradeRelationType>>>
                    strategies = provider.getStrategies(mockTradeLoanFacility);

            assertThatThrownBy(() -> strategies.add(mockBankCommitmentStrategy))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("should throw exception when getting strategies with null bank commitment strategy")
        void shouldThrowExceptionWhenGettingStrategiesWithNullBankCommitmentStrategy() {
            var providerWithNullBankCommitment = new DefaultDisbursementStrategyProvider(
                    null, mockPaymentAmountStrategy, mockDisbursedInterestStrategy);

            assertThatThrownBy(() -> providerWithNullBankCommitment.getStrategies(mockTradeLoanFacility))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("Interface Implementation Tests")
    final class InterfaceImplementationTests {

        @Test
        @DisplayName("should implement DisbursementStrategyProvider interface")
        void shouldImplementDisbursementStrategyProviderInterface() {
            assertThat(provider)
                    .isInstanceOf(
                            ir.dotin.loan.trade.core.domain.loanfacility.strategy.DisbursementStrategyProvider.class);
        }

        @Test
        @DisplayName("should be annotated with DomainComponent")
        void shouldBeAnnotatedWithDomainComponent() {
            assertThat(DefaultDisbursementStrategyProvider.class)
                    .hasAnnotation(ir.dotin.platform.domain.common.annotation.DomainComponent.class);
        }

        @Test
        @DisplayName("should be a final class")
        void shouldBeAFinalClass() {
            assertThat(DefaultDisbursementStrategyProvider.class).isFinal();
        }
    }
}
