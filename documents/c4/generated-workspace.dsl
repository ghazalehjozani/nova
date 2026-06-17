workspace "Trade Loan Service" {

    !adrs adr

    model {
        operator_admin = person "Operator / Admin" "Bank operations / administrator — monitors and remediates the service"

        esb = softwareSystem "ESB" "Enterprise Service Bus — routes business loan-lifecycle requests from upstream channels"
        tps_sso = softwareSystem "TPS SSO" "OAuth2/OIDC identity provider (token validation + JWKS)"
        fcb_core_banking = softwareSystem "FCB Core Banking" "Legacy core banking — accounts, postings, collateral, sanctions"
        opentelemetry_collector = softwareSystem "OpenTelemetry Collector" "OpenTelemetry collector — traces, metrics, logs"
        consul = softwareSystem "Consul" "Runtime configuration store (Consul KV, fed by GitOps)"
        trade_loan_service = softwareSystem "Trade Loan Service" "trade-loan microservice — hexagonal, DDD, CQRS, workflow orchestration" {
            trade_loan_application = container "Trade Loan Application" "Spring Boot hexagonal application (driving + driven adapters)" "Java 25, Spring Boot 4" {
                reconciliation_by_application_number_controller = component "Reconciliation By Application Number Controller" "Spring MVC Controller"
                dev_auth_callback_controller = component "Dev Auth Callback Controller" "Spring MVC Controller"
                loan_arrangement_query_controller = component "Loan Arrangement Query Controller" "Spring MVC Controller"
                installment_schedule_query_controller = component "Installment Schedule Query Controller" "Spring MVC Controller"
                loan_type_query_controller = component "Loan Type Query Controller" "Spring MVC Controller"
                facility_query_controller = component "Facility Query Controller" "Spring MVC Controller"
                approve_facility_controller = component "Approve Facility Controller" "Spring MVC Controller"
                issue_facility_contract_controller = component "Issue Facility Contract Controller" "Spring MVC Controller"
                add_facility_collateral_controller = component "Add Facility Collateral Controller" "Spring MVC Controller"
                define_loan_arrangement_controller = component "Define Loan Arrangement Controller" "Spring MVC Controller"
                lump_sum_disbursement_controller = component "Lump Sum Disbursement Controller" "Spring MVC Controller"
                define_loan_type_controller = component "Define Loan Type Controller" "Spring MVC Controller"
                submit_facility_for_approval_controller = component "Submit Facility For Approval Controller" "Spring MVC Controller"
                reject_facility_controller = component "Reject Facility Controller" "Spring MVC Controller"
                open_facility_case_controller = component "Open Facility Case Controller" "Spring MVC Controller"
                irregular_progressive_disbursement_controller = component "Irregular Progressive Disbursement Controller" "Spring MVC Controller"
                close_facility_defaulted_controller = component "Close Facility Defaulted Controller" "Spring MVC Controller"
                close_facility_paid_off_controller = component "Close Facility Paid Off Controller" "Spring MVC Controller"
                loan_facility_restructuring_command_handler = component "Loan Facility Restructuring Command Handler" "Command Handler"
                irregular_progressive_disbursement_command_handler = component "Irregular Progressive Disbursement Command Handler" "Command Handler"
                compensate_irregular_disbursement_command_handler = component "Compensate Irregular Disbursement Command Handler" "Command Handler"
                plan_equal_installment_schedule_command_handler = component "Plan Equal Installment Schedule Command Handler" "Command Handler"
                regular_disbursement_command_handler = component "Regular Disbursement Command Handler" "Command Handler"
                reject_facility_command_handler = component "Reject Facility Command Handler" "Command Handler"
                define_loan_type_command_handler = component "Define Loan Type Command Handler" "Command Handler"
                submit_facility_for_approval_command_handler = component "Submit Facility For Approval Command Handler" "Command Handler"
                compensate_approval_submission_command_handler = component "Compensate Approval Submission Command Handler" "Command Handler"
                update_facility_collateral_command_handler = component "Update Facility Collateral Command Handler" "Command Handler"
                cancel_facility_command_handler = component "Cancel Facility Command Handler" "Command Handler"
                approve_facility_command_handler = component "Approve Facility Command Handler" "Command Handler"
                compensate_approval_command_handler = component "Compensate Approval Command Handler" "Command Handler"
                originate_loan_facility_command_handler = component "Originate Loan Facility Command Handler" "Command Handler"
                compensate_origination_command_handler = component "Compensate Origination Command Handler" "Command Handler"
                close_facility_defaulted_command_handler = component "Close Facility Defaulted Command Handler" "Command Handler"
                close_facility_paid_off_command_handler = component "Close Facility Paid Off Command Handler" "Command Handler"
                compensate_close_facility_paid_off_command_handler = component "Compensate Close Facility Paid Off Command Handler" "Command Handler"
                issue_facility_contract_command_handler = component "Issue Facility Contract Command Handler" "Command Handler"
                compensate_contract_issuance_command_handler = component "Compensate Contract Issuance Command Handler" "Command Handler"
                define_trade_loan_arrangement_command_handler = component "Define Trade Loan Arrangement Command Handler" "Command Handler"
                lump_sum_disbursement_command_handler = component "Lump Sum Disbursement Command Handler" "Command Handler"
                compensate_lump_sum_disbursement_command_handler = component "Compensate Lump Sum Disbursement Command Handler" "Command Handler"
                add_facility_collateral_command_handler = component "Add Facility Collateral Command Handler" "Command Handler"
                compensate_collateral_command_handler = component "Compensate Collateral Command Handler" "Command Handler"
                collect_installment_command_handler = component "Collect Installment Command Handler" "Command Handler"
                compensate_collect_installment_command_handler = component "Compensate Collect Installment Command Handler" "Command Handler"
                get_loan_arrangement_by_code_query_handler = component "Get Loan Arrangement By Code Query Handler" "Query Handler"
                loan_type_arrangement_filter_query_handler = component "Loan Type Arrangement Filter Query Handler" "Query Handler"
                get_loan_arrangement_by_id_query_handler = component "Get Loan Arrangement By Id Query Handler" "Query Handler"
                find_all_loan_arrangements_query_handler = component "Find All Loan Arrangements Query Handler" "Query Handler"
                get_installment_schedule_by_id_query_handler = component "Get Installment Schedule By Id Query Handler" "Query Handler"
                find_all_loan_types_query_handler = component "Find All Loan Types Query Handler" "Query Handler"
                get_loan_type_by_id_query_handler = component "Get Loan Type By Id Query Handler" "Query Handler"
                loan_type_filter_query_handler = component "Loan Type Filter Query Handler" "Query Handler"
                get_loan_type_by_code_query_handler = component "Get Loan Type By Code Query Handler" "Query Handler"
                get_facility_by_id_query_handler = component "Get Facility By Id Query Handler" "Query Handler"
                get_facility_by_application_number_query_handler = component "Get Facility By Application Number Query Handler" "Query Handler"
                find_all_loan_facilities_query_handler = component "Find All Loan Facilities Query Handler" "Query Handler"
                resolve_facility_id_by_application_number_query_handler = component "Resolve Facility Id By Application Number Query Handler" "Query Handler"
                search_loan_facilities_query_handler = component "Search Loan Facilities Query Handler" "Query Handler"
                trade_loan_arrangement_repository_adapter = component "Trade Loan Arrangement Repository Adapter" "Spring Data JPA"
                installment_schedule_repository_adapter = component "Installment Schedule Repository Adapter" "Spring Data JPA"
                trade_loan_type_repository_adapter = component "Trade Loan Type Repository Adapter" "Spring Data JPA"
                trade_loan_facility_repository_adapter = component "Trade Loan Facility Repository Adapter" "Spring Data JPA"
                jpa_trade_loan_arrangement_query_adapter = component "Jpa Trade Loan Arrangement Query Adapter" "Spring Data JPA"
                jpa_trade_installment_schedule_query_adapter = component "Jpa Trade Installment Schedule Query Adapter" "Spring Data JPA"
                jpa_trade_loan_type_query_adapter = component "Jpa Trade Loan Type Query Adapter" "Spring Data JPA"
                jpa_facility_query_adapter = component "Jpa Facility Query Adapter" "Spring Data JPA"
                trade_loan_arrangement_outbox_handler = component "Trade Loan Arrangement Outbox Handler" "Outbox Publisher"
                installment_schedule_outbox_handler = component "Installment Schedule Outbox Handler" "Outbox Publisher"
                trade_loan_type_outbox_handler = component "Trade Loan Type Outbox Handler" "Outbox Publisher"
                trade_loan_facility_outbox_handler = component "Trade Loan Facility Outbox Handler" "Outbox Publisher"
                fcb_event_consumer = component "Fcb Event Consumer" "Spring Kafka Listener"
                reconciliation_ops_mcp_tools = component "Reconciliation Ops Mcp Tools" "MCP Tool"
                loan_facility_mcp_tools = component "Loan Facility Mcp Tools" "MCP Tool"
                loan_type_mcp_tools = component "Loan Type Mcp Tools" "MCP Tool"
                loan_arrangement_mcp_tools = component "Loan Arrangement Mcp Tools" "MCP Tool"
                installment_schedule_mcp_tools = component "Installment Schedule Mcp Tools" "MCP Tool"
                artemis_fcb_request_reply_client = component "Artemis Fcb Request Reply Client" "FCB corridor client"
                fcb_request_reply_client = component "Fcb Request Reply Client" "FCB corridor client"
                fcb_kafka_client = component "Fcb Kafka Client" "FCB corridor client"
                routing_fcb_request_reply_client = component "Routing Fcb Request Reply Client" "FCB corridor client"
                find_loan_arrangement_by_id_client = component "Find Loan Arrangement By Id Client" "FCB corridor client"
                facility_root_cause_classifier = component "Facility Root Cause Classifier" "Reconciliation engine"
                facility_convergence_action = component "Facility Convergence Action" "Reconciliation engine"
                facility_root_cause_classifier_signal_verdict = component "Facility Root Cause Classifier $ Signal Verdict" "Reconciliation engine"
                facility_divergence_probe = component "Facility Divergence Probe" "Reconciliation engine"
                facility_recon_mapping = component "Facility Recon Mapping" "Reconciliation engine"
                facility_root_cause_classifier_classifier_input = component "Facility Root Cause Classifier $ Classifier Input" "Reconciliation engine"
                facility_classification = component "Facility Classification" "Reconciliation engine"
                reconciliation_source_properties = component "Reconciliation Source Properties" "Reconciliation engine"
                facility_reconciliation_source = component "Facility Reconciliation Source" "Reconciliation engine"
                facility_convergence_action_1 = component "Facility Convergence Action $ 1" "Reconciliation engine"
                facility_divergence_probe_1 = component "Facility Divergence Probe $ 1" "Reconciliation engine"
                facility_dossier_builder_1 = component "Facility Dossier Builder $ 1" "Reconciliation engine"
                facility_dossier_builder = component "Facility Dossier Builder" "Reconciliation engine"
                facility_root_cause_classifier_1 = component "Facility Root Cause Classifier $ 1" "Reconciliation engine"
                facility_recon_mapping_1 = component "Facility Recon Mapping $ 1" "Reconciliation engine"
                trade_loan_arrangement = component "Trade Loan Arrangement" "DDD Aggregate Root"
                trade_loan_type = component "Trade Loan Type" "DDD Aggregate Root"
                trade_loan_application = component "Trade Loan Application" "DDD Aggregate Root"
                trade_sanctioned_loan = component "Trade Sanctioned Loan" "DDD Aggregate Root"
                trade_loan_facility = component "Trade Loan Facility" "DDD Aggregate Root"
                irregular_progressive_disbursement_transaction_service = component "Irregular Progressive Disbursement Transaction Service" "Domain Service"
                trade_issue_contract_transaction_service = component "Trade Issue Contract Transaction Service" "Domain Service"
                trade_lump_sum_disbursement_transaction_service = component "Trade Lump Sum Disbursement Transaction Service" "Domain Service"
                trade_repayment_scheduling_service = component "Trade Repayment Scheduling Service" "Domain Service"
                trade_loan_type_validation_service = component "Trade Loan Type Validation Service" "Domain Service"
                trade_loan_facility_service = component "Trade Loan Facility Service" "Domain Service"
                trade_sanction_validation_service = component "Trade Sanction Validation Service" "Domain Service"
                trade_loan_facility_validation_service = component "Trade Loan Facility Validation Service" "Domain Service"
                issue_contract_validate_facility = component "Issue Contract — Validate Facility" "Read-side validation that the facility can issue a contract" "Workflow read step"
                issue_contract_open_accounts = component "Issue Contract — Open Accounts" "Resolve/open FCB loan accounts per relation type" "Workflow remote step (FCB corridor)"
                issue_contract_post_transaction = component "Issue Contract — Post Transaction" "Post the contract-issuance transaction to FCB" "Workflow remote step (FCB corridor)"
                issue_contract_update_facility_state = component "Issue Contract — Update Facility State" "Apply issueContract to the aggregate, persist + publish events" "Workflow publishing-write step"
                lump_sum_validate_facility = component "Lump-Sum — Validate Facility" "Validate method == LUMP_SUM and disbursement date" "Workflow read step"
                lump_sum_resolve_accounts = component "Lump-Sum — Resolve Accounts" "Resolve/open FCB accounts for the disbursement" "Workflow remote step (FCB corridor)"
                lump_sum_post_transactions = component "Lump-Sum — Post Transactions" "Post the disbursement transactions to FCB" "Workflow remote step (FCB corridor)"
                lump_sum_apply_disbursement = component "Lump-Sum — Apply Disbursement" "Activate schedule + apply lump-sum disbursement, persist + publish" "Workflow publishing-write step"
                irregular_disb_validate_facility = component "Irregular Disb. — Validate Facility" "Validate method == IRREGULAR_PROGRESSIVE" "Workflow read step"
                irregular_disb_resolve_accounts = component "Irregular Disb. — Resolve Accounts" "Resolve/open FCB accounts for the disbursement" "Workflow remote step (FCB corridor)"
                irregular_disb_post_transactions = component "Irregular Disb. — Post Transactions" "Post the disbursement transactions to FCB" "Workflow remote step (FCB corridor)"
                irregular_disb_apply_disbursement = component "Irregular Disb. — Apply Disbursement" "Apply irregular-progressive disbursement, persist + publish" "Workflow publishing-write step"
                regular_disb_apply_regular_disbursement = component "Regular Disb. — Apply Regular Disbursement" "Single atomic write (ephemeral workflow, no durable run row)" "Workflow publishing-write step"
            }
            postgresql_database = container "PostgreSQL Database" "Loan data, inbox/outbox, workflow state, audit, reconciliation" "PostgreSQL 18"
            kafka = container "Kafka" "Event streaming + request/reply fallback transport" "Apache Kafka (SASL_PLAINTEXT/SCRAM-SHA-256)"
            redis = container "Redis" "Cache + idempotency store (Sentinel HA)" "Redis 8.6 (Sentinel HA: 1 master / 2 replicas / 3 sentinels)"
            activemq_artemis = container "ActiveMQ Artemis" "Primary FCB request/reply corridor broker" "ActiveMQ Artemis 2.43 (Jakarta JMS)"
        }
        trade_loan_application -> postgresql_database "Aggregates, inbox/outbox, workflow, audit, reconciliation"
        trade_loan_application -> redis "Query cache, idempotency, JWKS/token cache"
        trade_loan_application -> kafka "Domain events (outbox) + request/reply fallback"
        trade_loan_application -> activemq_artemis "FCB request/reply corridor (primary)"
        trade_loan_application -> consul "Loads runtime config"
        trade_loan_service -> consul "Loads runtime config"
        trade_loan_application -> tps_sso "Validates JWT / fetches JWKS"
        trade_loan_service -> tps_sso "Validates JWT / fetches JWKS"
        trade_loan_application -> fcb_core_banking "Core banking operations via corridor"
        trade_loan_service -> fcb_core_banking "Core banking operations via corridor"
        trade_loan_application -> opentelemetry_collector "Exports traces / metrics / logs"
        trade_loan_service -> opentelemetry_collector "Exports traces / metrics / logs"
        operator_admin -> trade_loan_application "Operates via OPS endpoints (/v1/ops/*) and MCP ops tools"
        operator_admin -> trade_loan_service "Operates via OPS endpoints (/v1/ops/*) and MCP ops tools"
        irregular_progressive_disbursement_command_handler -> trade_loan_facility ""
        irregular_progressive_disbursement_command_handler -> trade_sanctioned_loan ""
        add_facility_collateral_command_handler -> trade_loan_arrangement ""
        compensate_collateral_command_handler -> trade_loan_application ""
        compensate_collateral_command_handler -> trade_loan_facility ""
        routing_fcb_request_reply_client -> fcb_request_reply_client ""
        facility_root_cause_classifier -> facility_recon_mapping ""
        facility_root_cause_classifier -> facility_root_cause_classifier_signal_verdict ""
        facility_root_cause_classifier -> facility_classification ""
        facility_root_cause_classifier -> facility_root_cause_classifier_classifier_input ""
        facility_convergence_action -> facility_dossier_builder ""
        facility_convergence_action -> reconciliation_source_properties ""
        facility_convergence_action -> facility_recon_mapping ""
        facility_convergence_action -> facility_classification ""
        facility_convergence_action -> facility_root_cause_classifier_classifier_input ""
        facility_convergence_action -> facility_root_cause_classifier ""
        facility_divergence_probe -> facility_recon_mapping ""
        facility_reconciliation_source -> reconciliation_source_properties ""
        facility_dossier_builder -> facility_classification ""
        trade_loan_facility -> trade_loan_application ""
        trade_loan_facility -> trade_sanctioned_loan ""
        irregular_progressive_disbursement_transaction_service -> trade_loan_application ""
        irregular_progressive_disbursement_transaction_service -> trade_loan_facility ""
        irregular_progressive_disbursement_transaction_service -> trade_sanctioned_loan ""
        irregular_progressive_disbursement_transaction_service -> trade_loan_type ""
        trade_issue_contract_transaction_service -> trade_loan_application ""
        trade_issue_contract_transaction_service -> trade_loan_facility ""
        trade_issue_contract_transaction_service -> trade_loan_type ""
        trade_issue_contract_transaction_service -> trade_sanctioned_loan ""
        trade_lump_sum_disbursement_transaction_service -> trade_loan_application ""
        trade_lump_sum_disbursement_transaction_service -> trade_loan_facility ""
        trade_lump_sum_disbursement_transaction_service -> trade_loan_type ""
        trade_lump_sum_disbursement_transaction_service -> trade_sanctioned_loan ""
        trade_lump_sum_disbursement_transaction_service -> trade_loan_arrangement ""
        trade_loan_type_validation_service -> trade_loan_arrangement ""
        trade_loan_facility_service -> trade_loan_application ""
        trade_loan_facility_service -> trade_loan_facility ""
        trade_loan_facility_service -> trade_sanctioned_loan ""
        trade_loan_facility_validation_service -> trade_loan_arrangement ""
        esb -> trade_loan_application "Business loan-lifecycle requests"
        esb -> trade_loan_service "Business loan-lifecycle requests"
        trade_loan_arrangement_repository_adapter -> postgresql_database "Reads/Writes"
        trade_loan_application -> postgresql_database "Reads/Writes"
        installment_schedule_repository_adapter -> postgresql_database "Reads/Writes"
        trade_loan_type_repository_adapter -> postgresql_database "Reads/Writes"
        trade_loan_facility_repository_adapter -> postgresql_database "Reads/Writes"
        jpa_trade_loan_arrangement_query_adapter -> postgresql_database "Reads/Writes"
        jpa_trade_installment_schedule_query_adapter -> postgresql_database "Reads/Writes"
        jpa_trade_loan_type_query_adapter -> postgresql_database "Reads/Writes"
        jpa_facility_query_adapter -> postgresql_database "Reads/Writes"
        trade_loan_arrangement_outbox_handler -> kafka "Publishes events"
        trade_loan_application -> kafka "Publishes events"
        installment_schedule_outbox_handler -> kafka "Publishes events"
        trade_loan_type_outbox_handler -> kafka "Publishes events"
        trade_loan_facility_outbox_handler -> kafka "Publishes events"
        fcb_event_consumer -> kafka "Consumes from"
        trade_loan_application -> kafka "Consumes from"
        artemis_fcb_request_reply_client -> fcb_core_banking "Calls (corridor)"
        trade_loan_application -> fcb_core_banking "Calls (corridor)"
        trade_loan_service -> fcb_core_banking "Calls (corridor)"
        artemis_fcb_request_reply_client -> activemq_artemis "Request/reply (primary)"
        trade_loan_application -> activemq_artemis "Request/reply (primary)"
        fcb_request_reply_client -> fcb_core_banking "Calls (corridor)"
        fcb_kafka_client -> fcb_core_banking "Calls (corridor)"
        fcb_kafka_client -> kafka "Request/reply (fallback)"
        trade_loan_application -> kafka "Request/reply (fallback)"
        routing_fcb_request_reply_client -> fcb_core_banking "Calls (corridor)"
        find_loan_arrangement_by_id_client -> fcb_core_banking "Calls (corridor)"
        facility_root_cause_classifier -> postgresql_database "Reads reconciliation state"
        trade_loan_application -> postgresql_database "Reads reconciliation state"
        facility_root_cause_classifier -> fcb_core_banking "Probes / converges"
        trade_loan_application -> fcb_core_banking "Probes / converges"
        trade_loan_service -> fcb_core_banking "Probes / converges"
        facility_convergence_action -> postgresql_database "Reads reconciliation state"
        facility_convergence_action -> fcb_core_banking "Probes / converges"
        facility_root_cause_classifier_signal_verdict -> postgresql_database "Reads reconciliation state"
        facility_root_cause_classifier_signal_verdict -> fcb_core_banking "Probes / converges"
        facility_divergence_probe -> postgresql_database "Reads reconciliation state"
        facility_divergence_probe -> fcb_core_banking "Probes / converges"
        facility_recon_mapping -> postgresql_database "Reads reconciliation state"
        facility_recon_mapping -> fcb_core_banking "Probes / converges"
        facility_root_cause_classifier_classifier_input -> postgresql_database "Reads reconciliation state"
        facility_root_cause_classifier_classifier_input -> fcb_core_banking "Probes / converges"
        facility_classification -> postgresql_database "Reads reconciliation state"
        facility_classification -> fcb_core_banking "Probes / converges"
        reconciliation_source_properties -> postgresql_database "Reads reconciliation state"
        reconciliation_source_properties -> fcb_core_banking "Probes / converges"
        facility_reconciliation_source -> postgresql_database "Reads reconciliation state"
        facility_reconciliation_source -> fcb_core_banking "Probes / converges"
        facility_convergence_action_1 -> postgresql_database "Reads reconciliation state"
        facility_convergence_action_1 -> fcb_core_banking "Probes / converges"
        facility_divergence_probe_1 -> postgresql_database "Reads reconciliation state"
        facility_divergence_probe_1 -> fcb_core_banking "Probes / converges"
        facility_dossier_builder_1 -> postgresql_database "Reads reconciliation state"
        facility_dossier_builder_1 -> fcb_core_banking "Probes / converges"
        facility_dossier_builder -> postgresql_database "Reads reconciliation state"
        facility_dossier_builder -> fcb_core_banking "Probes / converges"
        facility_root_cause_classifier_1 -> postgresql_database "Reads reconciliation state"
        facility_root_cause_classifier_1 -> fcb_core_banking "Probes / converges"
        facility_recon_mapping_1 -> postgresql_database "Reads reconciliation state"
        facility_recon_mapping_1 -> fcb_core_banking "Probes / converges"
        issue_facility_contract_command_handler -> issue_contract_validate_facility "Starts"
        issue_contract_validate_facility -> issue_contract_open_accounts "Then"
        issue_contract_open_accounts -> fcb_core_banking "Calls"
        trade_loan_application -> fcb_core_banking "Calls"
        trade_loan_service -> fcb_core_banking "Calls"
        issue_contract_open_accounts -> issue_contract_post_transaction "Then"
        issue_contract_post_transaction -> fcb_core_banking "Calls"
        issue_contract_post_transaction -> issue_contract_update_facility_state "Then"
        issue_contract_update_facility_state -> postgresql_database "Persists"
        trade_loan_application -> postgresql_database "Persists"
        issue_contract_update_facility_state -> kafka "Emits events (outbox)"
        trade_loan_application -> kafka "Emits events (outbox)"
        issue_facility_contract_command_handler -> postgresql_database "Durable workflow state (workflow_run, workflow_compensation)"
        trade_loan_application -> postgresql_database "Durable workflow state (workflow_run, workflow_compensation)"
        issue_facility_contract_command_handler -> compensate_contract_issuance_command_handler "Compensates on failure"
        lump_sum_disbursement_command_handler -> lump_sum_validate_facility "Starts"
        lump_sum_validate_facility -> lump_sum_resolve_accounts "Then"
        lump_sum_resolve_accounts -> fcb_core_banking "Calls"
        lump_sum_resolve_accounts -> lump_sum_post_transactions "Then"
        lump_sum_post_transactions -> fcb_core_banking "Calls"
        lump_sum_post_transactions -> lump_sum_apply_disbursement "Then"
        lump_sum_apply_disbursement -> postgresql_database "Persists"
        lump_sum_apply_disbursement -> kafka "Emits events (outbox)"
        lump_sum_disbursement_command_handler -> postgresql_database "Durable workflow state (workflow_run, workflow_compensation)"
        lump_sum_disbursement_command_handler -> compensate_lump_sum_disbursement_command_handler "Compensates on failure"
        irregular_progressive_disbursement_command_handler -> irregular_disb_validate_facility "Starts"
        irregular_disb_validate_facility -> irregular_disb_resolve_accounts "Then"
        irregular_disb_resolve_accounts -> fcb_core_banking "Calls"
        irregular_disb_resolve_accounts -> irregular_disb_post_transactions "Then"
        irregular_disb_post_transactions -> fcb_core_banking "Calls"
        irregular_disb_post_transactions -> irregular_disb_apply_disbursement "Then"
        irregular_disb_apply_disbursement -> postgresql_database "Persists"
        irregular_disb_apply_disbursement -> kafka "Emits events (outbox)"
        irregular_progressive_disbursement_command_handler -> postgresql_database "Durable workflow state (workflow_run, workflow_compensation)"
        irregular_progressive_disbursement_command_handler -> compensate_irregular_disbursement_command_handler "Compensates on failure"
        regular_disbursement_command_handler -> regular_disb_apply_regular_disbursement "Starts"
        regular_disb_apply_regular_disbursement -> postgresql_database "Persists"
        regular_disb_apply_regular_disbursement -> kafka "Emits events (outbox)"
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

        component trade_loan_application "Components_5" {
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

        component trade_loan_application "DomainModel" {
            include *
            autoLayout tb 300 300
        }

        component trade_loan_application "Mcp" {
            include *
            autoLayout tb 300 300
        }

        component trade_loan_application "Repositories" {
            include *
            autoLayout tb 300 300
        }

        component trade_loan_application "Reconciliation" {
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

        component trade_loan_application "WorkflowFlow" {
            include *
            autoLayout lr 400 300
        }

        component trade_loan_application "Workflow" {
            include *
            autoLayout tb 300 300
        }

        component trade_loan_application "IssueContractWorkflow" {
            include *
            autoLayout tb 300 300
        }

        component trade_loan_application "LumpSumDisbursementWorkflow" {
            include *
            autoLayout tb 300 300
        }

        component trade_loan_application "IrregularDisbursementWorkflow" {
            include *
            autoLayout tb 300 300
        }

        component trade_loan_application "RegularDisbursementWorkflow" {
            include *
            autoLayout tb 300 300
        }

        styles {
            element "Admin" {
                background #5c3d6e
                color #ffffff
                shape Person
            }
            element "Aggregate" {
                background #ff9800
                color #000000
            }
            element "Cache" {
                background #e74c3c
                color #ffffff
                shape Cylinder
            }
            element "Client" {
                background #ec407a
                color #ffffff
            }
            element "Compensation" {
                background #ef5350
                color #ffffff
            }
            element "Component" {
                background #85bbf0
                color #000000
            }
            element "Config" {
                background #6d4c41
                color #ffffff
            }
            element "Consumer" {
                background #ff7043
                color #ffffff
                shape Hexagon
            }
            element "Container" {
                background #438dd5
                color #ffffff
            }
            element "Controller" {
                background #7cb342
                color #ffffff
            }
            element "Core Banking" {
                background #8d6e63
                color #ffffff
            }
            element "Database" {
                shape Cylinder
            }
            element "Domain" {
                background #ffa726
                color #000000
            }
            element "Entity" {
                background #ffb74d
                color #000000
            }
            element "External System" {
                background #999999
                color #ffffff
            }
            element "Handler" {
                background #42a5f5
                color #ffffff
            }
            element "MCP" {
                background #26a69a
                color #ffffff
                shape Robot
            }
            element "Message Broker" {
                background #f5a623
                color #000000
                shape Pipe
            }
            element "Observability" {
                background #00897b
                color #ffffff
            }
            element "Outbox" {
                background #66bb6a
                color #000000
                shape Hexagon
            }
            element "Person" {
                background #08427b
                color #ffffff
                shape Person
            }
            element "Reconciliation" {
                background #9575cd
                color #ffffff
            }
            element "Repository" {
                background #5c6bc0
                color #ffffff
                shape Cylinder
            }
            element "Security" {
                background #b71c1c
                color #ffffff
            }
            element "Service" {
                background #29b6f6
                color #ffffff
            }
            element "Software System" {
                background #1168bd
                color #ffffff
            }
            element "Workflow" {
                background #ab47bc
                color #ffffff
                shape Diamond
            }
        }
    }
}
