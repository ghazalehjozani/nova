package ir.dotin.loan.trade.core.domain.loanfacility.vo;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanApplicationId;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static java.util.UUID.randomUUID;
import static java.util.stream.IntStream.range;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("LoanApplicationId Identity Value Object")
@SuppressWarnings("NullAway")
final class LoanApplicationIdTest {

    @Nested
    @DisplayName("Factory Method Tests")
    final class FactoryMethodTests {

        @DisplayName("should generate unique ID with random UUID")
        @Test
        void shouldGenerateUniqueIdWithRandomUuid() {
            // when
            LoanApplicationId id = LoanApplicationId.of(randomUUID());

            // then
            assertThat(id).isNotNull();
            assertThat(id.value()).isNotNull();
            assertThat(id.value()).isInstanceOf(UUID.class);
        }

        @DisplayName("should generate different IDs on multiple calls")
        @Test
        void shouldGenerateDifferentIdsOnMultipleCalls() {
            // when
            LoanApplicationId id1 = LoanApplicationId.of(randomUUID());
            LoanApplicationId id2 = LoanApplicationId.of(randomUUID());
            LoanApplicationId id3 = LoanApplicationId.of(randomUUID());

            // then
            assertThat(id1).isNotEqualTo(id2).isNotEqualTo(id3);
            assertThat(id2).isNotEqualTo(id3);

            assertThat(id1.value()).isNotEqualTo(id2.value());
            assertThat(id1.value()).isNotEqualTo(id3.value());
            assertThat(id2.value()).isNotEqualTo(id3.value());
        }

        @DisplayName("should generate valid UUID format")
        @Test
        void shouldGenerateValidUuidFormat() {
            // when
            LoanApplicationId id = LoanApplicationId.of(randomUUID());

            // then
            UUID uuid = id.value();
            String uuidString = uuid.toString();

            // Verify UUID format (e.g., 123e4567-e89b-12d3-a456-426614174000)
            assertThat(uuidString)
                    .matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");
        }

        @DisplayName("should generate consistent UUID each time for same instance")
        @Test
        void shouldGenerateConsistentUuidEachTimeForSameInstance() {
            // given
            LoanApplicationId id = LoanApplicationId.of(randomUUID());

            // when
            UUID value1 = id.value();
            UUID value2 = id.value();

            // then
            assertThat(value1).isSameAs(value2);
        }
    }

    @Nested
    @DisplayName("Constructor Tests")
    final class ConstructorTests {

        @DisplayName("should create successfully with valid UUID")
        @Test
        void shouldCreateSuccessfullyWithValidUuid() {
            // given
            UUID validUuid = randomUUID();

            // when
            LoanApplicationId id = new LoanApplicationId(validUuid);

            // then
            assertThat(id.value()).isEqualTo(validUuid);
        }

        @DisplayName("should create successfully with specific UUID")
        @Test
        void shouldCreateSuccessfullyWithSpecificUuid() {
            // given
            UUID specificUuid = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

            // when
            LoanApplicationId id = new LoanApplicationId(specificUuid);

            // then
            assertThat(id.value()).isEqualTo(specificUuid);
        }
    }

    @Nested
    @DisplayName("Equality and Identity Tests")
    final class EqualityAndIdentityTests {

        @DisplayName("should be equal when UUIDs are equal")
        @Test
        void shouldBeEqualWhenUuidsAreEqual() {
            // given
            UUID uuid = randomUUID();
            LoanApplicationId id1 = new LoanApplicationId(uuid);
            LoanApplicationId id2 = new LoanApplicationId(uuid);

            // when & then
            assertThat(id1).isEqualTo(id2);
            assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
        }

        @DisplayName("should not be equal when UUIDs differ")
        @Test
        void shouldNotBeEqualWhenUuidsDiffer() {
            // given
            LoanApplicationId id1 = new LoanApplicationId(randomUUID());
            LoanApplicationId id2 = new LoanApplicationId(randomUUID());

            // when & then
            assertThat(id1).isNotEqualTo(id2);
        }

        @DisplayName("should be equal when created from same UUID string")
        @Test
        void shouldBeEqualWhenCreatedFromSameUuidString() {
            // given
            String uuidString = "123e4567-e89b-12d3-a456-426614174000";
            LoanApplicationId id1 = new LoanApplicationId(UUID.fromString(uuidString));
            LoanApplicationId id2 = new LoanApplicationId(UUID.fromString(uuidString));

            // when & then
            assertThat(id1).isEqualTo(id2);
            assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
        }

        @DisplayName("should not be equal to null")
        @Test
        void shouldNotBeEqualToNull() {
            // given
            LoanApplicationId id = LoanApplicationId.of(randomUUID());

            // when & then
            assertThat(id).isNotEqualTo(null);
        }

        @DisplayName("should not be equal to different class")
        @Test
        void shouldNotBeEqualToDifferentClass() {
            // given
            LoanApplicationId id = LoanApplicationId.of(randomUUID());
            String differentObject = "not an id";

            // when & then
            assertThat(id).isNotEqualTo(differentObject);
        }

