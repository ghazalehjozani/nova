package ir.dotin.loan.trade.i18n;

import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies the trade-loan i18n bundle resolves through the exact {@code MessageSource} configuration pangaea wires
 * ({@code classpath:i18n/messages}, UTF-8). Guards the response-message regression where unresolved keys leaked as raw
 * {@code error.loan.NNNNN}: a present key must resolve to localized text (fa) with its argument interpolated, an en
 * request must fall back to the English base bundle, and a genuine miss must return {@code null} (so the protocol layer
 * falls through to the hardcoded default rather than emitting the key).
 */
class MessageBundleResolutionTest {

    private static final Locale FA = Locale.forLanguageTag("fa");

    private final ReloadableResourceBundleMessageSource messageSource = newMessageSource();

    private static ReloadableResourceBundleMessageSource newMessageSource() {
        ReloadableResourceBundleMessageSource ms = new ReloadableResourceBundleMessageSource();
        ms.setBasename("classpath:i18n/messages");
        ms.setDefaultEncoding("UTF-8");
        return ms;
    }

    @Test
    void persianMessageResolvesAndInterpolatesArg() {
        String message = messageSource.getMessage("error.loan.404", new Object[] {"amount"}, FA);

        assertThat(message).isNotBlank().isNotEqualTo("error.loan.404").contains("amount");
        // contains at least one Persian-script code point => it came from messages_fa, not the base bundle
        assertThat(message.codePoints().anyMatch(cp -> cp >= 0x0600 && cp <= 0x06FF))
                .isTrue();
    }

    @Test
    void englishRequestFallsBackToBaseBundle() {
        // messages_en was folded into the no-suffix base bundle; an en request must resolve from it.
        String message = messageSource.getMessage("error.loan.404", new Object[] {"amount"}, Locale.ENGLISH);

        assertThat(message).isEqualTo("Validation failed: amount");
    }

    @Test
    void unknownKeyReturnsNullSoProtocolLayerCanFallBack() {
        String message = messageSource.getMessage("error.loan.99999", null, null, FA);

        assertThat(message).isNull();
    }
}
