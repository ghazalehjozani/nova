package ir.dotin.loan.trade.core.domain.loanfacility.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ExistResult Value Object")
final class ExistResultTest {

    @Nested
    @DisplayName("Constructor Tests")
    final class ConstructorTests {

        @DisplayName("should create with true value")
        @Test
        void shouldCreateWithTrueValue() {
            // when
            ExistResult result = new ExistResult(true);

            // then
            assertThat(result.exists()).isTrue();
        }

        @DisplayName("should create with false value")
        @Test
        void shouldCreateWithFalseValue() {
            // when
            ExistResult result = new ExistResult(false);

            // then
            assertThat(result.exists()).isFalse();
        }
    }

    @Nested
    @DisplayName("Equality and Identity Tests")
    final class EqualityAndIdentityTests {

        @DisplayName("should be equal when boolean values are equal")
        @Test
        void shouldBeEqualWhenBooleanValuesAreEqual() {
            // given
            ExistResult result1 = new ExistResult(true);
            ExistResult result2 = new ExistResult(true);

            // when & then
            assertThat(result1).isEqualTo(result2);
            assertThat(result1.hashCode()).isEqualTo(result2.hashCode());
        }

        @DisplayName("should not be equal when boolean values differ")
        @Test
        void shouldNotBeEqualWhenBooleanValuesDiffer() {
            // given
            ExistResult result1 = new ExistResult(true);
            ExistResult result2 = new ExistResult(false);

            // when & then
            assertThat(result1).isNotEqualTo(result2);
        }

        @DisplayName("should be equal for false values")
        @Test
        void shouldBeEqualForFalseValues() {
            // given
            ExistResult result1 = new ExistResult(false);
            ExistResult result2 = new ExistResult(false);

            // when & then
            assertThat(result1).isEqualTo(result2);
            assertThat(result1.hashCode()).isEqualTo(result2.hashCode());
        }

        @DisplayName("should not be equal to null")
        @Test
        void shouldNotBeEqualToNull() {
            // given
            ExistResult result = new ExistResult(true);

            // when & then
            assertThat(result).isNotEqualTo(null);
        }

        @DisplayName("should not be equal to different class")
        @Test
        void shouldNotBeEqualToDifferentClass() {
            // given
            ExistResult result = new ExistResult(true);
            Boolean booleanValue = Boolean.TRUE;

            // when & then
            assertThat(result).isNotEqualTo(booleanValue);
        }

        @DisplayName("should be symmetric")
        @Test
        void shouldBeSymmetric() {
            // given
            ExistResult result1 = new ExistResult(false);
            ExistResult result2 = new ExistResult(false);

            // when & then
            assertThat(result1).isEqualTo(result2);
            assertThat(result2).isEqualTo(result1);
        }

        @DisplayName("should be transitive")
        @Test
        void shouldBeTransitive() {
            // given
            ExistResult result1 = new ExistResult(true);
            ExistResult result2 = new ExistResult(true);
            ExistResult result3 = new ExistResult(true);

            // when & then
            assertThat(result1).isEqualTo(result2);
            assertThat(result2).isEqualTo(result3);
            assertThat(result1).isEqualTo(result3);
        }
    }

    @Nested
    @DisplayName("Value Semantics Tests")
    final class ValueSemanticsTests {

        @DisplayName("should have immutable behavior")
        @Test
        void shouldHaveImmutableBehavior() {
            // given
            ExistResult result = new ExistResult(true);

            // when
            boolean value1 = result.exists();
            boolean value2 = result.exists();

            // then
            assertThat(value1).isEqualTo(value2);
            assertThat(value1).isTrue();
        }

        @DisplayName("should preserve the exact boolean value")
        @Test
        void shouldPreserveTheExactBooleanValue() {
            // given
            boolean originalValue = true;
            ExistResult result = new ExistResult(originalValue);

            // when
            boolean retrievedValue = result.exists();

            // then
            assertThat(retrievedValue).isEqualTo(originalValue);
        }

        @DisplayName("should work correctly with boolean literals")
        @Test
        void shouldWorkCorrectlyWithBooleanLiterals() {
            // when
            ExistResult trueResult = new ExistResult(true);
            ExistResult falseResult = new ExistResult(false);

            // then
            assertThat(trueResult.exists()).isTrue();
            assertThat(falseResult.exists()).isFalse();
        }

        @DisplayName("should work correctly with Boolean objects")
        @Test
        void shouldWorkCorrectlyWithBooleanObjects() {
            // given
            Boolean trueObject = Boolean.TRUE;
            Boolean falseObject = Boolean.FALSE;

            // when
            ExistResult trueResult = new ExistResult(trueObject);
            ExistResult falseResult = new ExistResult(falseObject);

            // then
            assertThat(trueResult.exists()).isTrue();
            assertThat(falseResult.exists()).isFalse();
        }
    }

