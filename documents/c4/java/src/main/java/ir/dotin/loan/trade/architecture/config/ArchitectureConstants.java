package ir.dotin.loan.trade.architecture.config;

public final class ArchitectureConstants {
    private ArchitectureConstants() {}

    public static final class Systems {
        public static final String TRADE_LOAN_SERVICE = "Trade Loan Service";
        public static final String TPS_SSO = "TPS SSO";
        public static final String FCB_CORE_BANKING = "FCB Core Banking";
        public static final String OTEL_COLLECTOR = "OpenTelemetry Collector";
        public static final String ACCOUNT_SERVICE = "Account Service";
        public static final String CUSTOMER_SERVICE = "Customer Service";
        public static final String DEPOSIT_SERVICE = "Deposit Service";
        public static final String COLLATERAL_SERVICE = "Collateral Service";
        public static final String LOAN_SERVICE = "Loan Service";
        public static final String FORMULA_EVALUATOR_SERVICE = "Formula Evaluator Service";

        private Systems() {}
    }

    public static final class Containers {
        public static final String TRADE_LOAN_APPLICATION = "Trade Loan Application";
        public static final String POSTGRESQL_DATABASE = "PostgreSQL Database";
        public static final String KAFKA = "Kafka";
        public static final String REDIS = "Redis";
        public static final String KAFDROP = "Kafdrop";

        private Containers() {}
    }

    public static final class Tags {
        public static final String CONTROLLER = "Controller";
        public static final String CONSUMER = "Consumer";
        public static final String HANDLER = "Handler";
        public static final String SAGA = "Saga";
        public static final String ORCHESTRATOR = "Orchestrator";
        public static final String COMPENSATION = "Compensation";
        public static final String REPOSITORY = "Repository";
        public static final String CLIENT = "Client";
        public static final String OUTBOX = "Outbox";
        public static final String SERVICE = "Service";
        public static final String DOMAIN = "Domain";
        public static final String AGGREGATE = "Aggregate";
        public static final String ENTITY = "Entity";
        public static final String COMMAND = "Command";
        public static final String QUERY = "Query";
        public static final String PERSISTENCE = "Persistence";
        public static final String MESSAGING = "Messaging";
        public static final String EXTERNAL = "External";
        public static final String INTERNAL_USER = "Internal User";
        public static final String EXTERNAL_USER = "External User";
        public static final String ADMIN = "Admin";
        public static final String INTERNAL_SYSTEM = "Internal System";
        public static final String EXTERNAL_SYSTEM = "External System";
        public static final String SECURITY = "Security";
        public static final String CORE_BANKING = "Core Banking";
        public static final String OBSERVABILITY = "Observability";
        public static final String DOMAIN_SERVICE = "Domain Service";
        public static final String APPLICATION = "Application";
        public static final String SPRING_BOOT = "Spring Boot";
        public static final String DATABASE = "Database";
        public static final String STORAGE = "Storage";
        public static final String MESSAGE_BROKER = "Message Broker";
        public static final String CACHE = "Cache";
        public static final String MONITORING = "Monitoring";
        public static final String TOOL = "Tool";
        public static final String RELATIONSHIP = "Relationship";
        public static final String ASYNCHRONOUS = "Asynchronous";

        private Tags() {}
    }

    public static final class Technologies {
        public static final String JAVA_SPRING = "Java 25, Spring Boot 4";
        public static final String POSTGRESQL = "PostgreSQL 18";
        public static final String KAFKA = "Confluent Kafka 7.9";
        public static final String REDIS = "Redis 8.2";
        public static final String KAFDROP = "Kafdrop 4.2";
        public static final String JDBC = "JDBC/HikariCP";
        public static final String KAFKA_PROTOCOL = "Kafka Protocol";
        public static final String REDIS_PROTOCOL = "Redis Protocol";
        public static final String OAUTH2_OIDC = "OAuth2/OIDC";
        public static final String HTTP_REST = "HTTP/REST";
        public static final String OTLP_GRPC = "OTLP/gRPC";
        public static final String HTTPS_JWT = "HTTPS/JWT";
        public static final String HTTP = "HTTP";
        public static final String SPRING_REST = "Spring REST Controller";
        public static final String SPRING_KAFKA = "Spring Kafka Listener";
        public static final String SPRING_DATA_JPA = "Spring Data JPA";
        public static final String SPRING_WEBCLIENT = "Spring WebClient";
        public static final String OUTBOX_PATTERN = "Outbox Pattern";
        public static final String COMMAND_HANDLER = "Command Handler";
        public static final String QUERY_HANDLER = "Query Handler";
        public static final String SAGA_ORCHESTRATOR = "Saga Orchestrator";
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
        public static final String SAGAS = "Sagas";
        public static final String DOMAIN_MODEL = "DomainModel";
        public static final String REPOSITORIES = "Repositories";
        public static final String EXTERNAL_CLIENTS = "ExternalClients";
        public static final String MESSAGING = "Messaging";
        public static final String COMMAND_FLOW = "CommandFlow";
        public static final String QUERY_FLOW = "QueryFlow";
        public static final String SAGA_FLOW = "SagaFlow";

        private Views() {}
    }

    public static final class Persons {
        public static final String LOAN_OFFICER = "Loan Officer";
        public static final String BRANCH_MANAGER = "Branch Manager";
        public static final String SYSTEM_ADMINISTRATOR = "System Administrator";
        public static final String CUSTOMER = "Customer";

        private Persons() {}
    }

    public static String inferExternalSystemFromComponentName(String name) {
        if (name == null) return null;
        if (name.contains("Account") && !name.contains("Loan")) return Systems.ACCOUNT_SERVICE;
        if (name.contains("Customer")) return Systems.CUSTOMER_SERVICE;
        if (name.contains("Deposit")) return Systems.DEPOSIT_SERVICE;
        if (name.contains("Collateral") && name.contains("Adapter")) return Systems.COLLATERAL_SERVICE;
        if (name.equals("LoanServiceAdapter")) return Systems.LOAN_SERVICE;
        if (name.contains("Formula")) return Systems.FORMULA_EVALUATOR_SERVICE;
        if (name.contains("Fcb") || name.contains("Transaction")) return Systems.FCB_CORE_BANKING;
        return null;
    }
}
