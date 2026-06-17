package ir.dotin.loan.trade.architecture.config;

import java.util.List;
import java.util.Map;

import ir.dotin.loan.trade.architecture.config.ArchitectureConfig.*;
import ir.dotin.loan.trade.architecture.config.ArchitectureConstants.*;

public final class DefaultConfig {
    private DefaultConfig() {}

    public static ArchitectureConfig load() {
        return new ArchitectureConfig(
                workspace(), persons(), externalSystems(), mainSystem(), containers(), styles(), workflows());
    }

    private static WorkspaceConfig workspace() {
        return new WorkspaceConfig(
                Systems.TRADE_LOAN_SERVICE, "C4 architecture for the trade-loan microservice", Packages.BASE);
    }

    /**
     * Exactly two actors interact with the service directly:
     *
     * <ul>
     *   <li>the Operator/Admin, through the OPS endpoints and MCP ops tools; and
     *   <li>the ESB, which drives all business loan-lifecycle traffic (modelled as an external system).
     * </ul>
     */
    private static List<PersonConfig> persons() {
        return List.of(new PersonConfig(
                Persons.OPERATOR,
                "Bank operations / administrator — monitors and remediates the service",
                List.of(Tags.ADMIN),
                List.of(new UsesConfig(
                        Containers.TRADE_LOAN_APPLICATION,
                        "Operates via OPS endpoints (/v1/ops/*) and MCP ops tools",
                        Technologies.HTTPS_JWT))));
    }

    private static List<ExternalSystemConfig> externalSystems() {
        return List.of(
                new ExternalSystemConfig(
                        Systems.ESB,
                        "Enterprise Service Bus — routes business loan-lifecycle requests from upstream channels",
                        List.of(Tags.EXTERNAL_SYSTEM),
                        List.of(new UsesConfig(
                                Containers.TRADE_LOAN_APPLICATION,
                                "Business loan-lifecycle requests",
                                Technologies.REST_SWA101))),
                new ExternalSystemConfig(
                        Systems.TPS_SSO,
                        "OAuth2/OIDC identity provider (token validation + JWKS)",
                        List.of(Tags.EXTERNAL_SYSTEM, Tags.SECURITY),
                        List.of()),
                new ExternalSystemConfig(
                        Systems.FCB_CORE_BANKING,
                        "Legacy core banking — accounts, postings, collateral, sanctions",
                        List.of(Tags.EXTERNAL_SYSTEM, Tags.CORE_BANKING),
                        List.of()),
                new ExternalSystemConfig(
                        Systems.OTEL_COLLECTOR,
                        "OpenTelemetry collector — traces, metrics, logs",
                        List.of(Tags.EXTERNAL_SYSTEM, Tags.OBSERVABILITY),
                        List.of()),
                new ExternalSystemConfig(
                        Systems.CONSUL,
                        "Runtime configuration store (Consul KV, fed by GitOps)",
                        List.of(Tags.EXTERNAL_SYSTEM, Tags.CONFIG),
                        List.of()));
    }

    private static SystemConfig mainSystem() {
        return new SystemConfig(
                Systems.TRADE_LOAN_SERVICE,
                "trade-loan microservice — hexagonal, DDD, CQRS, workflow orchestration",
                List.of(Tags.INTERNAL_SYSTEM));
    }

