package ir.dotin.loan.trade.architecture.config;

public final class ArchitectureConstants {
    private ArchitectureConstants() {}

    public static final class Systems {
        public static final String TRADE_LOAN_SERVICE = "Trade Loan Service";
        public static final String ESB = "ESB";
        public static final String TPS_SSO = "TPS SSO";
        public static final String FCB_CORE_BANKING = "FCB Core Banking";
        public static final String OTEL_COLLECTOR = "OpenTelemetry Collector";
        public static final String CONSUL = "Consul";

        private Systems() {}
    }

    public static final class Containers {
        public static final String TRADE_LOAN_APPLICATION = "Trade Loan Application";
        public static final String POSTGRESQL_DATABASE = "PostgreSQL Database";
        public static final String KAFKA = "Kafka";
        public static final String REDIS = "Redis";
        public static final String ARTEMIS = "ActiveMQ Artemis";

        private Containers() {}
    }

    public static final class Tags {
        public static final String CONTROLLER = "Controller";
        public static final String CONSUMER = "Consumer";
        public static final String HANDLER = "Handler";
        public static final String WORKFLOW = "Workflow";
        public static final String COMPENSATION = "Compensation";
        public static final String REPOSITORY = "Repository";
        public static final String CLIENT = "Client";
        public static final String OUTBOX = "Outbox";
        public static final String MCP = "MCP";
        public static final String RECONCILIATION = "Reconciliation";
        public static final String SERVICE = "Service";
        public static final String DOMAIN = "Domain";
        public static final String AGGREGATE = "Aggregate";
        public static final String ENTITY = "Entity";
        public static final String COMMAND = "Command";
        public static final String QUERY = "Query";
        public static final String PERSISTENCE = "Persistence";
        public static final String MESSAGING = "Messaging";
        public static final String EXTERNAL = "External";
        public static final String ADMIN = "Admin";
        public static final String INTERNAL_SYSTEM = "Internal System";
        public static final String EXTERNAL_SYSTEM = "External System";
        public static final String SECURITY = "Security";
        public static final String CORE_BANKING = "Core Banking";
        public static final String OBSERVABILITY = "Observability";
        public static final String CONFIG = "Config";
        public static final String APPLICATION = "Application";
        public static final String SPRING_BOOT = "Spring Boot";
        public static final String DATABASE = "Database";
        public static final String STORAGE = "Storage";
        public static final String MESSAGE_BROKER = "Message Broker";
        public static final String CACHE = "Cache";
        public static final String RELATIONSHIP = "Relationship";
        public static final String ASYNCHRONOUS = "Asynchronous";

        private Tags() {}
    }

    public static final class Technologies {
        public static final String JAVA_SPRING = "Java 25, Spring Boot 4";
        public static final String POSTGRESQL = "PostgreSQL 18";
        public static final String KAFKA = "Apache Kafka (SASL_PLAINTEXT/SCRAM-SHA-256)";
        public static final String REDIS = "Redis 8.6 (Sentinel HA: 1 master / 2 replicas / 3 sentinels)";
        public static final String ARTEMIS = "ActiveMQ Artemis 2.43 (Jakarta JMS)";
        public static final String JDBC = "JDBC/HikariCP";
        public static final String KAFKA_PROTOCOL = "Kafka (SASL/SCRAM)";
        public static final String REDIS_PROTOCOL = "RESP3 (Sentinel)";
        public static final String ARTEMIS_JMS = "Jakarta JMS";
        public static final String OAUTH2_OIDC = "OAuth2/OIDC + JWKS";
        public static final String CORRIDOR = "Corridor (Artemis primary / Kafka fallback)";
        public static final String CONSUL_KV = "Consul KV (watch / @RefreshScope)";
        public static final String OTLP_HTTP = "OTLP http/protobuf";
        public static final String REST_SWA101 = "REST (SWA-101) / JWT";
        public static final String HTTPS_JWT = "HTTPS / JWT (sub-claim allowlist)";
        public static final String SPRING_REST = "Spring MVC Controller";
        public static final String SPRING_KAFKA = "Spring Kafka Listener";
        public static final String SPRING_DATA_JPA = "Spring Data JPA";
        public static final String MCP_TOOL = "MCP Tool";
        public static final String FCB_CLIENT = "FCB corridor client";
        public static final String RECON_COMPONENT = "Reconciliation engine";
        public static final String OUTBOX_PATTERN = "Outbox Publisher";
        public static final String COMMAND_HANDLER = "Command Handler";
        public static final String QUERY_HANDLER = "Query Handler";
        public static final String WORKFLOW_ORCHESTRATOR = "Workflow orchestration";
        public static final String WF_READ_STEP = "Workflow read step";
        public static final String WF_REMOTE_STEP = "Workflow remote step (FCB corridor)";
        public static final String WF_WRITE_STEP = "Workflow write step";
        public static final String WF_PUBLISH_STEP = "Workflow publishing-write step";
        public static final String WF_COMPENSATION_STEP = "Workflow compensation step";
        public static final String WF_DURABLE_STATE = "Durable workflow state (workflow_run, workflow_compensation)";
        public static final String DDD_AGGREGATE = "DDD Aggregate Root";
        public static final String DDD_ENTITY = "DDD Entity";
        public static final String DDD_SERVICE = "Domain Service";

        private Technologies() {}
    }

    public static final class Packages {
        public static final String BASE = "ir.dotin.loan.trade";

        private Packages() {}
    }

    public static final class Views {
        public static final String SYSTEM_CONTEXT = "SystemContext";
        public static final String CONTAINERS = "Containers";
        public static final String COMPONENTS = "Components";
        public static final String CONTROLLERS = "Controllers";
        public static final String HANDLERS = "Handlers";
        public static final String WORKFLOW = "Workflow";
        public static final String DOMAIN_MODEL = "DomainModel";
        public static final String REPOSITORIES = "Repositories";
        public static final String EXTERNAL_CLIENTS = "ExternalClients";
        public static final String MESSAGING = "Messaging";
        public static final String MCP = "Mcp";
        public static final String RECONCILIATION = "Reconciliation";
        public static final String COMMAND_FLOW = "CommandFlow";
        public static final String QUERY_FLOW = "QueryFlow";
        public static final String WORKFLOW_FLOW = "WorkflowFlow";

        private Views() {}
    }

    public static final class Persons {
        public static final String OPERATOR = "Operator / Admin";

        private Persons() {}
    }

    /**
     * Maps an outbound driven-adapter component (FCB corridor client) to the external system it talks to. Every FCB /
     * account / customer / deposit / collateral / loan / transaction client reaches the legacy core through the single
     * corridor, so they all resolve to FCB_CORE_BANKING. There are no longer any standalone HTTP/REST "domain
     * services".
     */
    public static String inferExternalSystemFromComponentName(String name) {
        if (name == null) return null;
        String lower = name.toLowerCase();
        if (lower.contains("fcb")
                || lower.contains("transaction")
                || lower.contains("account")
                || lower.contains("customer")
                || lower.contains("deposit")
                || lower.contains("collateral")
                || lower.contains("sanction")
                || lower.contains("loan")) {
            return Systems.FCB_CORE_BANKING;
        }
        return null;
    }
}
