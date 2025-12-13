package ir.dotin.loan.trade.core.domain.loanfacility.strategy.impl;

import java.math.BigDecimal;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.platform.commons.domain.vo.CurrencyType;
import ir.dotin.platform.commons.domain.vo.Money;
import ir.dotin.loan.baseloan.core.domain.shared.strategy.CalculationContext;
import ir.dotin.loan.baseloan.core.domain.shared.strategy.factory.DebitCreditArticleSpecFactory;
import ir.dotin.loan.baseloan.core.domain.shared.validator.ArticleBalanceValidator;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.AccountArticleComponent;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;
import ir.dotin.loan.trade.core.domain.loanfacility.enums.PaymentAmountArticleType;
import ir.dotin.loan.trade.core.domain.loanfacility.i18n.TradeLoanFacilityLocalizedMessageCodes;
import ir.dotin.loan.trade.core.domain.loantype.enums.TradeRelationType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;

@DisplayName("PaymentAmountTransactionStrategy Test")
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NullAway")
final class PaymentAmountTransactionStrategyTest {

    @Mock
    private DebitCreditArticleSpecFactory<PaymentAmountArticleType, TradeRelationType> mockSpecFactory;

    @Mock
    private ArticleBalanceValidator mockArticleBalanceValidator;

    @Mock
    private CalculationContext<TradeLoanFacility, TradeRelationType, PaymentAmountArticleType> mockCalculationContext;

    @Mock
    private AccountArticleComponent mockArticleComponent;

    private PaymentAmountTransactionStrategy strategy;
    private Money moneyZero;
    private static final CurrencyType DEFAULT_CURRENCY = CurrencyType.IRR;
    private MockedStatic<Money> mockedMoneyStatic;

    @BeforeEach
    void setUp() {
        strategy = new PaymentAmountTransactionStrategy(mockSpecFactory, mockArticleBalanceValidator);

        setupMoneyMocks();
        setupBasicMocks();
    }

    private void setupMoneyMocks() {
        mockedMoneyStatic = mockStatic(Money.class);
        moneyZero = new Money(BigDecimal.ZERO, DEFAULT_CURRENCY);

        mockedMoneyStatic.when(() -> Money.zero(DEFAULT_CURRENCY)).thenReturn(Result.success(moneyZero));
        mockedMoneyStatic
                .when(() -> Money.valueOf(any(BigDecimal.class), any(CurrencyType.class)))
                .thenAnswer(invocation -> {
                    BigDecimal amount = invocation.getArgument(0);
                    CurrencyType currency = invocation.getArgument(1);
                    return Result.success(new Money(amount, currency));
                });
    }

    @AfterEach
    void tearDown() {
        if (mockedMoneyStatic != null) {
            mockedMoneyStatic.close();
        }
    }

    private void setupBasicMocks() {
        lenient().when(mockCalculationContext.currencyType()).thenReturn(DEFAULT_CURRENCY);
        lenient().when(mockSpecFactory.getDebitArticleType()).thenReturn(PaymentAmountArticleType.PRINCIPAL_DEBIT_LEG);
        lenient().when(mockSpecFactory.getCreditArticleType()).thenReturn(PaymentAmountArticleType.DISBURSEMENT_CREDIT);
    }

    @Nested
    @DisplayName("Constructor Tests")
    final class ConstructorTests {

        @Test
        @DisplayName("should create strategy with valid parameters")
        void shouldCreateStrategyWithValidParameters() {
            var newStrategy = new PaymentAmountTransactionStrategy(mockSpecFactory, mockArticleBalanceValidator);

            assertThat(newStrategy).isNotNull();
        }

        @Test
        @DisplayName("should throw exception when spec factory is null")
        void shouldThrowExceptionWhenSpecFactoryIsNull() {
            assertThatThrownBy(() -> new PaymentAmountTransactionStrategy(null, mockArticleBalanceValidator))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Spec factory cannot be null");
        }

