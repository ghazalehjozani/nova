package ir.dotin.loan.trade.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;

import ir.dotin.platform.pangaea.archunit.config.ArchUnitConfiguration;
import ir.dotin.platform.pangaea.archunit.domain.BaseDomainArchitectureTest;

@AnalyzeClasses(packages = "ir.dotin.loan.trade.core.domain", importOptions = ImportOption.DoNotIncludeTests.class)
public class TradeLoanDomainArchitectureTest extends BaseDomainArchitectureTest {

    @Override
    protected ArchUnitConfiguration getConfiguration() {
        return ArchUnitConfiguration.builder("ir.dotin.loan.trade")
                // Shared kernel: trade domain specializes base-loan abstractions.
                .withAdditionalAllowedPackage("ir.dotin.loan.baseloan.core.domain..")
                // Sanctioned generic-subdomain dependency (ADR-0001): the trade document strategies/factories
                // build on the platform accounting-document model (api + core), as base-loan's shared/document does.
                .withAdditionalAllowedPackage("ir.dotin.platform.accounting.document..")
                // Sanctioned formula evaluation dependency (ADR-0005): trade formula bindings use expression-kit.
                .withAdditionalAllowedPackage("ir.dotin.platform.formula.api..")
                .build();
    }
}