    @Nested
    @DisplayName("toString Tests")
    final class ToStringTests {

        @DisplayName("should generate meaningful toString representation for true")
        @Test
        void shouldGenerateMeaningfulToStringRepresentationForTrue() {
            // given
            ExistResult result = new ExistResult(true);

            // when
            String toStringResult = result.toString();

            // then
            assertThat(toStringResult).contains("ExistResult");
            assertThat(toStringResult).contains("true");
        }

        @DisplayName("should generate meaningful toString representation for false")
        @Test
        void shouldGenerateMeaningfulToStringRepresentationForFalse() {
            // given
            ExistResult result = new ExistResult(false);

            // when
            String toStringResult = result.toString();

            // then
            assertThat(toStringResult).contains("ExistResult");
            assertThat(toStringResult).contains("false");
        }

        @DisplayName("should generate different toString for different values")
        @Test
        void shouldGenerateDifferentToStringForDifferentValues() {
            // given
            ExistResult trueResult = new ExistResult(true);
            ExistResult falseResult = new ExistResult(false);

            // when
            String trueToString = trueResult.toString();
            String falseToString = falseResult.toString();

            // then
            assertThat(trueToString).isNotEqualTo(falseToString);
        }
    }

    @Nested
    @DisplayName("Business Logic Tests")
    final class BusinessLogicTests {

        @DisplayName("should represent existence correctly")
        @Test
        void shouldRepresentExistenceCorrectly() {
            // given
            ExistResult exists = new ExistResult(true);
            ExistResult doesNotExist = new ExistResult(false);

            // when & then
            assertThat(exists.exists()).isTrue();
            assertThat(doesNotExist.exists()).isFalse();
        }

        @DisplayName("should be suitable for conditional logic")
        @Test
        void shouldBeSuitableForConditionalLogic() {
            // given
            ExistResult result = new ExistResult(true);

            // when
            boolean conditionalResult = result.exists() ? true : false;

            // then
            assertThat(conditionalResult).isTrue();
        }

        @DisplayName("should work in boolean expressions")
        @Test
        void shouldWorkInBooleanExpressions() {
            // given
            ExistResult exists = new ExistResult(true);
            ExistResult doesNotExist = new ExistResult(false);

            // when & then
            assertThat(exists.exists() && true).isTrue();
            assertThat(exists.exists() || false).isTrue();
            assertThat(doesNotExist.exists() && true).isFalse();
            assertThat(doesNotExist.exists() || false).isFalse();
        }

        @DisplayName("should support negation")
        @Test
        void shouldSupportNegation() {
            // given
            ExistResult exists = new ExistResult(true);
            ExistResult doesNotExist = new ExistResult(false);

            // when & then
            assertThat(exists.exists()).isTrue();
            assertThat(doesNotExist.exists()).isFalse();
        }

        @DisplayName("should be usable as method return type")
        @Test
        void shouldBeUsableAsMethodReturnType() {
            // when
            ExistResult result = createExistResult(true);

            // then
            assertThat(result.exists()).isTrue();
        }

        private ExistResult createExistResult(boolean exists) {
            return new ExistResult(exists);
        }
    }

    @Nested
    @DisplayName("Hash Code Tests")
    final class HashCodeTests {

        @DisplayName("should return consistent hash codes")
        @Test
        void shouldReturnConsistentHashCodes() {
            // given
            ExistResult result = new ExistResult(true);

            // when
            int hashCode1 = result.hashCode();
            int hashCode2 = result.hashCode();

            // then
            assertThat(hashCode1).isEqualTo(hashCode2);
        }

        @DisplayName("should return different hash codes for different values")
        @Test
        void shouldReturnDifferentHashCodesForDifferentValues() {
            // given
            ExistResult trueResult = new ExistResult(true);
            ExistResult falseResult = new ExistResult(false);

            // when
            int trueHashCode = trueResult.hashCode();
            int falseHashCode = falseResult.hashCode();

            // then
            assertThat(trueHashCode).isNotEqualTo(falseHashCode);
        }

        @DisplayName("should return same hash code for equal objects")
        @Test
        void shouldReturnSameHashCodeForEqualObjects() {
            // given
            ExistResult result1 = new ExistResult(true);
            ExistResult result2 = new ExistResult(true);

            // when & then
            assertThat(result1).isEqualTo(result2);
            assertThat(result1.hashCode()).isEqualTo(result2.hashCode());
        }
    }
}
