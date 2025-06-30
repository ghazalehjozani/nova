package ir.dotin.loan.trade.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;

import ir.dotin.platform.archunit.config.ArchUnitConfiguration;
import ir.dotin.platform.archunit.domain.BaseDomainArchitectureTest;

@AnalyzeClasses(packages = "ir.dotin.loan.trade.core.domain", importOptions = ImportOption.DoNotIncludeTests.class)
public class TradeLoanDomainArchitectureTest extends BaseDomainArchitectureTest {

    @Override
    protected ArchUnitConfiguration getConfiguration() {
        return ArchUnitConfiguration.builder("ir.dotin.loan.trade")
                .withAdditionalAllowedPackage("ir.dotin.loan.baseloan.core.domain..")
                .build();
    }
}
