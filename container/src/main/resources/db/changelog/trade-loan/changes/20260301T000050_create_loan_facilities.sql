--liquibase formatted sql

--changeset trade-loan:20260301T000050-create-loan-facilities logicalFilePath:trade-loan/20260301T000050-create-loan-facilities
-- Central loan facility (TradeLoanFacilityEntity, extends PersistentEntity) and ALL of its child tables. id = UUID
-- v7; created_at/modified_at = LocalDateTime -> `timestamp`. This is the greenfield END STATE: the close-paid-off
-- columns, the cancellation columns (CancellationDataEmb), and the @Embedded PeriodEmb effective_loan_duration are
-- folded straight into the create. The vestigial close_paid_off_transaction_ref (added then dropped historically)
-- is NOT created. disbursement_date is a LocalDate -> `date`. close_paid_off_at / cancel_date are LocalDate -> `date`.
--
-- CancellationDataEmb declares mixed-case @Column names "cancelDescription"/"cancelReason"; Spring Boot's
-- CamelCaseToUnderscoresNamingStrategy snake-cases even explicit @Column names, so the physical columns Hibernate
-- targets are cancel_description / cancel_reason (matching the prod XML migration).
-- Columns are ordered by descending storage alignment (8-byte timestamp -> 4-byte date/int -> uuid[char-aligned,
-- 16B fixed] -> variable-length last: varchar/numeric/text) to minimise per-row alignment padding on this central,
-- lifecycle-churned aggregate; `id` stays first by convention. (decimal/numeric is variable-length, so it sits in
-- the varlena tail, not the fixed-width run.) The @Embedded value-object column groupings are noted inline.
CREATE TABLE loan_facilities (
    id                              uuid           NOT NULL DEFAULT uuidv7(),
    -- 8-byte-aligned fixed-width (PersistentEntity LocalDateTime audit columns)
    created_at                      timestamp      NOT NULL,
    modified_at                     timestamp,
    -- 4-byte-aligned fixed-width (version, the LocalDate columns, effective_loan_duration PeriodEmb ints)
    version                         integer        NOT NULL,
    disbursement_date               date,
    close_paid_off_at               date,
    cancel_date                     date,
    effective_loan_duration_years   int,
    effective_loan_duration_months  int,
    effective_loan_duration_days    int,
    -- uuid (char-aligned, fixed 16 bytes): the FK/reference ids
    loan_application_id             uuid           NOT NULL,
    sanctioned_loan_id              uuid,
    loan_type_id                    uuid           NOT NULL,
    loan_arrangement_id             uuid           NOT NULL,
    installment_schedule_id         uuid,
    -- variable-length (varlena) last: varchar / numeric / text
    created_by                      varchar(255),
    modified_by                     varchar(255),
    current_state                   varchar(255)   NOT NULL,
    facility_type                   varchar(255),
    total_disbursed_amount          decimal(19, 4),
    total_disbursed_currency        varchar(3),
    total_close_paid_off_amount     decimal(19, 4),
    total_close_paid_off_currency   varchar(3),
    -- cancellation (CancellationDataEmb): camelCase @Column names snake-cased by the naming strategy
    cancel_description              text,
    cancel_reason                   text,
    CONSTRAINT pk_loan_facilities PRIMARY KEY (id),
    CONSTRAINT uq_loan_facilities_application UNIQUE (loan_application_id),
    CONSTRAINT uq_loan_facilities_sanctioned_loan UNIQUE (sanctioned_loan_id),
    CONSTRAINT fk_facilities_on_application
        FOREIGN KEY (loan_application_id) REFERENCES loan_applications (id),
    CONSTRAINT fk_facilities_on_sanctioned_loan
        FOREIGN KEY (sanctioned_loan_id) REFERENCES sanctioned_loans (id)
);

-- @ElementCollection loan_facility_accounts (Map<String,String> accountInfoMap; key=relation_type, value=account_id)
CREATE TABLE loan_facility_accounts (
    facility_id   uuid          NOT NULL,
    relation_type varchar(255)  NOT NULL,
    account_id    varchar(255),
    CONSTRAINT pk_loan_facility_accounts PRIMARY KEY (facility_id, relation_type),
    CONSTRAINT fk_facility_accounts_on_facility
        FOREIGN KEY (facility_id) REFERENCES loan_facilities (id)
);

-- @ElementCollection loan_facility_collaterals (List<CollateralEmb>). NO `percent` column (LN-59405: money-based
-- coverage). collateral_serial NOT NULL (CollateralSerialEmb override).
CREATE TABLE loan_facility_collaterals (
    loan_facility_id    uuid           NOT NULL,
    collateral_type_code varchar(255)  NOT NULL,
    description         text,
    collateral_serial   varchar(255)   NOT NULL,
    used_amount         decimal(19, 4),
    used_currency       varchar(3),
    CONSTRAINT fk_facility_collaterals_on_facility
        FOREIGN KEY (loan_facility_id) REFERENCES loan_facilities (id)
);
CREATE INDEX ix_trade_loan_facility_collaterals ON loan_facility_collaterals (loan_facility_id);

-- @ElementCollection loan_facility_disbursement_transaction_numbers (List<TransactionNumberEmb>). created_at is an
-- Instant -> timestamptz.
CREATE TABLE loan_facility_disbursement_transaction_numbers (
    loan_facility_id         uuid           NOT NULL,
    transaction_number_value varchar(255)   NOT NULL,
    created_at               timestamptz    NOT NULL,
    tracking_id              varchar(255)   NOT NULL,
    status                   varchar(255)   NOT NULL,
    CONSTRAINT fk_facility_disbursement_txns_on_facility
        FOREIGN KEY (loan_facility_id) REFERENCES loan_facilities (id)
);
-- FK-index coverage: loaded with the facility aggregate by loan_facility_id + RI-checked on facility delete.
CREATE INDEX ix_facility_disbursement_txns_facility
    ON loan_facility_disbursement_transaction_numbers (loan_facility_id);

