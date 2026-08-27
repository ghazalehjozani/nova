package ir.dotin.loan.trade.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;

import ir.dotin.platform.pangaea.archunit.protocol.LocalizedEnumRules;
import ir.dotin.platform.pangaea.commons.core.i18n.LocalizedEnum;
import ir.dotin.platform.pangaea.commons.core.i18n.LocalizedMessage;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Service-wide gate on localized-enum catalog names (ADR-0037).
 *
 * <p>A catalog name is a published contract value: it names the enum in {@code /v1/reference/enums} and is the OpenAPI
 * component every {@code $ref} resolves to. The compiler forces each enum to declare one but cannot check the literal,
 * and uniqueness is only meaningful where the whole classpath is assembled — Nova's localized enums arrive from four
 * independent repositories (pangaea, base-loan, accounting-document, and Nova itself), so two of them claiming one name
 * is a mistake no single library's build can see. Catching it here fails the build instead of the service's startup.
 */
class LocalizedEnumCatalogNameArchitectureTest {

    /** pangaea 8 + base-loan 22 + accounting-document 5 + Nova 3, minus room for a deliberate removal. */
    private static final int MINIMUM_LOCALIZED_ENUMS = 30;

    private static final JavaClasses PLATFORM_CLASSES = new ClassFileImporter().importPackages("ir.dotin");

    @Test
    void everyLocalizedEnumDeclaresAUniqueKebabCatalogName() {
        LocalizedEnumRules.CATALOG_NAMES_ARE_STABLE_AND_UNIQUE.check(PLATFORM_CLASSES);
    }

    @Test
    void platformClassesAreOnTheTestClasspath() {
        // Vacuous-pass guard: an empty import would let the rule above pass while checking nothing.
        assertFalse(
                PLATFORM_CLASSES.isEmpty(), "ArchUnit imported zero ir.dotin classes — the rule would pass vacuously");
    }

    @Test
    void everyContributingLibraryIsOnTheTestClasspath() {
        // The rule above is only a uniqueness check if the enums of all four repositories are actually visible. A
        // classpath that carries Nova's enums but not base-loan's would pass it while checking a third of the surface.
        long localizedEnums = PLATFORM_CLASSES.stream()
                .filter(clazz -> clazz.isEnum()
                        && clazz.isAssignableTo(LocalizedEnum.class)
                        && !clazz.isAssignableTo(LocalizedMessage.class))
                .count();

        assertTrue(
                localizedEnums >= MINIMUM_LOCALIZED_ENUMS,
                "expected at least " + MINIMUM_LOCALIZED_ENUMS + " localized enums on the classpath but saw "
                        + localizedEnums + " — a library is missing and the uniqueness check is only partial");
    }
}
