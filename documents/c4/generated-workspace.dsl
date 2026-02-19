workspace "Trade Loan Service" {

    !adrs adr

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
            trade_loan_application = container "Trade Loan Application" "Spring Boot Hexagonal Architecture" "Java 25, Spring Boot 4" {
                devauthcallbackcontroller = component "DevAuthCallbackController" "REST API: /api/{version}/dev/auth" "Spring REST Controller"
                addfacilitycollateralcontroller = component "AddFacilityCollateralController" "REST API: /api/{version}/facilities/{facilityId}/collaterals" "Spring REST Controller"
                approvefacilitycontroller = component "ApproveFacilityController" "REST API: /api/{version}/facilities/{facilityId}/approve" "Spring REST Controller"
                cancelfacilitycontroller = component "CancelFacilityController" "REST API: /api/{version}/facilities/{facilityId}/cancel" "Spring REST Controller"
                closefacilitydefaultedcontroller = component "CloseFacilityDefaultedController" "REST API: /api/{version}/facilities/{facilityId}/close-defaulted" "Spring REST Controller"
                closefacilitypaidoffcontroller = component "CloseFacilityPaidOffController" "REST API: /api/{version}/facilities/{facilityId}/close-paid-off" "Spring REST Controller"
                defineloanarrangementcontroller = component "DefineLoanArrangementController" "REST API: /api/{version}/loan-arrangements/define" "Spring REST Controller"
                defineloantypecontroller = component "DefineLoanTypeController" "REST API: /api/{version}/loan-types/define" "Spring REST Controller"
                fullloanfacilitylifecyclecontroller = component "FullLoanFacilityLifecycleController" "REST API: /api/{version}/facilities/full-lifecycle" "Spring REST Controller"
                irregularprogressivedisbursementcontroller = component "IrregularProgressiveDisbursementController" "REST API: /api/{version}/facilities/{facilityId}/disburse/progressive-irregular" "Spring REST Controller"
                issuefacilitycontractcontroller = component "IssueFacilityContractController" "REST API: /api/{version}/facilities/{facilityId}/issue-contract" "Spring REST Controller"
                lumpsumdisbursementcontroller = component "LumpSumDisbursementController" "REST API: /api/{version}/facilities/{facilityId}/disburse/lump-sum" "Spring REST Controller"
                openfacilitycasecontroller = component "OpenFacilityCaseController" "REST API: /api/{version}/facilities/open-case" "Spring REST Controller"
                regulardisbursementcontroller = component "RegularDisbursementController" "REST API: /api/{version}/facilities/{facilityId}/disburse/regular" "Spring REST Controller"
                rejectfacilitycontroller = component "RejectFacilityController" "REST API: /api/{version}/facilities/{facilityId}/reject" "Spring REST Controller"
                submitfacilityforapprovalcontroller = component "SubmitFacilityForApprovalController" "REST API: /api/{version}/facilities/{facilityId}/submit-for-approval" "Spring REST Controller"
                installmentschedulequerycontroller = component "InstallmentScheduleQueryController" "REST API: /api/{version}/installment-schedules" "Spring REST Controller"
                loanarrangementquerycontroller = component "LoanArrangementQueryController" "REST API: /api/{version}/loan-arrangements" "Spring REST Controller"
                facilityquerycontroller = component "FacilityQueryController" "REST API: /api/{version}/loan-facilities" "Spring REST Controller"
                loantypequerycontroller = component "LoanTypeQueryController" "REST API: /api/{version}/loan-types" "Spring REST Controller"
                installmentoperationcommandconsumer = component "InstallmentOperationCommandConsumer" "Kafka consumer" "Spring Kafka Listener"
                tradeloancommandconsumer = component "TradeLoanCommandConsumer" "Kafka consumer" "Spring Kafka Listener"
                addfacilitycollateralcommandhandler = component "AddFacilityCollateralCommandHandler" "Handles add facility collateral" "Command Handler"
                compensatecollateralcommandhandler = component "CompensateCollateralCommandHandler" "Handles compensate collateral" "Command Handler"
                approvefacilitycommandhandler = component "ApproveFacilityCommandHandler" "Handles approve facility" "Command Handler"
                compensateapprovalcommandhandler = component "CompensateApprovalCommandHandler" "Handles compensate approval" "Command Handler"
                cancelfacilitycommandhandler = component "CancelFacilityCommandHandler" "Handles cancel facility" "Command Handler"
                closefacilitydefaultedcommandhandler = component "CloseFacilityDefaultedCommandHandler" "Handles close facility defaulted" "Command Handler"
                closefacilitypaidoffcommandhandler = component "CloseFacilityPaidOffCommandHandler" "Handles close facility paid off" "Command Handler"
                collectinstallmentcommandhandler = component "CollectInstallmentCommandHandler" "Handles collect installment" "Command Handler"
                definetradeloanarrangementcommandhandler = component "DefineTradeLoanArrangementCommandHandler" "Handles define trade loan arrangement" "Command Handler"
                defineloantypecommandhandler = component "DefineLoanTypeCommandHandler" "Handles define loan type" "Command Handler"
                fullloanfacilitylifecyclecommandhandler = component "FullLoanFacilityLifecycleCommandHandler" "Handles full loan facility lifecycle" "Command Handler"
                fulllifecyclerevertcommandhandler = component "FullLifecycleRevertCommandHandler" "Handles full lifecycle revert" "Command Handler"
                irregularprogressivedisbursementcommandhandler = component "IrregularProgressiveDisbursementCommandHandler" "Handles irregular progressive disbursement" "Command Handler"
                compensateirregulardisbursementcommandhandler = component "CompensateIrregularDisbursementCommandHandler" "Handles compensate irregular disbursement" "Command Handler"
                issuefacilitycontractcommandhandler = component "IssueFacilityContractCommandHandler" "Handles issue facility contract" "Command Handler"
                compensatecontractissuancecommandhandler = component "CompensateContractIssuanceCommandHandler" "Handles compensate contract issuance" "Command Handler"
                lumpsumdisbursementcommandhandler = component "LumpSumDisbursementCommandHandler" "Handles lump sum disbursement" "Command Handler"
                compensatelumpsumdisbursementcommandhandler = component "CompensateLumpSumDisbursementCommandHandler" "Handles compensate lump sum disbursement" "Command Handler"
                originateloanfacilitycommandhandler = component "OriginateLoanFacilityCommandHandler" "Handles originate loan facility" "Command Handler"
                compensateoriginationcommandhandler = component "CompensateOriginationCommandHandler" "Handles compensate origination" "Command Handler"
                planequalinstallmentschedulecommandhandler = component "PlanEqualInstallmentScheduleCommandHandler" "Handles plan equal installment schedule" "Command Handler"
                regulardisbursementcommandhandler = component "RegularDisbursementCommandHandler" "Handles regular disbursement" "Command Handler"
                rejectfacilitycommandhandler = component "RejectFacilityCommandHandler" "Handles reject facility" "Command Handler"
                submitfacilityforapprovalcommandhandler = component "SubmitFacilityForApprovalCommandHandler" "Handles submit facility for approval" "Command Handler"
                compensateapprovalsubmissioncommandhandler = component "CompensateApprovalSubmissionCommandHandler" "Handles compensate approval submission" "Command Handler"
                getinstallmentschedulebyidqueryhandler = component "GetInstallmentScheduleByIdQueryHandler" "Handles get installment schedule by id queries" "Query Handler"
                findallloanarrangementsqueryhandler = component "FindAllLoanArrangementsQueryHandler" "Handles find all loan arrangements queries" "Query Handler"
                getloanarrangementbyidqueryhandler = component "GetLoanArrangementByIdQueryHandler" "Handles get loan arrangement by id queries" "Query Handler"
                loantypearrangementfilterqueryhandler = component "LoanTypeArrangementFilterQueryHandler" "Handles loan type arrangement filter queries" "Query Handler"
                findallloanfacilitiesqueryhandler = component "FindAllLoanFacilitiesQueryHandler" "Handles find all loan facilities queries" "Query Handler"
                getfacilitybyidqueryhandler = component "GetFacilityByIdQueryHandler" "Handles get facility by id queries" "Query Handler"
                searchloanfacilitiesqueryhandler = component "SearchLoanFacilitiesQueryHandler" "Handles search loan facilities queries" "Query Handler"
                findallloantypesqueryhandler = component "FindAllLoanTypesQueryHandler" "Handles find all loan types queries" "Query Handler"
                getloantypebyidqueryhandler = component "GetLoanTypeByIdQueryHandler" "Handles get loan type by id queries" "Query Handler"
                loantypefilterqueryhandler = component "LoanTypeFilterQueryHandler" "Handles loan type filter queries" "Query Handler"
                fullloanfacilitylifecyclesaga = component "FullLoanFacilityLifecycleSaga" "Orchestrates full loan facility lifecycle" "Saga Orchestrator"
                issuefacilitycontractsaga = component "IssueFacilityContractSaga" "Orchestrates issue facility contract" "Saga Orchestrator"
                tradeloanarrangement = component "TradeLoanArrangement" "trade loan arrangement aggregate" "DDD Aggregate Root"
                tradeloanapplication = component "TradeLoanApplication" "trade loan application aggregate" "DDD Aggregate Root"
                tradeloanfacility = component "TradeLoanFacility" "trade loan facility aggregate" "DDD Aggregate Root"
                tradeloanfacilityeventfactory = component "TradeLoanFacilityEventFactory" "trade loan facility event factory entity" "DDD Entity"
                tradesanctionedloan = component "TradeSanctionedLoan" "trade sanctioned loan aggregate" "DDD Aggregate Root"
                tradeloantype = component "TradeLoanType" "trade loan type aggregate" "DDD Aggregate Root"
                traderepaymentschedulingservice = component "TradeRepaymentSchedulingService" "trade repayment scheduling service domain service" "Domain Service"
                tradeloanfacilityservice = component "TradeLoanFacilityService" "trade loan facility service domain service" "Domain Service"
                irregularprogressivedisbursementtransactionservice = component "IrregularProgressiveDisbursementTransactionService" "irregular progressive disbursement transaction service domain service" "Domain Service"
                tradeissuecontracttransactionservice = component "TradeIssueContractTransactionService" "trade issue contract transaction service domain service" "Domain Service"
                tradelampsundisbursementtransactionservice = component "TradeLampSunDisbursementTransactionService" "trade lamp sun disbursement transaction service domain service" "Domain Service"
                tradeloanfacilityvalidationservice = component "TradeLoanFacilityValidationService" "trade loan facility validation service domain service" "Domain Service"
                tradesanctionvalidationservice = component "TradeSanctionValidationService" "trade sanction validation service domain service" "Domain Service"
                tradeloantypevalidationservice = component "TradeLoanTypeValidationService" "trade loan type validation service domain service" "Domain Service"
                installmentschedulerepositoryadapter = component "InstallmentScheduleRepositoryAdapter" "InstallmentSchedule persistence adapter" "Spring Data JPA"
                tradeloanarrangementrepositoryadapter = component "TradeLoanArrangementRepositoryAdapter" "TradeLoanArrangement persistence adapter" "Spring Data JPA"
                tradeloanfacilityrepositoryadapter = component "TradeLoanFacilityRepositoryAdapter" "TradeLoanFacility persistence adapter" "Spring Data JPA"
                tradeloantyperepositoryadapter = component "TradeLoanTypeRepositoryAdapter" "TradeLoanType persistence adapter" "Spring Data JPA"
                jpatradeinstallmentschedulequeryadapter = component "JpaTradeInstallmentScheduleQueryAdapter" "TradeInstallmentSchedule query adapter" "Spring Data JPA"
                jpatradeloanarrangementqueryadapter = component "JpaTradeLoanArrangementQueryAdapter" "TradeLoanArrangement query adapter" "Spring Data JPA"
                jpafacilityqueryadapter = component "JpaFacilityQueryAdapter" "Facility query adapter" "Spring Data JPA"
                jpatradeloantypequeryadapter = component "JpaTradeLoanTypeQueryAdapter" "TradeLoanType query adapter" "Spring Data JPA"
                accountserviceadapter = component "AccountServiceAdapter" "account service client" "Spring WebClient"
                collateraladapter = component "CollateralAdapter" "collateral client" "Spring WebClient"
                customerserviceadapter = component "CustomerServiceAdapter" "customer service client" "Spring WebClient"
                depositserviceadapter = component "DepositServiceAdapter" "deposit service client" "Spring WebClient"
                fcbserviceimpl = component "FcbServiceImpl" "fcb client" "Spring WebClient"
                loanserviceadapter = component "LoanServiceAdapter" "loan service client" "Spring WebClient"
                transactionpostingadapter = component "TransactionPostingAdapter" "transaction posting client" "Spring WebClient"
                installmentscheduleoutboxhandler = component "InstallmentScheduleOutboxHandler" "InstallmentSchedule outbox handler" "Outbox Pattern"
                tradeloanarrangementoutboxhandler = component "TradeLoanArrangementOutboxHandler" "TradeLoanArrangement outbox handler" "Outbox Pattern"
                tradeloanfacilityoutboxhandler = component "TradeLoanFacilityOutboxHandler" "TradeLoanFacility outbox handler" "Outbox Pattern"
                tradeloantypeoutboxhandler = component "TradeLoanTypeOutboxHandler" "TradeLoanType outbox handler" "Outbox Pattern"
            }
            postgresql_database = container "PostgreSQL Database" "Loan data storage" "PostgreSQL 18"
            kafka = container "Kafka" "Event streaming" "Confluent Kafka 7.9"
            redis = container "Redis" "Caching and locks" "Redis 8.2"
            kafdrop = container "Kafdrop" "Kafka monitoring" "Kafdrop 4.2"
        }
        trade_loan_application -> postgresql_database "Reads/Writes"
        trade_loan_application -> kafka "Publishes/Consumes"
        trade_loan_application -> redis "Caches data"
        trade_loan_application -> tps_sso "Authenticates"
        trade_loan_service -> tps_sso "Authenticates"
        trade_loan_application -> fcb_core_banking "Core banking"
        trade_loan_service -> fcb_core_banking "Core banking"
        trade_loan_application -> opentelemetry_collector "Telemetry"
        trade_loan_service -> opentelemetry_collector "Telemetry"
        trade_loan_application -> account_service "Calls"
        trade_loan_service -> account_service "Calls"
        trade_loan_application -> customer_service "Calls"
        trade_loan_service -> customer_service "Calls"
        trade_loan_application -> deposit_service "Calls"
        trade_loan_service -> deposit_service "Calls"
        trade_loan_application -> collateral_service "Calls"
        trade_loan_service -> collateral_service "Calls"
        trade_loan_application -> loan_service "Calls"
        trade_loan_service -> loan_service "Calls"
        trade_loan_application -> formula_evaluator_service "Calls"
        trade_loan_service -> formula_evaluator_service "Calls"
        kafdrop -> kafka "Monitors"
        loan_officer -> trade_loan_application "Creates and manages loans"
        loan_officer -> trade_loan_service "Creates and manages loans"
        branch_manager -> trade_loan_application "Approves facilities"
        branch_manager -> trade_loan_service "Approves facilities"
        system_administrator -> trade_loan_application "Monitors system"
        system_administrator -> trade_loan_service "Monitors system"
        system_administrator -> kafdrop "Monitors Kafka"
        system_administrator -> trade_loan_service "Monitors Kafka"
        installmentoperationcommandconsumer -> kafka "Consumes from"
        trade_loan_application -> kafka "Consumes from"
        tradeloancommandconsumer -> kafka "Consumes from"
        addfacilitycollateralcommandhandler -> tradeloanfacilityservice "Uses"
        cancelfacilitycommandhandler -> tradeloanfacilityservice "Uses"
        closefacilitydefaultedcommandhandler -> tradeloanfacilityservice "Uses"
        closefacilitypaidoffcommandhandler -> tradeloanfacilityservice "Uses"
        defineloantypecommandhandler -> tradeloantypevalidationservice "Uses"
        irregularprogressivedisbursementcommandhandler -> irregularprogressivedisbursementtransactionservice "Uses"
        lumpsumdisbursementcommandhandler -> tradelampsundisbursementtransactionservice "Uses"
        planequalinstallmentschedulecommandhandler -> traderepaymentschedulingservice "Uses"
        submitfacilityforapprovalcommandhandler -> tradeloanfacilityservice "Uses"
        issuefacilitycontractsaga -> tradeissuecontracttransactionservice "Uses"
        installmentschedulerepositoryadapter -> postgresql_database "Reads/Writes"
        tradeloanarrangementrepositoryadapter -> postgresql_database "Reads/Writes"
        tradeloanfacilityrepositoryadapter -> postgresql_database "Reads/Writes"
        tradeloantyperepositoryadapter -> postgresql_database "Reads/Writes"
        jpatradeinstallmentschedulequeryadapter -> postgresql_database "Reads/Writes"
        jpatradeloanarrangementqueryadapter -> postgresql_database "Reads/Writes"
        jpafacilityqueryadapter -> postgresql_database "Reads/Writes"
        jpatradeloantypequeryadapter -> postgresql_database "Reads/Writes"
        accountserviceadapter -> account_service "Calls"
        collateraladapter -> collateral_service "Calls"
        customerserviceadapter -> customer_service "Calls"
        depositserviceadapter -> deposit_service "Calls"
        fcbserviceimpl -> fcb_core_banking "Calls"
        trade_loan_application -> fcb_core_banking "Calls"
        trade_loan_service -> fcb_core_banking "Calls"
        loanserviceadapter -> loan_service "Calls"
        transactionpostingadapter -> fcb_core_banking "Calls"
        installmentscheduleoutboxhandler -> kafka "Publishes events"
        trade_loan_application -> kafka "Publishes events"
        tradeloanarrangementoutboxhandler -> kafka "Publishes events"
        tradeloanfacilityoutboxhandler -> kafka "Publishes events"
        tradeloantypeoutboxhandler -> kafka "Publishes events"
    }

    views {
        systemContext trade_loan_service "SystemContext" {
            include *
            autoLayout tb 300 300
        }

        container trade_loan_service "Containers" {
            include *
            autoLayout tb 300 300
        }

        component trade_loan_application "Components" {
            include *
            autoLayout tb 300 300
        }

        component trade_loan_application "Components_2" {
            include *
            autoLayout tb 300 300
        }

        component trade_loan_application "Components_3" {
            include *
            autoLayout tb 300 300
        }

        component trade_loan_application "Components_4" {
            include *
            autoLayout tb 300 300
        }

        component trade_loan_application "Controllers" {
            include *
            autoLayout tb 300 300
        }

        component trade_loan_application "Handlers" {
            include *
            autoLayout tb 300 300
        }

        component trade_loan_application "Sagas" {
            include *
            autoLayout tb 300 300
        }

        component trade_loan_application "DomainModel" {
            include *
            autoLayout tb 300 300
        }

        component trade_loan_application "Repositories" {
            include *
            autoLayout tb 300 300
        }

        component trade_loan_application "ExternalClients" {
            include *
            autoLayout tb 300 300
        }

        component trade_loan_application "Messaging" {
            include *
            autoLayout tb 300 300
        }

        component trade_loan_application "CommandFlow" {
            include *
            autoLayout lr 400 300
        }

        component trade_loan_application "QueryFlow" {
            include *
            autoLayout lr 400 300
        }

        component trade_loan_application "SagaFlow" {
            include *
            autoLayout lr 400 300
        }

        styles {
            element "Software System" {
                background #1168bd
                color #ffffff
            }
            element "Consumer" {
                background #ff7043
                color #ffffff
                shape Hexagon
            }
            element "Person" {
                background #08427b
                color #ffffff
                shape Person
            }
            element "Entity" {
                background #ffb74d
                color #000000
            }
            element "Aggregate" {
                background #ff9800
                color #000000
            }
            element "Cache" {
                background #e74c3c
                shape Cylinder
            }
            element "Container" {
                background #438dd5
                color #ffffff
            }
            element "External User" {
                background #666666
            }
            element "Admin" {
                background #5c3d6e
            }
            element "Outbox" {
                background #66bb6a
                color #000000
                shape Hexagon
            }
            element "Repository" {
                background #5c6bc0
                color #ffffff
                shape Cylinder
            }
            element "Compensation" {
                background #ef5350
                color #ffffff
            }
            element "Saga" {
                background #ab47bc
                color #ffffff
                shape Diamond
            }
            element "Message Broker" {
                background #f5a623
                color #000000
                shape Pipe
            }
            element "Component" {
                background #85bbf0
                color #000000
            }
            element "Controller" {
                background #7cb342
                color #ffffff
            }
            element "Client" {
                background #ec407a
                color #ffffff
            }
            element "Monitoring" {
                background #27ae60
                shape WebBrowser
            }
            element "Service" {
                background #29b6f6
                color #ffffff
            }
            element "Domain" {
                background #ffa726
                color #000000
            }
            element "External System" {
                background #999999
            }
            element "Internal User" {
                background #08427b
            }
            element "Database" {
                shape Cylinder
            }
            element "Handler" {
                background #42a5f5
                color #ffffff
            }
        }
    }
}
