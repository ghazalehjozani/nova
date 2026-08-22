--liquibase formatted sql

--changeset trade-loan:20260301T000060-create-installment-schedules logicalFilePath:trade-loan/20260301T000060-create-installment-schedules
-- Installment schedule (InstallmentScheduleEntity), its restructuring-history side table, the installments
-- (InstallmentEntity), and the per-installment payments side table. Both entities extend PersistentEntity:
-- id = UUID v7; created_at/modified_at = LocalDateTime -> `timestamp`.
-- initiated_at, last_modified_at (schedule) and restructured_at (RestructuringRecordEmb) are Instant fields ->
-- `timestamptz` (prod XML had DATETIME, corrected). Installment due_date/paid_date and payment value_date/payment_date
-- are LocalDate -> `date`. Money columns are the flattened @Embedded MoneyEmb overrides.
CREATE TABLE loan_installment_schedules (
    id                            uuid           NOT NULL DEFAULT uuidv7(),
    version                       integer        NOT NULL,
    created_at                    timestamp      NOT NULL,
    modified_at                   timestamp,
    created_by                    varchar(255),
    modified_by                   varchar(255),
    loan_facility_id              uuid           NOT NULL,
    -- @Enumerated(EnumType.STRING) InstallmentScheduleType. Permitted values:
    -- SINGLE_INSTALLMENT | EQUAL_INSTALLMENTS | GRADUAL_INSTALLMENTS.
    -- SINGLE_INSTALLMENT was added by CBS-282143:LN-59253:LN-59261:LN-59661 (base-loan ADR-0006). No migration is
    -- needed: the name, not the ordinal, is persisted, so existing rows read unchanged and the baseline records the
    -- new permitted value here rather than in a no-op changeset (nova ADR-0006, greenfield-baseline convention).
    schedule_type                 varchar(255)   NOT NULL,
    status                        varchar(255)   NOT NULL,
    initiated_at                  timestamptz    NOT NULL,
    last_modified_at              timestamptz    NOT NULL,
    interest_rate                 decimal(10, 6) NOT NULL,
    -- totalLoanAmount MoneyEmb (default column names amount/currency) + currency CurrencyTypeEmb override
    amount                        decimal(19, 4) NOT NULL,
    currency                      varchar(3)     NOT NULL,
    schedule_currency             varchar(3)     NOT NULL,
    -- GracePeriodEmb
    grace_period_days             int,
    grace_period_months           int,
    grace_period_years            int,
    -- RestructuringRecordEmb
    new_installment_count         int,
    preserved_installments_count  int,
    previous_installment_count    int,
    restructured_at               timestamptz,
    restructured_by               varchar(50),
    restructuring_amount          decimal(19, 4),
    restructuring_amount_currency varchar(3),
    restructuring_reason          varchar(500),
    unpaid_installments_count     int,
    CONSTRAINT pk_loan_installment_schedules PRIMARY KEY (id)
);

-- @ElementCollection loan_installment_schedule_history_ids (ScheduleHistoryEmb.previousScheduleIds : List<UUID>)
-- keyed (current_schedule_id, sequence_order) with the @OrderColumn.
CREATE TABLE loan_installment_schedule_history_ids (
    current_schedule_id  uuid NOT NULL,
    sequence_order       int  NOT NULL,
    previous_schedule_id uuid,
    CONSTRAINT pk_loan_installment_schedule_history_ids PRIMARY KEY (current_schedule_id, sequence_order),
    CONSTRAINT fk_schedule_history_on_installment_schedule
        FOREIGN KEY (current_schedule_id) REFERENCES loan_installment_schedules (id)
);

-- loan_installments (InstallmentEntity)
CREATE TABLE loan_installments (
    id                          uuid           NOT NULL DEFAULT uuidv7(),
    version                     integer        NOT NULL,
    created_at                  timestamp      NOT NULL,
    modified_at                 timestamp,
    created_by                  varchar(255),
    modified_by                 varchar(255),
    sequence_number             int,
    due_date                    date,
    status                      varchar(255),
    paid_date                   date,
    installment_schedule_id     uuid           NOT NULL,
    -- InstallmentAmountEmb (scheduledAmount): total / principal / interest
    total_amount                decimal(19, 4) NOT NULL,
    total_amount_currency       varchar(255)   NOT NULL,
    principal_amount            decimal(19, 4) NOT NULL,
    principal_amount_currency   varchar(255)   NOT NULL,
    interest_amount             decimal(19, 4) NOT NULL,
    interest_amount_currency    varchar(255)   NOT NULL,
    -- paidAmount / outstandingAmount MoneyEmb overrides
    paid_amount_amount          decimal(19, 4),
    paid_amount_currency        varchar(255),
    outstanding_amount          decimal(19, 4),
    outstanding_amount_currency varchar(255),
    CONSTRAINT pk_loan_installments PRIMARY KEY (id),
    CONSTRAINT fk_installments_on_schedule
        FOREIGN KEY (installment_schedule_id) REFERENCES loan_installment_schedules (id)
);
-- FK-index coverage + read path: installments are listed/joined by their schedule (and parent-delete RI checks
-- it). installment_schedule_id is neither a PK leading column nor otherwise indexed, so without this every such
-- access is a seqscan. Single biggest FK win on this aggregate.
CREATE INDEX ix_loan_installments_schedule_id ON loan_installments (installment_schedule_id);

-- @ElementCollection loan_installment_payments (List<InstallmentPaymentEmb>). value_date/payment_date LocalDate.
CREATE TABLE loan_installment_payments (
    installment_id                uuid           NOT NULL,
    payment_reference             varchar(255)   NOT NULL,
    principal_amount              decimal(19, 4),
    principal_amount_currency     varchar(3),
    interest_amount               decimal(19, 4),
    interest_amount_currency      varchar(3),
    total_paid_amount             decimal(19, 4),
    total_paid_amount_currency    varchar(3),
    value_date                    date           NOT NULL,
    payment_date                  date           NOT NULL,
    channel                       varchar(255),
    transaction_reference         varchar(255),
    remarks                       varchar(255),
    installment_sequence_number   int            NOT NULL DEFAULT 0,
    CONSTRAINT fk_installment_payments_on_installment
        FOREIGN KEY (installment_id) REFERENCES loan_installments (id)
);
CREATE INDEX ix_installment_payments_installment_id ON loan_installment_payments (installment_id);

--rollback DROP TABLE loan_installment_payments;
--rollback DROP INDEX ix_loan_installments_schedule_id;
--rollback DROP TABLE loan_installments;
--rollback DROP TABLE loan_installment_schedule_history_ids;
--rollback DROP TABLE loan_installment_schedules;
