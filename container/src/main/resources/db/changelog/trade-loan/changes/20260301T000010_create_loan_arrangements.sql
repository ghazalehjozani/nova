--liquibase formatted sql

--changeset trade-loan:20260301T000010-create-loan-arrangements logicalFilePath:trade-loan/20260301T000010-create-loan-arrangements
-- Loan-arrangement product rules (TradeLoanArrangementEntity, extends PersistentEntity). All columns below are the
-- flattened @Embedded policy embeddables (Interest/Penalty/Installment/GracePeriod/RepaymentPriority/Regulatory/
-- Collateral policies + AmountRangeEmb + PeriodRangeEmb + EconomicSector/CurrencyType). id = UUID v7.
-- PersistentEntity created_at/modified_at = LocalDateTime -> `timestamp`. Reference data: no fillfactor/BRIN.
CREATE TABLE loan_arrangements (
    id                                    uuid           NOT NULL DEFAULT uuidv7(),
    version                               integer        NOT NULL,
    created_at                            timestamp      NOT NULL,
    modified_at                           timestamp,
    created_by                            varchar(255),
    modified_by                           varchar(255),
    code                                  varchar(255)   NOT NULL,
    active                                boolean         NOT NULL,
    disable                               boolean         NOT NULL,
    guarantor_count                       int,
    party_type                            varchar(255)   NOT NULL,
    has_installment_card                  boolean         NOT NULL,
    life_insurance_payment_type           varchar(255)   NOT NULL,
    loan_secondary_type                   varchar(255)   NOT NULL,
    section_type                          varchar(255)   NOT NULL,
    previous_version_id                   uuid,
    title                                 varchar(255)   NOT NULL,
    currency                              varchar(255),
    economic_sector_code                  varchar(255)   NOT NULL,
    disbursement_type                     varchar(255),
    -- AmountRangeEmb
    min_amount                            decimal(19, 4),
    max_amount                            decimal(19, 4),
    amount_currency                       varchar(255),
    -- PeriodRangeEmb (durationRange)
    min_years                             int,
    min_months                            int,
    min_days                              int,
    max_years                             int,
    max_months                            int,
    max_days                              int,
    -- InterestPolicyEmb
    base_interest_rate                    decimal(10, 6),
    preferential_min_rate                 decimal(10, 6),
    preferential_max_rate                 decimal(10, 6),
    interest_formula                      text,
    refund_interest_formula               text,
    daily_interest                        boolean,
    -- PenaltyPolicyEmb
    penalty_rate                          decimal(10, 6),
    deferral_interest_rate                decimal(10, 6),
    penalty_formula                       text,
    penalty_payment_type                  varchar(255),
    -- InstallmentPolicyEmb
    installment_period_days               int,
    installment_formula                   text,
    interest_component_formula            text,
    installment_payment_type              varchar(255),
    -- GracePeriodPolicyEmb. The entity declares @Column(name="gracePeriodFormula"); Spring Boot's
    -- CamelCaseToUnderscoresNamingStrategy snake-cases it to grace_period_formula (the column Hibernate targets).
    min_grace_period_days                 int,
    max_grace_period_days                 int,
    grace_period_formula                  text,
    -- RepaymentPriorityPolicyEmb
    installment_main_amount_priority      int,
    installment_interest_amount_priority  int,
    installment_penalty_amount_priority   int,
    installment_income_amount_priority    int,
    insurance_amount_priority             int,
    insurance_penalty_amount_priority     int,
    has_equal_priority                    boolean,
    -- RegulatoryCompliancePolicyEmb
    overdue_period_days                   int,
    deferral_period_days                  int,
    suspicious_period_days                int,
    -- CollateralPolicyEmb (scalar part; collateral_types is a side table)
    collateral_total_percent              int,
    collateral_calculation_type           varchar(255)   NOT NULL,
    CONSTRAINT pk_loan_arrangements PRIMARY KEY (id),
    CONSTRAINT uq_loan_arrangements_code UNIQUE (code)
);

-- @ElementCollection loan_arrangement_collateral_types (CollateralPolicyEmb.collateralTypes : Set<CollateralType>)
CREATE TABLE loan_arrangement_collateral_types (
    loan_arrangement_id uuid          NOT NULL,
    collateral_code     varchar(255),
    CONSTRAINT fk_loan_arrangement_collateral_types_on_arrangement
        FOREIGN KEY (loan_arrangement_id) REFERENCES loan_arrangements (id)
);
CREATE INDEX ix_arrangement_collateral ON loan_arrangement_collateral_types (loan_arrangement_id);

-- @ElementCollection loan_arrangement_confirm_types (List<ConfirmTypeEmb> -> confirm_person_code)
CREATE TABLE loan_arrangement_confirm_types (
    loan_arrangement_id uuid          NOT NULL,
    confirm_person_code varchar(255),
    CONSTRAINT fk_loan_arrangement_confirm_types_on_arrangement
        FOREIGN KEY (loan_arrangement_id) REFERENCES loan_arrangements (id)
);
-- FK-index coverage: Postgres does NOT auto-index a FK referencing column. Without this index a parent
-- loan_arrangements delete (or any RI check) seqscans the whole child table and takes a stronger lock.
CREATE INDEX ix_loan_arrangement_confirm_types_arrangement
    ON loan_arrangement_confirm_types (loan_arrangement_id);

--rollback DROP INDEX ix_loan_arrangement_confirm_types_arrangement;
--rollback DROP TABLE loan_arrangement_confirm_types;
--rollback DROP TABLE loan_arrangement_collateral_types;
--rollback DROP TABLE loan_arrangements;
