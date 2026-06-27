# LN-59559 — Operationalize the `LoanTypeGroup` aggregate

**Date:** 2026-06-27
**Jira:** LN-59559 (Task) → epic LN-59558 → summer epic CBS-284638
**Status:** Approved design, ready for implementation plan
**Repos touched:** `base-loan`, `nova`, `nova-testkit`
**Out of scope:** FCB / corridor sync, M:N membership, group-level formula params.

## 1. Problem

`LoanTypeGroup` is a domain skeleton in `base-loan`
(`…/core/domain/loantypegroup/aggregate/LoanTypeGroup.java`): it carries a `Title`,
a `parentGroup`/`childGroups` tree, and a `loanTypeIds` list, but has **no** factory,
behavior, invariants, events, persistence, REST, or query. `AbstractLoanType` already
holds a back-reference `groupId` (`loan_types.group_id` column exists, currently NULL for
all rows). The catalog cannot create groups, build a category tree, or assign loan types
to a group.

The DB (`nova`, `10.100.7.20:5433`) currently has 4 ungrouped loan types:

| loan_type_code | title |
|----------------|-------|
| 1404 | تسهیلات کوتاه‌مدت |
| 202601 | نوع تسهیلات بارگذاری ۲۰۲۶۰۱ (seed) |
| 202602 | نوع تسهیلات بارگذاری ۲۰۲۶۰۲ (seed) |
| 202603 | نوع تسهیلات بارگذاری ۲۰۲۶۰۳ (seed) |

## 2. Goal

Operationalize `LoanTypeGroup` end-to-end (command / event / persistence / REST / query)
so an admin can build a category tree and assign loan types to groups, mirroring the
existing `TradeLoanType` stack. Nova-only.

## 3. Key design decisions

### 3.1 Membership authority — `loan_types.group_id`, 1:N
Each loan type belongs to **at most one** group. `loan_types.group_id` is the source of
truth. A loan type's group is set/cleared by a command that loads the `TradeLoanType`
aggregate and updates its `groupId` (reusing the existing loan-type repository). The
group aggregate does **not** own a member list in its write model.

### 3.2 Write aggregate = flat node, read model = nested tree
The write aggregate holds only `id`, `title`, `parentGroupId` (nullable), `active`, and
optimistic `version`. It does **not** embed child groups or member loan types — so
renaming or moving a node never loads the whole subtree, and the aggregate boundary stays
small. The skeleton's embedded `childGroups` + `loanTypeIds` shape becomes the **query
DTO**, assembled on the read side from flat rows (`parent_group_id`) and
`loan_types.group_id`.

### 3.3 No `TradeLoanTypeGroup` wrapper
`TradeLoanType` exists because the trade context adds topics/relation-types. A group is
pure categorization with no trade-specific fields, so nova ports reference the base-loan
`LoanTypeGroup` aggregate directly. Introduce a wrapper only if trade-specific group
fields later appear.

### 3.4 Cycle guard
A node may not move under itself or any of its descendants. The aggregate only holds
`parentGroupId`, so the command handler walks the ancestor chain of the proposed new
parent via the repository and rejects if the moved node appears in it.

## 4. Components

### 4.1 base-loan/domain — `…/loantypegroup`
- `LoanTypeGroup`: replace embedded `parentGroup`/`childGroups` with `parentGroupId`
  (`LoanTypeGroupId`, nullable); drop `loanTypeIds` from the write aggregate. Add
  factory `create(title, parentGroupId?)` and behaviors `rename(Title)`,
  `moveUnder(@Nullable LoanTypeGroupId)`. Invariants: title non-blank; `parentGroupId`
  ≠ own id (no self-parent). Record domain events on each mutation.
- `LoanTypeGroupErrors` (error catalog, i18n keys).
- Domain events: `LoanTypeGroupCreated`, `LoanTypeGroupRenamed`, `LoanTypeGroupMoved`
  (+ membership events emitted from the loan-type side — see 4.3).
- Domain unit tests: factory, rename, move, self-parent rejection.

