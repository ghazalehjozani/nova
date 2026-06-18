--liquibase formatted sql

--changeset trade-loan:20260301T000040-create-sanctioned-loans logicalFilePath:trade-loan/20260301T000040-create-sanctioned-loans
-- Sanctioned (approved) loan (TradeSanctionedLoanEntity, extends PersistentEntity). id = UUID v7;
-- created_at/modified_at = LocalDateTime -> `timestamp`. Greenfield END STATE for the base-loan 6.8 refactor:
-- the revocation_reason column is NOT created (RevocationReasonEmb no longer persisted). Columns are the flattened
-- @Embedded value objects (SanctionSerial / Money approvedAmount / Currency / GracePeriod / InstallmentCount /
-- Period loanDuration / ConfirmType). confirm_person_code: the entity's @AttributeOverride(name="value") does not
-- match ConfirmTypeEmb's `personCode` field, so the effective column stays the embeddable default.
CREATE TABLE sanctioned_loans (
    id                       uuid           NOT NULL DEFAULT uuidv7(),
    version                  integer        NOT NULL,
    created_at               timestamp      NOT NULL,
    modified_at              timestamp,
    created_by               varchar(255),
    modified_by              varchar(255),
    life_insurance_id        varchar(255),
    disbursement_schedule_id uuid,
    disbursement_method      varchar(255),
    sanction_serial_value    varchar(255)   NOT NULL,
    sanction_type            varchar(255)   NOT NULL,
    approved_amount          decimal(19, 4),
    approved_currency        varchar(3),
    currency                 varchar(255),
    grace_period_days        int,
    grace_period_months      int,
    grace_period_years       int,
    installment_count        int            NOT NULL,
    loan_duration_years      int,
    loan_duration_months     int,
    loan_duration_days       int,
    confirm_person_code      varchar(255),
    CONSTRAINT pk_sanctioned_loans PRIMARY KEY (id),
    CONSTRAINT uq_sanctioned_loans_serial UNIQUE (sanction_serial_value),
    CONSTRAINT uq_sanctioned_loans_disbursement_schedule UNIQUE (disbursement_schedule_id),
    CONSTRAINT fk_sanctioned_loans_on_disbursement_schedule
        FOREIGN KEY (disbursement_schedule_id) REFERENCES loan_disbursement_schedules (id)
);

--rollback DROP TABLE sanctioned_loans;
