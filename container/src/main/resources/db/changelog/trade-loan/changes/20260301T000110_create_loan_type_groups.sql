--liquibase formatted sql

--changeset trade-loan:20260301T000110-create-loan-type-groups logicalFilePath:trade-loan/20260301T000110-create-loan-type-groups
-- Loan-type group catalogue (LoanTypeGroupEntity, extends PersistentEntity) — a flat category node holding a soft,
-- self-referencing parent (parent_group_id, no FK: the tree is assembled on the read side and ordering/back-fill must
-- not be constrained). id is UUID v7 (uuidv7(), PG18-native). created_at / modified_at are PersistentEntity
-- LocalDateTime fields -> `timestamp` (NOT timestamptz). Reference data: low write volume, no fillfactor/BRIN.
CREATE TABLE loan_type_groups (
    id              uuid          NOT NULL DEFAULT uuidv7(),
    version         integer       NOT NULL,
    created_at      timestamp     NOT NULL,
    modified_at     timestamp,
    created_by      varchar(255),
    modified_by     varchar(255),
    title           varchar(255)  NOT NULL,
    parent_group_id uuid,
    CONSTRAINT pk_loan_type_groups PRIMARY KEY (id)
);
CREATE INDEX ix_loan_type_groups_parent ON loan_type_groups (parent_group_id);

--rollback DROP TABLE loan_type_groups;