### 4.2 nova core/application/ports
- **outbound** `LoanTypeGroupRepository`: `save`, `findById`, `findChildren(parentId)`,
  `findRoots`, `existsById`, `findAncestorChain(id)` (for the cycle guard).
- **inbound**: command contracts (Create/Rename/Move/Assign/Unassign) and query
  contracts (GetTree/GetById/ListRoots).

### 4.3 nova core/application/service — command handlers
WriteCommandHandler per pangaea workflow model; each applies the invariant-checked change,
persists via the port, and emits its event to the outbox.
- `CreateLoanTypeGroup` (title, parentGroupId?)
- `RenameLoanTypeGroup` (groupId, title)
- `MoveLoanTypeGroup` (groupId, newParentGroupId?) — cycle-guarded
- `AssignLoanTypeToGroup` (loanTypeId, groupId) — loads `TradeLoanType`, sets `groupId`,
  emits `LoanTypeAssignedToGroup`
- `UnassignLoanTypeFromGroup` (loanTypeId) — clears `groupId`, emits
  `LoanTypeUnassignedFromGroup`

### 4.4 nova core/application/query — read side
- `GetLoanTypeGroupTree`: load flat group rows + `loan_types.group_id`, assemble the
  nested DTO (group → childGroups → loanTypeIds), matching the skeleton's getter shape.
- `GetLoanTypeGroupById`, `ListRootGroups`.

### 4.5 nova adapters/driven/persistence
- New table `loan_type_groups` (`id, version, title, parent_group_id, active,
  created_at, modified_at, created_by, modified_by`) + `loan_type_group_outbox_events`
  / `loan_type_group_outbox_metadata`, mirroring the `loan_type_outbox_*` shape. DB
  migration script in the project's migration mechanism.
- `LoanTypeGroupRepositoryAdapter` (JPA) implementing the outbound port.
- Membership uses the existing `loan_types.group_id` column (no schema change there).

### 4.6 nova adapters/driving/rest
- `DefineLoanTypeGroupController` (command): `POST /v1/loan-type-groups`,
  `PATCH /v1/loan-type-groups/{id}` (rename), `PATCH /v1/loan-type-groups/{id}/parent`
  (move), `PUT /v1/loan-types/{loanTypeId}/group` (assign),
  `DELETE /v1/loan-types/{loanTypeId}/group` (unassign).
- `LoanTypeGroupQueryController`: `GET /v1/loan-type-groups/tree`,
  `GET /v1/loan-type-groups/{id}`, `GET /v1/loan-type-groups` (roots).
- SWA-101 envelope; command headers (Idempotency-Key / X-Correlation-ID /
  X-Request-DateTime) per the workspace header policy; query headers for reads.

### 4.7 nova-testkit
- `make generate` regenerates `bruno/reference/` from the new OpenAPI doc — new endpoints
  gain contract + envelope asserts automatically (never hand-edited).
- New curated journey `bruno/e2e/NN-loan-type-group/`: create root «تسهیلات» → child
  «کوتاه‌مدت» + «بارگذاری ۱۴۰۵» → resolve the 4 real loan-type ids by code via the
  loan-type query → assign `1404`→کوتاه‌مدت and `202601/2/3`→بارگذاری → `GET /tree`,
  assert tree structure + membership. Repeatable (idempotent on re-run).
- k6: add a read scenario hitting `GET /v1/loan-type-groups/tree`.

## 5. Testing & gates
- base-loan domain unit tests (invariants + self-parent).
- nova command-handler tests (each handler, incl. cycle-guard rejection) + query-handler
  test (tree assembly).
- ArchUnit suites stay green (`base-loan/architecture-tests`, `nova/architecture-tests`).
- testkit: contract/drift gate + the new e2e journey.
- `mvn compile` (format plugin) on every touched module before any commit.

## 6. Risks
- **Cycle guard correctness** under concurrent moves — guard reads the ancestor chain
  inside the write transaction; optimistic `version` on the moved node prevents lost
  updates.
- **Orphaned members** if a group is deleted — v1 has no delete command (only
  create/rename/move/assign/unassign), so this cannot occur yet. A future delete must
  guard against non-empty groups.
