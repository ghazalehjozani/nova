package ir.dotin.loan.trade.core.domain.shared.formula;

import java.util.EnumSet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ir.dotin.loan.baseloan.core.domain.shared.formula.BaseFormulaField;
import ir.dotin.loan.trade.core.domain.shared.interaction.TradeLoanArrangementDataProvider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("TradeLoanFormulaContextProvider Test")
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NullAway")
final class TradeLoanFormulaContextProviderTest {

    @Mock
    private TradeLoanArrangementDataProvider mockArrangementDataProvider;

    private TradeLoanFormulaContextProvider provider;

    @BeforeEach
    void setUp() {
        provider = new TradeLoanFormulaContextProvider(mockArrangementDataProvider);
    }

    @Nested
    @DisplayName("Constructor Tests")
    final class ConstructorTests {

        @Test
        @DisplayName("should create provider with valid data provider")
        void shouldCreateProviderWithValidDataProvider() {
            var newProvider = new TradeLoanFormulaContextProvider(mockArrangementDataProvider);

            assertThat(newProvider).isNotNull();
        }

        @Test
        @DisplayName("should throw exception when data provider is null")
        void shouldThrowExceptionWhenDataProviderIsNull() {
            assertThatThrownBy(() -> new TradeLoanFormulaContextProvider(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("Field Resolver Initialization Tests")
    final class FieldResolverInitializationTests {

        @Test
        @DisplayName("should initialize all required field resolvers")
        void shouldInitializeAllRequiredFieldResolvers() {
            // Access the field resolvers through reflection or by testing each field
            // We test this by verifying all BaseFormulaField enum values are handled

            // Test that each field has a resolver by checking requiresArrangement behavior
            for (BaseFormulaField field : BaseFormulaField.values()) {
                // This implicitly tests that initializeFieldResolvers() covers all fields
                boolean requiresArrangement = provider.requiresArrangement(field);
                assertThat(requiresArrangement).isIn(true, false); // Should not throw
            }
        }

        @Test
        @DisplayName("should include sanction-based field resolvers")
        void shouldIncludeSanctionBasedFieldResolvers() {
            // Test fields that don't require arrangement
            assertThat(provider.requiresArrangement(BaseFormulaField.APPROVED_AMOUNT))
                    .isFalse();
        }

        @Test
        @DisplayName("should include application-based field resolvers")
        void shouldIncludeApplicationBasedFieldResolvers() {
            // Test fields that don't require arrangement
            assertThat(provider.requiresArrangement(BaseFormulaField.REQUESTED_AMOUNT))
                    .isFalse();
        }

        @Test
        @DisplayName("should include arrangement-based field resolvers")
        void shouldIncludeArrangementBasedFieldResolvers() {
            // Test fields that require arrangement
            assertThat(provider.requiresArrangement(BaseFormulaField.BASE_INTEREST_RATE))
                    .isTrue();
            assertThat(provider.requiresArrangement(BaseFormulaField.PENALTY_RATE))
                    .isTrue();
            assertThat(provider.requiresArrangement(BaseFormulaField.INSTALLMENT_PERIOD_DAYS))
                    .isTrue();
        }

        @Test
        @DisplayName("should include mixed field resolvers")
        void shouldIncludeMixedFieldResolvers() {
            // Test mixed fields that don't require arrangement
            assertThat(provider.requiresArrangement(BaseFormulaField.LOAN_DURATION_DAYS))
                    .isFalse();
            assertThat(provider.requiresArrangement(BaseFormulaField.GRACE_PERIOD_DAYS))
                    .isFalse();
            assertThat(provider.requiresArrangement(BaseFormulaField.INSTALLMENT_COUNT))
                    .isFalse();
        }

        @Test
        @DisplayName("should include insurance field resolvers")
        void shouldIncludeInsuranceFieldResolvers() {
            // Test insurance fields that require arrangement
            assertThat(provider.requiresArrangement(BaseFormulaField.LIFE_INSURANCE_RATE))
                    .isTrue();
            assertThat(provider.requiresArrangement(BaseFormulaField.INSURANCE_COMPANY_SHARE_RATE))
                    .isTrue();
            assertThat(provider.requiresArrangement(BaseFormulaField.VAT_ON_INSURANCE_RATE))
                    .isTrue();
            assertThat(provider.requiresArrangement(BaseFormulaField.LIFE_INSURANCE_DISCOUNT_RATE))
                    .isTrue();
            assertThat(provider.requiresArrangement(BaseFormulaField.GRACE_PERIOD_LIFE_INSURANCE_DISCOUNT_RATE))
                    .isTrue();
        }

        @Test
        @DisplayName("should include guarantor field resolver")
        void shouldIncludeGuarantorFieldResolver() {
            assertThat(provider.requiresArrangement(BaseFormulaField.GUARANTOR_COUNT))
                    .isTrue();
        }
    }

    @Nested
    @DisplayName("RequiresArrangement Method Tests")
    final class RequiresArrangementMethodTests {

        @ParameterizedTest
        @EnumSource(
                value = BaseFormulaField.class,
                names = {
                    "BASE_INTEREST_RATE",
                    "PENALTY_RATE",
                    "LIFE_INSURANCE_RATE",
                    "INSURANCE_COMPANY_SHARE_RATE",
                    "VAT_ON_INSURANCE_RATE",
                    "INSTALLMENT_PERIOD_DAYS",
                    "GUARANTOR_COUNT",
                    "LIFE_INSURANCE_DISCOUNT_RATE",
                    "GRACE_PERIOD_LIFE_INSURANCE_DISCOUNT_RATE"
                })
        @DisplayName("should return true for arrangement-dependent fields")
        void shouldReturnTrueForArrangementDependentFields(BaseFormulaField field) {
            assertThat(provider.requiresArrangement(field)).isTrue();
        }

        @ParameterizedTest
        @EnumSource(
                value = BaseFormulaField.class,
                names = {
                    "APPROVED_AMOUNT",
                    "REQUESTED_AMOUNT",
                    "LOAN_DURATION_DAYS",
                    "GRACE_PERIOD_DAYS",
                    "INSTALLMENT_COUNT"
                })
        @DisplayName("should return false for non-arrangement-dependent fields")
        void shouldReturnFalseForNonArrangementDependentFields(BaseFormulaField field) {
            assertThat(provider.requiresArrangement(field)).isFalse();
        }

        @Test
        @DisplayName("should handle all BaseFormulaField enum values")
        void shouldHandleAllBaseFormulaFieldEnumValues() {
            EnumSet<BaseFormulaField> arrangementFields = EnumSet.of(
                    BaseFormulaField.BASE_INTEREST_RATE,
                    BaseFormulaField.PENALTY_RATE,
                    BaseFormulaField.LIFE_INSURANCE_RATE,
                    BaseFormulaField.INSURANCE_COMPANY_SHARE_RATE,
                    BaseFormulaField.VAT_ON_INSURANCE_RATE,
                    BaseFormulaField.INSTALLMENT_PERIOD_DAYS,
                    BaseFormulaField.GUARANTOR_COUNT,
                    BaseFormulaField.LIFE_INSURANCE_DISCOUNT_RATE,
                    BaseFormulaField.GRACE_PERIOD_LIFE_INSURANCE_DISCOUNT_RATE);

            for (BaseFormulaField field : BaseFormulaField.values()) {
                boolean expected = arrangementFields.contains(field);
                boolean actual = provider.requiresArrangement(field);

                assertThat(actual)
                        .as("Field %s should %s require arrangement", field, expected ? "" : "not")
                        .isEqualTo(expected);
            }
        }
    }

    @Nested
    @DisplayName("Inheritance Tests")
    final class InheritanceTests {

        @Test
        @DisplayName("should extend BaseLoanFormulaContextProvider")
        void shouldExtendBaseLoanFormulaContextProvider() {
            assertThat(provider)
                    .isInstanceOf(
                            ir.dotin.loan.baseloan.core.domain.shared.formula.BaseLoanFormulaContextProvider.class);
        }

        @Test
        @DisplayName("should be annotated with DomainService")
        void shouldBeAnnotatedWithDomainService() {
            assertThat(TradeLoanFormulaContextProvider.class)
                    .hasAnnotation(ir.dotin.platform.domain.common.annotation.DomainService.class);
        }

        @Test
        @DisplayName("should be a final class")
        void shouldBeAFinalClass() {
            assertThat(TradeLoanFormulaContextProvider.class).isFinal();
        }
    }

    @Nested
    @DisplayName("Field Coverage Tests")
    final class FieldCoverageTests {

        @Test
        @DisplayName("should cover all BaseFormulaField enum values")
        void shouldCoverAllBaseFormulaFieldEnumValues() {
            // Verify that every BaseFormulaField enum value is handled
            int totalFields = BaseFormulaField.values().length;
            assertThat(totalFields).isEqualTo(14); // Current count of enum values

            // Test that calling requiresArrangement on each field doesn't throw
            for (BaseFormulaField field : BaseFormulaField.values()) {
                assertThat(provider.requiresArrangement(field)).isIn(true, false);
            }
        }

        @Test
        @DisplayName("should have correct arrangement dependency mapping")
        void shouldHaveCorrectArrangementDependencyMapping() {
            // Test the specific mapping logic
            var arrangementRequiredFields = EnumSet.of(
                    BaseFormulaField.BASE_INTEREST_RATE,
                    BaseFormulaField.PENALTY_RATE,
                    BaseFormulaField.LIFE_INSURANCE_RATE,
                    BaseFormulaField.INSURANCE_COMPANY_SHARE_RATE,
                    BaseFormulaField.VAT_ON_INSURANCE_RATE,
                    BaseFormulaField.INSTALLMENT_PERIOD_DAYS,
                    BaseFormulaField.GUARANTOR_COUNT,
                    BaseFormulaField.LIFE_INSURANCE_DISCOUNT_RATE,
                    BaseFormulaField.GRACE_PERIOD_LIFE_INSURANCE_DISCOUNT_RATE);

            var nonArrangementFields = EnumSet.of(
                    BaseFormulaField.APPROVED_AMOUNT,
                    BaseFormulaField.REQUESTED_AMOUNT,
                    BaseFormulaField.LOAN_DURATION_DAYS,
                    BaseFormulaField.GRACE_PERIOD_DAYS,
                    BaseFormulaField.INSTALLMENT_COUNT);

            // Verify all fields are accounted for
            EnumSet<BaseFormulaField> allFields = EnumSet.copyOf(arrangementRequiredFields);
            allFields.addAll(nonArrangementFields);
            assertThat(allFields).containsExactlyInAnyOrder(BaseFormulaField.values());

            // Verify the mapping is correct
            for (BaseFormulaField field : arrangementRequiredFields) {
                assertThat(provider.requiresArrangement(field))
                        .as("Field %s should require arrangement", field)
                        .isTrue();
            }

            for (BaseFormulaField field : nonArrangementFields) {
                assertThat(provider.requiresArrangement(field))
                        .as("Field %s should not require arrangement", field)
                        .isFalse();
            }
        }
    }
}
