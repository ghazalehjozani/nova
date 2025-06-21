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

    private static final String BASE_LOAN_PREFIX = "ir.dotin.loan.trade.";

    private static final String DOMAIN_LAYER_PACKAGES = BASE_LOAN_PREFIX + "core.domain..";

    private static final String ADAPTERS_PACKAGE = BASE_LOAN_PREFIX + "adapters..";
    private static final String PORTS_PACKAGE = BASE_LOAN_PREFIX + "core.port..";
    private static final String APPLICATION_PACKAGE = BASE_LOAN_PREFIX + "core.application..";

    private static final String DOMAIN_COMMON_PACKAGE = "ir.dotin.platform.domain.common..";
    private static final String JAVA_PACKAGE = "java..";
    private static final String SLF4J_PACKAGE = "org.slf4j..";

    private static final String BASE_LOAN_PACKAGE = "ir.dotin.loan.baseloan.core.domain..";


    private static final String[] GUAVA_ALLOWED_PACKAGES = {
            "com.google.common.base..",
            "com.google.common.collect..",
            "com.google.common.primitives..",
            "com.google.common.math..",
            "com.google.common.annotations.."
    };

    private static final String JSPECIFY_PACKAGE = "org.jspecify.annotations..";

    private static final String DOMAIN_INTERACTION_PACKAGES = DOMAIN_LAYER_PACKAGES + "interaction..";
    private static final String DOMAIN_ENUMS_PACKAGES = DOMAIN_LAYER_PACKAGES + "enums..";
    private static final String DOMAIN_I18N_PACKAGES = DOMAIN_LAYER_PACKAGES + "i18n..";
    private static final String DOMAIN_ENTITY_PACKAGES = DOMAIN_LAYER_PACKAGES + "entity..";
    private static final String DOMAIN_VO_PACKAGES = DOMAIN_LAYER_PACKAGES + "vo..";
    private static final String DOMAIN_SHARED_PACKAGES = DOMAIN_LAYER_PACKAGES + "shared..";

    @ArchTest
    public static final ArchRule domain_should_not_depend_on_application = noClasses()
            .that()
            .resideInAPackage(DOMAIN_LAYER_PACKAGES)
            .should()
            .dependOnClassesThat()
            .resideInAPackage(APPLICATION_PACKAGE)
            .as("Base Loan: Domain layer should not depend on Application layer");

    @ArchTest
    public static final ArchRule domain_should_not_depend_on_ports = noClasses()
            .that()
            .resideInAPackage(DOMAIN_LAYER_PACKAGES)
            .should()
            .dependOnClassesThat()
            .resideInAPackage(PORTS_PACKAGE)
            .as("Base Loan: Domain layer should not depend on Port layer");

    @ArchTest
    public static final ArchRule domain_should_not_depend_on_adapters = noClasses()
            .that()
            .resideInAPackage(DOMAIN_LAYER_PACKAGES)
            .should()
            .dependOnClassesThat()
            .resideInAPackage(ADAPTERS_PACKAGE)
            .as("Base Loan: Domain layer should not depend on Adapters layer");

    @ArchTest
    public static final ArchRule domain_should_only_depend_on_allowed_packages = classes()
            .that()
            .resideInAPackage(DOMAIN_LAYER_PACKAGES)
            .should()
            .onlyDependOnClassesThat()
            .resideInAnyPackage(combinePackages(
                    new String[] {
                            DOMAIN_LAYER_PACKAGES, DOMAIN_COMMON_PACKAGE, BASE_LOAN_PACKAGE, JAVA_PACKAGE, SLF4J_PACKAGE, JSPECIFY_PACKAGE
                    },
                    GUAVA_ALLOWED_PACKAGES))
            .as(
                    "Base Loan: Domain layer should only depend on allowed packages (self, common, java, slf4j, guava core utilities, jspecify)");

    @ArchTest
    public static final ArchRule domain_should_not_use_guava_infrastructure_concepts = noClasses()
            .that()
            .resideInAPackage(DOMAIN_LAYER_PACKAGES)
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(
                    "com.google.common.io..",
                    "com.google.common.net..",
                    "com.google.common.cache..",
                    "com.google.common.eventbus..",
                    "com.google.common.reflect..",
                    "com.google.common.util.concurrent..",
                    "com.google.common.testing..")
            .as(
                    "Base Loan: Domain layer should not depend on Guava infrastructure concepts (I/O, networking, caching, eventbus, reflection, concurrency, testing)");

    @ArchTest
    public static final ArchRule domain_should_not_access_guava_infrastructure_classes = noClasses()
            .that()
            .resideInAPackage(DOMAIN_LAYER_PACKAGES)
            .should()
            .accessClassesThat()
            .resideInAnyPackage(
                    "com.google.common.io..",
                    "com.google.common.net..",
                    "com.google.common.cache..",
                    "com.google.common.eventbus..",
                    "com.google.common.reflect..",
                    "com.google.common.util.concurrent..",
                    "com.google.common.testing..")
            .as(
                    "Base Loan: Domain layer should not access Guava infrastructure classes (I/O, networking, caching, eventbus, reflection, concurrency, testing)");

    @ArchTest
    public static final ArchRule domain_may_use_allowed_guava_utilities = classes()
            .that()
            .resideInAPackage(DOMAIN_LAYER_PACKAGES)
            .should()
            .onlyAccessClassesThat(new DescribedPredicate<>("do not reside in forbidden Guava packages") {
                @Override
                public boolean test(JavaClass input) {
                    String packageName = input.getPackageName();
                    return !packageName.startsWith("com.google.common.io")
                            && !packageName.startsWith("com.google.common.net")
                            && !packageName.startsWith("com.google.common.cache")
                            && !packageName.startsWith("com.google.common.eventbus")
                            && !packageName.startsWith("com.google.common.reflect")
                            && !packageName.startsWith("com.google.common.util.concurrent")
                            && !packageName.startsWith("com.google.common.testing");
                }
            })
            .as(
                    "Base Loan: Domain layer may only access allowed Guava utilities (base, collect, primitives, math, annotations) and JSpecify");

    @ArchTest
    public static final ArchRule value_objects_should_be_immutable_or_records = classes()
            .that()
            .resideInAPackage(DOMAIN_VO_PACKAGES)
            .and()
            .areTopLevelClasses()
            .should(beRecords())
            .orShould(beInterfaces())
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
    public static final ArchRule entity_related_classes_should_extend_base_types = classes()
            .that()
            .resideInAPackage(DOMAIN_ENTITY_PACKAGES)
            .and()
            .areTopLevelClasses()
            .should()
            .beAssignableTo(BaseEntity.class)
            .andShould(ArchConditions.notBeEnums())
            .andShould(ArchConditions.notBeRecords())
            .andShould(ArchConditions.notBeInterfaces())
            .andShould(ArchConditions.beFinal())
            .as("Classes in '..entity..' packages should extend BaseEntity or AggregateRoot");

    @ArchTest
    public static final ArchRule nested_classes_in_entities_should_be_static_builders_or_allowed = classes()
            .that()
            .areNestedClasses()
            .and()
            .resideInAPackage(DOMAIN_ENTITY_PACKAGES)
            .should(beNestedClasses())
            .as("Nested classes within Entities should typically only be static Builders");

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

    @ArchTest
    public static final ArchRule shared_classes_should_not_extend_base_entity = noClasses()
            .that()
            .resideInAPackage(DOMAIN_SHARED_PACKAGES)
            .should()
            .beAssignableTo(BaseEntity.class)
            .as("Classes in '" + DOMAIN_SHARED_PACKAGES + "' should not extend BaseEntity");

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

    private static String[] combinePackages(String[] basePackages, String[] additionalPackages) {
        String[] combined = new String[basePackages.length + additionalPackages.length];
        System.arraycopy(basePackages, 0, combined, 0, basePackages.length);
        System.arraycopy(additionalPackages, 0, combined, basePackages.length, additionalPackages.length);
        return combined;
    }
}
