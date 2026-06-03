package ir.dotin.loan.trade.architecture;

import java.util.Set;

import com.tngtech.archunit.core.domain.JavaAnnotation;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaEnumConstant;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Verb-safety gate for the REST driving adapter. The HTTP-verb classifier (gateway / observability) keys command vs
 * query semantics off the package a controller lives in: anything under {@code ..adapters.driving.rest.command..} is
 * assumed to mutate state, anything under {@code ..adapters.driving.rest.query..} is assumed to be a safe read. This
 * test pins that assumption to the actual HTTP mappings so a misplaced verb cannot silently lie to the classifier.
 *
 * <ul>
 *   <li>Command controllers may only carry mutating mappings — {@code @PostMapping} / {@code @PutMapping} /
 *       {@code @PatchMapping} / {@code @DeleteMapping} (or {@code @RequestMapping(method = ...)} restricted to those
 *       methods). A {@code @GetMapping} on a command controller is a violation.
 *   <li>Query controllers may only carry safe mappings — {@code @GetMapping} (or {@code @RequestMapping(method = GET /
 *       HEAD)}). Any {@code @PostMapping} / {@code @PutMapping} / {@code @PatchMapping} / {@code @DeleteMapping} on a
 *       query controller is a violation.
 * </ul>
 *
 * <p>Scoped to {@code ir.dotin.loan.trade.adapters.driving.rest} only (the architecture-tests POM puts that module's
 * bytecode on the test classpath). A missing dependency would make these rules pass vacuously; the non-empty assertions
 * below guard against that.
 */
class RestVerbConventionArchitectureTest {

    private static final String COMMAND_PACKAGE = "ir.dotin.loan.trade.adapters.driving.rest.command";
    private static final String QUERY_PACKAGE = "ir.dotin.loan.trade.adapters.driving.rest.query";

    private static final String GET_MAPPING = "org.springframework.web.bind.annotation.GetMapping";
    private static final String POST_MAPPING = "org.springframework.web.bind.annotation.PostMapping";
    private static final String PUT_MAPPING = "org.springframework.web.bind.annotation.PutMapping";
    private static final String PATCH_MAPPING = "org.springframework.web.bind.annotation.PatchMapping";
    private static final String DELETE_MAPPING = "org.springframework.web.bind.annotation.DeleteMapping";
    private static final String REQUEST_MAPPING = "org.springframework.web.bind.annotation.RequestMapping";

    private static final Set<String> MUTATING_MAPPINGS =
            Set.of(POST_MAPPING, PUT_MAPPING, PATCH_MAPPING, DELETE_MAPPING);
    private static final Set<String> SAFE_REQUEST_METHODS = Set.of("GET", "HEAD");

    private static final JavaClasses REST_CLASSES = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages(COMMAND_PACKAGE, QUERY_PACKAGE);

    @Test
    void restControllerPackagesAreNotEmpty() {
        // Guard against a vacuous pass: if the rest module is absent from the test classpath, the rules below see zero
        // classes and "succeed" silently. Fail loudly instead.
        assertFalse(
                REST_CLASSES.isEmpty(), "REST driving-adapter bytecode must be on the architecture-tests classpath");
    }

    @Test
    void commandControllersMustNotExposeSafeReadMappings() {
        ArchRule rule = classes()
                .that()
                .resideInAPackage(COMMAND_PACKAGE + "..")
                .should(carryOnlyMutatingHttpMappings())
                .because("controllers under ..rest.command.. are classified as state-mutating: a @GetMapping there "
                        + "would make the HTTP-verb classifier treat a read as a write");
        rule.check(REST_CLASSES);
    }

    @Test
    void queryControllersMustNotExposeMutatingMappings() {
        ArchRule rule = classes()
                .that()
                .resideInAPackage(QUERY_PACKAGE + "..")
                .should(carryOnlySafeHttpMappings())
                .because("controllers under ..rest.query.. are classified as safe reads: a "
                        + "@Post/@Put/@Patch/@DeleteMapping there would make the HTTP-verb classifier treat a write "
                        + "as a read");
        rule.check(REST_CLASSES);
    }

    private static ArchCondition<JavaClass> carryOnlyMutatingHttpMappings() {
        return new ArchCondition<>("carry only mutating HTTP mappings (no @GetMapping / safe @RequestMapping)") {
            @Override
            public void check(JavaClass controller, ConditionEvents events) {
                for (JavaMethod method : controller.getMethods()) {
                    for (JavaAnnotation<JavaMethod> annotation : method.getAnnotations()) {
                        String type = annotation.getRawType().getName();
                        if (GET_MAPPING.equals(type) || isSafeRequestMapping(type, annotation)) {
                            events.add(SimpleConditionEvent.violated(
                                    method,
                                    "%s is a command controller but exposes a safe-read mapping (%s)"
                                            .formatted(method.getFullName(), type)));
                        }
                    }
                }
            }
        };
    }

    private static ArchCondition<JavaClass> carryOnlySafeHttpMappings() {
        return new ArchCondition<>("carry only safe HTTP mappings (@GetMapping / HEAD)") {
            @Override
            public void check(JavaClass controller, ConditionEvents events) {
                for (JavaMethod method : controller.getMethods()) {
                    for (JavaAnnotation<JavaMethod> annotation : method.getAnnotations()) {
                        String type = annotation.getRawType().getName();
                        if (MUTATING_MAPPINGS.contains(type) || isMutatingRequestMapping(type, annotation)) {
                            events.add(SimpleConditionEvent.violated(
                                    method,
                                    "%s is a query controller but exposes a mutating mapping (%s)"
                                            .formatted(method.getFullName(), type)));
                        }
                    }
                }
            }
        };
    }

    private static boolean isSafeRequestMapping(String type, JavaAnnotation<JavaMethod> annotation) {
        return REQUEST_MAPPING.equals(type) && requestMethodsMatch(annotation, SAFE_REQUEST_METHODS::contains);
    }

    private static boolean isMutatingRequestMapping(String type, JavaAnnotation<JavaMethod> annotation) {
        return REQUEST_MAPPING.equals(type)
                && requestMethodsMatch(annotation, name -> !SAFE_REQUEST_METHODS.contains(name));
    }

    /**
     * Reads the {@code method} attribute of a {@code @RequestMapping} and reports whether any declared
     * {@link org.springframework.web.bind.annotation.RequestMethod} matches the predicate. An empty/absent
     * {@code method} attribute matches no verb (it would default to all methods at runtime, but Nova never uses the
     * method-less form on a handler method, so we treat "no explicit verb" as not-classifiable rather than guessing).
     */
    private static boolean requestMethodsMatch(
            JavaAnnotation<JavaMethod> annotation, java.util.function.Predicate<String> verbPredicate) {
        Object methodValue = annotation.get("method").orElse(null);
        if (!(methodValue instanceof JavaEnumConstant[] verbs)) {
            return false;
        }
        for (JavaEnumConstant verb : verbs) {
            if (verbPredicate.test(verb.name())) {
                return true;
            }
        }
        return false;
    }
}
