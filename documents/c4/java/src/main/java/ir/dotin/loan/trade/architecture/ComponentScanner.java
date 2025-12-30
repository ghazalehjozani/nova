package ir.dotin.loan.trade.architecture;

import java.util.*;

import ir.dotin.loan.trade.architecture.config.ArchitectureConstants.Tags;
import ir.dotin.loan.trade.architecture.config.ArchitectureConstants.Technologies;

import io.github.classgraph.*;

public class ComponentScanner {

    private static final Set<String> EXCLUDED_SUFFIXES = Set.of(
            "Test",
            "Tests",
            "IT",
            "Config",
            "Configuration",
            "Properties",
            "Mapper",
            "Dto",
            "DTO",
            "Request",
            "Response",
            "Exception",
            "Emb",
            "Projection");

    private final String basePackage;

    public ComponentScanner(String basePackage) {
        this.basePackage = basePackage;
    }

    public Map<String, ScannedComponent> scanComponents() {
        var components = new LinkedHashMap<String, ScannedComponent>();

        try (var result =
                new ClassGraph().enableAllInfo().acceptPackages(basePackage).scan()) {
            System.out.println(
                    "   📦 Packages found: " + result.getPackageInfo().size());

            int before = components.size();
            scanRestControllers(result, components);
            System.out.println("      Controllers: " + (components.size() - before));

            before = components.size();
            scanMessageConsumers(result, components);
            System.out.println("      Consumers: " + (components.size() - before));

            before = components.size();
            scanCommandHandlers(result, components);
            System.out.println("      CommandHandlers: " + (components.size() - before));

            before = components.size();
            scanQueryHandlers(result, components);
            System.out.println("      QueryHandlers: " + (components.size() - before));

            before = components.size();
            scanSagas(result, components);
            System.out.println("      Sagas: " + (components.size() - before));

            before = components.size();
            scanAggregatesAndEntities(result, components);
            System.out.println("      Domain: " + (components.size() - before));

            before = components.size();
            scanDomainServices(result, components);
            System.out.println("      DomainServices: " + (components.size() - before));

            before = components.size();
            scanRepositories(result, components);
            System.out.println("      Repositories: " + (components.size() - before));

            before = components.size();
            scanExternalClients(result, components);
            System.out.println("      ExternalClients: " + (components.size() - before));

            before = components.size();
            scanOutboxHandlers(result, components);
            System.out.println("      OutboxHandlers: " + (components.size() - before));

        } catch (Exception e) {
            System.err.println("   ⚠️  Scan error: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("   🔍 Total scanned: " + components.size() + " components");
        return components;
    }

    private void scanRestControllers(ScanResult result, Map<String, ScannedComponent> components) {
        result.getClassesWithAnnotation("org.springframework.web.bind.annotation.RestController")
                .filter(this::isValid)
                .forEach(ci -> {
                    var tags = new ArrayList<>(List.of(Tags.CONTROLLER));
                    tags.add(ci.getName().toLowerCase().contains("query") ? Tags.QUERY : Tags.COMMAND);
                    add(
                            components,
                            ci.getSimpleName(),
                            extractEndpoint(ci),
                            Technologies.SPRING_REST,
                            tags,
                            extractDeps(ci));
                });

        result.getAllClasses().stream()
                .filter(ci -> ci.getPackageName().contains("adapters.driving.rest"))
                .filter(ci -> ci.getSimpleName().endsWith("Controller"))
                .filter(this::isValid)
                .forEach(ci -> {
                    if (components.containsKey(ci.getSimpleName())) return;
                    var tags = new ArrayList<>(List.of(Tags.CONTROLLER));
                    tags.add(ci.getName().toLowerCase().contains("query") ? Tags.QUERY : Tags.COMMAND);
                    add(
                            components,
                            ci.getSimpleName(),
                            extractEndpoint(ci),
                            Technologies.SPRING_REST,
                            tags,
                            extractDeps(ci));
                });
    }

    private void scanMessageConsumers(ScanResult result, Map<String, ScannedComponent> components) {
        result.getClassesWithMethodAnnotation("org.springframework.kafka.annotation.KafkaListener")
                .filter(this::isValid)
                .forEach(ci -> add(
                        components,
                        ci.getSimpleName(),
                        "Kafka consumer",
                        Technologies.SPRING_KAFKA,
                        List.of(Tags.CONSUMER, Tags.MESSAGING),
                        extractDeps(ci)));

        result.getAllClasses().stream()
                .filter(ci -> ci.getPackageName().contains("adapters.driving.messaging"))
                .filter(ci -> ci.getSimpleName().endsWith("Consumer"))
                .filter(this::isValid)
                .forEach(ci -> {
                    if (components.containsKey(ci.getSimpleName())) return;
                    add(
                            components,
                            ci.getSimpleName(),
                            "Kafka consumer",
                            Technologies.SPRING_KAFKA,
                            List.of(Tags.CONSUMER, Tags.MESSAGING),
                            extractDeps(ci));
                });
    }

    private void scanCommandHandlers(ScanResult result, Map<String, ScannedComponent> components) {
        result.getAllClasses().stream()
                .filter(this::isValid)
                .filter(ci -> ci.getSimpleName().endsWith("CommandHandler"))
                .forEach(ci -> {
                    var tags = new ArrayList<>(List.of(Tags.HANDLER, Tags.COMMAND));
                    if (ci.getSimpleName().startsWith("Compensate")) tags.add(Tags.COMPENSATION);
                    add(
                            components,
                            ci.getSimpleName(),
                            "Handles " + humanize(ci.getSimpleName().replace("CommandHandler", "")),
                            Technologies.COMMAND_HANDLER,
                            tags,
                            extractDeps(ci));
                });
    }

    private void scanQueryHandlers(ScanResult result, Map<String, ScannedComponent> components) {
        result.getAllClasses().stream()
                .filter(this::isValid)
                .filter(ci -> ci.getSimpleName().endsWith("QueryHandler"))
                .forEach(ci -> add(
                        components,
                        ci.getSimpleName(),
                        "Handles " + humanize(ci.getSimpleName().replace("QueryHandler", "")) + " queries",
                        Technologies.QUERY_HANDLER,
                        List.of(Tags.HANDLER, Tags.QUERY),
                        extractDeps(ci)));
    }

    private void scanSagas(ScanResult result, Map<String, ScannedComponent> components) {
        result.getAllClasses().stream()
                .filter(this::isValid)
                .filter(ci -> ci.getSimpleName().endsWith("Saga"))
                .filter(ci -> !ci.getSimpleName().endsWith("SagaData"))
                .forEach(ci -> add(
                        components,
                        ci.getSimpleName(),
                        "Orchestrates " + humanize(ci.getSimpleName().replace("Saga", "")),
                        Technologies.SAGA_ORCHESTRATOR,
                        List.of(Tags.SAGA, Tags.ORCHESTRATOR),
                        extractDeps(ci)));
    }

    private void scanAggregatesAndEntities(ScanResult result, Map<String, ScannedComponent> components) {
        var aggregates = Set.of(
                "TradeLoanFacility",
                "TradeLoanArrangement",
                "TradeLoanType",
                "InstallmentSchedule",
                "TradeLoanApplication",
                "TradeSanctionedLoan");

        result.getAllClasses().stream()
                .filter(ci -> ci.getPackageName().contains(".core.domain."))
                .filter(ci -> ci.getPackageName().contains(".entity"))
                .filter(this::isValid)
                .filter(ci -> !ci.getSimpleName().endsWith("Entity"))
                .forEach(ci -> {
                    var isAgg = aggregates.contains(ci.getSimpleName());
                    add(
                            components,
                            ci.getSimpleName(),
                            humanize(ci.getSimpleName()) + (isAgg ? " aggregate" : " entity"),
                            isAgg ? Technologies.DDD_AGGREGATE : Technologies.DDD_ENTITY,
                            List.of(Tags.DOMAIN, isAgg ? Tags.AGGREGATE : Tags.ENTITY),
                            List.of());
                });
    }

    private void scanDomainServices(ScanResult result, Map<String, ScannedComponent> components) {
        result.getAllClasses().stream()
                .filter(ci -> ci.getPackageName().contains(".core.domain."))
                .filter(ci -> ci.getPackageName().contains(".service"))
                .filter(this::isValid)
                .filter(ci -> ci.getSimpleName().endsWith("Service"))
                .forEach(ci -> add(
                        components,
                        ci.getSimpleName(),
                        humanize(ci.getSimpleName()) + " domain service",
                        Technologies.DDD_SERVICE,
                        List.of(Tags.DOMAIN, Tags.SERVICE),
                        extractDeps(ci)));
    }

    private void scanRepositories(ScanResult result, Map<String, ScannedComponent> components) {
        result.getAllClasses().stream()
                .filter(this::isValid)
                .filter(ci -> ci.getSimpleName().endsWith("RepositoryAdapter"))
                .forEach(ci -> {
                    var entity = ci.getSimpleName().replace("RepositoryAdapter", "");
                    add(
                            components,
                            ci.getSimpleName(),
                            entity + " persistence adapter",
                            Technologies.SPRING_DATA_JPA,
                            List.of(Tags.REPOSITORY, Tags.PERSISTENCE),
                            extractDeps(ci));
                });

        result.getAllClasses().stream()
                .filter(this::isValid)
                .filter(ci -> ci.getPackageName().contains("adapters.driven.persistence"))
                .filter(ci -> ci.getSimpleName().startsWith("Jpa")
                        && ci.getSimpleName().endsWith("QueryAdapter"))
                .forEach(ci -> {
                    var entity = ci.getSimpleName().replace("Jpa", "").replace("QueryAdapter", "");
                    add(
                            components,
                            ci.getSimpleName(),
                            entity + " query adapter",
                            Technologies.SPRING_DATA_JPA,
                            List.of(Tags.REPOSITORY, Tags.QUERY),
                            extractDeps(ci));
                });
    }

    private void scanExternalClients(ScanResult result, Map<String, ScannedComponent> components) {
        result.getAllClasses().stream()
                .filter(this::isValid)
                .filter(ci -> ci.getPackageName().contains("adapters.driven"))
                .filter(ci -> !ci.getPackageName().contains("persistence"))
                .filter(ci -> ci.getSimpleName().endsWith("Adapter")
                        || ci.getSimpleName().endsWith("ServiceImpl"))
                .filter(ci -> !ci.getSimpleName().toLowerCase().contains("noop"))
                .filter(ci -> !ci.getSimpleName().contains("Repository"))
                .forEach(ci -> {
                    var name = ci.getSimpleName().replace("Adapter", "").replace("ServiceImpl", "");
                    add(
                            components,
                            ci.getSimpleName(),
                            humanize(name) + " client",
                            Technologies.SPRING_WEBCLIENT,
                            List.of(Tags.CLIENT, Tags.EXTERNAL),
                            extractDeps(ci));
                });

        result.getAllClasses().stream()
                .filter(this::isValid)
                .filter(ci -> ci.getPackageName().contains("adapters.driven.fcbclient.service"))
                .filter(ci -> ci.getSimpleName().endsWith("Adapter")
                        || ci.getSimpleName().endsWith("ServiceImpl"))
                .forEach(ci -> {
                    if (components.containsKey(ci.getSimpleName())) return;
                    var name = ci.getSimpleName().replace("Adapter", "").replace("ServiceImpl", "");
                    add(
                            components,
                            ci.getSimpleName(),
                            humanize(name) + " client",
                            Technologies.SPRING_WEBCLIENT,
                            List.of(Tags.CLIENT, Tags.EXTERNAL),
                            extractDeps(ci));
                });
    }

    private void scanOutboxHandlers(ScanResult result, Map<String, ScannedComponent> components) {
        result.getAllClasses().stream()
                .filter(this::isValid)
                .filter(ci -> ci.getSimpleName().endsWith("OutboxHandler"))
                .forEach(ci -> {
                    var entity = ci.getSimpleName().replace("OutboxHandler", "");
                    add(
                            components,
                            ci.getSimpleName(),
                            entity + " outbox handler",
                            Technologies.OUTBOX_PATTERN,
                            List.of(Tags.OUTBOX, Tags.MESSAGING),
                            extractDeps(ci));
                });
    }

    private void add(
            Map<String, ScannedComponent> m,
            String name,
            String desc,
            String tech,
            List<String> tags,
            List<String> deps) {
        if (!m.containsKey(name)) m.put(name, new ScannedComponent(name, desc, tech, tags, deps));
    }

    private boolean isValid(ClassInfo ci) {
        if (ci.isInterface() || ci.isAbstract() || ci.isInnerClass()) return false;
        var n = ci.getSimpleName();
        if (n == null || n.isBlank()) return false;
        for (var s : EXCLUDED_SUFFIXES) if (n.endsWith(s) && !n.equals(s)) return false;
        return !n.contains("$") && !n.contains("_");
    }

    private String extractEndpoint(ClassInfo ci) {
        try {
            var ann = ci.getAnnotationInfo("org.springframework.web.bind.annotation.RequestMapping");
            if (ann != null) {
                var v = ann.getParameterValues().get("value");
                if (v != null) {
                    var arr = (Object[]) v.getValue();
                    if (arr != null && arr.length > 0) return "REST API: " + arr[0];
                }
            }
        } catch (Exception ignored) {
        }
        return "REST API endpoint";
    }

    private List<String> extractDeps(ClassInfo ci) {
        try {
            return ci.getFieldInfo().stream()
                    .map(FieldInfo::getTypeDescriptor)
                    .filter(Objects::nonNull)
                    .map(TypeSignature::toString)
                    .filter(t -> t.startsWith(basePackage))
                    .map(t -> t.substring(t.lastIndexOf('.') + 1))
                    .filter(n -> !n.isBlank())
                    .distinct()
                    .toList();
        } catch (Exception e) {
            return List.of();
        }
    }

    private String humanize(String c) {
        return c == null ? "" : c.replaceAll("([A-Z])", " $1").trim().toLowerCase();
    }
}
