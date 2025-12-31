package ir.dotin.loan.trade.architecture.config;

import java.util.List;
import java.util.Map;

import ir.dotin.loan.trade.architecture.config.ArchitectureConfig.*;
import ir.dotin.loan.trade.architecture.config.ArchitectureConstants.*;

public final class DefaultConfig {
    private DefaultConfig() {}

    public static ArchitectureConfig load() {
        return new ArchitectureConfig(workspace(), persons(), externalSystems(), mainSystem(), containers(), styles());
    }

    private static WorkspaceConfig workspace() {
        return new WorkspaceConfig(
                Systems.TRADE_LOAN_SERVICE, "C4 Architecture for Morabehe Loans Microservice", Packages.BASE);
    }

    private static List<PersonConfig> persons() {
        return List.of(
                new PersonConfig(
                        Persons.LOAN_OFFICER,
                        "Bank staff managing loans",
                        List.of(Tags.INTERNAL_USER),
                        List.of(new UsesConfig(
                                Containers.TRADE_LOAN_APPLICATION,
                                "Creates and manages loans",
                                Technologies.HTTPS_JWT))),
                new PersonConfig(
                        Persons.BRANCH_MANAGER,
                        "Branch level approval authority",
                        List.of(Tags.INTERNAL_USER),
                        List.of(new UsesConfig(
                                Containers.TRADE_LOAN_APPLICATION, "Approves facilities", Technologies.HTTPS_JWT))),
                new PersonConfig(
                        Persons.SYSTEM_ADMINISTRATOR,
                        "Technical staff",
                        List.of(Tags.INTERNAL_USER, Tags.ADMIN),
                        List.of(
                                new UsesConfig(
                                        Containers.TRADE_LOAN_APPLICATION, "Monitors system", Technologies.HTTPS_JWT),
                                new UsesConfig(Containers.KAFDROP, "Monitors Kafka", Technologies.HTTP))),
                new PersonConfig(Persons.CUSTOMER, "End users", List.of(Tags.EXTERNAL_USER), List.of()));
    }

    private static List<ExternalSystemConfig> externalSystems() {
        return List.of(
                new ExternalSystemConfig(
                        Systems.TPS_SSO, "OAuth2/OIDC provider", List.of(Tags.EXTERNAL_SYSTEM, Tags.SECURITY)),
                new ExternalSystemConfig(
                        Systems.FCB_CORE_BANKING, "Core Banking", List.of(Tags.EXTERNAL_SYSTEM, Tags.CORE_BANKING)),
                new ExternalSystemConfig(
                        Systems.OTEL_COLLECTOR,
                        "Observability backend",
                        List.of(Tags.EXTERNAL_SYSTEM, Tags.OBSERVABILITY)),
                new ExternalSystemConfig(
                        Systems.ACCOUNT_SERVICE,
                        "Account management",
                        List.of(Tags.EXTERNAL_SYSTEM, Tags.DOMAIN_SERVICE)),
                new ExternalSystemConfig(
                        Systems.CUSTOMER_SERVICE, "Customer data", List.of(Tags.EXTERNAL_SYSTEM, Tags.DOMAIN_SERVICE)),
                new ExternalSystemConfig(
                        Systems.DEPOSIT_SERVICE,
                        "Deposit operations",
                        List.of(Tags.EXTERNAL_SYSTEM, Tags.DOMAIN_SERVICE)),
                new ExternalSystemConfig(
                        Systems.COLLATERAL_SERVICE,
                        "Collateral management",
                        List.of(Tags.EXTERNAL_SYSTEM, Tags.DOMAIN_SERVICE)),
                new ExternalSystemConfig(
                        Systems.LOAN_SERVICE,
                        "Cross-loan operations",
                        List.of(Tags.EXTERNAL_SYSTEM, Tags.DOMAIN_SERVICE)),
                new ExternalSystemConfig(
                        Systems.FORMULA_EVALUATOR_SERVICE,
                        "Formula calculation",
                        List.of(Tags.EXTERNAL_SYSTEM, Tags.DOMAIN_SERVICE)));
    }

    private static SystemConfig mainSystem() {
        return new SystemConfig(
                Systems.TRADE_LOAN_SERVICE,
                "Morabehe loans with CQRS and Event Sourcing",
                List.of(Tags.INTERNAL_SYSTEM));
    }