    private static List<ContainerConfig> containers() {
        return List.of(
                new ContainerConfig(
                        Containers.TRADE_LOAN_APPLICATION,
                        "Spring Boot hexagonal application (driving + driven adapters)",
                        Technologies.JAVA_SPRING,
                        List.of(Tags.APPLICATION, Tags.SPRING_BOOT),
                        List.of(
                                new UsesConfig(
                                        Containers.POSTGRESQL_DATABASE,
                                        "Aggregates, inbox/outbox, workflow, audit, reconciliation",
                                        Technologies.JDBC),
                                new UsesConfig(
                                        Containers.REDIS,
                                        "Query cache, idempotency, JWKS/token cache",
                                        Technologies.REDIS_PROTOCOL),
                                new UsesConfig(
                                        Containers.KAFKA,
                                        "Domain events (outbox) + request/reply fallback",
                                        Technologies.KAFKA_PROTOCOL),
                                new UsesConfig(
                                        Containers.ARTEMIS,
                                        "FCB request/reply corridor (primary)",
                                        Technologies.ARTEMIS_JMS),
                                new UsesConfig(Systems.CONSUL, "Loads runtime config", Technologies.CONSUL_KV),
                                new UsesConfig(
                                        Systems.TPS_SSO, "Validates JWT / fetches JWKS", Technologies.OAUTH2_OIDC),
                                new UsesConfig(
                                        Systems.FCB_CORE_BANKING,
                                        "Core banking operations via corridor",
                                        Technologies.CORRIDOR),
                                new UsesConfig(
                                        Systems.OTEL_COLLECTOR,
                                        "Exports traces / metrics / logs",
                                        Technologies.OTLP_HTTP))),
                new ContainerConfig(
                        Containers.POSTGRESQL_DATABASE,
                        "Loan data, inbox/outbox, workflow state, audit, reconciliation",
                        Technologies.POSTGRESQL,
                        List.of(Tags.DATABASE, Tags.STORAGE),
                        List.of()),
                new ContainerConfig(
                        Containers.KAFKA,
                        "Event streaming + request/reply fallback transport",
                        Technologies.KAFKA,
                        List.of(Tags.MESSAGE_BROKER, Tags.MESSAGING),
                        List.of()),
                new ContainerConfig(
                        Containers.REDIS,
                        "Cache + idempotency store (Sentinel HA)",
                        Technologies.REDIS,
                        List.of(Tags.CACHE, Tags.STORAGE),
                        List.of()),
                new ContainerConfig(
                        Containers.ARTEMIS,
                        "Primary FCB request/reply corridor broker",
                        Technologies.ARTEMIS,
                        List.of(Tags.MESSAGE_BROKER, Tags.MESSAGING),
                        List.of()));
    }

    /**
     * Curated durable workflows (saga orchestration). Orchestrator + compensation handlers are discovered
     * automatically; the ordered steps below are added explicitly because the step classes share simple names
     * across use cases (so bytecode auto-discovery would collide). Mirrors the pangaea workflow-api definitions
     * in {@code core/application/service/<usecase>}.
     */
    private static List<WorkflowConfig> workflows() {
        return List.of(
                new WorkflowConfig(
                        "Issue Facility Contract",
                        "Issue Contract",
                        "IssueContractWorkflow",
                        "IssueFacilityContractCommandHandler",
                        "CompensateContractIssuanceCommandHandler",
                        true,
                        List.of(
                                new WorkflowStepConfig(
                                        "Validate Facility",
                                        "Read-side validation that the facility can issue a contract",
                                        StepKind.READ,
                                        false),
                                new WorkflowStepConfig(
                                        "Open Accounts",
                                        "Resolve/open FCB loan accounts per relation type",
                                        StepKind.REMOTE,
                                        true),
                                new WorkflowStepConfig(
                                        "Post Transaction",
                                        "Post the contract-issuance transaction to FCB",
                                        StepKind.REMOTE,
                                        true),
                                new WorkflowStepConfig(
                                        "Update Facility State",
                                        "Apply issueContract to the aggregate, persist + publish events",
                                        StepKind.PUBLISH,
                                        true))),
                new WorkflowConfig(
                        "Lump-Sum Disbursement",
                        "Lump-Sum",
                        "LumpSumDisbursementWorkflow",
                        "LumpSumDisbursementCommandHandler",
                        "CompensateLumpSumDisbursementCommandHandler",
                        true,
                        List.of(
                                new WorkflowStepConfig(
                                        "Validate Facility",
                                        "Validate method == LUMP_SUM and disbursement date",
                                        StepKind.READ,
                                        false),
                                new WorkflowStepConfig(
                                        "Resolve Accounts",
                                        "Resolve/open FCB accounts for the disbursement",
                                        StepKind.REMOTE,
                                        true),
                                new WorkflowStepConfig(
                                        "Post Transactions",
                                        "Post the disbursement transactions to FCB",
                                        StepKind.REMOTE,
                                        true),
                                new WorkflowStepConfig(
                                        "Apply Disbursement",
                                        "Activate schedule + apply lump-sum disbursement, persist + publish",
                                        StepKind.PUBLISH,
                                        true))),
                new WorkflowConfig(
                        "Irregular-Progressive Disbursement",
                        "Irregular Disb.",
                        "IrregularDisbursementWorkflow",
                        "IrregularProgressiveDisbursementCommandHandler",
                        "CompensateIrregularDisbursementCommandHandler",
                        true,
                        List.of(
                                new WorkflowStepConfig(
                                        "Validate Facility",
                                        "Validate method == IRREGULAR_PROGRESSIVE",
                                        StepKind.READ,
                                        false),
                                new WorkflowStepConfig(
                                        "Resolve Accounts",
                                        "Resolve/open FCB accounts for the disbursement",
                                        StepKind.REMOTE,
                                        true),
                                new WorkflowStepConfig(
                                        "Post Transactions",
                                        "Post the disbursement transactions to FCB",
                                        StepKind.REMOTE,
                                        true),
                                new WorkflowStepConfig(
                                        "Apply Disbursement",
                                        "Apply irregular-progressive disbursement, persist + publish",
                                        StepKind.PUBLISH,
                                        true))),
                new WorkflowConfig(
                        "Regular Disbursement (single-write)",
                        "Regular Disb.",
                        "RegularDisbursementWorkflow",
                        "RegularDisbursementCommandHandler",
                        null,
                        false,
                        List.of(new WorkflowStepConfig(
                                "Apply Regular Disbursement",
                                "Single atomic write (ephemeral workflow, no durable run row)",
                                StepKind.PUBLISH,
                                false))));
    }

