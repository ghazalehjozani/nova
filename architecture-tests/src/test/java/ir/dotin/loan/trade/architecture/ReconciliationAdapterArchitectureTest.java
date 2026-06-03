package ir.dotin.loan.trade.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Structural gate for the reconciliation driven adapter ({@code adapters/driven/reconciliation}). INV-4: recon
 * converges by re-driving STORED outbox/inbox rows through the admin ports — it must NEVER re-run a business command
 * (which would mint new ids and defeat the stored idempotency key), so it must not depend on the command dispatcher or
 * any application-service command handler.
 *
 * <p>Scoped to {@code ir.dotin.loan.trade.adapters.driven.reconciliation} only; the assertion below guards against a
 * vacuous pass if that module's bytecode is missing from the test classpath.
 */
class ReconciliationAdapterArchitectureTest {

    private static final String RECON_PACKAGE = "ir.dotin.loan.trade.adapters.driven.reconciliation";

    private static final JavaClasses RECON_CLASSES = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages(RECON_PACKAGE);

    @Test
    void reconAdapterPackageIsNotEmpty() {
        assertFalse(
                RECON_CLASSES.isEmpty(),
                "reconciliation driven-adapter bytecode must be on the architecture-tests classpath");
    }

    @Test
    void reconAdapterMustNotDependOnDispatcherOrCommandHandlers() {
        ArchRule rule = noClasses()
                .that()
                .resideInAPackage(RECON_PACKAGE + "..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage(
                        "ir.dotin.platform.pangaea.dispatcher..", "ir.dotin.loan.trade.core.application.service..")
                .because("INV-4: recon must converge by re-driving the STORED event via the admin ports, never by "
                        + "re-running a command through the dispatcher or an application-service command handler");
        rule.check(RECON_CLASSES);
    }
}
