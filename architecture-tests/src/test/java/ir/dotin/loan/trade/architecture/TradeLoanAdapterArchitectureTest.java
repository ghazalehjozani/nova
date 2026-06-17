package ir.dotin.loan.trade.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;

import ir.dotin.platform.pangaea.archunit.adapter.BaseAdapterArchitectureTest;
import ir.dotin.platform.pangaea.archunit.config.ArchUnitConfiguration;

/**
 * Adapter-layer architecture gate for trade-loan. Consumes the platform's {@link BaseAdapterArchitectureTest}, which
 * enforces the hexagonal driving/driven boundaries (driving adapters must not reach outbound ports or driven adapters)
 * and the REST verb-safety convention (command controllers mutate, query controllers read).
 *
 * <p>This replaces the previously hand-written {@code RestVerbConventionArchitectureTest} — the verb rules now live in
 * {@code ir.dotin.platform.pangaea.archunit.adapter.RestVerbRules} and are shared across every Pangaea consumer. Nova's
 * truly bespoke adapter invariants stay in their own tests ({@code McpDrivingAdapterArchitectureTest},
 * {@code ReconciliationAdapterArchitectureTest}).
 */
@AnalyzeClasses(packages = "ir.dotin.loan.trade.adapters", importOptions = ImportOption.DoNotIncludeTests.class)
public class TradeLoanAdapterArchitectureTest extends BaseAdapterArchitectureTest {

    @Override
    protected ArchUnitConfiguration getConfiguration() {
        return ArchUnitConfiguration.builder("ir.dotin.loan.trade")
                .withAdditionalAllowedPackage("ir.dotin.loan.baseloan.core.domain..")
                .build();
    }
}
