--liquibase formatted sql

--changeset trade-loan:20260301T000020-create-loan-applications logicalFilePath:trade-loan/20260301T000020-create-loan-applications
-- Loan application (TradeLoanApplicationEntity, extends PersistentEntity) + its parties side table.
-- id = UUID v7. created_at/modified_at = LocalDateTime -> `timestamp`. request_date is an Instant entity field
-- (requestDate) -> `timestamptz` (this is the one true point-in-time column on this table; the prod XML had it as
-- DATETIME, corrected here). Columns are the flattened @Embedded value objects (Money/Period/Branch/Party/Samat/...).
-- Columns are ordered by descending storage alignment (8-byte timestamp/timestamptz -> 4-byte integer ->
-- variable-length last: varchar / numeric / text) to minimise per-row alignment padding; `id` stays first by
-- convention. (decimal/numeric is variable-length, so requested_amount sits in the varlena tail.) The @Embedded
-- value-object groupings are noted inline.
CREATE TABLE loan_applications (
    id                                uuid           NOT NULL DEFAULT uuidv7(),
    -- 8-byte-aligned fixed-width (PersistentEntity LocalDateTime audit columns + the requestDate Instant)
    created_at                        timestamp      NOT NULL,
    modified_at                       timestamp,
    request_date                      timestamptz    NOT NULL,
    -- 4-byte-aligned fixed-width (version + the PeriodEmb/GracePeriodEmb/InstallmentCountEmb ints)
    version                           integer        NOT NULL,
    requested_duration_years          int,
    requested_duration_months         int,
    requested_duration_days           int,
    grace_period_days                 int,
    grace_period_months               int,
    grace_period_years                int,
    installment_count                 int            NOT NULL,
    -- variable-length (varlena) last: varchar / numeric / text
    created_by                        varchar(255),
    modified_by                       varchar(255),
    applicant_channel                 varchar(255)   NOT NULL,
    disbursement_method               varchar(255),
    currency                          varchar(255),
    -- requestedAmount MoneyEmb (override)
    requested_amount                  decimal(19, 4),
    requested_currency                varchar(3),
    -- DisburseDestinationEmb
    deposit_number                    varchar(255),
    account_number                    varchar(255),
    disburse_destination_type         varchar(255)   NOT NULL,
    -- EconomicSectorEmb / BranchEmb
    economic_sector_code              varchar(255)   NOT NULL,
    branch_code                       varchar(255)   NOT NULL,
    -- RequestReasonEmb / SubSourceEmb / DescriptionEmb / CredibilityRankEmb
    request_reason_code               varchar(500),
    sub_source_code                   varchar(500),
    description                       text,
    credibility_rank                  varchar(255),
    -- ApplicationNumberEmb (branch / loan-type-code / party / derived value)
    application_branch_code           varchar(255)   NOT NULL,
    loan_type_code                    varchar(255)   NOT NULL,
    application_customer_number       varchar(255)   NOT NULL,
    application_customer_type         varchar(255)   NOT NULL,
    application_customer_first_name   varchar(255),
    application_customer_last_name    varchar(255),
    application_customer_company_name varchar(255),
    application_customer_role         varchar(255)   NOT NULL,
    derived_value                     varchar(255)   NOT NULL,
    -- SamatEmb
    tracking_number                   varchar(16),
    isic_economic_sector              varchar(255),
    sub_isic_economic_sector          varchar(255),
    use_type                          varchar(255),
    consumption_place_code            varchar(255),
    exception_code                    varchar(255),
    CONSTRAINT pk_loan_applications PRIMARY KEY (id),
    CONSTRAINT uq_loan_application_natural_key
        UNIQUE (application_branch_code, loan_type_code, application_customer_number, derived_value)
);

-- @ElementCollection loan_application_parties (Set<PartyEmb>)
CREATE TABLE loan_application_parties (
    loan_application_id  uuid          NOT NULL,
    customer_number      varchar(255)  NOT NULL,
    party_type           varchar(255)  NOT NULL,
    party_role           varchar(255)  NOT NULL,
    first_name           varchar(255),
    last_name            varchar(255),
    company_name         varchar(255),
    guarantee_percentage decimal(5, 2),
    CONSTRAINT fk_application_parties_on_application
        FOREIGN KEY (loan_application_id) REFERENCES loan_applications (id)
);
CREATE INDEX ix_application_parties_app_id ON loan_application_parties (loan_application_id);

--rollback DROP TABLE loan_application_parties;
--rollback DROP TABLE loan_applications;
