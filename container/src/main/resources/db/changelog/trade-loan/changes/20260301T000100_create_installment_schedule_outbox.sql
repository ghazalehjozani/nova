--liquibase formatted sql

--changeset trade-loan:20260301T000100-create-installment-schedule-outbox logicalFilePath:trade-loan/20260301T000100-create-installment-schedule-outbox
-- Transactional outbox for the installment-schedule aggregate (InstallmentScheduleOutboxEventEntity, extends
-- AbstractOutboxEventEntity). id = UUID v7. Actor-envelope + traceparent/tracestate folded in. next_retry_at /
-- processed_at / locked_at = Instant -> timestamptz; created_at/modified_at = LocalDateTime -> timestamp.
-- Hot UPDATE path => fillfactor=80. Columns ordered by descending alignment (8B -> 4B -> uuid -> varlena) to
-- drop padding; payload/error_stacktrace SET COMPRESSION lz4; aggressive per-table autovacuum for the
-- poller-UPDATE + reaper-DELETE churn. See loan_arrangement_outbox for the full rationale.
CREATE TABLE installment_schedule_outbox_events (
    id                    uuid          NOT NULL DEFAULT uuidv7(),
    -- 8-byte-aligned fixed-width
    sequence_number       bigint        NOT NULL,
    next_retry_at         timestamptz,
    processed_at          timestamptz,
    locked_at             timestamptz,
    created_at            timestamp     NOT NULL,
    modified_at           timestamp,
    -- 4-byte-aligned fixed-width
    event_version         int           NOT NULL,
    retry_count           int           NOT NULL,
    version               integer       NOT NULL,
    -- uuid (char-aligned, fixed 16 bytes)
    event_id              uuid          NOT NULL,
    aggregate_id          uuid          NOT NULL,
    correlation_id        uuid,
    causation_id          uuid,
    -- variable-length (varlena) last
    idempotency_key       varchar(255),
    event_type            varchar(255)  NOT NULL,
    payload               text          NOT NULL,
    partition_key         varchar(255)  NOT NULL,
    target_topic          varchar(255)  NOT NULL,
    status                varchar(255)  NOT NULL,
    error_message         varchar(4000),
    error_stacktrace      text,
    aggregate_type        varchar(255)  NOT NULL,
    locked_by             varchar(255),
    traceparent           varchar(64),
    tracestate            varchar(512),
    initiator_sub         varchar(128),
    initiator_type        varchar(32),
    initiator_branch_code varchar(16),
    initiator_source      varchar(32),
    accountability_sub    varchar(128),
    accountability_type   varchar(32),
    execution_trigger     varchar(32),
    execution_mode        varchar(8),
    created_by            varchar(255),
    modified_by           varchar(255),
    CONSTRAINT pk_installment_schedule_outbox_events PRIMARY KEY (id),
    CONSTRAINT uq_installment_schedule_outbox_event_id UNIQUE (event_id),
    CONSTRAINT uq_installment_schedule_outbox_idempotency_key UNIQUE (idempotency_key)
) WITH (
    fillfactor = 80,
    autovacuum_vacuum_scale_factor = 0.05,
    autovacuum_analyze_scale_factor = 0.02,
    autovacuum_vacuum_cost_delay = 0
);
ALTER TABLE installment_schedule_outbox_events ALTER COLUMN payload SET COMPRESSION lz4;
ALTER TABLE installment_schedule_outbox_events ALTER COLUMN error_stacktrace SET COMPRESSION lz4;

CREATE INDEX ix_installment_schedule_outbox_dispatch
    ON installment_schedule_outbox_events (status, aggregate_id, sequence_number)
    WHERE status IN ('PENDING', 'RETRYING');

CREATE INDEX ix_installment_schedule_outbox_created_brin
    ON installment_schedule_outbox_events USING brin (created_at);

CREATE TABLE installment_schedule_outbox_metadata (
    outbox_event_id uuid          NOT NULL,
    meta_key        varchar(255)  NOT NULL,
    meta_value      varchar(2000),
    CONSTRAINT pk_installment_schedule_outbox_metadata PRIMARY KEY (outbox_event_id, meta_key),
    CONSTRAINT fk_installment_schedule_outbox_metadata_on_event
        FOREIGN KEY (outbox_event_id) REFERENCES installment_schedule_outbox_events (id)
);

--rollback DROP TABLE installment_schedule_outbox_metadata;
--rollback DROP TABLE installment_schedule_outbox_events;
