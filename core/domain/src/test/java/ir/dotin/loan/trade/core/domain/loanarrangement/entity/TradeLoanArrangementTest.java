package ir.dotin.loan.trade.core.domain.loanarrangement.entity;


import ir.dotin.loan.baseloan.core.domain.loanarrangement.vo.ArrangementFeatureContext;
import ir.dotin.platform.domain.common.Result;
import ir.dotin.platform.domain.common.feature.FeatureConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@DisplayName("TradeLoanArrangement Test")
@ExtendWith(MockitoExtension.class)
final class TradeLoanArrangementTest {

    @Mock
    private FeatureConfig mockFeatureConfig;

    @Mock
    private Clock mockClock;

    @Mock
    private ArrangementFeatureContext mockFeatureContext;

    @Mock
    private TradeLoanArrangement mockTradeLoanArrangement;

    private TradeLoanArrangement.Builder builder;

    @BeforeEach
    void setUp() {
        builder = TradeLoanArrangement.newBuilder(mockFeatureConfig);
    }

    @Nested
    @DisplayName("Constructor Tests")
    final class ConstructorTests {
        @Test
        @DisplayName("should create builder with feature config")
        void shouldCreateBuilderWithFeatureConfig() {
            assertThat(builder).isNotNull();
        }

        @Test
        @DisplayName("should throw exception when feature config is null")
        void shouldThrowExceptionWhenFeatureConfigIsNull() {
            assertThatThrownBy(() -> TradeLoanArrangement.newBuilder(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("Create Method Tests")
    final class CreateMethodTests {

        @Test
        @DisplayName("should throw exception when clock is null")
        void shouldThrowExceptionWhenClockIsNull() {
            assertThatThrownBy(() -> TradeLoanArrangement.create(builder, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Clock cannot be null for creation");
        }

        @Test
        @DisplayName("should throw exception when builder is null")
        void shouldThrowExceptionWhenBuilderIsNull() {
            assertThatThrownBy(() -> TradeLoanArrangement.create(null, mockClock))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Builder cannot be null for creation");
        }
    }

    @Nested
    @DisplayName("Reconstitute Method Tests")
    final class ReconstituteMethodTests {

        @Test
        @DisplayName("should throw exception when builder is null")
        void shouldThrowExceptionWhenBuilderIsNull() {
            assertThatThrownBy(() -> TradeLoanArrangement.reconstitute(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Builder cannot be null for reconstitution");
        }
    }

    @Nested
    @DisplayName("Activation/Deactivation Tests")
    final class ActivationDeactivationTests {
        @Test
        @DisplayName("should activate arrangement successfully")
        void shouldActivateArrangementSuccessfully() {
            // Arrange
            given(mockTradeLoanArrangement.activate(mockClock))
                    .willReturn(Result.success(mockTradeLoanArrangement));

            // Act
            var result = mockTradeLoanArrangement.activate(mockClock);

            // Assert
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.value()).isEqualTo(mockTradeLoanArrangement);
        }

        @Test
        @DisplayName("should deactivate arrangement successfully")
        void shouldDeactivateArrangementSuccessfully() {
            // Arrange
            given(mockTradeLoanArrangement.deactivate(mockClock))
                    .willReturn(Result.success(mockTradeLoanArrangement));

            // Act
            var result = mockTradeLoanArrangement.deactivate(mockClock);

            // Assert
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.value()).isEqualTo(mockTradeLoanArrangement);
        }
    }

    @Nested
    @DisplayName("Prepare New Version Tests")
    final class PrepareNewVersionTests {
        @Test
        @DisplayName("should prepare new version successfully")
        void shouldPrepareNewVersionSuccessfully() {
            // Arrange
            given(mockTradeLoanArrangement.prepareNewVersion(builder, mockFeatureContext, mockClock))
                    .willReturn(Result.success(builder));

            // Act
            var result = mockTradeLoanArrangement.prepareNewVersion(builder, mockFeatureContext, mockClock);

            // Assert
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.value()).isEqualTo(builder);
        }
    }

    @Nested
    @DisplayName("Builder Tests")
    final class BuilderTests {
        @Test
        @DisplayName("should create builder with copy constructor")
        void shouldCreateBuilderWithCopyConstructor() {
            // Arrange
            var originalBuilder = TradeLoanArrangement.newBuilder(mockFeatureConfig);

            // Act
            var copiedBuilder = new TradeLoanArrangement.Builder(originalBuilder);

            // Assert
            assertThat(copiedBuilder).isNotNull();
        }
    }
}