--liquibase formatted sql

--changeset trade-loan:20260301T000000-create-loan-types logicalFilePath:trade-loan/20260301T000000-create-loan-types
-- Loan-type product catalogue (TradeLoanTypeEntity, extends PersistentEntity) + its EAGER element-collection side
-- tables. id is UUID v7 (uuidv7(), PG18-native) to match the platform Identity.generateV7() surrogate-key model.
-- created_at / modified_at are PersistentEntity LocalDateTime fields -> `timestamp` (NOT timestamptz). Reference
-- data: low write volume, no fillfactor/BRIN.
CREATE TABLE loan_types (
    id                       uuid          NOT NULL DEFAULT uuidv7(),
    version                  integer       NOT NULL,
    created_at               timestamp     NOT NULL,
    modified_at              timestamp,
    created_by               varchar(255),
    modified_by              varchar(255),
    gateway_type             varchar(255)  NOT NULL,
    loan_application_allowed boolean        NOT NULL,
    group_id                 uuid,
    active                   boolean        NOT NULL,
    disable                  boolean        NOT NULL,
    previous_version_id      uuid,
    loan_type_code           varchar(255)  NOT NULL,
    title                    varchar(255)  NOT NULL,
    edit_reason              varchar(500),
    CONSTRAINT pk_loan_types PRIMARY KEY (id),
    CONSTRAINT uq_loan_types_loan_type_code UNIQUE (loan_type_code)
);

-- @ElementCollection loan_type_arrangement_ids (Set<UUID> loanArrangementIds)
CREATE TABLE loan_type_arrangement_ids (
    loan_type_id   uuid NOT NULL,
    arrangement_id uuid NOT NULL,
    CONSTRAINT fk_loan_type_arrangement_ids_on_loan_type
        FOREIGN KEY (loan_type_id) REFERENCES loan_types (id)
);
CREATE INDEX ix_loan_type_arrangement ON loan_type_arrangement_ids (loan_type_id);

-- @ElementCollection loan_type_economic_sectors (Set<EconomicSectorCurrencyEmb>); jsonb currency-type map
CREATE TABLE loan_type_economic_sectors (
    loan_type_id                   uuid          NOT NULL,
    economic_sector_code           varchar(255)  NOT NULL,
    economic_sector_currency_types jsonb,
    CONSTRAINT fk_loan_type_economic_sectors_on_loan_type
        FOREIGN KEY (loan_type_id) REFERENCES loan_types (id)
);
CREATE INDEX ix_loan_type_economic_sector ON loan_type_economic_sectors (loan_type_id);

-- @ElementCollection loan_type_income_ids (Set<UUID> incomeIds)
CREATE TABLE loan_type_income_ids (
    loan_type_id uuid NOT NULL,
    income_id    uuid,
    CONSTRAINT fk_loan_type_income_ids_on_loan_type
        FOREIGN KEY (loan_type_id) REFERENCES loan_types (id)
);
CREATE INDEX ix_loan_type_income ON loan_type_income_ids (loan_type_id);

-- @ElementCollection loan_type_topics (Set<RelationTypeLoanTopicEmb>); jsonb economic-sector set
CREATE TABLE loan_type_topics (
    loan_type_id                uuid          NOT NULL,
    trade_relation_type         varchar(255)  NOT NULL,
    topic_name                  varchar(255)  NOT NULL,
    topic_code                  varchar(255)  NOT NULL,
    loan_topic_economic_sectors jsonb,
    CONSTRAINT fk_loan_type_topics_on_loan_type
        FOREIGN KEY (loan_type_id) REFERENCES loan_types (id)
);
CREATE INDEX ix_loan_type_topic ON loan_type_topics (loan_type_id);

--rollback DROP TABLE loan_type_topics;
--rollback DROP TABLE loan_type_income_ids;
--rollback DROP TABLE loan_type_economic_sectors;
--rollback DROP TABLE loan_type_arrangement_ids;
--rollback DROP TABLE loan_types;
