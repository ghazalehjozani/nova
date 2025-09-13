package ir.dotin.loan.trade.core.domain.loanfacility.strategy.factory;

import java.util.List;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ir.dotin.platform.commons.core.Result;
import ir.dotin.platform.commons.domain.vo.Money;
import ir.dotin.loan.baseloan.core.domain.shared.enums.TransactionCause;
import ir.dotin.loan.baseloan.core.domain.shared.enums.TransactionType;
import ir.dotin.loan.baseloan.core.domain.shared.enums.transaction.MetadataSection;
import ir.dotin.loan.baseloan.core.domain.shared.strategy.ArticleSpec;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTopic;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.ArticleComponent;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.metadata.TransactionInfo;
import ir.dotin.loan.trade.core.domain.loanfacility.enums.DisbursedInterestArticleType;
import ir.dotin.loan.trade.core.domain.loanfacility.strategy.config.DisbursedInterestMetadataConfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.lenient;

@DisplayName("DisbursedInterestArticleSpecFactory Test")
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NullAway")
final class DisbursedInterestArticleSpecFactoryTest {

    @Mock
    private DisbursedInterestMetadataConfig mockMetadataConfig;

    @Mock
    private ArticleComponent mockComponent;

    @Mock
    private Money mockAmount;

    @Mock
    private LoanTopic mockTopic;

    private DisbursedInterestArticleSpecFactory factory;
    private TransactionInfo expectedTransactionInfo;

    @BeforeEach
    void setUp() {
        expectedTransactionInfo = TransactionInfo.of(
                        TransactionType.CODE_11009, TransactionCause.IDENTIFY_FUTURE_INTEREST)
                .orElseThrow();
        factory = new DisbursedInterestArticleSpecFactory(mockMetadataConfig);

        lenient().when(mockComponent.amount()).thenReturn(mockAmount);
        lenient().when(mockComponent.topic()).thenReturn(mockTopic);
    }

    @Nested
    @DisplayName("Constructor Tests")
    final class ConstructorTests {

        @Test
        @DisplayName("should create factory with metadata config")
        void shouldCreateFactoryWithMetadataConfig() {
            var newFactory = new DisbursedInterestArticleSpecFactory(mockMetadataConfig);

            assertThat(newFactory).isNotNull();
        }

        @Test
        @DisplayName("should throw exception when metadata config is null")
        void shouldThrowExceptionWhenMetadataConfigIsNull() {
            assertThatThrownBy(() -> new DisbursedInterestArticleSpecFactory(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("DebitCreditArticleSpecFactory Tests")
    final class DebitCreditArticleSpecFactoryTests {

        @Test
        @DisplayName("should return correct debit article type")
        void shouldReturnCorrectDebitArticleType() {
            var result = factory.getDebitArticleType();

            assertThat(result).isEqualTo(DisbursedInterestArticleType.INTEREST_DEBIT_LEG);
        }

        @Test
        @DisplayName("should return correct credit article type")
        void shouldReturnCorrectCreditArticleType() {
            var result = factory.getCreditArticleType();

            assertThat(result).isEqualTo(DisbursedInterestArticleType.INTEREST_CREDIT_LEG);
        }

        @Test
        @DisplayName("should create debit spec successfully")
        void shouldCreateDebitSpecSuccessfully() {
            List<MetadataSection> expectedSections = ImmutableList.of(MetadataSection.TRANSACTION_INFO);
            given(mockMetadataConfig.getMetadataSections(DisbursedInterestArticleType.INTEREST_DEBIT_LEG))
                    .willReturn(expectedSections);

            Result<ArticleSpec<DisbursedInterestArticleType>> result = factory.createDebitSpec(mockComponent);

            assertThat(result.isSuccess()).isTrue();
            ArticleSpec<DisbursedInterestArticleType> spec = result.orElseThrow();
            assertThat(spec.articleType()).isEqualTo(DisbursedInterestArticleType.INTEREST_DEBIT_LEG);
            assertThat(spec.amount()).isEqualTo(mockAmount);
            assertThat(spec.topic()).isEqualTo(mockTopic);
            assertThat(spec.transactionInfo()).isEqualTo(expectedTransactionInfo);
            then(mockMetadataConfig).should().getMetadataSections(DisbursedInterestArticleType.INTEREST_DEBIT_LEG);
        }

        @Test
        @DisplayName("should create credit spec successfully")
        void shouldCreateCreditSpecSuccessfully() {
            List<MetadataSection> expectedSections = ImmutableList.of(MetadataSection.TRANSACTION_INFO);
            given(mockMetadataConfig.getMetadataSections(DisbursedInterestArticleType.INTEREST_CREDIT_LEG))
                    .willReturn(expectedSections);

            Result<ArticleSpec<DisbursedInterestArticleType>> result = factory.createCreditSpec(mockComponent);

            assertThat(result.isSuccess()).isTrue();
            ArticleSpec<DisbursedInterestArticleType> spec = result.orElseThrow();
            assertThat(spec.articleType()).isEqualTo(DisbursedInterestArticleType.INTEREST_CREDIT_LEG);
            assertThat(spec.amount()).isEqualTo(mockAmount);
            assertThat(spec.topic()).isEqualTo(mockTopic);
            assertThat(spec.transactionInfo()).isEqualTo(expectedTransactionInfo);
            then(mockMetadataConfig).should().getMetadataSections(DisbursedInterestArticleType.INTEREST_CREDIT_LEG);
        }
    }

    @Nested
    @DisplayName("Transaction Info Tests")
    final class TransactionInfoTests {

        @Test
        @DisplayName("should have correct transaction type")
        void shouldHaveCorrectTransactionType() {
            List<MetadataSection> expectedSections = ImmutableList.of(MetadataSection.TRANSACTION_INFO);
            given(mockMetadataConfig.getMetadataSections(DisbursedInterestArticleType.INTEREST_DEBIT_LEG))
                    .willReturn(expectedSections);

            Result<ArticleSpec<DisbursedInterestArticleType>> result = factory.createDebitSpec(mockComponent);

            assertThat(result.isSuccess()).isTrue();
            ArticleSpec<DisbursedInterestArticleType> spec = result.orElseThrow();
            assertThat(spec.transactionInfo().transactionType()).isEqualTo(TransactionType.CODE_11009);
        }

        @Test
        @DisplayName("should have correct transaction cause")
        void shouldHaveCorrectTransactionCause() {
            List<MetadataSection> expectedSections = ImmutableList.of(MetadataSection.TRANSACTION_INFO);
            given(mockMetadataConfig.getMetadataSections(DisbursedInterestArticleType.INTEREST_CREDIT_LEG))
                    .willReturn(expectedSections);

            Result<ArticleSpec<DisbursedInterestArticleType>> result = factory.createCreditSpec(mockComponent);

            assertThat(result.isSuccess()).isTrue();
            ArticleSpec<DisbursedInterestArticleType> spec = result.orElseThrow();
            assertThat(spec.transactionInfo().transactionCause()).isEqualTo(TransactionCause.IDENTIFY_FUTURE_INTEREST);
        }
    }
}