-- @ElementCollection loan_facility_issue_contract_transaction_numbers (List<TransactionNumberEmb>)
CREATE TABLE loan_facility_issue_contract_transaction_numbers (
    loan_facility_id         uuid           NOT NULL,
    transaction_number_value varchar(255)   NOT NULL,
    created_at               timestamptz    NOT NULL,
    tracking_id              varchar(255)   NOT NULL,
    status                   varchar(255)   NOT NULL,
    CONSTRAINT fk_facility_contract_txns_on_facility
        FOREIGN KEY (loan_facility_id) REFERENCES loan_facilities (id)
);
-- FK-index coverage: loaded with the facility aggregate by loan_facility_id + RI-checked on facility delete.
CREATE INDEX ix_facility_contract_txns_facility
    ON loan_facility_issue_contract_transaction_numbers (loan_facility_id);

-- @ElementCollection cancel_loan_facility_transaction_numbers (List<TransactionNumberEmb>)
CREATE TABLE cancel_loan_facility_transaction_numbers (
    loan_facility_id         uuid           NOT NULL,
    transaction_number_value varchar(255)   NOT NULL,
    created_at               timestamptz    NOT NULL,
    tracking_id              varchar(255)   NOT NULL,
    status                   varchar(255)   NOT NULL,
    CONSTRAINT fk_cancel_facility_txns_on_facility
        FOREIGN KEY (loan_facility_id) REFERENCES loan_facilities (id)
);
-- FK-index coverage: loaded with the facility aggregate by loan_facility_id + RI-checked on facility delete.
CREATE INDEX ix_cancel_facility_txns_facility
    ON cancel_loan_facility_transaction_numbers (loan_facility_id);

-- @ElementCollection loan_facility_restructuring_transaction_numbers (List<TransactionNumberEmb>): base-loan 6.8
-- retype END STATE — the four TransactionNumberEmb columns, NOT the legacy single-String tatweel column. The entity
-- @CollectionTable declares idx_trade_loan_facility_restructuring on loan_facility_id.
CREATE TABLE loan_facility_restructuring_transaction_numbers (
    loan_facility_id         uuid           NOT NULL,
    transaction_number_value varchar(255)   NOT NULL,
    created_at               timestamptz    NOT NULL,
    tracking_id              varchar(255)   NOT NULL,
    status                   varchar(255)   NOT NULL,
    CONSTRAINT fk_facility_restructuring_txns_on_facility
        FOREIGN KEY (loan_facility_id) REFERENCES loan_facilities (id)
);
CREATE INDEX idx_trade_loan_facility_restructuring
    ON loan_facility_restructuring_transaction_numbers (loan_facility_id);

-- @ElementCollection loan_facility_close_paid_off_transaction_numbers (List<TransactionNumberEmb>)
CREATE TABLE loan_facility_close_paid_off_transaction_numbers (
    loan_facility_id         uuid           NOT NULL,
    transaction_number_value varchar(255)   NOT NULL,
    created_at               timestamptz    NOT NULL,
    tracking_id              varchar(255)   NOT NULL,
    status                   varchar(255)   NOT NULL,
    CONSTRAINT fk_close_paid_off_txns_on_facility
        FOREIGN KEY (loan_facility_id) REFERENCES loan_facilities (id)
);
-- FK-index coverage: loaded with the facility aggregate by loan_facility_id + RI-checked on facility delete.
CREATE INDEX ix_close_paid_off_txns_facility
    ON loan_facility_close_paid_off_transaction_numbers (loan_facility_id);

-- @ElementCollection loan_disbursement_records (DisbursementHistoryEmb.records : List<DisbursementRecordEmb>):
-- base-loan 6.8 repoint END STATE — keyed on (loan_facility_id, record_order), FK to loan_facilities, NO
-- sanctioned_loan_id. record_order is the @OrderColumn. disbursed_at is a LocalDate -> `date`.
CREATE TABLE loan_disbursement_records (
    loan_facility_id  uuid           NOT NULL,
    record_order      int            NOT NULL,
    disbursed_amount  decimal(19, 4) NOT NULL,
    disbursed_currency varchar(3)    NOT NULL,
    disbursed_at      date           NOT NULL,
    disbursed_by      varchar(50),
    CONSTRAINT pk_loan_disbursement_records PRIMARY KEY (loan_facility_id, record_order),
    CONSTRAINT fk_disbursement_records_on_facility
        FOREIGN KEY (loan_facility_id) REFERENCES loan_facilities (id)
);

--rollback DROP TABLE loan_disbursement_records;
--rollback DROP INDEX ix_close_paid_off_txns_facility;
--rollback DROP TABLE loan_facility_close_paid_off_transaction_numbers;
--rollback DROP TABLE loan_facility_restructuring_transaction_numbers;
--rollback DROP INDEX ix_cancel_facility_txns_facility;
--rollback DROP TABLE cancel_loan_facility_transaction_numbers;
--rollback DROP INDEX ix_facility_contract_txns_facility;
--rollback DROP TABLE loan_facility_issue_contract_transaction_numbers;
--rollback DROP INDEX ix_facility_disbursement_txns_facility;
--rollback DROP TABLE loan_facility_disbursement_transaction_numbers;
--rollback DROP TABLE loan_facility_collaterals;
--rollback DROP TABLE loan_facility_accounts;
--rollback DROP TABLE loan_facilities;
