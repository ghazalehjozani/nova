package ir.dotin.loan.trade.core.domain;

import java.util.Arrays;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.platform.commons.core.i18n.LocalizedMessage;

import static org.assertj.core.api.Assertions.assertThat;

/** Utility class providing common assertion methods for testing {@link Result} and {@link Notification}. */
public final class ResultAssert {

    private ResultAssert() {}

    /**
     * Asserts that the given Result represents a successful outcome. Checks for non-null number, success status, and
     * absence of notification errors.
     */
    public static <T> void assertSuccess(Result<T> result) {
        assertThat(result).isNotNull();
        assertThat(result.isFailure())
                .withFailMessage(
                        "Expected Result to have no errors on success, but it was a failure: %s",
                        result.err()
                                .map(fc -> fc.notification().getErrorMessages())
                                .orElse(null))
                .isFalse();
    }

    /** Asserts that the given Result represents a failure outcome and contains a specific error in its Notification. */
    public static void assertFailure(Result<?> result, LocalizedMessage<?> expectedErrorCode, Object... expectedArgs) {
        assertThat(result).isNotNull();
        assertThat(result.isFailure())
                .withFailMessage("Expected Result to be failure but was success.")
                .isTrue();
        Notification notification = result.err().orElseThrow().notification();
        assertThat(notification.hasErrors())
                .withFailMessage("Expected Result notification to have errors on failure.")
                .isTrue();
        assertThatNotificationContainsError(notification, expectedErrorCode, expectedArgs);
    }

    /**
     * Asserts that the given Result represents a failure outcome and checks only that the notification has errors,
     * without verifying specific codes. Useful when the exact error code isn't critical or multiple are possible.
     */
    public static void assertFailureHasErrors(Result<?> result) {
        assertThat(result).isNotNull();
        assertThat(result.isFailure())
                .withFailMessage("Expected Result to be failure but was success.")
                .isTrue();
        assertThat(result.err().orElseThrow().notification().hasErrors())
                .withFailMessage("Expected Result notification to have errors on failure.")
                .isTrue();
    }

    /**
     * Asserts that the given Notification contains a specific error, identified by its LocalizedMessage code and
     * arguments.
     */
    public static void assertThatNotificationContainsError(
            Notification notification, LocalizedMessage<?> expectedErrorCode, Object... expectedArgs) {
        assertThat(notification).isNotNull();
        assertThat(notification.errors())
                .withFailMessage(
                        "Notification expected to contain error code [%s] with args %s, but errors were: %s",
                        expectedErrorCode, Arrays.toString(expectedArgs), notification.errors())
                .anySatisfy(error -> {
                    assertThat(error.messageKey())
                            .withFailMessage("Error message key mismatch")
                            .isEqualTo(expectedErrorCode);
                    boolean isEquals = Arrays.equals(expectedArgs, error.getArgsAsArray());
                    assertThat(isEquals)
                            .withFailMessage("Args mismatch for error code %s".formatted(expectedErrorCode))
                            .isTrue();
                });
    }

    /**
     * Asserts that the given Notification does NOT contain a specific error, identified by its LocalizedMessage code.
     */
    public static void assertThatNotificationDoesNotContainError(
            Notification notification, LocalizedMessage<?> unexpectedErrorCode) {
        assertThat(notification).isNotNull();
        assertThat(notification.errors())
                .withFailMessage(
                        "Notification expected NOT to contain error code [%s], but errors were: %s",
                        unexpectedErrorCode, notification.errors())
                .noneSatisfy(error -> assertThat(error.messageKey())
                        .withFailMessage("Unexpected error message key found")
                        .isEqualTo(unexpectedErrorCode));
    }
}
