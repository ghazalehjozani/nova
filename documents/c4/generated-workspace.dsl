workspace "Trade Loan Service" {

    description "C4 Architecture for Morabehe Loans Microservice"

    model {
        loan_officer = person "Loan Officer" "Bank staff managing loans"
        branch_manager = person "Branch Manager" "Branch level approval authority"
        system_administrator = person "System Administrator" "Technical staff"
        customer = person "Customer" "End users"

        tps_sso = softwareSystem "TPS SSO" "OAuth2/OIDC provider"
        fcb_core_banking = softwareSystem "FCB Core Banking" "Core Banking"
        opentelemetry_collector = softwareSystem "OpenTelemetry Collector" "Observability backend"
        account_service = softwareSystem "Account Service" "Account management"
        customer_service = softwareSystem "Customer Service" "Customer data"
        deposit_service = softwareSystem "Deposit Service" "Deposit operations"
        collateral_service = softwareSystem "Collateral Service" "Collateral management"
        loan_service = softwareSystem "Loan Service" "Cross-loan operations"
        formula_evaluator_service = softwareSystem "Formula Evaluator Service" "Formula calculation"
        trade_loan_service = softwareSystem "Trade Loan Service" "Morabehe loans with CQRS and Event Sourcing" {
            trade_loan_application = container "Trade Loan Application" "Spring Boot Hexagonal Architecture" "Java 25, Spring Boot 4"
            postgresql_database = container "PostgreSQL Database" "Loan data storage" "PostgreSQL 18"
            kafka = container "Kafka" "Event streaming" "Confluent Kafka 7.9"
            redis = container "Redis" "Caching and locks" "Redis 8.2"
            kafdrop = container "Kafdrop" "Kafka monitoring" "Kafdrop 4.2"
        }
    }

    views {
        systemContext trade_loan_service "SystemContext" { include * autoLayout }
        container trade_loan_service "Containers" { include * autoLayout }
        component trade_loan_application "Components" { include * autoLayout }
        component trade_loan_application "Components_2" { include * autoLayout }
        component trade_loan_application "Components_3" { include * autoLayout }
        component trade_loan_application "Components_4" { include * autoLayout }
        component trade_loan_application "Controllers" { include * autoLayout }
        component trade_loan_application "Handlers" { include * autoLayout }
        component trade_loan_application "Sagas" { include * autoLayout }
        component trade_loan_application "DomainModel" { include * autoLayout }
        component trade_loan_application "Repositories" { include * autoLayout }
        component trade_loan_application "ExternalClients" { include * autoLayout }
        component trade_loan_application "Messaging" { include * autoLayout }
        component trade_loan_application "CommandFlow" { include * autoLayout }
        component trade_loan_application "QueryFlow" { include * autoLayout }
        component trade_loan_application "SagaFlow" { include * autoLayout }
        styles { }
    }
}