    private static List<ContainerConfig> containers() {
        return List.of(
                new ContainerConfig(
                        Containers.TRADE_LOAN_APPLICATION,
                        "Spring Boot Hexagonal Architecture",
                        Technologies.JAVA_SPRING,
                        List.of(Tags.APPLICATION, Tags.SPRING_BOOT),
                        List.of(
                                new UsesConfig(Containers.POSTGRESQL_DATABASE, "Reads/Writes", Technologies.JDBC),
                                new UsesConfig(Containers.KAFKA, "Publishes/Consumes", Technologies.KAFKA_PROTOCOL),
                                new UsesConfig(Containers.REDIS, "Caches data", Technologies.REDIS_PROTOCOL),
                                new UsesConfig(Systems.TPS_SSO, "Authenticates", Technologies.OAUTH2_OIDC),
                                new UsesConfig(Systems.FCB_CORE_BANKING, "Core banking", Technologies.HTTP_REST),
                                new UsesConfig(Systems.OTEL_COLLECTOR, "Telemetry", Technologies.OTLP_GRPC),
                                new UsesConfig(Systems.ACCOUNT_SERVICE, "Calls", Technologies.HTTP_REST),
                                new UsesConfig(Systems.CUSTOMER_SERVICE, "Calls", Technologies.HTTP_REST),
                                new UsesConfig(Systems.DEPOSIT_SERVICE, "Calls", Technologies.HTTP_REST),
                                new UsesConfig(Systems.COLLATERAL_SERVICE, "Calls", Technologies.HTTP_REST),
                                new UsesConfig(Systems.LOAN_SERVICE, "Calls", Technologies.HTTP_REST),
                                new UsesConfig(Systems.FORMULA_EVALUATOR_SERVICE, "Calls", Technologies.HTTP_REST))),
                new ContainerConfig(
                        Containers.POSTGRESQL_DATABASE,
                        "Loan data storage",
                        Technologies.POSTGRESQL,
                        List.of(Tags.DATABASE, Tags.STORAGE),
                        List.of()),
                new ContainerConfig(
                        Containers.KAFKA,
                        "Event streaming",
                        Technologies.KAFKA,
                        List.of(Tags.MESSAGE_BROKER, Tags.MESSAGING),
                        List.of()),
                new ContainerConfig(
                        Containers.REDIS,
                        "Caching and locks",
                        Technologies.REDIS,
                        List.of(Tags.CACHE, Tags.STORAGE),
                        List.of()),
                new ContainerConfig(
                        Containers.KAFDROP,
                        "Kafka monitoring",
                        Technologies.KAFDROP,
                        List.of(Tags.MONITORING, Tags.TOOL),
                        List.of(new UsesConfig(Containers.KAFKA, "Monitors", Technologies.KAFKA_PROTOCOL))));
    }

    private static StyleConfig styles() {
        return new StyleConfig(
                Map.ofEntries(
                        Map.entry("Person", new ElementStyleConfig("#08427b", "#ffffff", "Person", null)),
                        Map.entry(Tags.INTERNAL_USER, new ElementStyleConfig("#08427b", null, null, null)),
                        Map.entry(Tags.EXTERNAL_USER, new ElementStyleConfig("#666666", null, null, null)),
                        Map.entry(Tags.ADMIN, new ElementStyleConfig("#5c3d6e", null, null, null)),
                        Map.entry("Software System", new ElementStyleConfig("#1168bd", "#ffffff", null, null)),
                        Map.entry(Tags.EXTERNAL_SYSTEM, new ElementStyleConfig("#999999", null, null, "Dashed")),
                        Map.entry("Container", new ElementStyleConfig("#438dd5", "#ffffff", null, null)),
                        Map.entry(Tags.DATABASE, new ElementStyleConfig(null, null, "Cylinder", null)),
                        Map.entry(Tags.MESSAGE_BROKER, new ElementStyleConfig("#f5a623", "#000000", "Pipe", null)),
                        Map.entry(Tags.CACHE, new ElementStyleConfig("#e74c3c", null, "Cylinder", null)),
                        Map.entry(Tags.MONITORING, new ElementStyleConfig("#27ae60", null, "WebBrowser", null)),
                        Map.entry("Component", new ElementStyleConfig("#85bbf0", "#000000", null, null)),
                        Map.entry(Tags.CONTROLLER, new ElementStyleConfig("#7CB342", "#ffffff", null, null)),
                        Map.entry(Tags.CONSUMER, new ElementStyleConfig("#FF7043", "#ffffff", "Hexagon", null)),
                        Map.entry(Tags.HANDLER, new ElementStyleConfig("#42A5F5", "#ffffff", null, null)),
                        Map.entry(Tags.SAGA, new ElementStyleConfig("#AB47BC", "#ffffff", "Diamond", null)),
                        Map.entry(Tags.COMPENSATION, new ElementStyleConfig("#EF5350", "#ffffff", null, null)),
                        Map.entry(Tags.DOMAIN, new ElementStyleConfig("#FFA726", "#000000", null, null)),
                        Map.entry(Tags.AGGREGATE, new ElementStyleConfig("#FF9800", "#000000", null, null)),
                        Map.entry(Tags.ENTITY, new ElementStyleConfig("#FFB74D", "#000000", null, null)),
                        Map.entry(Tags.REPOSITORY, new ElementStyleConfig("#5C6BC0", "#ffffff", "Cylinder", null)),
                        Map.entry(Tags.CLIENT, new ElementStyleConfig("#EC407A", "#ffffff", null, null)),
                        Map.entry(Tags.OUTBOX, new ElementStyleConfig("#66BB6A", "#000000", "Hexagon", null)),
                        Map.entry(Tags.SERVICE, new ElementStyleConfig("#29B6F6", "#ffffff", null, null))),
                Map.of(
                        Tags.RELATIONSHIP,
                        new RelationshipStyleConfig("#707070", 2, null),
                        Tags.ASYNCHRONOUS,
                        new RelationshipStyleConfig("#ff6600", 2, "Dashed")));
    }
}
