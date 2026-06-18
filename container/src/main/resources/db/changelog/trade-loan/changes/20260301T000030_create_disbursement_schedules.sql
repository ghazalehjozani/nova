--liquibase formatted sql

--changeset trade-loan:20260301T000030-create-disbursement-schedules logicalFilePath:trade-loan/20260301T000030-create-disbursement-schedules
-- Planned disbursement schedule (DisbursementScheduleEntity, extends PersistentEntity) + its tranche side table.
-- id = UUID v7; created_at/modified_at = LocalDateTime -> `timestamp`. ScheduledTrancheEmb.scheduledDate is an
-- Instant -> `timestamptz` (prod XML had DATETIME, corrected). Created BEFORE sanctioned_loans so that table's FK
-- on disbursement_schedule_id can reference it.
CREATE TABLE loan_disbursement_schedules (
    id          uuid       NOT NULL DEFAULT uuidv7(),
    version     integer    NOT NULL,
    created_at  timestamp  NOT NULL,
    modified_at timestamp,
    created_by  varchar(255),
    modified_by varchar(255),
    CONSTRAINT pk_loan_disbursement_schedules PRIMARY KEY (id)
);

-- @ElementCollection loan_disbursement_schedule_tranches (List<ScheduledTrancheEmb>)
CREATE TABLE loan_disbursement_schedule_tranches (
    disbursement_schedule_id uuid          NOT NULL,
    scheduled_date           timestamptz,
    amount                   decimal(19, 4) NOT NULL,
    currency                 varchar(3)     NOT NULL,
    CONSTRAINT fk_disbursement_tranches_on_schedule
        FOREIGN KEY (disbursement_schedule_id) REFERENCES loan_disbursement_schedules (id)
);
CREATE INDEX ix_disbursement_tranches_schedule ON loan_disbursement_schedule_tranches (disbursement_schedule_id);

--rollback DROP TABLE loan_disbursement_schedule_tranches;
--rollback DROP TABLE loan_disbursement_schedules;