        @Test
        @DisplayName("should throw exception when balance validator is null")
        void shouldThrowExceptionWhenBalanceValidatorIsNull() {
            assertThatThrownBy(() -> new PaymentAmountTransactionStrategy(mockSpecFactory, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Balance validator cannot be null");
        }
    }

    @Nested
    @DisplayName("Generate Debits Tests")
    final class GenerateDebitsTests {

        @Test
        @DisplayName("should call required components when generating debits")
        void shouldCallRequiredComponentsWhenGeneratingDebits() {
            given(mockCalculationContext.requireArticleComponent(PaymentAmountArticleType.PRINCIPAL_DEBIT_LEG))
                    .willReturn(Result.failure(Notification.ofError(
                            TradeLoanFacilityLocalizedMessageCodes.UNEXPECTED_ERROR, "Component not found")));

            var result = strategy.generateDebits(mockCalculationContext);

            assertThat(result.isFailure()).isTrue();
            then(mockCalculationContext).should().requireArticleComponent(PaymentAmountArticleType.PRINCIPAL_DEBIT_LEG);
        }

        @Test
        @DisplayName("should return failure when article component is missing")
        void shouldReturnFailureWhenArticleComponentIsMissing() {
            given(mockCalculationContext.requireArticleComponent(PaymentAmountArticleType.PRINCIPAL_DEBIT_LEG))
                    .willReturn(Result.failure(Notification.ofError(
                            TradeLoanFacilityLocalizedMessageCodes.UNEXPECTED_ERROR, "Article component not found")));

            var result = strategy.generateDebits(mockCalculationContext);

            assertThat(result.isFailure()).isTrue();
            then(mockSpecFactory).should(never()).createDebitSpec(any());
        }

        @Test
        @DisplayName("should return failure when spec creation fails")
        void shouldReturnFailureWhenSpecCreationFails() {
            given(mockCalculationContext.requireArticleComponent(PaymentAmountArticleType.PRINCIPAL_DEBIT_LEG))
                    .willReturn(Result.success(mockArticleComponent));
            given(mockSpecFactory.createDebitSpec(mockArticleComponent))
                    .willReturn(Result.failure(Notification.ofError(
                            TradeLoanFacilityLocalizedMessageCodes.UNEXPECTED_ERROR, "Spec creation failed")));

            var result = strategy.generateDebits(mockCalculationContext);

            assertThat(result.isFailure()).isTrue();
        }
    }

    @Nested
    @DisplayName("Generate Credits Tests")
    final class GenerateCreditsTests {

        @Test
        @DisplayName("should call required components when generating credits")
        void shouldCallRequiredComponentsWhenGeneratingCredits() {
            given(mockCalculationContext.requireArticleComponent(PaymentAmountArticleType.DISBURSEMENT_CREDIT))
                    .willReturn(Result.failure(Notification.ofError(
                            TradeLoanFacilityLocalizedMessageCodes.UNEXPECTED_ERROR, "Component not found")));

            var result = strategy.generateCredits(mockCalculationContext);

            assertThat(result.isFailure()).isTrue();
            then(mockCalculationContext).should().requireArticleComponent(PaymentAmountArticleType.DISBURSEMENT_CREDIT);
        }

        @Test
        @DisplayName("should return failure when article component is missing")
        void shouldReturnFailureWhenArticleComponentIsMissing() {
            given(mockCalculationContext.requireArticleComponent(PaymentAmountArticleType.DISBURSEMENT_CREDIT))
                    .willReturn(Result.failure(Notification.ofError(
                            TradeLoanFacilityLocalizedMessageCodes.UNEXPECTED_ERROR, "Article component not found")));

            var result = strategy.generateCredits(mockCalculationContext);

            assertThat(result.isFailure()).isTrue();
            then(mockSpecFactory).should(never()).createCreditSpec(any());
        }

        @Test
        @DisplayName("should return failure when spec creation fails")
        void shouldReturnFailureWhenSpecCreationFails() {
            given(mockCalculationContext.requireArticleComponent(PaymentAmountArticleType.DISBURSEMENT_CREDIT))
                    .willReturn(Result.success(mockArticleComponent));
            given(mockSpecFactory.createCreditSpec(mockArticleComponent))
                    .willReturn(Result.failure(Notification.ofError(
                            TradeLoanFacilityLocalizedMessageCodes.UNEXPECTED_ERROR, "Spec creation failed")));

            var result = strategy.generateCredits(mockCalculationContext);

            assertThat(result.isFailure()).isTrue();
        }
    }

    @Nested
    @DisplayName("Strategy Interface Tests")
    final class StrategyInterfaceTests {

        @Test
        @DisplayName("should implement CashMovementStrategy interface")
        void shouldImplementCashMovementStrategyInterface() {
            assertThat(strategy)
                    .isInstanceOf(ir.dotin.loan.trade.core.domain.loanfacility.strategy.CashMovementStrategy.class);
        }

        @Test
        @DisplayName("should extend AbstractMultiArticleCalculationStrategy")
        void shouldExtendAbstractMultiArticleCalculationStrategy() {
            assertThat(strategy)
                    .isInstanceOf(
                            ir.dotin.loan.baseloan.core.domain.shared.strategy.AbstractMultiArticleCalculationStrategy
                                    .class);
        }
    }
}
