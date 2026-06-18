--liquibase formatted sql

--changeset trade-loan:20260301T000070-create-loan-arrangement-outbox logicalFilePath:trade-loan/20260301T000070-create-loan-arrangement-outbox
-- Transactional outbox for the loan-arrangement aggregate (TradeLoanArrangementOutboxEventEntity, extends
-- AbstractOutboxEventEntity). id = UUID v7. Folds the historical actor-envelope (8 cols) + W3C traceparent/tracestate
-- migrations into the create. AbstractOutboxEventEntity temporal fields next_retry_at/processed_at/locked_at are
-- Instant -> `timestamptz`; the inherited PersistentEntity created_at/modified_at are LocalDateTime -> `timestamp`.
-- Hot UPDATE path (status / retry_count / locked_* / processed_at rewritten by the poller) => fillfactor=80.
-- Columns are ordered by descending storage alignment (8-byte fixed -> 4-byte -> uuid[char-aligned, 16B fixed]
-- -> variable-length last) to minimise per-row alignment padding; `id` stays first by convention. payload and
-- error_stacktrace carry SET COMPRESSION lz4 — event payloads are highly compressible JSON and lz4 cuts CPU on
-- the hot poller insert/read path vs the pglz default (PG18 ships lz4 column compression on postgres:18.0-alpine).
-- Per-table autovacuum is tuned aggressively (scale 0.05 / analyze 0.02, cost-delay 0): the poller UPDATEs then
-- the reaper DELETEs every PROCESSED row, so dead tuples accrue fast and the default 0.2 scale factor lets bloat
-- build before a vacuum fires.
CREATE TABLE loan_arrangement_outbox_events (
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
    CONSTRAINT pk_loan_arrangement_outbox_events PRIMARY KEY (id),
    CONSTRAINT uq_arrangement_outbox_event_id UNIQUE (event_id),
    CONSTRAINT uq_arrangement_outbox_idempotency_key UNIQUE (idempotency_key)
) WITH (
    fillfactor = 80,
    autovacuum_vacuum_scale_factor = 0.05,
    autovacuum_analyze_scale_factor = 0.02,
    autovacuum_vacuum_cost_delay = 0
);
ALTER TABLE loan_arrangement_outbox_events ALTER COLUMN payload SET COMPRESSION lz4;
ALTER TABLE loan_arrangement_outbox_events ALTER COLUMN error_stacktrace SET COMPRESSION lz4;

-- Poller dispatch path (BaseOutboxRepository.findEventsForProcessing): status equality + partial predicate, then
-- ORDER BY aggregate_id, sequence_number. Partial WHERE keeps the index tiny (processed/dead-letter rows excluded).
CREATE INDEX ix_loan_arrangement_outbox_dispatch
    ON loan_arrangement_outbox_events (status, aggregate_id, sequence_number)
    WHERE status IN ('PENDING', 'RETRYING');

-- Append-only growth: BRIN on created_at for cheap time-range scans (retention / audit sweeps).
CREATE INDEX ix_loan_arrangement_outbox_created_brin
    ON loan_arrangement_outbox_events USING brin (created_at);

-- @ElementCollection loan_arrangement_outbox_metadata (Map<String,String> metadata)
CREATE TABLE loan_arrangement_outbox_metadata (
    outbox_event_id uuid          NOT NULL,
    meta_key        varchar(255)  NOT NULL,
    meta_value      varchar(2000),
    CONSTRAINT pk_loan_arrangement_outbox_metadata PRIMARY KEY (outbox_event_id, meta_key),
    CONSTRAINT fk_arrangement_outbox_metadata_on_event
        FOREIGN KEY (outbox_event_id) REFERENCES loan_arrangement_outbox_events (id)
);

--rollback DROP TABLE loan_arrangement_outbox_metadata;
--rollback DROP TABLE loan_arrangement_outbox_events;