        @DisplayName("should be symmetric")
        @Test
        void shouldBeSymmetric() {
            // given
            UUID uuid = randomUUID();
            LoanApplicationId id1 = new LoanApplicationId(uuid);
            LoanApplicationId id2 = new LoanApplicationId(uuid);

            // when & then
            assertThat(id1).isEqualTo(id2);
            assertThat(id2).isEqualTo(id1);
        }

        @DisplayName("should be transitive")
        @Test
        void shouldBeTransitive() {
            // given
            UUID uuid = randomUUID();
            LoanApplicationId id1 = new LoanApplicationId(uuid);
            LoanApplicationId id2 = new LoanApplicationId(uuid);
            LoanApplicationId id3 = new LoanApplicationId(uuid);

            // when & then
            assertThat(id1).isEqualTo(id2);
            assertThat(id2).isEqualTo(id3);
            assertThat(id1).isEqualTo(id3);
        }
    }

    @Nested
    @DisplayName("Identity Contract Tests")
    final class IdentityContractTests {

        @DisplayName("should implement Identity interface correctly")
        @Test
        void shouldImplementIdentityInterfaceCorrectly() {
            // given
            LoanApplicationId id = LoanApplicationId.of(randomUUID());

            // when & then
            assertThat(id).isInstanceOf(ir.dotin.platform.domain.common.entity.Identity.class);
            assertThat(id.value()).isInstanceOf(UUID.class);
        }

        @DisplayName("should have immutable behavior")
        @Test
        void shouldHaveImmutableBehavior() {
            // given
            LoanApplicationId id = LoanApplicationId.of(randomUUID());
            UUID originalValue = id.value();

            // when
            UUID valueAccess1 = id.value();
            UUID valueAccess2 = id.value();

            // then
            assertThat(valueAccess1).isSameAs(originalValue);
            assertThat(valueAccess2).isSameAs(originalValue);
        }

        @DisplayName("should maintain UUID uniqueness guarantee")
        @Test
        void shouldMaintainUuidUniquenessGuarantee() {
            // given
            int numberOfIds = 1000;

            // when
            var generatedIds = range(0, numberOfIds)
                    .mapToObj(i -> LoanApplicationId.of(randomUUID()))
                    .collect(toImmutableSet());

            // then
            assertThat(generatedIds).hasSize(numberOfIds);
        }
    }

    @Nested
    @DisplayName("toString Tests")
    final class ToStringTests {

        @DisplayName("should generate meaningful toString representation")
        @Test
        void shouldGenerateMeaningfulToStringRepresentation() {
            // given
            LoanApplicationId id = LoanApplicationId.of(randomUUID());

            // when
            String toStringResult = id.toString();

            // then
            assertThat(toStringResult)
                    .contains("LoanApplicationId")
                    .contains(id.value().toString());
        }

        @DisplayName("should generate different toString for different IDs")
        @Test
        void shouldGenerateDifferentToStringForDifferentIds() {
            // given
            LoanApplicationId id1 = LoanApplicationId.of(randomUUID());
            LoanApplicationId id2 = LoanApplicationId.of(randomUUID());

            // when
            String toString1 = id1.toString();
            String toString2 = id2.toString();

            // then
            assertThat(toString1).isNotEqualTo(toString2);
        }

        @DisplayName("should include UUID in toString output")
        @Test
        void shouldIncludeUuidInToStringOutput() {
            // given
            UUID specificUuid = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
            LoanApplicationId id = new LoanApplicationId(specificUuid);

            // when
            String toStringResult = id.toString();

            // then
            assertThat(toStringResult).contains("123e4567-e89b-12d3-a456-426614174000");
        }
    }

    @Nested
    @DisplayName("Serialization Tests")
    final class SerializationTests {

        @DisplayName("should preserve UUID value through serialization")
        @Test
        void shouldPreserveUuidValueThroughSerialization() {
            // given
            LoanApplicationId originalId = LoanApplicationId.of(randomUUID());
            UUID originalUuid = originalId.value();

            // when
            // Simulate serialization by converting to string and back
            String uuidString = originalUuid.toString();
            UUID reconstructedUuid = UUID.fromString(uuidString);
            LoanApplicationId reconstructedId = new LoanApplicationId(reconstructedUuid);

            // then
            assertThat(reconstructedId).isEqualTo(originalId);
            assertThat(reconstructedId.value()).isEqualTo(originalUuid);
        }

        @DisplayName("should handle UUID version types")
        @Test
        void shouldHandleUuidVersionTypes() {
            // given
            UUID version4Uuid = randomUUID(); // Version 4 (random)
            LoanApplicationId id = new LoanApplicationId(version4Uuid);

            // when & then
            assertThat(id.value()).isEqualTo(version4Uuid);
            assertThat(id.value().version()).isEqualTo(4);
        }
    }
}
