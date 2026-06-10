package ir.dotin.loan.trade.architecture;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaAccess;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.properties.HasName;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Structural gate for the reconciliation driven adapter ({@code adapters/driven/reconciliation}).
 *
 * <ul>
 *   <li>INV-4: recon converges by re-driving STORED outbox/inbox rows through the admin ports — it must NEVER re-run a
 *       business command (which would mint new ids and defeat the stored idempotency key), so it must not depend on the
 *       command dispatcher or any application-service command handler.
 *   <li>Replay confinement: {@code OutboxAdminPort.republish(..)} re-delivers a stored event onto the FCB corridor — a
 *       money-state lever — so only the reconciliation adapter (behind its fail-closed remediate gates) may call it. No
 *       other Nova code may auto-trigger a republish.
 *   <li>Decision/serialization purity: {@code FacilityRootCauseClassifier} and {@code FacilityDossierBuilder} are pure
 *       logic — they must not depend on any outbound/admin port, Spring, FCB client, or saga/inbox/outbox admin type.
 *   <li>Single gated path: {@code FacilityConvergenceAction} is the only {@code ConvergenceAction} implementation, so
 *       the maker-checker gates cannot be bypassed by a second, un-gated impl.
 * </ul>
 *
 * <p>The recon-scoped rules import {@code ir.dotin.loan.trade.adapters.driven.reconciliation} only; the replay-
 * confinement rule imports the whole {@code ir.dotin.loan.trade} tree so it can prove no sibling package calls
 * {@code republish}. {@link #reconAdapterPackageIsNotEmpty()} guards against a vacuous pass if that module's bytecode
 * is missing from the test classpath.
 */
class ReconciliationAdapterArchitectureTest {

    private static final String RECON_PACKAGE = "ir.dotin.loan.trade.adapters.driven.reconciliation";

    private static final String TRADE_ROOT_PACKAGE = "ir.dotin.loan.trade";

    private static final String OUTBOX_ADMIN_PORT = "ir.dotin.platform.pangaea.outbox.api.admin.OutboxAdminPort";

    private static final String CONVERGENCE_ACTION_SPI =
            "ir.dotin.platform.pangaea.reconciliation.api.spi.ConvergenceAction";

    private static final String FACILITY_CLASSIFIER = RECON_PACKAGE + ".FacilityRootCauseClassifier";

    private static final String FACILITY_DOSSIER_BUILDER = RECON_PACKAGE + ".FacilityDossierBuilder";

    private static final JavaClasses RECON_CLASSES = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages(RECON_PACKAGE);

    private static final JavaClasses TRADE_CLASSES = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages(TRADE_ROOT_PACKAGE);

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
                        "ir.dotin.platform.pangaea.servicelayer..", "ir.dotin.loan.trade.core.application.service..")
                .because("INV-4: recon must converge by re-driving the STORED event via the admin ports, never by "
                        + "re-running a command through the dispatcher or an application-service command handler");
        rule.check(RECON_CLASSES);
    }

    @Test
    void onlyReconAdapterMayCallOutboxRepublish() {
        ArchRule rule = noClasses()
                .that()
                .resideOutsideOfPackage(RECON_PACKAGE + "..")
                .should()
                .callMethodWhere(targetIsRepublishOnOutboxAdminPort())
                .because("INV-2/maker-checker: replaying a stored event back onto the FCB corridor is a money-state "
                        + "lever; only the reconciliation driven adapter (behind its fail-closed remediate gates) may "
                        + "trigger OutboxAdminPort.republish — no other Nova code may auto-trigger a republish");
        rule.check(TRADE_CLASSES);
    }

    @Test
    void facilityClassifierAndDossierBuilderOnlyDependOnPureTypes() {
        ArchRule rule = classes()
                .that()
                .haveFullyQualifiedName(FACILITY_CLASSIFIER)
                .or()
                .haveFullyQualifiedName(FACILITY_DOSSIER_BUILDER)
                .should()
                .onlyDependOnClassesThat()
                .resideInAnyPackage(
                        "java..",
                        "org.jspecify.annotations..",
                        "ir.dotin.platform.pangaea.reconciliation.api.model..",
                        "ir.dotin.platform.pangaea.outbox.api",
                        "ir.dotin.platform.pangaea.outbox.api.admin",
                        RECON_PACKAGE,
                        "ir.dotin.loan.baseloan.core.domain.loanfacility.enums",
                        "ir.dotin.loan.trade.core.application.ports.outbound.client.reconservice")
                .because(
                        "FacilityRootCauseClassifier and FacilityDossierBuilder are pure decision/serialization logic — "
                                + "deciding the root cause and serializing the dossier hash deterministically; the only "
                                + "outbound-port-package type they may touch is the read-only ReconLoanFileState DTO");
        rule.check(RECON_CLASSES);

        ArchRule denyRule = noClasses()
                .that()
                .haveFullyQualifiedName(FACILITY_CLASSIFIER)
                .or()
                .haveFullyQualifiedName(FACILITY_DOSSIER_BUILDER)
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage(
                        "org.springframework..",
                        "ir.dotin.platform.pangaea.inbox.api.admin..",
                        "ir.dotin.platform.pangaea.saga.api.admin..",
                        "ir.dotin.platform.pangaea.reconciliation.api.admin..")
                .orShould()
                .dependOnClassesThat()
                .haveFullyQualifiedName(OUTBOX_ADMIN_PORT)
                .orShould()
                .dependOnClassesThat()
                .haveSimpleNameEndingWith("AdminPort")
                .orShould()
                .dependOnClassesThat()
                .haveSimpleName("FcbReconStatePort")
                .orShould()
                .dependOnClassesThat()
                .haveSimpleName("FacilityReconReadPort")
                .because(
                        "the pure classifier/dossier logic must NOT reach any outbound/admin port, Spring, FCB client, "
                                + "or saga/inbox/outbox admin type — no I/O, no side effects");
        denyRule.check(RECON_CLASSES);
    }

    @Test
    void facilityConvergenceActionIsTheOnlyConvergenceActionImpl() {
        ArchRule rule = classes()
                .that()
                .implement(CONVERGENCE_ACTION_SPI)
                .should()
                .haveFullyQualifiedName(RECON_PACKAGE + ".FacilityConvergenceAction")
                .because("the maker-checker gates (tier, money gate, dossier-staleness, replay confinement) live in "
                        + "FacilityConvergenceAction; a second ConvergenceAction impl would be an un-gated path");
        rule.check(RECON_CLASSES);
    }

    private static DescribedPredicate<JavaAccess<?>> targetIsRepublishOnOutboxAdminPort() {
        return new DescribedPredicate<>("call OutboxAdminPort.republish(..)") {
            @Override
            public boolean test(JavaAccess<?> access) {
                if (!"republish".equals(access.getTarget().getName())) {
                    return false;
                }
                JavaClass owner = access.getTargetOwner();
                if (owner.getName().equals(OUTBOX_ADMIN_PORT)) {
                    return true;
                }
                return owner.getAllRawInterfaces().stream()
                        .map(HasName::getName)
                        .anyMatch(OUTBOX_ADMIN_PORT::equals);
            }
        };
    }
}
