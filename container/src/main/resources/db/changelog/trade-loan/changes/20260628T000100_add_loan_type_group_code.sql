--liquibase formatted sql

--changeset trade-loan:20260628T000100-add-loan-type-group-code logicalFilePath:trade-loan/20260628T000100-add-loan-type-group-code
-- LN-59559: every loan-type group carries a REQUIRED, globally-UNIQUE, IMMUTABLE business code (caller-supplied on
-- create, like loan-type codes). Add the column nullable first, backfill any pre-existing rows deterministically from
-- their id (no-op on a fresh table), enforce NOT NULL, then add the unique index. The index name matches the entity's
-- @UniqueConstraint(name = "uq_loan_type_groups_code"). Code is never updated (no rename-of-code use case).
ALTER TABLE loan_type_groups ADD COLUMN code varchar(64);
UPDATE loan_type_groups SET code = 'AUTO-' || substr(replace(id::text, '-', ''), 1, 12) WHERE code IS NULL;
ALTER TABLE loan_type_groups ALTER COLUMN code SET NOT NULL;
CREATE UNIQUE INDEX uq_loan_type_groups_code ON loan_type_groups (code);

--rollback DROP INDEX uq_loan_type_groups_code;
--rollback ALTER TABLE loan_type_groups DROP COLUMN code;
