// package ir.dotin.loan.trade.core.domain.loanfacility.service;
//
// import com.google.common.collect.ImmutableList;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Nested;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;
//
// import ir.dotin.platform.pangaea.commons.core.Notification;
// import ir.dotin.platform.pangaea.commons.core.Result;
// import ir.dotin.platform.pangaea.commons.domain.annotation.DomainService;
// import ir.dotin.loan.baseloan.core.domain.shared.factory.DocumentFactory;
// import ir.dotin.loan.baseloan.core.domain.shared.strategy.CalculationContext;
// import ir.dotin.loan.baseloan.core.domain.shared.strategy.DocumentCalculationStrategy;
// import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTransaction;
// import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;
// import ir.dotin.loan.trade.core.domain.loanfacility.enums.DisbursedInterestArticleType;
// import ir.dotin.loan.trade.core.domain.loanfacility.error.TradeLoanFacilityLocalizedMessageCodes;
// import ir.dotin.loan.trade.core.domain.loantype.enums.TradeRelationType;
//
// import static org.assertj.core.api.Assertions.assertThat;
// import static org.assertj.core.api.Assertions.assertThatThrownBy;
// import static org.mockito.BDDMockito.given;
// import static org.mockito.BDDMockito.then;
// import static org.mockito.Mockito.never;
//
// @DisplayName("TradeDisbursementTransactionService Test")
// @ExtendWith(MockitoExtension.class)
// @SuppressWarnings("NullAway")
// final class TradeDisbursementTransactionServiceTest {
//
//    @Mock
//    private DocumentFactory mockDocumentFactory;
//
//    @Mock
//    private CalculationContext<TradeLoanFacility, TradeRelationType, DisbursedInterestArticleType> mockContext;
//
//    @Mock
//    private DocumentCalculationStrategy<TradeLoanFacility, TradeRelationType, DisbursedInterestArticleType>
//            mockStrategy;
//
//    @Mock
//    private DocumentCalculationStrategy<TradeLoanFacility, TradeRelationType, DisbursedInterestArticleType>
//            mockStrategy2;
//
//    @Mock
//    private LoanTransaction mockTransaction;
//
//    @Mock
//    private LoanTransaction mockTransaction2;
//
//    private TradeDisbursementTransactionService service;
//    private static final String POST_TITLE = "Test Post Title";
//
//    @BeforeEach
//    void setUp() {
//        service = new TradeDisbursementTransactionService(mockDocumentFactory);
//    }
//
//    @Nested
//    @DisplayName("Constructor Tests")
//    final class ConstructorTests {
//
//        @Test
//        @DisplayName("should create service with valid document factory")
//        void shouldCreateServiceWithValidDocumentFactory() {
//            var newService = new TradeDisbursementTransactionService(mockDocumentFactory);
//
//            assertThat(newService).isNotNull();
//        }
//
//        @Test
//        @DisplayName("should throw exception when document factory is null")
//        void shouldThrowExceptionWhenDocumentFactoryIsNull() {
//            assertThatThrownBy(() -> new TradeDisbursementTransactionService(null))
//                    .isInstanceOf(NullPointerException.class);
//        }
//    }
//
//    @Nested
//    @DisplayName("Calculate Single Disbursement Transaction Tests")
//    final class CalculateSingleDisbursementTransactionTests {
//
//        @Test
//        @DisplayName("should calculate transaction successfully")
//        void shouldCalculateTransactionSuccessfully() {
//            given(mockDocumentFactory.createTransaction(mockContext, mockStrategy, POST_TITLE))
//                    .willReturn(Result.success(mockTransaction));
//
//            var result = service.calculateDisbursementTransaction(mockContext, mockStrategy, POST_TITLE);
//
//            assertThat(result.isSuccessWithValue()).isTrue();
//            assertThat(result.value()).isEqualTo(mockTransaction);
//            then(mockDocumentFactory).should().createTransaction(mockContext, mockStrategy, POST_TITLE);
//        }
//
//        @Test
//        @DisplayName("should return failure when document factory fails")
//        void shouldReturnFailureWhenDocumentFactoryFails() {
//            var failureNotification = Notification.ofError(
//                    TradeLoanFacilityLocalizedMessageCodes.UNEXPECTED_ERROR, "Transaction creation failed");
//            given(mockDocumentFactory.createTransaction(mockContext, mockStrategy, POST_TITLE))
//                    .willReturn(Result.failure(failureNotification));
//
//            var result = service.calculateDisbursementTransaction(mockContext, mockStrategy, POST_TITLE);
//
//            assertThat(result.isFailure()).isTrue();
//            then(mockDocumentFactory).should().createTransaction(mockContext, mockStrategy, POST_TITLE);
//        }
//
//        @Test
//        @DisplayName("should throw exception when context is null")
//        void shouldThrowExceptionWhenContextIsNull() {
//            assertThatThrownBy(() -> service.calculateDisbursementTransaction(null, mockStrategy, POST_TITLE))
//                    .isInstanceOf(NullPointerException.class)
//                    .hasMessageContaining("context cannot be null");
//
//            then(mockDocumentFactory).should(never()).createTransaction(null, mockStrategy, POST_TITLE);
//        }
//
//        @Test
//        @DisplayName("should throw exception when strategy is null")
//        void shouldThrowExceptionWhenStrategyIsNull() {
//            assertThatThrownBy(() -> service.calculateDisbursementTransaction(mockContext, null, POST_TITLE))
//                    .isInstanceOf(NullPointerException.class)
//                    .hasMessageContaining("strategy cannot be null");
//
//            then(mockDocumentFactory).should(never()).createTransaction(mockContext, null, POST_TITLE);
//        }
//
//        @Test
//        @DisplayName("should throw exception when post title is null")
//        void shouldThrowExceptionWhenPostTitleIsNull() {
//            assertThatThrownBy(() -> service.calculateDisbursementTransaction(mockContext, mockStrategy, null))
//                    .isInstanceOf(NullPointerException.class)
//                    .hasMessageContaining("postTitle cannot be null");
//
//            then(mockDocumentFactory).should(never()).createTransaction(mockContext, mockStrategy, null);
//        }
//    }
//
//    @Nested
//    @DisplayName("Calculate Multiple Disbursement Transactions Tests")
//    final class CalculateMultipleDisbursementTransactionsTests {
//
//        @Test
//        @DisplayName("should calculate multiple transactions successfully")
//        void shouldCalculateMultipleTransactionsSuccessfully() {
//            var strategies = ImmutableList.of(mockStrategy, mockStrategy2);
//
//            given(mockDocumentFactory.createTransaction(mockContext, mockStrategy, POST_TITLE))
//                    .willReturn(Result.success(mockTransaction));
//            given(mockDocumentFactory.createTransaction(mockContext, mockStrategy2, POST_TITLE))
//                    .willReturn(Result.success(mockTransaction2));
//
//            var result = service.calculateMultipleDisbursementTransactions(mockContext, strategies, POST_TITLE);
//
//            assertThat(result.isSuccessWithValue()).isTrue();
//            assertThat(result.value()).containsExactly(mockTransaction, mockTransaction2);
//            then(mockDocumentFactory).should().createTransaction(mockContext, mockStrategy, POST_TITLE);
//            then(mockDocumentFactory).should().createTransaction(mockContext, mockStrategy2, POST_TITLE);
//        }
//
//        @Test
//        @DisplayName("should return empty list when strategies list is empty")
//        void shouldReturnEmptyListWhenStrategiesListIsEmpty() {
//            var emptyStrategies = ImmutableList
//                    .<DocumentCalculationStrategy<TradeLoanFacility, TradeRelationType, DisbursedInterestArticleType>>
//                            of();
//
//            var result = service.calculateMultipleDisbursementTransactions(mockContext, emptyStrategies, POST_TITLE);
//
//            assertThat(result.isSuccessWithValue()).isTrue();
//            assertThat(result.value()).isEmpty();
//            then(mockDocumentFactory).should(never()).createTransaction(mockContext, mockStrategy, POST_TITLE);
//        }
//
//        @Test
//        @DisplayName("should return failure when one strategy fails")
//        void shouldReturnFailureWhenOneStrategyFails() {
//            var strategies = ImmutableList.of(mockStrategy, mockStrategy2);
//            var failureNotification = Notification.ofError(
//                    TradeLoanFacilityLocalizedMessageCodes.UNEXPECTED_ERROR, "Second transaction failed");
//
//            given(mockDocumentFactory.createTransaction(mockContext, mockStrategy, POST_TITLE))
//                    .willReturn(Result.success(mockTransaction));
//            given(mockDocumentFactory.createTransaction(mockContext, mockStrategy2, POST_TITLE))
//                    .willReturn(Result.failure(failureNotification));
//
//            var result = service.calculateMultipleDisbursementTransactions(mockContext, strategies, POST_TITLE);
//
//            assertThat(result.isFailure()).isTrue();
//            then(mockDocumentFactory).should().createTransaction(mockContext, mockStrategy, POST_TITLE);
//            then(mockDocumentFactory).should().createTransaction(mockContext, mockStrategy2, POST_TITLE);
//        }
//
//        @Test
//        @DisplayName("should handle single strategy in list")
//        void shouldHandleSingleStrategyInList() {
//            var strategies = ImmutableList.of(mockStrategy);
//
//            given(mockDocumentFactory.createTransaction(mockContext, mockStrategy, POST_TITLE))
//                    .willReturn(Result.success(mockTransaction));
//
//            var result = service.calculateMultipleDisbursementTransactions(mockContext, strategies, POST_TITLE);
//
//            assertThat(result.isSuccessWithValue()).isTrue();
//            assertThat(result.value()).containsExactly(mockTransaction);
//            then(mockDocumentFactory).should().createTransaction(mockContext, mockStrategy, POST_TITLE);
//        }
//
//        @Test
//        @DisplayName("should throw exception when context is null")
//        void shouldThrowExceptionWhenContextIsNull() {
//            var strategies = ImmutableList.of(mockStrategy);
//
//            assertThatThrownBy(() -> service.calculateMultipleDisbursementTransactions(null, strategies, POST_TITLE))
//                    .isInstanceOf(NullPointerException.class)
//                    .hasMessageContaining("context cannot be null");
//        }
//
//        @Test
//        @DisplayName("should throw exception when strategies list is null")
//        void shouldThrowExceptionWhenStrategiesListIsNull() {
//            assertThatThrownBy(() -> service.calculateMultipleDisbursementTransactions(mockContext, null, POST_TITLE))
//                    .isInstanceOf(NullPointerException.class)
//                    .hasMessageContaining("strategies cannot be null");
//        }
//
//        @Test
//        @DisplayName("should throw exception when post title is null")
//        void shouldThrowExceptionWhenPostTitleIsNull() {
//            var strategies = ImmutableList.of(mockStrategy);
//
//            assertThatThrownBy(() -> service.calculateMultipleDisbursementTransactions(mockContext, strategies, null))
//                    .isInstanceOf(NullPointerException.class)
//                    .hasMessageContaining("postTitle cannot be null");
//        }
//    }
//
//    @Nested
//    @DisplayName("Service Annotation Tests")
//    final class ServiceAnnotationTests {
//
//        @Test
//        @DisplayName("should be annotated with DomainService")
//        void shouldBeAnnotatedWithDomainService() {
//            assertThat(TradeDisbursementTransactionService.class).hasAnnotation(DomainService.class);
//        }
//
//        @Test
//        @DisplayName("should be a final class")
//        void shouldBeAFinalClass() {
//            assertThat(TradeDisbursementTransactionService.class).isFinal();
//        }
//    }
//
//    @Nested
//    @DisplayName("Integration Tests")
//    final class IntegrationTests {
//
//        @Test
//        @DisplayName("should delegate to document factory correctly")
//        void shouldDelegateToDocumentFactoryCorrectly() {
//            given(mockDocumentFactory.createTransaction(mockContext, mockStrategy, POST_TITLE))
//                    .willReturn(Result.success(mockTransaction));
//
//            var result = service.calculateDisbursementTransaction(mockContext, mockStrategy, POST_TITLE);
//
//            assertThat(result.isSuccessWithValue()).isTrue();
//            assertThat(result.value()).isSameAs(mockTransaction);
//            then(mockDocumentFactory).should().createTransaction(mockContext, mockStrategy, POST_TITLE);
//        }
//
//        @Test
//        @DisplayName("should use Result.traverse for multiple transactions")
//        void shouldUseResultTraverseForMultipleTransactions() {
//            var strategies = ImmutableList.of(mockStrategy, mockStrategy2);
//
//            given(mockDocumentFactory.createTransaction(mockContext, mockStrategy, POST_TITLE))
//                    .willReturn(Result.success(mockTransaction));
//            given(mockDocumentFactory.createTransaction(mockContext, mockStrategy2, POST_TITLE))
//                    .willReturn(Result.success(mockTransaction2));
//
//            var result = service.calculateMultipleDisbursementTransactions(mockContext, strategies, POST_TITLE);
//
//            assertThat(result.isSuccessWithValue()).isTrue();
//            assertThat(result.value()).hasSize(2);
//            assertThat(result.value()).containsExactly(mockTransaction, mockTransaction2);
//        }
//    }
// }
