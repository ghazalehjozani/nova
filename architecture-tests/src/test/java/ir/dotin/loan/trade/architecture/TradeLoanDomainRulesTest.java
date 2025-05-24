package ir.dotin.loan.trade.architecture;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.conditions.ArchConditions;

import ir.dotin.platform.domain.common.entity.BaseEntity;
import ir.dotin.platform.domain.common.i18n.LocalizedMessage;

import static com.tngtech.archunit.lang.conditions.ArchConditions.*;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;

@AnalyzeClasses(packages = "ir.dotin.loan.trade.core.domain", importOptions = ImportOption.DoNotIncludeTests.class)
public class TradeLoanDomainRulesTest {

    private static final String MORABEHE_LOAN_PREFIX = "ir.dotin.loan.trade.";

    private static final String DOMAIN_LAYER_PACKAGES = MORABEHE_LOAN_PREFIX + "core.domain..";

    private static final String ADAPTERS_PACKAGE = MORABEHE_LOAN_PREFIX + "adapters..";
    private static final String PORTS_PACKAGE = MORABEHE_LOAN_PREFIX + "port..";
    private static final String APPLICATION_PACKAGE = MORABEHE_LOAN_PREFIX + "core.application..";

    private static final String DOMAIN_COMMON_PACKAGE = "ir.dotin.platform.domain.common..";
    private static final String BASE_LOAN_PACKAGE = "ir.dotin.loan.baseloan.core.domain..";
    private static final String JAVA_PACKAGE = "java..";
    private static final String SLF4J_PACKAGE = "org.slf4j..";

    private static final String DOMAIN_INTERACTION_PACKAGES = DOMAIN_LAYER_PACKAGES + "interaction..";
    private static final String DOMAIN_ENUMS_PACKAGES = DOMAIN_LAYER_PACKAGES + "enums..";
    private static final String DOMAIN_I18N_PACKAGES = DOMAIN_LAYER_PACKAGES + "i18n..";
    private static final String DOMAIN_AGGREGATE_PACKAGES = DOMAIN_LAYER_PACKAGES + "aggregate..";
    private static final String DOMAIN_VO_PACKAGES = DOMAIN_LAYER_PACKAGES + "vo..";

    @ArchTest
    public static final ArchRule domain_should_not_depend_on_application = noClasses()
            .that()
            .resideInAPackage(DOMAIN_LAYER_PACKAGES)
            .should()
            .dependOnClassesThat()
            .resideInAPackage(APPLICATION_PACKAGE)
            .as("Trade Loan: Domain layer should not depend on Application layer");

    @ArchTest
    public static final ArchRule domain_should_not_depend_on_ports = noClasses()
            .that()
            .resideInAPackage(DOMAIN_LAYER_PACKAGES)
            .should()
            .dependOnClassesThat()
            .resideInAPackage(PORTS_PACKAGE)
            .as("Trade Loan: Domain layer should not depend on Port layer");

    @ArchTest
    public static final ArchRule domain_should_not_depend_on_adapters = noClasses()
            .that()
            .resideInAPackage(DOMAIN_LAYER_PACKAGES)
            .should()
            .dependOnClassesThat()
            .resideInAPackage(ADAPTERS_PACKAGE)
            .as("Trade Loan: Domain layer should not depend on Adapters layer");

    @ArchTest
    public static final ArchRule domain_should_only_depend_on_allowed_packages = classes()
            .that()
            .resideInAPackage(DOMAIN_LAYER_PACKAGES)
            .should()
            .onlyDependOnClassesThat()
            .resideInAnyPackage(
                    DOMAIN_LAYER_PACKAGES, DOMAIN_COMMON_PACKAGE, BASE_LOAN_PACKAGE, JAVA_PACKAGE, SLF4J_PACKAGE)
            .as(
                    "Trade Loan: Domain layer should only depend on allowed packages (self, base-loan domain, common, java, slf4j)");

    @ArchTest
    public static final ArchRule value_objects_should_be_immutable_or_records = classes()
            .that()
            .resideInAPackage(DOMAIN_VO_PACKAGES)
            .and()
            .areTopLevelClasses()
            .should(beRecords())
            .as("Value Objects should be Records or effectively immutable");

    @ArchTest
    public static final ArchRule value_objects_should_not_have_setters = noMethods()
            .that()
            .areDeclaredInClassesThat()
            .resideInAPackage(DOMAIN_VO_PACKAGES)
            .and()
            .areDeclaredInClassesThat()
            .areNotRecords()
            .and()
            .haveNameStartingWith("set")
            .and()
            .haveRawParameterTypes(Object.class)
            .or()
            .haveNameStartingWith("with")
            .should()
            .beFinal()
            .as("Value Objects should not have traditional setters (use Records or constructor/with methods)");

    @ArchTest
    public static final ArchRule interaction_interfaces_should_be_functional_interfaces = classes()
            .that()
            .resideInAPackage(DOMAIN_INTERACTION_PACKAGES)
            .and()
            .areTopLevelClasses()
            .should()
            .beInterfaces()
            .as("Interaction types in '..interaction..' should be Interfaces");

    @ArchTest
    public static final ArchRule enums_package_should_contain_enums_or_interfaces = classes()
            .that()
            .resideInAPackage(DOMAIN_ENUMS_PACKAGES)
            .should()
            .beEnums()
            .orShould()
            .beInterfaces()
            .as("Classes in '..enums..' packages should be Enums or Interfaces");

    @ArchTest
    public static final ArchRule i18n_classes_should_be_enums_implementing_localized_message = classes()
            .that()
            .resideInAPackage(DOMAIN_I18N_PACKAGES)
            .should()
            .beEnums()
            .andShould()
            .implement(LocalizedMessage.class)
            .as("Classes in '..i18n..' should be Enums implementing LocalizedMessage");

    @ArchTest
    public static final ArchRule aggregate_related_classes_should_extend_base_types = classes()
            .that()
            .resideInAPackage(DOMAIN_AGGREGATE_PACKAGES)
            .and()
            .areTopLevelClasses()
            .should()
            .beAssignableFrom(BaseEntity.class)
            .andShould(ArchConditions.notBeEnums())
            .andShould(ArchConditions.notBeRecords())
            .andShould(ArchConditions.notBeInterfaces())
            .andShould(ArchConditions.beFinal())
            .as("Classes in '..aggregate..' packages should extend BaseEntity or AggregateRoot");

    @ArchTest
    public static final ArchRule nested_classes_in_aggregates_should_be_static_builders_or_allowed = classes()
            .that()
            .areNestedClasses()
            .and()
            .resideInAPackage(DOMAIN_AGGREGATE_PACKAGES)
            .should(beNestedClasses())
            .as("Nested classes within Aggregates should typically only be static Builders");

    @ArchTest
    public static final ArchRule builder_with_methods_should_return_builder_type = methods()
            .that()
            .areDeclaredInClassesThat(areNestedStaticBuilderClasses())
            .and()
            .haveNameStartingWith("with")
            .and()
            .arePublic()
            .should(haveNameStartingWith("with"))
            .as("Builder 'with...' methods should return the Builder type");

    private static DescribedPredicate<JavaClass> areNestedStaticBuilderClasses() {
        return new DescribedPredicate<>("are nested static Builder classes") {
            @Override
            public boolean test(JavaClass input) {
                return input.isNestedClass()
                        && input.getModifiers().contains(JavaModifier.STATIC)
                        && (input.getSimpleName().endsWith("Builder")
                                || input.getSimpleName().endsWith("AbstractBuilder"));
            }
        };
    }
}
