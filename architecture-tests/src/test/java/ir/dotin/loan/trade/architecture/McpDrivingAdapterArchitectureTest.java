package ir.dotin.loan.trade.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Structural gate for the MCP driving adapter ({@code adapters/driving/mcp}). The generic "driving adapters MUST NOT
 * depend on outbound ports" invariant is enforced for every driving adapter (mcp included) by the platform
 * {@code BaseAdapterArchitectureTest} (see {@code TradeLoanAdapterArchitectureTest}); this class keeps only the
 * MCP-specific rule pinning the adapter's allowed dependencies to the read path: it must never reach the command/write
 * side, a driven adapter, a sibling driving adapter, or the MCP transport runtime — the MCP adapter is read-only.
 *
 * <p>Scoped to {@code ir.dotin.loan.trade.adapters.driving.mcp} only (the architecture-tests POM puts that module's
 * bytecode on the test classpath). A missing dependency would make these rules pass vacuously; the assertion below that
 * the package actually imported classes guards against that.
 */
class McpDrivingAdapterArchitectureTest {

    private static final String MCP_PACKAGE = "ir.dotin.loan.trade.adapters.driving.mcp";

    private static final JavaClasses MCP_CLASSES = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages(MCP_PACKAGE);

    @Test
    void mcpAdapterPackageIsNotEmpty() {
        // Guard against a vacuous pass: if the module is absent from the test classpath, the rules below see zero
        // classes and "succeed" silently. Fail loudly instead.
        assertFalse(MCP_CLASSES.isEmpty(), "MCP driving-adapter bytecode must be on the architecture-tests classpath");
    }

    @Test
    void mcpAdapterMustNotDependOnWriteSideOrOtherAdapters() {
        ArchRule rule = noClasses()
                .that()
                .resideInAPackage(MCP_PACKAGE + "..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage(
                        "ir.dotin.loan.trade.core.application.service..",
                        "ir.dotin.loan.trade.adapters.driven..",
                        "ir.dotin.loan.trade.adapters.driving.rest..",
                        "ir.dotin.loan.trade.adapters.driving.contract..",
                        "ir.dotin.loan.trade.adapters.driving.messaging..",
                        "ir.dotin.platform.pangaea.ai.mcp.server..")
                .because("the MCP business adapter is a read-only driving adapter: it must not reach the command/write "
                        + "side, a driven adapter, a sibling driving adapter, or the MCP transport runtime");
        rule.check(MCP_CLASSES);
    }
}