    private static StyleConfig styles() {
        return new StyleConfig(
                Map.ofEntries(
                        Map.entry("Person", new ElementStyleConfig("#08427b", "#ffffff", "Person", null)),
                        Map.entry(Tags.ADMIN, new ElementStyleConfig("#5c3d6e", "#ffffff", "Person", null)),
                        Map.entry("Software System", new ElementStyleConfig("#1168bd", "#ffffff", null, null)),
                        Map.entry(Tags.EXTERNAL_SYSTEM, new ElementStyleConfig("#999999", "#ffffff", null, "Dashed")),
                        Map.entry(Tags.CORE_BANKING, new ElementStyleConfig("#8d6e63", "#ffffff", null, null)),
                        Map.entry(Tags.SECURITY, new ElementStyleConfig("#b71c1c", "#ffffff", null, null)),
                        Map.entry(Tags.OBSERVABILITY, new ElementStyleConfig("#00897b", "#ffffff", null, null)),
                        Map.entry(Tags.CONFIG, new ElementStyleConfig("#6d4c41", "#ffffff", null, null)),
                        Map.entry("Container", new ElementStyleConfig("#438dd5", "#ffffff", null, null)),
                        Map.entry(Tags.DATABASE, new ElementStyleConfig(null, null, "Cylinder", null)),
                        Map.entry(Tags.MESSAGE_BROKER, new ElementStyleConfig("#f5a623", "#000000", "Pipe", null)),
                        Map.entry(Tags.CACHE, new ElementStyleConfig("#e74c3c", "#ffffff", "Cylinder", null)),
                        Map.entry("Component", new ElementStyleConfig("#85bbf0", "#000000", null, null)),
                        Map.entry(Tags.CONTROLLER, new ElementStyleConfig("#7CB342", "#ffffff", null, null)),
                        Map.entry(Tags.CONSUMER, new ElementStyleConfig("#FF7043", "#ffffff", "Hexagon", null)),
                        Map.entry(Tags.HANDLER, new ElementStyleConfig("#42A5F5", "#ffffff", null, null)),
                        Map.entry(Tags.WORKFLOW, new ElementStyleConfig("#AB47BC", "#ffffff", "Diamond", null)),
                        Map.entry(Tags.COMPENSATION, new ElementStyleConfig("#EF5350", "#ffffff", null, null)),
                        Map.entry(Tags.MCP, new ElementStyleConfig("#26A69A", "#ffffff", "Robot", null)),
                        Map.entry(Tags.RECONCILIATION, new ElementStyleConfig("#9575CD", "#ffffff", null, null)),
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
