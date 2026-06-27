# LoanTypeGroup Implementation Plan (LN-59559)

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Operationalize the `LoanTypeGroup` aggregate end-to-end (domain behavior, events, persistence, REST command + query) so an admin can build a loan-type category tree and assign loan types to groups — mirroring the existing `TradeLoanType` stack, Nova-only.

**Architecture:** Hexagonal + DDD + CQRS. `LoanTypeGroup` is a base-loan shared-kernel aggregate holding a **flat** node (`id`, `title`, `parentGroupId`, `version`) — the parent/child *tree* and member loan-types are assembled on the **read side** only. Membership is 1:N, authoritative on `loan_types.group_id` (set by a behavior added to `AbstractLoanType`). Writes go through pangaea `WorkflowCommandHandler` + `Workflow.singleWrite` + `PublishingWriteActivity` (events drained from the aggregate to a per-aggregate outbox). Reads go through `QueryHandler`/`QueryDispatcher` returning SWA-101-enveloped DTOs.

**Tech Stack:** Java 25, Maven, Spring Boot 4, pangaea (`ir.dotin.platform.pangaea:*`), base-loan (`ir.dotin.loan:base-loan-*`), MapStruct, JPA, Liquibase, springdoc; nova-testkit (Bruno OpenCollection + k6).

## Global Constraints

- **No inline fully-qualified Java imports** — add an `import`; static imports only when they shorten meaningfully. Applies to tests too.
- **No code comments / no Javadoc** — minimal only if genuinely cryptic.
- **JSpecify nullness** — modules are `@NullMarked` at `module-info`; annotate nullable refs with `org.jspecify.annotations.Nullable`.
- **Hexagonal/DDD/ArchUnit** — domain (base-loan) carries zero framework/adapter deps; driving adapters MUST NOT depend on outbound ports. `base-loan/architecture-tests` + `nova/architecture-tests` must stay green.
- **`mvn -pl <module> compile` pulls reactor deps** (`-am` is preset). Run `mvn compile` on a touched nova module (or `mvn -pl <module> -am compile` from `nova/`) before any `git add` — the spotless format plugin runs at `compile`.
- **base-loan changes:** build/install base-loan locally (`mvn install` from `base-loan/`, carries SNAPSHOT `${revision}`) so nova's reactor sees the new domain symbols. Do NOT pass `-Drevision`.
- **Cross-cutting headers are invisible to controllers** — never `@RequestHeader` Idempotency-Key / X-Correlation-ID / X-Request-DateTime; read idempotency via `BaseController.getIdempotencyKey()`.
- **SWA-101 envelope:** queries return `BaseResponse<T>` (`resultData`/`errorList`); commands return `Void` via `CommandResponseFactory` (`201 Created` + `Location`, or `204`).
- **Enums on the wire** serialize as `{code,label}` via the platform `LocalizedEnum` serializer when the DTO carries the enum type; request DTOs accept bare string codes.
- **Commit subject (loan repos):** `feat(CBS-284638:LN-59558:LN-59559): <subject>`. nova-testkit uses `feat(LN-59559): <subject>`. Commit only when the user asks.
- **Do NOT run FCB builds.** No FCB changes in this plan (Nova-only).

## File-structure map

```
base-loan/domain/.../core/domain/
  loantypegroup/
    aggregate/LoanTypeGroup.java            (REWORK: flat node + factory + rename + moveUnder)
    event/LoanTypeGroupEvents.java          (NEW sealed)
    event/LoanTypeGroupEventType.java       (NEW)
    event/LoanTypeGroupCreated.java         (NEW)
    event/LoanTypeGroupRenamed.java         (NEW)
    event/LoanTypeGroupMoved.java           (NEW)
    error/LoanTypeGroupErrors.java          (NEW)
  loantype/entity/AbstractLoanType.java     (MODIFY: groupId non-final + assignToGroup/removeFromGroup + abstract event hooks)

nova/core/domain/.../trade/core/domain/loantype/event/
  TradeLoanTypeGroupAssigned.java           (NEW)
  TradeLoanTypeGroupRemoved.java            (NEW)
  TradeLoanTypeEvents.java                  (MODIFY permits)
  TradeLoanTypeEventType.java               (MODIFY add GROUP_ASSIGNED/REMOVED)
nova/core/domain/.../trade/core/domain/loantype/entity/TradeLoanType.java (MODIFY: implement event hooks)

nova/core/application/ports/inbound/.../command/
  CreateLoanTypeGroupCommand / RenameLoanTypeGroupCommand / MoveLoanTypeGroupCommand
  AssignLoanTypeToGroupCommand / RemoveLoanTypeFromGroupCommand            (NEW records)
nova/core/application/ports/outbound/.../command/repository/
  LoanTypeGroupRepository.java              (NEW)
nova/core/application/ports/outbound/.../query/ (or query module)
  LoanTypeGroupQueryRepository.java         (NEW)

nova/core/application/service/.../loantypegroup/
  commandhandler/{Create,Rename,Move}LoanTypeGroupCommandHandler.java
  step/{Create,Rename,Move}LoanTypeGroupStep.java
  data/{Create,Rename,Move}LoanTypeGroupData.java
nova/core/application/service/.../assignloantypegroup/
  commandhandler/{Assign,Remove}...CommandHandler.java + step/ + data/

nova/core/application/query/.../loantypegroup/
  request/{GetLoanTypeGroupTreeQuery,GetLoanTypeGroupByIdQuery,ListRootLoanTypeGroupsQuery}.java
  handler/{...}QueryHandler.java
  dto/{LoanTypeGroupTreeDto,LoanTypeGroupNodeDto,LoanTypeRefDto}.java

nova/adapters/driven/persistence/.../loantypegroup/
  entity/LoanTypeGroupEntity.java + repository/LoanTypeGroupJpaRepository.java
  mapper/LoanTypeGroupPersistenceMapper.java + LoanTypeGroupRepositoryAdapter.java
  outbox/{Entity,Mapper,Repository,Handler}.java
  query/{LoanTypeGroupQueryRepositoryAdapter, query mapper, membership projection}

nova/adapters/driving/contract/.../dto/
  CreateLoanTypeGroupRequest / RenameLoanTypeGroupRequest / MoveLoanTypeGroupRequest
  AssignLoanTypeToGroupRequest                                            (NEW)
nova/adapters/driving/rest/.../command/controller/DefineLoanTypeGroupController.java (NEW)
nova/adapters/driving/rest/.../query/loantypegroup/LoanTypeGroupQueryController.java (NEW)
nova/adapters/driving/rest/.../config/SwaggerConfig.java                 (MODIFY: tag constants)

nova/container/src/main/resources/db/changelog/trade-loan/
  changes/20260301T000110_create_loan_type_groups.sql                    (NEW)
  db.changelog-trade-loan.xml                                            (MODIFY include)

nova-testkit/
  bruno/e2e/09-loan-type-groups/{folder.yml,01..NN.yml}                  (NEW)
  bruno/reference/loan-type-groups-*/                                    (GENERATED)
  Makefile                                                               (MODIFY target)
  k6/journeys/loan-type-groups-read.js + k6/journeys/registry.js         (NEW/MODIFY)
```

---

## Phase 0 — base-loan domain

### Task 1: `LoanTypeGroupErrors` error catalog

**Files:**
- Create: `base-loan/domain/src/main/java/ir/dotin/loan/baseloan/core/domain/loantypegroup/error/LoanTypeGroupErrors.java`
- Read first: `base-loan/.../loantype/error/LoanTypeErrors.java`, `.../shared/error/LoanErrorCategory.java` (confirm the `ProductErrorCode` numbering contract and whether a per-category sequence space exists).

**Interfaces — Produces:** `LoanTypeGroupErrors` enum constants used by Tasks 3, 10: `TITLE_REQUIRED`, `SELF_PARENT_NOT_ALLOWED`, `PARENT_NOT_FOUND`, `GROUP_NOT_FOUND`, `CYCLE_NOT_ALLOWED`.

- [ ] **Step 1: Read the templates.** Open `LoanTypeErrors.java` and `LoanErrorCategory.java`. Confirm: constructor is `(ErrorCategory category, int sequence, String messageFormat)`; pick a category. If `LoanErrorCategory` has a `LOAN_TYPE_GROUP` constant use it; else add one mirroring the existing constants (own, low-risk change) — otherwise reuse `PlatformErrorCategory.VALIDATION`/`STATE_CONFLICT`/`NOT_FOUND` and a sequence range not used by other constants of that category.

- [ ] **Step 2: Write the enum.**

```java
package ir.dotin.loan.baseloan.core.domain.loantypegroup.error;

import org.jspecify.annotations.NonNull;

import ir.dotin.platform.pangaea.commons.core.error.ErrorCategory;
import ir.dotin.platform.pangaea.commons.core.error.PlatformErrorCategory;
import ir.dotin.platform.pangaea.commons.core.error.ProductErrorCode;

public enum LoanTypeGroupErrors implements ProductErrorCode<LoanTypeGroupErrors> {

    TITLE_REQUIRED(PlatformErrorCategory.VALIDATION, 1, "Loan type group title is required."),
    SELF_PARENT_NOT_ALLOWED(PlatformErrorCategory.VALIDATION, 2,
            "A loan type group (ID: {0}) cannot be its own parent."),
    PARENT_NOT_FOUND(PlatformErrorCategory.NOT_FOUND, 3,
            "Parent loan type group (ID: {0}) does not exist."),
    GROUP_NOT_FOUND(PlatformErrorCategory.NOT_FOUND, 4,
            "Loan type group (ID: {0}) does not exist."),
    CYCLE_NOT_ALLOWED(PlatformErrorCategory.STATE_CONFLICT, 5,
            "Moving group (ID: {0}) under (ID: {1}) would create a cycle.");

    private final ErrorCategory category;
    private final int sequence;
    private final String defaultMessageFormat;

    LoanTypeGroupErrors(ErrorCategory category, int sequence, String defaultMessageFormat) {
        this.category = category;
        this.sequence = sequence;
        this.defaultMessageFormat = defaultMessageFormat;
        category.validate();
    }

    @Override public ErrorCategory category() { return category; }
    @Override public int sequence() { return sequence; }
    @Override public @NonNull String getDefaultMessageFormat() { return defaultMessageFormat; }
}
```
(Adjust `ErrorCategory`/`ProductErrorCode` import FQNs to whatever `LoanTypeErrors.java` actually imports — copy them verbatim from that file.)

- [ ] **Step 3: Compile base-loan.**

Run: `cd /home/m.amirabdollahi/workspaces/base-loan && mvn -q -pl domain -am compile`
Expected: BUILD SUCCESS.

- [ ] **Step 4: Commit.**

```bash
git -C /home/m.amirabdollahi/workspaces/base-loan add domain/src/main/java/ir/dotin/loan/baseloan/core/domain/loantypegroup/error/LoanTypeGroupErrors.java
git -C /home/m.amirabdollahi/workspaces/base-loan commit -m "feat(CBS-284638:LN-59558:LN-59559): add LoanTypeGroupErrors"
```

---

### Task 2: `LoanTypeGroup` domain events

**Files:**
- Create: `…/loantypegroup/event/LoanTypeGroupEventType.java`, `LoanTypeGroupEvents.java`, `LoanTypeGroupCreated.java`, `LoanTypeGroupRenamed.java`, `LoanTypeGroupMoved.java`
- Test: `…/test/…/loantypegroup/event/LoanTypeGroupEventTest.java`

**Interfaces — Produces:** `LoanTypeGroupCreated.of(id, title, parentGroupId, clock)`, `LoanTypeGroupRenamed.of(id, title, clock)`, `LoanTypeGroupMoved.of(id, newParentGroupId, clock)`; all implement `LoanTypeGroupEvents<T>` whose `aggregateType()` returns `LoanTypeGroup.class`. Consumed by Task 3.

- [ ] **Step 1: Write the failing test.**

```java
package ir.dotin.loan.baseloan.core.domain.loantypegroup.event;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTypeGroupId;

import static org.assertj.core.api.Assertions.assertThat;

final class LoanTypeGroupEventTest {

    private final Clock clock = Clock.fixed(Instant.parse("2026-06-27T00:00:00Z"), ZoneOffset.UTC);

    @Test
    void created_event_carries_aggregate_id_and_type() {
        LoanTypeGroupId id = LoanTypeGroupId.generate();
        LoanTypeGroupCreated event = LoanTypeGroupCreated.of(id, "تسهیلات", null, clock);

        assertThat(event.aggregateId()).isEqualTo(id.value());
        assertThat(event.aggregateType()).isEqualTo(
                ir.dotin.loan.baseloan.core.domain.loantypegroup.aggregate.LoanTypeGroup.class);
        assertThat(event.createdAt()).isEqualTo(Instant.parse("2026-06-27T00:00:00Z"));
        assertThat(event.eventType()).isEqualTo(LoanTypeGroupEventType.CREATED.getFullType());
    }
}
```

- [ ] **Step 2: Run, verify it fails.**

Run: `cd /home/m.amirabdollahi/workspaces/base-loan && mvn -q -pl domain -am test -Dtest=LoanTypeGroupEventTest`
Expected: FAIL — `LoanTypeGroupCreated` / `LoanTypeGroupEventType` not found.

- [ ] **Step 3: Write `LoanTypeGroupEventType`.**

```java
package ir.dotin.loan.baseloan.core.domain.loantypegroup.event;

public enum LoanTypeGroupEventType {
    CREATED("loan-type-group.created"),
    RENAMED("loan-type-group.renamed"),
    MOVED("loan-type-group.moved");

    private final String fullType;

    LoanTypeGroupEventType(String fullType) { this.fullType = fullType; }

    public String getFullType() { return fullType; }
}
```

- [ ] **Step 4: Write the sealed `LoanTypeGroupEvents` interface.**

```java
package ir.dotin.loan.baseloan.core.domain.loantypegroup.event;

import ir.dotin.loan.baseloan.core.domain.loantypegroup.aggregate.LoanTypeGroup;
import ir.dotin.platform.pangaea.commons.domain.event.DomainEvent;

public sealed interface LoanTypeGroupEvents<T extends Record & LoanTypeGroupEvents<T>>
        extends DomainEvent<T>
        permits LoanTypeGroupCreated, LoanTypeGroupRenamed, LoanTypeGroupMoved {

    @Override
    default Class<LoanTypeGroup> aggregateType() {
        return LoanTypeGroup.class;
    }
}
```

- [ ] **Step 5: Write the three event records.**

```java
package ir.dotin.loan.baseloan.core.domain.loantypegroup.event;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTypeGroupId;

import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

public record LoanTypeGroupCreated(
        UUID eventId, UUID aggregateId, String eventType,
        String title, @Nullable UUID parentGroupId, Instant createdAt)
        implements LoanTypeGroupEvents<LoanTypeGroupCreated> {

    public LoanTypeGroupCreated {
        requireNonNull(eventId); requireNonNull(aggregateId);
        requireNonNull(eventType); requireNonNull(title); requireNonNull(createdAt);
    }

    public static LoanTypeGroupCreated of(
            LoanTypeGroupId id, String title, @Nullable LoanTypeGroupId parentGroupId, Clock clock) {
        return new LoanTypeGroupCreated(
                randomUUID(), id.value(), LoanTypeGroupEventType.CREATED.getFullType(),
                title, parentGroupId == null ? null : parentGroupId.value(), clock.instant());
    }
}
```
```java
package ir.dotin.loan.baseloan.core.domain.loantypegroup.event;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTypeGroupId;

import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

public record LoanTypeGroupRenamed(
        UUID eventId, UUID aggregateId, String eventType, String title, Instant createdAt)
        implements LoanTypeGroupEvents<LoanTypeGroupRenamed> {

    public LoanTypeGroupRenamed {
        requireNonNull(eventId); requireNonNull(aggregateId);
        requireNonNull(eventType); requireNonNull(title); requireNonNull(createdAt);
    }

    public static LoanTypeGroupRenamed of(LoanTypeGroupId id, String title, Clock clock) {
        return new LoanTypeGroupRenamed(
                randomUUID(), id.value(), LoanTypeGroupEventType.RENAMED.getFullType(),
                title, clock.instant());
    }
}
```
```java
package ir.dotin.loan.baseloan.core.domain.loantypegroup.event;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTypeGroupId;

import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

public record LoanTypeGroupMoved(
        UUID eventId, UUID aggregateId, String eventType, @Nullable UUID newParentGroupId, Instant createdAt)
        implements LoanTypeGroupEvents<LoanTypeGroupMoved> {

    public LoanTypeGroupMoved {
        requireNonNull(eventId); requireNonNull(aggregateId);
        requireNonNull(eventType); requireNonNull(createdAt);
    }

    public static LoanTypeGroupMoved of(LoanTypeGroupId id, @Nullable LoanTypeGroupId newParentGroupId, Clock clock) {
        return new LoanTypeGroupMoved(
                randomUUID(), id.value(), LoanTypeGroupEventType.MOVED.getFullType(),
                newParentGroupId == null ? null : newParentGroupId.value(), clock.instant());
    }
}
```

- [ ] **Step 6: Run test, verify it passes.**

Run: `cd /home/m.amirabdollahi/workspaces/base-loan && mvn -q -pl domain -am test -Dtest=LoanTypeGroupEventTest`
Expected: PASS. (If `DomainEvent` requires more methods than `eventId/aggregateId/createdAt/eventType/aggregateType`, add them per the interface in `pangaea/.../commons/domain/event/DomainEvent.java`.)

- [ ] **Step 7: Commit.**

```bash
git -C /home/m.amirabdollahi/workspaces/base-loan add domain/src/main/java/ir/dotin/loan/baseloan/core/domain/loantypegroup/event/ domain/src/test/java/ir/dotin/loan/baseloan/core/domain/loantypegroup/event/
git -C /home/m.amirabdollahi/workspaces/base-loan commit -m "feat(CBS-284638:LN-59558:LN-59559): add LoanTypeGroup domain events"
```

---

### Task 3: Rework `LoanTypeGroup` aggregate (flat node + behaviors)

**Files:**
- Modify: `…/loantypegroup/aggregate/LoanTypeGroup.java`
- Test: `…/test/…/loantypegroup/aggregate/LoanTypeGroupTest.java` (already exists — extend it)

**Interfaces — Produces:**
- `LoanTypeGroup.create(Title, @Nullable LoanTypeGroupId parentGroupId, Clock) : Result<LoanTypeGroup>`
- `LoanTypeGroup.reconstitute(LoanTypeGroupId, Title, @Nullable LoanTypeGroupId parentGroupId, @Nullable Integer version) : LoanTypeGroup`
- `rename(Title, Clock) : Result<LoanTypeGroup>`, `moveUnder(@Nullable LoanTypeGroupId, Clock) : Result<LoanTypeGroup>`
- getters `getTitle()`, `getParentGroupId()`, `getVersion()`.
Consumed by Tasks 8–10, 14, 16.

**Interfaces — Consumes:** `LoanTypeGroupCreated/Renamed/Moved.of(...)` (Task 2); `LoanTypeGroupErrors` (Task 1); `AbstractAggregateRoot.registerEvent` / `domainEvents()`.

- [ ] **Step 1: Write failing tests.**

```java
package ir.dotin.loan.baseloan.core.domain.loantypegroup.aggregate;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

import ir.dotin.loan.baseloan.core.domain.loantypegroup.event.LoanTypeGroupCreated;
import ir.dotin.loan.baseloan.core.domain.loantypegroup.event.LoanTypeGroupMoved;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTypeGroupId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.Title;
import ir.dotin.platform.pangaea.commons.core.Result;

import static org.assertj.core.api.Assertions.assertThat;

final class LoanTypeGroupTest {

    private final Clock clock = Clock.fixed(Instant.parse("2026-06-27T00:00:00Z"), ZoneOffset.UTC);
    private final Title title = Title.of("تسهیلات").getValue();

    @Test
    void create_registers_created_event() {
        Result<LoanTypeGroup> result = LoanTypeGroup.create(title, null, clock);

        assertThat(result.isSuccess()).isTrue();
        LoanTypeGroup group = result.getValue();
        assertThat(group.getParentGroupId()).isNull();
        assertThat(group.domainEvents()).hasSize(1);
        assertThat(group.domainEvents().get(0)).isInstanceOf(LoanTypeGroupCreated.class);
    }

    @Test
    void moveUnder_self_is_rejected() {
        LoanTypeGroup group = LoanTypeGroup.create(title, null, clock).getValue();

        Result<LoanTypeGroup> moved = group.moveUnder(group.getId(), clock);

        assertThat(moved.isFailure()).isTrue();
    }

    @Test
    void moveUnder_other_registers_moved_event() {
        LoanTypeGroup group = LoanTypeGroup.create(title, null, clock).getValue();
        LoanTypeGroupId newParent = LoanTypeGroupId.generate();

        LoanTypeGroup moved = group.moveUnder(newParent, clock).getValue();

        assertThat(moved.getParentGroupId()).isEqualTo(newParent);
        assertThat(moved.domainEvents()).anyMatch(e -> e instanceof LoanTypeGroupMoved);
    }
}
```
(Use whatever `Result` accessor the codebase exposes — `getValue()`/`get()`/`value()`. Confirm from `Result.java` and match; the loan-type tests already use it.)

- [ ] **Step 2: Run, verify it fails.**

Run: `cd /home/m.amirabdollahi/workspaces/base-loan && mvn -q -pl domain -am test -Dtest=LoanTypeGroupTest`
Expected: FAIL — `create`/`moveUnder`/`getParentGroupId` not found.

- [ ] **Step 3: Rewrite the aggregate.**

```java
package ir.dotin.loan.baseloan.core.domain.loantypegroup.aggregate;

import java.time.Clock;

import org.jspecify.annotations.Nullable;

import ir.dotin.loan.baseloan.core.domain.loantypegroup.error.LoanTypeGroupErrors;
import ir.dotin.loan.baseloan.core.domain.loantypegroup.event.LoanTypeGroupCreated;
import ir.dotin.loan.baseloan.core.domain.loantypegroup.event.LoanTypeGroupMoved;
import ir.dotin.loan.baseloan.core.domain.loantypegroup.event.LoanTypeGroupRenamed;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTypeGroupId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.Title;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.domain.entity.AbstractAggregateRoot;

import static java.util.Objects.requireNonNull;

public final class LoanTypeGroup extends AbstractAggregateRoot<LoanTypeGroupId> {

    private Title title;
    @Nullable private LoanTypeGroupId parentGroupId;
    @Nullable private final Integer version;

    private LoanTypeGroup(
            LoanTypeGroupId id, Title title, @Nullable LoanTypeGroupId parentGroupId, @Nullable Integer version) {
        super(requireNonNull(id, "LoanTypeGroup id cannot be null"));
        this.title = requireNonNull(title, "LoanTypeGroup title cannot be null");
        this.parentGroupId = parentGroupId;
        this.version = version;
    }

    public static Result<LoanTypeGroup> create(
            Title title, @Nullable LoanTypeGroupId parentGroupId, Clock clock) {
        LoanTypeGroupId id = LoanTypeGroupId.generate();
        LoanTypeGroup group = new LoanTypeGroup(id, title, parentGroupId, null);
        group.registerEvent(LoanTypeGroupCreated.of(id, title.value(), parentGroupId, clock));
        return Result.success(group);
    }

    public static LoanTypeGroup reconstitute(
            LoanTypeGroupId id, Title title, @Nullable LoanTypeGroupId parentGroupId, @Nullable Integer version) {
        return new LoanTypeGroup(id, title, parentGroupId, version);
    }

    public Result<LoanTypeGroup> rename(Title newTitle, Clock clock) {
        this.title = requireNonNull(newTitle, "newTitle cannot be null");
        registerEvent(LoanTypeGroupRenamed.of(getId(), newTitle.value(), clock));
        return Result.success(this);
    }

    public Result<LoanTypeGroup> moveUnder(@Nullable LoanTypeGroupId newParentGroupId, Clock clock) {
        if (getId().equals(newParentGroupId)) {
            return Result.failure(LoanTypeGroupErrors.SELF_PARENT_NOT_ALLOWED, getId());
        }
        this.parentGroupId = newParentGroupId;
        registerEvent(LoanTypeGroupMoved.of(getId(), newParentGroupId, clock));
        return Result.success(this);
    }

    public Title getTitle() { return title; }
    public @Nullable LoanTypeGroupId getParentGroupId() { return parentGroupId; }
    public @Nullable Integer getVersion() { return version; }
}
```
(Match `Result.failure(ProductErrorCode, args...)` to the exact signature used in `AbstractLoanType.activate` — copy it. Delete the old `LoanTypeId`/`Title`/`List` imports that are no longer used. The descendant-cycle check is enforced in the Move *step* (Task 10), not here, because it needs the repository.)

- [ ] **Step 4: Run tests, verify they pass.**

Run: `cd /home/m.amirabdollahi/workspaces/base-loan && mvn -q -pl domain -am test -Dtest=LoanTypeGroupTest,LoanTypeGroupEventTest`
Expected: PASS.

- [ ] **Step 5: Full base-loan build (ArchUnit + format).**

Run: `cd /home/m.amirabdollahi/workspaces/base-loan && mvn -q install -DskipITs=false`
Expected: BUILD SUCCESS (architecture-tests green; `LoanTypeGroup` no longer references removed embedded types). Installs the new SNAPSHOT so nova sees it.

- [ ] **Step 6: Commit.**

```bash
git -C /home/m.amirabdollahi/workspaces/base-loan add domain/src/main/java/ir/dotin/loan/baseloan/core/domain/loantypegroup/aggregate/LoanTypeGroup.java domain/src/test/java/ir/dotin/loan/baseloan/core/domain/loantypegroup/aggregate/LoanTypeGroupTest.java
git -C /home/m.amirabdollahi/workspaces/base-loan commit -m "feat(CBS-284638:LN-59558:LN-59559): operationalize LoanTypeGroup aggregate (flat node, factory, rename, move)"
```

---

### Task 4: Loan-type membership behavior (`assignToGroup` / `removeFromGroup`)

**Files:**
- Modify: `base-loan/.../loantype/entity/AbstractLoanType.java` (make `groupId` non-final; add behaviors + abstract event hooks)
- Create (nova): `nova/core/domain/.../loantype/event/TradeLoanTypeGroupAssigned.java`, `TradeLoanTypeGroupRemoved.java`
- Modify (nova): `…/loantype/event/TradeLoanTypeEvents.java` (permits), `…/loantype/event/TradeLoanTypeEventType.java` (add constants), `…/loantype/entity/TradeLoanType.java` (implement hooks)
- Test: `nova/core/domain/src/test/java/…/loantype/entity/TradeLoanTypeGroupAssignmentTest.java`

**Interfaces — Produces:** `TradeLoanType.assignToGroup(LoanTypeGroupId, Clock) : Result<? extends AbstractLoanType>`, `removeFromGroup(Clock) : Result<? extends AbstractLoanType>`, registering `TradeLoanTypeGroupAssigned` / `TradeLoanTypeGroupRemoved`. Consumed by Tasks 11–12.

- [ ] **Step 1: Write the failing nova-domain test.**

```java
package ir.dotin.loan.trade.core.domain.loantype.entity;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTypeGroupId;
import ir.dotin.loan.trade.core.domain.loantype.event.TradeLoanTypeGroupAssigned;
import ir.dotin.platform.pangaea.commons.core.Result;

import static org.assertj.core.api.Assertions.assertThat;

final class TradeLoanTypeGroupAssignmentTest {

    private final Clock clock = Clock.fixed(Instant.parse("2026-06-27T00:00:00Z"), ZoneOffset.UTC);

    @Test
    void assignToGroup_sets_group_and_registers_event() {
        TradeLoanType loanType = TradeLoanTypeFixtures.persisted();   // build a reconstituted instance
        LoanTypeGroupId group = LoanTypeGroupId.generate();

        Result<? extends ir.dotin.loan.baseloan.core.domain.loantype.entity.AbstractLoanType> result =
                loanType.assignToGroup(group, clock);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getValue().getGroupId()).isEqualTo(group);
        assertThat(loanType.domainEvents()).anyMatch(e -> e instanceof TradeLoanTypeGroupAssigned);
    }
}
```
(If no `TradeLoanTypeFixtures` exists, build the loan type inline using `TradeLoanType.reconstitute(new TradeLoanType.Builder()...)` exactly as an existing `TradeLoanType` test does — locate one under `nova/core/domain/src/test` and copy its construction.)

- [ ] **Step 2: Run, verify it fails.**

Run: `cd /home/m.amirabdollahi/workspaces/nova && mvn -q -pl core/domain -am test -Dtest=TradeLoanTypeGroupAssignmentTest`
Expected: FAIL — `assignToGroup` / `TradeLoanTypeGroupAssigned` not found.

- [ ] **Step 3: Add the event-type constants + event records (nova).**

In `TradeLoanTypeEventType.java` add `GROUP_ASSIGNED` and `GROUP_REMOVED` constants following the existing pattern (copy how `CREATED.getFullType()` is built). Add the two records, mirroring `TradeLoanTypeCreated`:

```java
package ir.dotin.loan.trade.core.domain.loantype.event;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTypeGroupId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTypeId;

import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

public record TradeLoanTypeGroupAssigned(
        UUID eventId, UUID aggregateId, String eventType, UUID groupId, Instant createdAt)
        implements TradeLoanTypeEvents<TradeLoanTypeGroupAssigned> {

    public TradeLoanTypeGroupAssigned {
        requireNonNull(eventId); requireNonNull(aggregateId);
        requireNonNull(eventType); requireNonNull(groupId); requireNonNull(createdAt);
    }

    public static TradeLoanTypeGroupAssigned of(LoanTypeId id, LoanTypeGroupId groupId, Clock clock) {
        return new TradeLoanTypeGroupAssigned(
                randomUUID(), id.value(), TradeLoanTypeEventType.GROUP_ASSIGNED.getFullType(),
                groupId.value(), clock.instant());
    }
}
```
```java
package ir.dotin.loan.trade.core.domain.loantype.event;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTypeId;

import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

public record TradeLoanTypeGroupRemoved(
        UUID eventId, UUID aggregateId, String eventType, Instant createdAt)
        implements TradeLoanTypeEvents<TradeLoanTypeGroupRemoved> {

    public TradeLoanTypeGroupRemoved {
        requireNonNull(eventId); requireNonNull(aggregateId);
        requireNonNull(eventType); requireNonNull(createdAt);
    }

    public static TradeLoanTypeGroupRemoved of(LoanTypeId id, Clock clock) {
        return new TradeLoanTypeGroupRemoved(
                randomUUID(), id.value(), TradeLoanTypeEventType.GROUP_REMOVED.getFullType(), clock.instant());
    }
}
```
Add both to the `permits` clause of `TradeLoanTypeEvents`.

- [ ] **Step 4: Add behaviors + abstract hooks to `AbstractLoanType` (base-loan).**

Change `private final LoanTypeGroupId groupId;` → `@Nullable private LoanTypeGroupId groupId;`. Add (mirroring the `activate` shape that delegates to an abstract `getLoanTypeActivatedEvent`):

```java
public Result<? extends AbstractLoanType> assignToGroup(LoanTypeGroupId groupId, Clock clock) {
    this.groupId = requireNonNull(groupId, "groupId cannot be null");
    registerEvent(getGroupAssignedEvent(getId(), groupId, clock));
    return Result.success(this);
}

public Result<? extends AbstractLoanType> removeFromGroup(Clock clock) {
    this.groupId = null;
    registerEvent(getGroupRemovedEvent(getId(), clock));
    return Result.success(this);
}

protected abstract DomainEvent<?> getGroupAssignedEvent(LoanTypeId id, LoanTypeGroupId groupId, Clock clock);

protected abstract DomainEvent<?> getGroupRemovedEvent(LoanTypeId id, Clock clock);
```
(Use the exact `Result`/`DomainEvent`/`LoanTypeId` import FQNs already present in `AbstractLoanType.java`. Match the abstract-event-hook signature style to the existing `getLoanTypeActivatedEvent`.)

- [ ] **Step 5: Implement the hooks in `TradeLoanType`.**

```java
@Override
protected DomainEvent<?> getGroupAssignedEvent(LoanTypeId id, LoanTypeGroupId groupId, Clock clock) {
    return TradeLoanTypeGroupAssigned.of(id, groupId, clock);
}

@Override
protected DomainEvent<?> getGroupRemovedEvent(LoanTypeId id, Clock clock) {
    return TradeLoanTypeGroupRemoved.of(id, clock);
}
```
(Add the matching imports. Mirror how `TradeLoanType` implements its existing abstract event hook for activate/deactivate.)

- [ ] **Step 6: Build base-loan then nova-domain; run the test.**

Run: `cd /home/m.amirabdollahi/workspaces/base-loan && mvn -q install`
Then: `cd /home/m.amirabdollahi/workspaces/nova && mvn -q -pl core/domain -am test -Dtest=TradeLoanTypeGroupAssignmentTest`
Expected: both PASS.

- [ ] **Step 7: Commit (two repos).**

```bash
git -C /home/m.amirabdollahi/workspaces/base-loan add domain/src/main/java/ir/dotin/loan/baseloan/core/domain/loantype/entity/AbstractLoanType.java
git -C /home/m.amirabdollahi/workspaces/base-loan commit -m "feat(CBS-284638:LN-59558:LN-59559): add loan-type group membership behaviors"
git -C /home/m.amirabdollahi/workspaces/nova add core/domain/src/main/java/ir/dotin/loan/trade/core/domain/loantype/event/ core/domain/src/main/java/ir/dotin/loan/trade/core/domain/loantype/entity/TradeLoanType.java core/domain/src/test/java/ir/dotin/loan/trade/core/domain/loantype/entity/TradeLoanTypeGroupAssignmentTest.java
git -C /home/m.amirabdollahi/workspaces/nova commit -m "feat(CBS-284638:LN-59558:LN-59559): add TradeLoanType group-assignment events"
```

---

## Phase 1 — nova application ports, commands, queries

### Task 5: Outbound repository ports

**Files:**
- Create: `nova/core/application/ports/outbound/src/main/java/ir/dotin/loan/trade/core/application/ports/outbound/command/repository/LoanTypeGroupRepository.java`
- Create: `nova/core/application/ports/outbound/.../query/repository/LoanTypeGroupQueryRepository.java` (place beside the existing loan-type query repository port; confirm its package by reading `TradeLoanTypeQueryRepository`).

**Interfaces — Produces:** ports consumed by Tasks 8–10 (command) and 16 (query adapter), 12 (handlers).

- [ ] **Step 1: Write the command port.**

```java
package ir.dotin.loan.trade.core.application.ports.outbound.command.repository;

import java.util.List;
import java.util.Optional;

import ir.dotin.loan.baseloan.core.domain.loantypegroup.aggregate.LoanTypeGroup;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTypeGroupId;

public interface LoanTypeGroupRepository {

    LoanTypeGroup save(LoanTypeGroup group);

    Optional<LoanTypeGroup> findById(LoanTypeGroupId id);

    Boolean existsById(LoanTypeGroupId id);

    List<LoanTypeGroup> findChildren(LoanTypeGroupId parentId);

    List<LoanTypeGroupId> findAncestorChain(LoanTypeGroupId id);
}
```
(Match `Optional`/`Boolean` conventions to `TradeLoanTypeRepository` — copy its style verbatim.)

- [ ] **Step 2: Write the query port.**

```java
package ir.dotin.loan.trade.core.application.query.loantypegroup.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import ir.dotin.loan.trade.core.application.query.loantypegroup.dto.LoanTypeGroupNodeDto;
import ir.dotin.loan.trade.core.application.query.loantypegroup.dto.LoanTypeRefDto;

public interface LoanTypeGroupQueryRepository {

    List<LoanTypeGroupNodeDto> findAllGroups();

    List<LoanTypeRefDto> findAllMemberships();

    Optional<LoanTypeGroupNodeDto> findById(UUID id);
}
```
(Put the query port in whichever module/package `TradeLoanTypeQueryRepository` lives — read its file and match exactly; the package above assumes it sits in the `query` module.)

- [ ] **Step 3: Compile + commit.**

Run: `cd /home/m.amirabdollahi/workspaces/nova && mvn -q -pl core/application/ports/outbound,core/application/query -am compile`
(Query DTOs come in Task 7; if compile fails on missing DTO types, do Task 7 first then return — or create the DTO stubs now.)
```bash
git -C /home/m.amirabdollahi/workspaces/nova add core/application/ports/outbound/.../LoanTypeGroupRepository.java core/application/query/.../loantypegroup/repository/LoanTypeGroupQueryRepository.java
git -C /home/m.amirabdollahi/workspaces/nova commit -m "feat(CBS-284638:LN-59558:LN-59559): add LoanTypeGroup repository ports"
```

---

### Task 6: Inbound command records

**Files:**
- Create under `nova/core/application/ports/inbound/src/main/java/ir/dotin/loan/trade/core/application/ports/inbound/command/`:
  `CreateLoanTypeGroupCommand.java`, `RenameLoanTypeGroupCommand.java`, `MoveLoanTypeGroupCommand.java`, `AssignLoanTypeToGroupCommand.java`, `RemoveLoanTypeFromGroupCommand.java`

**Interfaces — Produces:** the five command records (each `implements Command`, `@Builder(toBuilder = true)`, first field `@NotNull UUID uid`). Consumed by Tasks 8–12 and the controllers (18).

- [ ] **Step 1: Write the records.** (Confirm `Command` FQN + the validation annotation imports from `DefineLoanTypeCommand.java`, copy verbatim.)

```java
package ir.dotin.loan.trade.core.application.ports.inbound.command;

import java.util.UUID;

import org.jspecify.annotations.Nullable;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import ir.dotin.platform.pangaea.servicelayer.api.command.Command;

@Builder(toBuilder = true)
public record CreateLoanTypeGroupCommand(
        @NotNull UUID uid,
        @NotBlank String title,
        @Nullable UUID parentGroupId)
        implements Command {}
```
```java
@Builder(toBuilder = true)
public record RenameLoanTypeGroupCommand(
        @NotNull UUID uid,
        @NotNull Long version,
        @NotNull UUID groupId,
        @NotBlank String title)
        implements Command {}
```
```java
@Builder(toBuilder = true)
public record MoveLoanTypeGroupCommand(
        @NotNull UUID uid,
        @NotNull Long version,
        @NotNull UUID groupId,
        @Nullable UUID newParentGroupId)
        implements Command {}
```
```java
@Builder(toBuilder = true)
public record AssignLoanTypeToGroupCommand(
        @NotNull UUID uid,
        @NotNull Long version,
        @NotNull UUID loanTypeId,
        @NotNull UUID groupId)
        implements Command {}
```
```java
@Builder(toBuilder = true)
public record RemoveLoanTypeFromGroupCommand(
        @NotNull UUID uid,
        @NotNull Long version,
        @NotNull UUID loanTypeId)
        implements Command {}
```
(Each is its own file with the full package + imports of the first one. `version` is `@NotNull` on state-change commands, absent on create — per the `DefineLoanTypeCommand` convention.)

- [ ] **Step 2: Compile + commit.**

Run: `cd /home/m.amirabdollahi/workspaces/nova && mvn -q -pl core/application/ports/inbound -am compile`
```bash
git -C /home/m.amirabdollahi/workspaces/nova add core/application/ports/inbound/.../command/
git -C /home/m.amirabdollahi/workspaces/nova commit -m "feat(CBS-284638:LN-59558:LN-59559): add LoanTypeGroup inbound commands"
```

---

### Task 7: Query records + read DTOs

**Files:**
- Create under `nova/core/application/query/src/main/java/ir/dotin/loan/trade/core/application/query/loantypegroup/`:
  `dto/LoanTypeRefDto.java`, `dto/LoanTypeGroupNodeDto.java`, `dto/LoanTypeGroupTreeDto.java`,
  `request/GetLoanTypeGroupTreeQuery.java`, `request/GetLoanTypeGroupByIdQuery.java`, `request/ListRootLoanTypeGroupsQuery.java`

**Interfaces — Produces:** `GetLoanTypeGroupTreeQuery implements Query<LoanTypeGroupTreeDto>`, `GetLoanTypeGroupByIdQuery implements Query<LoanTypeGroupTreeDto>`, `ListRootLoanTypeGroupsQuery implements Query<List<LoanTypeGroupTreeDto>>`; DTOs `implements QueryResult`. Consumed by Tasks 16 (adapter), 19 (controller).

- [ ] **Step 1: Write the DTOs.** (Confirm `QueryResult` FQN from `TradeLoanTypeQueryDto.java`.)

```java
package ir.dotin.loan.trade.core.application.query.loantypegroup.dto;

import java.util.UUID;

import ir.dotin.platform.pangaea.servicelayer.api.query.QueryResult;

public record LoanTypeRefDto(UUID loanTypeId, String code, String title) implements QueryResult {}
```
```java
package ir.dotin.loan.trade.core.application.query.loantypegroup.dto;

import java.util.UUID;

import org.jspecify.annotations.Nullable;

import ir.dotin.platform.pangaea.servicelayer.api.query.QueryResult;

public record LoanTypeGroupNodeDto(UUID id, String title, @Nullable UUID parentGroupId) implements QueryResult {}
```
```java
package ir.dotin.loan.trade.core.application.query.loantypegroup.dto;

import java.util.List;
import java.util.UUID;

import ir.dotin.platform.pangaea.protocol.projection.api.ProjectableResource;
import ir.dotin.platform.pangaea.servicelayer.api.query.QueryResult;

@ProjectableResource(
        views = @ProjectableResource.View(name = "SUMMARY", fields = {"id", "title"}))
public record LoanTypeGroupTreeDto(
        UUID id,
        String title,
        List<LoanTypeGroupTreeDto> children,
        List<LoanTypeRefDto> loanTypes)
        implements QueryResult {}
```

- [ ] **Step 2: Write the query records.** (Confirm `Query<R>` + `CacheableQuery`/`CacheTag` usage from `GetLoanTypeByIdQuery.java`; if cache infra is mandatory on queries, mirror it — else implement only `Query<R>`.)

```java
package ir.dotin.loan.trade.core.application.query.loantypegroup.request;

import ir.dotin.loan.trade.core.application.query.loantypegroup.dto.LoanTypeGroupTreeDto;
import ir.dotin.platform.pangaea.servicelayer.api.query.Query;

import lombok.Builder;

@Builder
public record GetLoanTypeGroupTreeQuery() implements Query<LoanTypeGroupTreeDto> {
    @Override public Class<LoanTypeGroupTreeDto> getResultType() { return LoanTypeGroupTreeDto.class; }
}
```
```java
@Builder
public record GetLoanTypeGroupByIdQuery(java.util.UUID groupId) implements Query<LoanTypeGroupTreeDto> {
    @Override public Class<LoanTypeGroupTreeDto> getResultType() { return LoanTypeGroupTreeDto.class; }
}
```
```java
// ListRootLoanTypeGroupsQuery returns List — if the Query<R> contract can't carry List<T>,
// wrap in a LoanTypeGroupTreeListResult record implementing QueryResult (mirror LoanTypeQueryResult).
@Builder
public record ListRootLoanTypeGroupsQuery() implements Query<LoanTypeGroupTreeListResult> {
    @Override public Class<LoanTypeGroupTreeListResult> getResultType() { return LoanTypeGroupTreeListResult.class; }
}
```
(If `Query<R>`'s `getResultType` can't represent a `List`, create `LoanTypeGroupTreeListResult(List<LoanTypeGroupTreeDto> items) implements QueryResult` — confirm by checking how `FindAllLoanTypesQuery`/`LoanTypeQueryResult` handle lists, and copy that exact approach.)

- [ ] **Step 3: Compile + commit.**

Run: `cd /home/m.amirabdollahi/workspaces/nova && mvn -q -pl core/application/query -am compile`
```bash
git -C /home/m.amirabdollahi/workspaces/nova add core/application/query/.../loantypegroup/
git -C /home/m.amirabdollahi/workspaces/nova commit -m "feat(CBS-284638:LN-59558:LN-59559): add LoanTypeGroup query records + read DTOs"
```

---

## Phase 2 — command handlers + steps

> Pattern for every handler in this phase (from `DefineLoanTypeCommandHandler` + `DefineLoanTypeStep`):
> handler `@Service implements WorkflowCommandHandler<C, D>`; `definition()` returns `Workflow.singleWrite("<name>", writePublishing(step))`; `seed(command)` returns `Result.success(new D(command))`. Step `@Component implements PublishingWriteActivity<D>`; `execute(ctx)` returns `StepResult.fromWriteResult(write(...))` where `write` returns `Result<List<DomainEvent<?>>>` ending in `.map(<Aggregate>::domainEvents)`. `writePublishing` is statically imported from `ir.dotin.platform.pangaea.workflow.api.definition.Steps`.

### Task 8: Create-group handler + step + data

**Files:** under `nova/core/application/service/src/main/java/ir/dotin/loan/trade/core/application/service/loantypegroup/`:
- Create: `data/CreateLoanTypeGroupData.java`, `step/CreateLoanTypeGroupStep.java`, `commandhandler/CreateLoanTypeGroupCommandHandler.java`
- Test: `…/loantypegroup/commandhandler/CreateLoanTypeGroupCommandHandlerTest.java`

**Interfaces — Consumes:** `CreateLoanTypeGroupCommand` (T6), `LoanTypeGroupRepository` (T5), `LoanTypeGroup.create` (T3), `Title.of`. **Produces:** registered handler for `CreateLoanTypeGroupCommand`.

- [ ] **Step 1: Write the failing handler test.** Mirror an existing `*CommandHandlerTest` (e.g. `AddGuarantorsCommandHandlerTest`) — read one and copy its harness (how it builds the workflow context / invokes the step, mocks the repo). Test: given a valid command, the step saves a group and returns one `LoanTypeGroupCreated` event.

- [ ] **Step 2: Run, verify it fails.**

Run: `cd /home/m.amirabdollahi/workspaces/nova && mvn -q -pl core/application/service -am test -Dtest=CreateLoanTypeGroupCommandHandlerTest`
Expected: FAIL.

- [ ] **Step 3: Write the data record.**

```java
package ir.dotin.loan.trade.core.application.service.loantypegroup.data;

import ir.dotin.loan.trade.core.application.ports.inbound.command.CreateLoanTypeGroupCommand;

public record CreateLoanTypeGroupData(CreateLoanTypeGroupCommand command) {}
```

- [ ] **Step 4: Write the step.**

```java
package ir.dotin.loan.trade.core.application.service.loantypegroup.step;

import java.time.Clock;
import java.util.List;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import ir.dotin.loan.baseloan.core.domain.loantypegroup.aggregate.LoanTypeGroup;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTypeGroupId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.Title;
import ir.dotin.loan.trade.core.application.ports.inbound.command.CreateLoanTypeGroupCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.LoanTypeGroupRepository;
import ir.dotin.loan.trade.core.application.service.loantypegroup.data.CreateLoanTypeGroupData;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.domain.event.DomainEvent;
import ir.dotin.platform.pangaea.workflow.api.activity.PublishingWriteActivity;
import ir.dotin.platform.pangaea.workflow.api.context.WorkflowContext;
import ir.dotin.platform.pangaea.workflow.api.step.StepResult;

@Component
@RequiredArgsConstructor
public class CreateLoanTypeGroupStep implements PublishingWriteActivity<CreateLoanTypeGroupData> {

    private final LoanTypeGroupRepository repository;
    private final Clock clock;

    @Override
    public StepResult<List<DomainEvent<?>>> execute(WorkflowContext<CreateLoanTypeGroupData> ctx) {
        CreateLoanTypeGroupCommand command = ctx.data().command();
        return StepResult.fromWriteResult(write(command));
    }

    private Result<List<DomainEvent<?>>> write(CreateLoanTypeGroupCommand command) {
        LoanTypeGroupId parentId =
                command.parentGroupId() == null ? null : new LoanTypeGroupId(command.parentGroupId());
        return Title.of(command.title())
                .flatMap(title -> LoanTypeGroup.create(title, parentId, clock))
                .onSuccess(repository::save)
                .map(LoanTypeGroup::domainEvents);
    }
}
```
(Confirm `PublishingWriteActivity`, `WorkflowContext`, `StepResult`, `Steps.writePublishing` FQNs from `DefineLoanTypeStep.java` and copy verbatim. If a parent is supplied, validate existence here — `repository.existsById(parentId)` → fail `LoanTypeGroupErrors.PARENT_NOT_FOUND` before `create`.)

- [ ] **Step 5: Write the handler.**

```java
package ir.dotin.loan.trade.core.application.service.loantypegroup.commandhandler;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import ir.dotin.loan.trade.core.application.ports.inbound.command.CreateLoanTypeGroupCommand;
import ir.dotin.loan.trade.core.application.service.loantypegroup.data.CreateLoanTypeGroupData;
import ir.dotin.loan.trade.core.application.service.loantypegroup.step.CreateLoanTypeGroupStep;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.workflow.api.command.WorkflowCommandHandler;
import ir.dotin.platform.pangaea.workflow.api.definition.Workflow;

import static ir.dotin.platform.pangaea.workflow.api.definition.Steps.writePublishing;

@Service
@RequiredArgsConstructor
public final class CreateLoanTypeGroupCommandHandler
        implements WorkflowCommandHandler<CreateLoanTypeGroupCommand, CreateLoanTypeGroupData> {

    private final CreateLoanTypeGroupStep step;

    @Override
    public Workflow<CreateLoanTypeGroupData> definition() {
        return Workflow.singleWrite("create-loan-type-group", writePublishing(step));
    }

    @Override
    public Result<CreateLoanTypeGroupData> seed(CreateLoanTypeGroupCommand command) {
        return Result.success(new CreateLoanTypeGroupData(command));
    }
}
```

- [ ] **Step 6: Run test, verify pass; commit.**

Run: `cd /home/m.amirabdollahi/workspaces/nova && mvn -q -pl core/application/service -am test -Dtest=CreateLoanTypeGroupCommandHandlerTest`
Expected: PASS.
```bash
git -C /home/m.amirabdollahi/workspaces/nova add core/application/service/.../loantypegroup/
git -C /home/m.amirabdollahi/workspaces/nova commit -m "feat(CBS-284638:LN-59558:LN-59559): add create-loan-type-group command handler"
```

---

### Task 9: Rename-group handler + step + data

**Files:** `…/loantypegroup/data/RenameLoanTypeGroupData.java`, `step/RenameLoanTypeGroupStep.java`, `commandhandler/RenameLoanTypeGroupCommandHandler.java` + test.

**Interfaces — Consumes:** `RenameLoanTypeGroupCommand` (T6), `LoanTypeGroupRepository.findById` (T5), `LoanTypeGroup.rename` (T3).

- [ ] **Step 1: Failing test** — given an existing group, rename returns a `LoanTypeGroupRenamed` event and saves; unknown id → failure `GROUP_NOT_FOUND`.
- [ ] **Step 2: Run, verify fails.** `-Dtest=RenameLoanTypeGroupCommandHandlerTest`
- [ ] **Step 3: Data record.**

```java
package ir.dotin.loan.trade.core.application.service.loantypegroup.data;

import ir.dotin.loan.trade.core.application.ports.inbound.command.RenameLoanTypeGroupCommand;

public record RenameLoanTypeGroupData(RenameLoanTypeGroupCommand command) {}
```

- [ ] **Step 4: Step** (load → mutate → save → events):

```java
package ir.dotin.loan.trade.core.application.service.loantypegroup.step;

import java.time.Clock;
import java.util.List;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import ir.dotin.loan.baseloan.core.domain.loantypegroup.aggregate.LoanTypeGroup;
import ir.dotin.loan.baseloan.core.domain.loantypegroup.error.LoanTypeGroupErrors;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTypeGroupId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.Title;
import ir.dotin.loan.trade.core.application.ports.inbound.command.RenameLoanTypeGroupCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.LoanTypeGroupRepository;
import ir.dotin.loan.trade.core.application.service.loantypegroup.data.RenameLoanTypeGroupData;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.domain.event.DomainEvent;
import ir.dotin.platform.pangaea.workflow.api.activity.PublishingWriteActivity;
import ir.dotin.platform.pangaea.workflow.api.context.WorkflowContext;
import ir.dotin.platform.pangaea.workflow.api.step.StepResult;

@Component
@RequiredArgsConstructor
public class RenameLoanTypeGroupStep implements PublishingWriteActivity<RenameLoanTypeGroupData> {

    private final LoanTypeGroupRepository repository;
    private final Clock clock;

    @Override
    public StepResult<List<DomainEvent<?>>> execute(WorkflowContext<RenameLoanTypeGroupData> ctx) {
        RenameLoanTypeGroupCommand command = ctx.data().command();
        LoanTypeGroupId id = new LoanTypeGroupId(command.groupId());
        Result<List<DomainEvent<?>>> result = repository.findById(id)
                .map(group -> Title.of(command.title())
                        .flatMap(title -> group.rename(title, clock))
                        .onSuccess(repository::save)
                        .map(LoanTypeGroup::domainEvents))
                .orElseGet(() -> Result.failure(LoanTypeGroupErrors.GROUP_NOT_FOUND, id));
        return StepResult.fromWriteResult(result);
    }
}
```
(Match `Result.failure(...)`/`Optional.map/orElseGet` ergonomics to the codebase; if `Result` has a `fromOptional` helper, prefer it.)

- [ ] **Step 5: Handler** — identical shape to Task 8 handler with `RenameLoanTypeGroup*` types and `Workflow.singleWrite("rename-loan-type-group", writePublishing(step))`.
- [ ] **Step 6: Run, pass, commit** `feat(CBS-284638:LN-59558:LN-59559): add rename-loan-type-group command handler`.

---

### Task 10: Move-group handler + step + data (cycle guard)

**Files:** `…/loantypegroup/data/MoveLoanTypeGroupData.java`, `step/MoveLoanTypeGroupStep.java`, `commandhandler/MoveLoanTypeGroupCommandHandler.java` + test.

**Interfaces — Consumes:** `MoveLoanTypeGroupCommand` (T6), `LoanTypeGroupRepository.{findById,existsById,findAncestorChain}` (T5), `LoanTypeGroup.moveUnder` (T3).

- [ ] **Step 1: Failing test** — three cases: (a) valid re-parent → `LoanTypeGroupMoved` event; (b) new parent does not exist → `PARENT_NOT_FOUND`; (c) new parent is a descendant of the moved node (`findAncestorChain(newParent)` contains `groupId`) → `CYCLE_NOT_ALLOWED`.
- [ ] **Step 2: Run, verify fails.** `-Dtest=MoveLoanTypeGroupCommandHandlerTest`
- [ ] **Step 3: Data record.**

```java
package ir.dotin.loan.trade.core.application.service.loantypegroup.data;

import ir.dotin.loan.trade.core.application.ports.inbound.command.MoveLoanTypeGroupCommand;

public record MoveLoanTypeGroupData(MoveLoanTypeGroupCommand command) {}
```

- [ ] **Step 4: Step with cycle guard.**

```java
package ir.dotin.loan.trade.core.application.service.loantypegroup.step;

import java.time.Clock;
import java.util.List;

import org.springframework.stereotype.Component;

import org.jspecify.annotations.Nullable;

import lombok.RequiredArgsConstructor;

import ir.dotin.loan.baseloan.core.domain.loantypegroup.aggregate.LoanTypeGroup;
import ir.dotin.loan.baseloan.core.domain.loantypegroup.error.LoanTypeGroupErrors;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTypeGroupId;
import ir.dotin.loan.trade.core.application.ports.inbound.command.MoveLoanTypeGroupCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.LoanTypeGroupRepository;
import ir.dotin.loan.trade.core.application.service.loantypegroup.data.MoveLoanTypeGroupData;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.domain.event.DomainEvent;
import ir.dotin.platform.pangaea.workflow.api.activity.PublishingWriteActivity;
import ir.dotin.platform.pangaea.workflow.api.context.WorkflowContext;
import ir.dotin.platform.pangaea.workflow.api.step.StepResult;

@Component
@RequiredArgsConstructor
public class MoveLoanTypeGroupStep implements PublishingWriteActivity<MoveLoanTypeGroupData> {

    private final LoanTypeGroupRepository repository;
    private final Clock clock;

    @Override
    public StepResult<List<DomainEvent<?>>> execute(WorkflowContext<MoveLoanTypeGroupData> ctx) {
        MoveLoanTypeGroupCommand command = ctx.data().command();
        LoanTypeGroupId id = new LoanTypeGroupId(command.groupId());
        @Nullable LoanTypeGroupId newParent =
                command.newParentGroupId() == null ? null : new LoanTypeGroupId(command.newParentGroupId());

        if (newParent != null) {
            if (!repository.existsById(newParent)) {
                return StepResult.fromWriteResult(Result.failure(LoanTypeGroupErrors.PARENT_NOT_FOUND, newParent));
            }
            if (repository.findAncestorChain(newParent).contains(id)) {
                return StepResult.fromWriteResult(
                        Result.failure(LoanTypeGroupErrors.CYCLE_NOT_ALLOWED, id, newParent));
            }
        }

        Result<List<DomainEvent<?>>> result = repository.findById(id)
                .map(group -> group.moveUnder(newParent, clock)
                        .onSuccess(repository::save)
                        .map(LoanTypeGroup::domainEvents))
                .orElseGet(() -> Result.failure(LoanTypeGroupErrors.GROUP_NOT_FOUND, id));
        return StepResult.fromWriteResult(result);
    }
}
```

- [ ] **Step 5: Handler** — same shape, `Workflow.singleWrite("move-loan-type-group", writePublishing(step))`.
- [ ] **Step 6: Run, pass, commit** `feat(CBS-284638:LN-59558:LN-59559): add move-loan-type-group command handler with cycle guard`.

---

### Task 11: Assign-loan-type-to-group handler + step + data

**Files:** under `nova/core/application/service/.../assignloantypegroup/`:
`data/AssignLoanTypeToGroupData.java`, `step/AssignLoanTypeToGroupStep.java`, `commandhandler/AssignLoanTypeToGroupCommandHandler.java` + test.

**Interfaces — Consumes:** `AssignLoanTypeToGroupCommand` (T6), `TradeLoanTypeRepository.findById/save`, `LoanTypeGroupRepository.existsById` (T5), `TradeLoanType.assignToGroup` (T4).

- [ ] **Step 1: Failing test** — given an existing loan type + existing group, assign sets `groupId`, emits `TradeLoanTypeGroupAssigned`, saves; unknown group → `GROUP_NOT_FOUND`; unknown loan type → loan-type not-found error (reuse `LoanTypeQueryErrorCodes`/loan-type error used by `GetLoanTypeByIdQueryHandler`).
- [ ] **Step 2: Run, verify fails.** `-Dtest=AssignLoanTypeToGroupCommandHandlerTest`
- [ ] **Step 3: Data record.**

```java
package ir.dotin.loan.trade.core.application.service.assignloantypegroup.data;

import ir.dotin.loan.trade.core.application.ports.inbound.command.AssignLoanTypeToGroupCommand;

public record AssignLoanTypeToGroupData(AssignLoanTypeToGroupCommand command) {}
```

- [ ] **Step 4: Step.**

```java
package ir.dotin.loan.trade.core.application.service.assignloantypegroup.step;

import java.time.Clock;
import java.util.List;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import ir.dotin.loan.baseloan.core.domain.loantype.entity.AbstractLoanType;
import ir.dotin.loan.baseloan.core.domain.loantypegroup.error.LoanTypeGroupErrors;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTypeGroupId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTypeId;
import ir.dotin.loan.trade.core.application.ports.inbound.command.AssignLoanTypeToGroupCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.LoanTypeGroupRepository;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanTypeRepository;
import ir.dotin.loan.trade.core.application.service.assignloantypegroup.data.AssignLoanTypeToGroupData;
import ir.dotin.loan.trade.core.domain.loantype.entity.TradeLoanType;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.domain.event.DomainEvent;
import ir.dotin.platform.pangaea.workflow.api.activity.PublishingWriteActivity;
import ir.dotin.platform.pangaea.workflow.api.context.WorkflowContext;
import ir.dotin.platform.pangaea.workflow.api.step.StepResult;

@Component
@RequiredArgsConstructor
public class AssignLoanTypeToGroupStep implements PublishingWriteActivity<AssignLoanTypeToGroupData> {

    private final TradeLoanTypeRepository loanTypeRepository;
    private final LoanTypeGroupRepository groupRepository;
    private final Clock clock;

    @Override
    public StepResult<List<DomainEvent<?>>> execute(WorkflowContext<AssignLoanTypeToGroupData> ctx) {
        AssignLoanTypeToGroupCommand command = ctx.data().command();
        LoanTypeGroupId groupId = new LoanTypeGroupId(command.groupId());
        LoanTypeId loanTypeId = LoanTypeId.of(command.loanTypeId());

        if (!groupRepository.existsById(groupId)) {
            return StepResult.fromWriteResult(Result.failure(LoanTypeGroupErrors.GROUP_NOT_FOUND, groupId));
        }

        Result<List<DomainEvent<?>>> result = loanTypeRepository.findById(loanTypeId)
                .map(loanType -> loanType.assignToGroup(groupId, clock)
                        .onSuccess(lt -> loanTypeRepository.save((TradeLoanType) lt))
                        .map(AbstractLoanType::domainEvents))
                .orElseGet(() -> Result.failure(LoanTypeGroupErrors.GROUP_NOT_FOUND, loanTypeId));
        return StepResult.fromWriteResult(result);
    }
}
```
(If `assignToGroup` returns `Result<? extends AbstractLoanType>` the cast `(TradeLoanType) lt` on save matches `TradeLoanTypeRepository.save(TradeLoanType)`. For the loan-type-not-found branch use the existing loan-type not-found error code rather than `GROUP_NOT_FOUND` — locate it in `GetLoanTypeByIdQueryHandler`.)

- [ ] **Step 5: Handler** — `Workflow.singleWrite("assign-loan-type-to-group", writePublishing(step))`.
- [ ] **Step 6: Run, pass, commit** `feat(CBS-284638:LN-59558:LN-59559): add assign-loan-type-to-group command handler`.

---

### Task 12: Remove-loan-type-from-group handler + step + data

**Files:** `…/assignloantypegroup/data/RemoveLoanTypeFromGroupData.java`, `step/RemoveLoanTypeFromGroupStep.java`, `commandhandler/RemoveLoanTypeFromGroupCommandHandler.java` + test.

**Interfaces — Consumes:** `RemoveLoanTypeFromGroupCommand` (T6), `TradeLoanTypeRepository.findById/save`, `TradeLoanType.removeFromGroup` (T4).

- [ ] **Step 1: Failing test** — existing loan type → `groupId` cleared, `TradeLoanTypeGroupRemoved` emitted; unknown loan type → not-found.
- [ ] **Step 2: Run, verify fails.** `-Dtest=RemoveLoanTypeFromGroupCommandHandlerTest`
- [ ] **Step 3: Data record** (mirror Task 11 data, `Remove…`).
- [ ] **Step 4: Step** — same as Task 11 step minus the group existence check; call `loanType.removeFromGroup(clock)`.
- [ ] **Step 5: Handler** — `Workflow.singleWrite("remove-loan-type-from-group", writePublishing(step))`.
- [ ] **Step 6: Run, pass; full service-module build + commit.**

Run: `cd /home/m.amirabdollahi/workspaces/nova && mvn -q -pl core/application/service -am test`
Commit `feat(CBS-284638:LN-59558:LN-59559): add remove-loan-type-from-group command handler`.

---

## Phase 3 — persistence + migration

### Task 13: Liquibase migration

**Files:**
- Create: `nova/container/src/main/resources/db/changelog/trade-loan/changes/20260301T000110_create_loan_type_groups.sql`
- Modify: `nova/container/src/main/resources/db/changelog/trade-loan/db.changelog-trade-loan.xml` (add `<include>` after the `000100` line)
- Read first: `20260301T000000_create_loan_types.sql` and `20260301T000090_create_loan_type_outbox.sql` to copy the exact outbox DDL shape.

- [ ] **Step 1: Write the migration** (mirror the loan-type + loan-type-outbox DDL verbatim for column types/`uuidv7()`/timestamps):

```sql
--liquibase formatted sql

--changeset trade-loan:20260301T000110-create-loan-type-groups logicalFilePath:trade-loan/20260301T000110-create-loan-type-groups
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

--changeset trade-loan:20260301T000110-create-loan-type-groups-outbox logicalFilePath:trade-loan/20260301T000110-create-loan-type-groups-outbox
-- Copy the body of 20260301T000090_create_loan_type_outbox.sql, renaming:
--   loan_type_outbox_events    -> loan_type_groups_outbox_events
--   loan_type_outbox_metadata  -> loan_type_groups_outbox_metadata
-- Keep every column, PK, FK and index identical to the loan_type outbox tables.
```
(Replace the second changeset's comment block with the actual renamed DDL copied from `…000090…`. Do NOT add an FK from `loan_types.group_id` → `loan_type_groups.id`: the existing column is a soft reference (no FK) and back-filling/ordering would break; keep it soft.)

- [ ] **Step 2: Add the include** to `db.changelog-trade-loan.xml`, immediately after the `…000100…` include, matching the existing element style:

```xml
<include file="trade-loan/changes/20260301T000110_create_loan_type_groups.sql" relativeToChangelogFile="false"/>
```
(Copy an existing `<include>` line and change only the filename.)

- [ ] **Step 3: Validate (offline parse).** Build the container module so Liquibase parses the changelog at startup test, or run the app's migration in the dev profile if available. At minimum: `cd /home/m.amirabdollahi/workspaces/nova && mvn -q -pl container -am compile`.

- [ ] **Step 4: Commit** `feat(CBS-284638:LN-59558:LN-59559): add loan_type_groups + outbox migration`.

---

### Task 14: Command-side JPA entity, repo, mapper, adapter

**Files:** under `nova/adapters/driven/persistence/src/main/java/ir/dotin/loan/trade/adapters/driven/persistence/loantypegroup/`:
- `entity/LoanTypeGroupEntity.java`, `repository/LoanTypeGroupJpaRepository.java`, `mapper/LoanTypeGroupPersistenceMapper.java`, `LoanTypeGroupRepositoryAdapter.java`
- Test: `…/loantypegroup/LoanTypeGroupRepositoryAdapterTest.java` (or an `*IT` against the Postgres container support, mirroring an existing persistence IT)

**Interfaces — Consumes:** `LoanTypeGroupRepository` (T5), `LoanTypeGroup` (T3). **Produces:** the `@Repository` adapter bean implementing the command port.

- [ ] **Step 1: Write the entity** (mirror `TradeLoanTypeEntity` extending `PersistentEntity` for id/version/audit):

```java
package ir.dotin.loan.trade.adapters.driven.persistence.loantypegroup.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import org.jspecify.annotations.Nullable;

import ir.dotin.loan.trade.adapters.driven.persistence.shared.PersistentEntity;   // confirm FQN from TradeLoanTypeEntity

@Entity
@Table(name = "loan_type_groups")
public class LoanTypeGroupEntity extends PersistentEntity {

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "parent_group_id")
    @Nullable
    private UUID parentGroupId;

    // getters/setters (or the field-access style PersistentEntity subclasses use — match TradeLoanTypeEntity)
}
```
(Copy the exact `PersistentEntity` import + access style from `TradeLoanTypeEntity.java`.)

- [ ] **Step 2: Write the Spring Data repo.**

```java
package ir.dotin.loan.trade.adapters.driven.persistence.loantypegroup.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import ir.dotin.loan.trade.adapters.driven.persistence.loantypegroup.entity.LoanTypeGroupEntity;
import ir.dotin.loan.trade.adapters.driven.persistence.shared.PersistentRepository;   // confirm FQN

@Repository
public interface LoanTypeGroupJpaRepository extends PersistentRepository<LoanTypeGroupEntity> {
    List<LoanTypeGroupEntity> findByParentGroupId(UUID parentGroupId);
    List<LoanTypeGroupEntity> findByParentGroupIdIsNull();
}
```

- [ ] **Step 3: Write the MapStruct mapper** (mirror `TradeLoanTypePersistenceMapper`: ignore audit on domain→entity; map `LoanTypeGroupId`/`Title` VOs via `ValueObjectMapper` or explicit expressions). Domain→entity and entity→`LoanTypeGroup.reconstitute(...)`.

- [ ] **Step 4: Write the adapter.**

```java
package ir.dotin.loan.trade.adapters.driven.persistence.loantypegroup;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import ir.dotin.loan.baseloan.core.domain.loantypegroup.aggregate.LoanTypeGroup;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTypeGroupId;
import ir.dotin.loan.trade.adapters.driven.persistence.loantypegroup.mapper.LoanTypeGroupPersistenceMapper;
import ir.dotin.loan.trade.adapters.driven.persistence.loantypegroup.repository.LoanTypeGroupJpaRepository;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.LoanTypeGroupRepository;

import static java.util.Objects.requireNonNull;

@Repository
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class LoanTypeGroupRepositoryAdapter implements LoanTypeGroupRepository {

    private final LoanTypeGroupJpaRepository jpaRepository;
    private final LoanTypeGroupPersistenceMapper mapper;

    @Override
    @Transactional
    public LoanTypeGroup save(LoanTypeGroup group) {
        var saved = jpaRepository.save(requireNonNull(mapper.map(group)));
        return mapper.map(saved);
    }

    @Override
    public Optional<LoanTypeGroup> findById(LoanTypeGroupId id) {
        return jpaRepository.findById(id.value()).map(mapper::map);
    }

    @Override
    public Boolean existsById(LoanTypeGroupId id) { return jpaRepository.existsById(id.value()); }

    @Override
    public List<LoanTypeGroup> findChildren(LoanTypeGroupId parentId) {
        return jpaRepository.findByParentGroupId(parentId.value()).stream().map(mapper::map).toList();
    }

    @Override
    public List<LoanTypeGroupId> findAncestorChain(LoanTypeGroupId id) {
        var chain = new java.util.ArrayList<LoanTypeGroupId>();
        var current = jpaRepository.findById(id.value());
        while (current.isPresent() && current.get().getParentGroupId() != null) {
            LoanTypeGroupId parent = new LoanTypeGroupId(current.get().getParentGroupId());
            chain.add(parent);
            current = jpaRepository.findById(parent.value());
        }
        return List.copyOf(chain);
    }
}
```
(Add an `import java.util.ArrayList;` instead of the inline FQN per the no-inline-FQ rule.)

- [ ] **Step 5: Persistence test/IT** — save a root + child, `findById`, `findChildren`, `findAncestorChain` returns `[parent]` for the child. Mirror an existing persistence IT's container bootstrap.
- [ ] **Step 6: Build + commit.** `mvn -q -pl adapters/driven/persistence -am test` → commit `feat(CBS-284638:LN-59558:LN-59559): add LoanTypeGroup persistence adapter`.

---

### Task 15: Outbox entity/mapper/repo/handler

**Files:** under `…/persistence/loantypegroup/outbox/`:
- `LoanTypeGroupOutboxEventEntity.java`, `LoanTypeGroupOutboxEventMapper.java`, `LoanTypeGroupOutboxEventRepository.java`, `LoanTypeGroupOutboxHandler.java`
- Read first: the `TradeLoanType` outbox quartet (`…/loantype/outbox/…`) — copy each, renaming types + the two `@Table`/`@CollectionTable` names to `loan_type_groups_outbox_events` / `loan_type_groups_outbox_metadata`.

**Interfaces — Produces:** `@Service LoanTypeGroupOutboxHandler` whose `aggregateType()` returns `LoanTypeGroup.class` — auto-discovered by pangaea `OutboxHandlerRegistry`. This is what drains `LoanTypeGroupCreated/Renamed/Moved` to the DB.

- [ ] **Step 1: Copy the four classes** from the loan-type outbox package, renaming:
  - entity extends `AbstractOutboxEventEntity`, `@Table(name="loan_type_groups_outbox_events")`, metadata `@CollectionTable(name="loan_type_groups_outbox_metadata")`
  - mapper extends `BaseOutboxEventMapper<LoanTypeGroupOutboxEventEntity>`
  - repository extends `BaseOutboxRepository<LoanTypeGroupOutboxEventEntity>`
  - handler `@Service implements OutboxHandler<LoanTypeGroupOutboxEventEntity, LoanTypeGroupOutboxEventMapper>` with `aggregateType() → LoanTypeGroup.class`, `repository()`, `mapper()`.
- [ ] **Step 2: Build** `mvn -q -pl adapters/driven/persistence -am compile`.
- [ ] **Step 3: Commit** `feat(CBS-284638:LN-59558:LN-59559): add LoanTypeGroup outbox`.

---

### Task 16: Query adapter (tree assembly source data)

**Files:** under `…/persistence/loantypegroup/query/`:
- `LoanTypeGroupQueryRepositoryAdapter.java`, plus a membership projection query against `loan_types`.

**Interfaces — Consumes:** `LoanTypeGroupQueryRepository` (T5), `LoanTypeGroupNodeDto`/`LoanTypeRefDto` (T7). **Produces:** `@Repository` adapter returning flat groups + memberships for the tree handler (T17a).

- [ ] **Step 1: Write the adapter.**

```java
package ir.dotin.loan.trade.adapters.driven.persistence.loantypegroup.query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import ir.dotin.loan.trade.adapters.driven.persistence.loantypegroup.repository.LoanTypeGroupJpaRepository;
import ir.dotin.loan.trade.adapters.driven.persistence.loantype.repository.TradeLoanTypeJpaRepository;
import ir.dotin.loan.trade.core.application.query.loantypegroup.dto.LoanTypeGroupNodeDto;
import ir.dotin.loan.trade.core.application.query.loantypegroup.dto.LoanTypeRefDto;
import ir.dotin.loan.trade.core.application.query.loantypegroup.repository.LoanTypeGroupQueryRepository;

@Repository
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class LoanTypeGroupQueryRepositoryAdapter implements LoanTypeGroupQueryRepository {

    private final LoanTypeGroupJpaRepository groupRepository;
    private final TradeLoanTypeJpaRepository loanTypeRepository;

    @Override
    public List<LoanTypeGroupNodeDto> findAllGroups() {
        return groupRepository.findAll().stream()
                .map(e -> new LoanTypeGroupNodeDto(e.getId(), e.getTitle(), e.getParentGroupId()))
                .toList();
    }

    @Override
    public List<LoanTypeRefDto> findAllMemberships() {
        return loanTypeRepository.findAll().stream()
                .filter(lt -> lt.getGroupId() != null)
                .map(lt -> new LoanTypeRefDto(lt.getId(), lt.getCode().getValue(), lt.getTitle().getValue()))
                .toList();
    }

    @Override
    public Optional<LoanTypeGroupNodeDto> findById(UUID id) {
        return groupRepository.findById(id)
                .map(e -> new LoanTypeGroupNodeDto(e.getId(), e.getTitle(), e.getParentGroupId()));
    }
}
```
(`findAllMemberships` reads `loan_types.group_id` directly — replace `.getCode().getValue()`/`.getTitle().getValue()` with the actual embedded-VO accessors on `TradeLoanTypeEntity`. For a large catalog, swap `findAll()` for a projection query selecting only `id, group_id, loan_type_code, title where group_id is not null` — add it to `TradeLoanTypeJpaRepository`. For now the catalog is tiny (4 rows).)

- [ ] **Step 2: Build + commit** `feat(CBS-284638:LN-59558:LN-59559): add LoanTypeGroup query adapter`.

---

### Task 17: Query handlers (tree assembly)

**Files:** under `nova/core/application/query/.../loantypegroup/handler/`:
- `GetLoanTypeGroupTreeQueryHandler.java`, `GetLoanTypeGroupByIdQueryHandler.java`, `ListRootLoanTypeGroupsQueryHandler.java`
- Test: `…/handler/GetLoanTypeGroupTreeQueryHandlerTest.java`

**Interfaces — Consumes:** `LoanTypeGroupQueryRepository` (T5/16), query records + DTOs (T7). **Produces:** `@Service` `QueryHandler` beans routed by `QueryDispatcher`.

- [ ] **Step 1: Failing test** — given 3 flat groups (root, two children) + 2 memberships, the tree handler returns a single root with two children and the loan-type refs nested under the right child.
- [ ] **Step 2: Run, verify fails.** `-Dtest=GetLoanTypeGroupTreeQueryHandlerTest`
- [ ] **Step 3: Write the tree handler.**

```java
package ir.dotin.loan.trade.core.application.query.loantypegroup.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import ir.dotin.loan.trade.core.application.query.loantypegroup.dto.LoanTypeGroupNodeDto;
import ir.dotin.loan.trade.core.application.query.loantypegroup.dto.LoanTypeGroupTreeDto;
import ir.dotin.loan.trade.core.application.query.loantypegroup.dto.LoanTypeRefDto;
import ir.dotin.loan.trade.core.application.query.loantypegroup.repository.LoanTypeGroupQueryRepository;
import ir.dotin.loan.trade.core.application.query.loantypegroup.request.GetLoanTypeGroupTreeQuery;
import ir.dotin.platform.pangaea.servicelayer.api.query.QueryHandler;

@Service
@RequiredArgsConstructor
public class GetLoanTypeGroupTreeQueryHandler
        implements QueryHandler<GetLoanTypeGroupTreeQuery, LoanTypeGroupTreeDto> {

    private final LoanTypeGroupQueryRepository repository;

    @Override
    public LoanTypeGroupTreeDto handle(GetLoanTypeGroupTreeQuery query) {
        List<LoanTypeGroupNodeDto> nodes = repository.findAllGroups();
        Map<UUID, List<LoanTypeRefDto>> membersByGroup = repository.findAllMemberships().stream()
                .collect(Collectors.groupingBy(LoanTypeRefDto::loanTypeId));   // see note
        Map<UUID, List<LoanTypeGroupNodeDto>> childrenByParent = nodes.stream()
                .filter(n -> n.parentGroupId() != null)
                .collect(Collectors.groupingBy(LoanTypeGroupNodeDto::parentGroupId));

        List<LoanTypeGroupTreeDto> roots = nodes.stream()
                .filter(n -> n.parentGroupId() == null)
                .map(n -> build(n, childrenByParent, membersByGroup))
                .toList();

        return new LoanTypeGroupTreeDto(null, "ROOT", roots, List.of());
    }

    private LoanTypeGroupTreeDto build(
            LoanTypeGroupNodeDto node,
            Map<UUID, List<LoanTypeGroupNodeDto>> childrenByParent,
            Map<UUID, List<LoanTypeRefDto>> membersByGroup) {
        List<LoanTypeGroupTreeDto> children = childrenByParent.getOrDefault(node.id(), List.of()).stream()
                .map(c -> build(c, childrenByParent, membersByGroup))
                .toList();
        List<LoanTypeRefDto> members = membersByGroup.getOrDefault(node.id(), List.of());
        return new LoanTypeGroupTreeDto(node.id(), node.title(), children, members);
    }
}
```
**Note:** `findAllMemberships()` must carry each loan type's `groupId` for the grouping. Either (a) add a `groupId` field to `LoanTypeRefDto` and group by it, or (b) have the query repo return `Map<UUID,List<LoanTypeRefDto>>` directly. Choose (a): extend `LoanTypeRefDto` to `(UUID loanTypeId, String code, String title, UUID groupId)` and `groupingBy(LoanTypeRefDto::groupId)`. Update T7 + T16 accordingly. Fix this inconsistency when implementing (the test will force it).

- [ ] **Step 4: Write `GetLoanTypeGroupByIdQueryHandler`** (return the subtree rooted at `groupId`; `orElseThrow` `FailureCauseException(FailureCause.notFound(...))` with `LoanTypeGroupErrors.GROUP_NOT_FOUND` when absent — mirror `GetLoanTypeByIdQueryHandler`).
- [ ] **Step 5: Write `ListRootLoanTypeGroupsQueryHandler`** (return only roots, no recursion, wrapped in `LoanTypeGroupTreeListResult` if needed per T7).
- [ ] **Step 6: Run, pass, build query module, commit** `feat(CBS-284638:LN-59558:LN-59559): add LoanTypeGroup query handlers (tree assembly)`.

---

## Phase 4 — REST

### Task 18: Swagger tags + request DTOs

**Files:**
- Modify: `nova/adapters/driving/rest/.../config/SwaggerConfig.java` — add `TAG_LOAN_TYPE_GROUP_COMMANDS = "Loan Type Groups · Commands"` and `TAG_LOAN_TYPE_GROUP_QUERIES = "Loan Type Groups · Queries"` constants + register them with `tagsCustomizer()`/`x-tagGroups` exactly as the loan-type tags are.
- Create request DTOs under `nova/adapters/driving/contract/.../dto/`: `CreateLoanTypeGroupRequest`, `RenameLoanTypeGroupRequest`, `MoveLoanTypeGroupRequest`, `AssignLoanTypeToGroupRequest`.

- [ ] **Step 1: Write request DTOs** (mirror `DefineLoanTypeRequest`: `implements BaseRequest`, `@Schema`, validation annotations):

```java
package ir.dotin.loan.trade.adapters.driving.contract.dto;

import java.util.UUID;

import org.jspecify.annotations.Nullable;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import ir.dotin.platform.pangaea.protocol.api.request.BaseRequest;

@Schema(name = "CreateLoanTypeGroupRequest", description = "ایجاد گروه نوع تسهیلات")
public record CreateLoanTypeGroupRequest(
        @NotBlank String title,
        @Nullable UUID parentGroupId)
        implements BaseRequest {}
```
```java
@Schema(name = "RenameLoanTypeGroupRequest", description = "تغییر نام گروه نوع تسهیلات")
public record RenameLoanTypeGroupRequest(@NotNull Long version, @NotBlank String title) implements BaseRequest {}
```
```java
@Schema(name = "MoveLoanTypeGroupRequest", description = "جابجایی گروه در درخت")
public record MoveLoanTypeGroupRequest(@NotNull Long version, @Nullable UUID newParentGroupId) implements BaseRequest {}
```
```java
@Schema(name = "AssignLoanTypeToGroupRequest", description = "تخصیص نوع تسهیلات به گروه")
public record AssignLoanTypeToGroupRequest(@NotNull Long version, @NotNull UUID groupId) implements BaseRequest {}
```

- [ ] **Step 2: Compile + commit** `feat(CBS-284638:LN-59558:LN-59559): add LoanTypeGroup request DTOs + swagger tags`.

---

### Task 19: Command controller

**Files:**
- Create: `nova/adapters/driving/rest/.../command/controller/DefineLoanTypeGroupController.java`
- Create a request→command mapper (mirror `DefineLoanTypeRequestToCommandMapper`) or map inline.

**Interfaces — Consumes:** request DTOs (T18), commands (T6), `CommandDispatcher`, `CommandResponseFactory`, `BaseController.getIdempotencyKey()`.

- [ ] **Step 1: Write the controller** (mirror `DefineLoanTypeController`; class package-private; never `@RequestHeader`):

```java
package ir.dotin.loan.trade.adapters.driving.rest.command.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import ir.dotin.loan.trade.adapters.driving.contract.dto.AssignLoanTypeToGroupRequest;
import ir.dotin.loan.trade.adapters.driving.contract.dto.CreateLoanTypeGroupRequest;
import ir.dotin.loan.trade.adapters.driving.contract.dto.MoveLoanTypeGroupRequest;
import ir.dotin.loan.trade.adapters.driving.contract.dto.RenameLoanTypeGroupRequest;
import ir.dotin.loan.trade.adapters.driving.rest.config.SwaggerConfig;
import ir.dotin.loan.trade.core.application.ports.inbound.command.AssignLoanTypeToGroupCommand;
import ir.dotin.loan.trade.core.application.ports.inbound.command.CreateLoanTypeGroupCommand;
import ir.dotin.loan.trade.core.application.ports.inbound.command.MoveLoanTypeGroupCommand;
import ir.dotin.loan.trade.core.application.ports.inbound.command.RemoveLoanTypeFromGroupCommand;
import ir.dotin.loan.trade.core.application.ports.inbound.command.RenameLoanTypeGroupCommand;
import ir.dotin.platform.pangaea.protocol.rest.controller.BaseController;
import ir.dotin.platform.pangaea.servicelayer.api.dispatcher.CommandDispatcher;
import ir.dotin.platform.pangaea.protocol.rest.response.CommandResponseFactory;   // confirm FQN

@RestController
@RequestMapping("/v{version}/loan-type-groups")
@Tag(name = SwaggerConfig.TAG_LOAN_TYPE_GROUP_COMMANDS, description = "مدیریت گروه نوع تسهیلات")
@RequiredArgsConstructor
class DefineLoanTypeGroupController extends BaseController {

    private final CommandDispatcher dispatcher;
    private final CommandResponseFactory responseFactory;

    @PostMapping(version = "1+")
    @Operation(summary = "ایجاد گروه نوع تسهیلات")
    public ResponseEntity<Void> create(@RequestBody @Valid CreateLoanTypeGroupRequest request) {
        var command = CreateLoanTypeGroupCommand.builder()
                .uid(getIdempotencyKey())
                .title(request.title())
                .parentGroupId(request.parentGroupId())
                .build();
        var result = dispatcher.dispatch(command);
        return responseFactory.created(result, "loan-type-groups");
    }

    @PatchMapping(value = "/{groupId}", version = "1+")
    @Operation(summary = "تغییر نام گروه")
    public ResponseEntity<Void> rename(
            @PathVariable UUID groupId, @RequestBody @Valid RenameLoanTypeGroupRequest request) {
        var command = RenameLoanTypeGroupCommand.builder()
                .uid(getIdempotencyKey()).version(request.version()).groupId(groupId).title(request.title()).build();
        return responseFactory.accepted(dispatcher.dispatch(command));   // or .noContent(...) — match codebase
    }

    @PatchMapping(value = "/{groupId}/parent", version = "1+")
    @Operation(summary = "جابجایی گروه در درخت")
    public ResponseEntity<Void> move(
            @PathVariable UUID groupId, @RequestBody @Valid MoveLoanTypeGroupRequest request) {
        var command = MoveLoanTypeGroupCommand.builder()
                .uid(getIdempotencyKey()).version(request.version())
                .groupId(groupId).newParentGroupId(request.newParentGroupId()).build();
        return responseFactory.accepted(dispatcher.dispatch(command));
    }

    @PutMapping(value = "/../loan-types/{loanTypeId}/group", version = "1+")
    @Operation(summary = "تخصیص نوع تسهیلات به گروه")
    public ResponseEntity<Void> assign(
            @PathVariable UUID loanTypeId, @RequestBody @Valid AssignLoanTypeToGroupRequest request) {
        var command = AssignLoanTypeToGroupCommand.builder()
                .uid(getIdempotencyKey()).version(request.version())
                .loanTypeId(loanTypeId).groupId(request.groupId()).build();
        return responseFactory.accepted(dispatcher.dispatch(command));
    }
}
```
**Important:** the assign/unassign endpoints belong under `/v1/loan-types/{loanTypeId}/group`, NOT under the `/loan-type-groups` base path — put `assign` + the `DELETE` unassign in a SEPARATE controller mapped to `/v{version}/loan-types` (e.g. `LoanTypeGroupMembershipController`) so the `@RequestMapping` base path is correct. The `/../` hack above is a placeholder reminder — split it out. Unassign:

```java
@DeleteMapping(value = "/{loanTypeId}/group", version = "1+")
@Operation(summary = "حذف نوع تسهیلات از گروه")
public ResponseEntity<Void> unassign(@PathVariable UUID loanTypeId, @RequestParam @NotNull Long version) {
    var command = RemoveLoanTypeFromGroupCommand.builder()
            .uid(getIdempotencyKey()).version(version).loanTypeId(loanTypeId).build();
    return responseFactory.accepted(dispatcher.dispatch(command));
}
```
(Confirm `CommandResponseFactory` method names — `created(result, "<segment>")`, and the right state-change return: `accepted`/`noContent`/`ok` — by reading the class; copy exact signatures.)

- [ ] **Step 2: Build** `mvn -q -pl adapters/driving/rest -am compile`.
- [ ] **Step 3: Commit** `feat(CBS-284638:LN-59558:LN-59559): add LoanTypeGroup command + membership controllers`.

---

### Task 20: Query controller

**Files:**
- Create: `nova/adapters/driving/rest/.../query/loantypegroup/LoanTypeGroupQueryController.java`

**Interfaces — Consumes:** query records (T7), `QueryDispatcher`, `BaseResponse`.

- [ ] **Step 1: Write the controller** (mirror `LoanTypeQueryController`):

```java
package ir.dotin.loan.trade.adapters.driving.rest.query.loantypegroup;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import ir.dotin.loan.trade.adapters.driving.rest.config.SwaggerConfig;
import ir.dotin.loan.trade.core.application.query.loantypegroup.dto.LoanTypeGroupTreeDto;
import ir.dotin.loan.trade.core.application.query.loantypegroup.request.GetLoanTypeGroupByIdQuery;
import ir.dotin.loan.trade.core.application.query.loantypegroup.request.GetLoanTypeGroupTreeQuery;
import ir.dotin.platform.pangaea.protocol.api.response.BaseResponse;
import ir.dotin.platform.pangaea.protocol.projection.api.PartialResponse;   // confirm FQN
import ir.dotin.platform.pangaea.protocol.rest.controller.BaseController;
import ir.dotin.platform.pangaea.servicelayer.api.dispatcher.QueryDispatcher;

@RestController
@RequestMapping("/v{version}/loan-type-groups")
@Tag(name = SwaggerConfig.TAG_LOAN_TYPE_GROUP_QUERIES, description = "استعلام گروه نوع تسهیلات")
@RequiredArgsConstructor
class LoanTypeGroupQueryController extends BaseController {

    private final QueryDispatcher dispatcher;

    @GetMapping(value = "/tree", version = "1")
    @Operation(summary = "دریافت درخت گروه‌های نوع تسهیلات")
    public ResponseEntity<BaseResponse<LoanTypeGroupTreeDto>> tree(PartialResponse partial) {
        return ResponseEntity.ok(BaseResponse.success(dispatcher.dispatch(GetLoanTypeGroupTreeQuery.builder().build())));
    }

    @GetMapping(value = "/{groupId}", version = "1")
    @Operation(summary = "دریافت زیردرخت گروه بر اساس شناسه")
    public ResponseEntity<BaseResponse<LoanTypeGroupTreeDto>> byId(
            @PathVariable UUID groupId, PartialResponse partial) {
        return ResponseEntity.ok(BaseResponse.success(
                dispatcher.dispatch(GetLoanTypeGroupByIdQuery.builder().groupId(groupId).build())));
    }
}
```
(If `findAll` roots endpoint is wanted, add `GET` with `ListRootLoanTypeGroupsQuery` returning a list per the codebase list-response helper.)

- [ ] **Step 2: Build whole REST module** `mvn -q -pl adapters/driving/rest -am compile`.
- [ ] **Step 3: Commit** `feat(CBS-284638:LN-59558:LN-59559): add LoanTypeGroup query controller`.

---

### Task 21: Full nova reactor build + ArchUnit + run app

- [ ] **Step 1: Reactor build with tests.**

Run: `cd /home/m.amirabdollahi/workspaces/nova && mvn -q install`
Expected: BUILD SUCCESS — all modules compile, unit tests + ArchUnit green.

- [ ] **Step 2: Boot the app (dev profile) via IDE run config** (do NOT `kill` an IDE-launched JVM; if the port is held, ask the user to stop the running config first). Confirm Liquibase applied `loan_type_groups` and the new endpoints appear at `/v3/api-docs/v1` (search the doc for `/v1/loan-type-groups`).
- [ ] **Step 3: Smoke the command + query** with curl/IDE (create a root group → GET `/v1/loan-type-groups/tree`). Confirm 201 + Location and a tree envelope.
- [ ] **Step 4: Commit** any fixups.

---

## Phase 5 — nova-testkit

### Task 22: Regenerate reference + drift gate

**Files:** `nova-testkit/bruno/reference/loan-type-groups-commands/*`, `…-queries/*` (generated).

- [ ] **Step 1: Regenerate** (nova must be up on `:8085` with `SPRINGDOC_ENABLED=true`):

Run: `cd /home/m.amirabdollahi/workspaces/nova-testkit && make generate`
Expected: new folders `bruno/reference/loan-type-groups-commands/` + `loan-type-groups-queries/` with per-operation `.yml` carrying `assertEnvelope`/`assertContract`.

- [ ] **Step 2: Drift gate** `make verify-drift` → clean.
- [ ] **Step 3: Commit** `feat(LN-59559): regenerate reference for loan-type-group endpoints`.

---

### Task 23: Curated e2e journey `09-loan-type-groups`

**Files:** under `nova-testkit/bruno/e2e/09-loan-type-groups/`:
`folder.yml`, `01-create-root.yml`, `02-create-child-short.yml`, `03-create-child-upload.yml`, `04-resolve-loan-types.yml` (one per code, or a loop), `05-assign-1404.yml`, `06-assign-202601.yml`, `07-assign-202602.yml`, `08-assign-202603.yml`, `09-get-tree.yml`. Modify `Makefile` (+ `.PHONY`).

**Interfaces — Consumes:** the live endpoints + `scripts/headers.js` (`stampHeaders`/`newJourney`) + the existing `GET /v1/loan-types/search?code=` request shape.

- [ ] **Step 1: `folder.yml`** (metadata only):

```yaml
info:
  name: 09-loan-type-groups
```

- [ ] **Step 2: `01-create-root.yml`** — POST a root group, capture id from `Location`:

```yaml
info:
  name: 01-create-root
  type: http
  seq: 1
http:
  method: POST
  url: "{{baseUrl}}/v1/loan-type-groups"
  body:
    type: json
    data: |-
      { "title": "تسهیلات" }
  auth: inherit
runtime:
  scripts:
    - type: before-request
      code: |-
        const { stampHeaders, newJourney } = require('./scripts/headers.js');
        newJourney(bru);
        stampHeaders(req, bru);
    - type: after-response
      code: |-
        const loc = res.getHeader('location') || res.getHeader('Location') || '';
        bru.setVar('rootGroupId', String(loc).split('/').filter(Boolean).pop());
    - type: tests
      code: |-
        test('root created 201', () => expect(res.getStatus()).to.equal(201));
        test('rootGroupId captured', () => expect(bru.getVar('rootGroupId')).to.match(/[0-9a-fA-F-]{8,}/));
settings: { encodeUrl: true, timeout: 0, followRedirects: true, maxRedirects: 5 }
```

- [ ] **Step 3: `02-create-child-short.yml` / `03-create-child-upload.yml`** — POST child groups with `"parentGroupId": "{{rootGroupId}}"`, capture `shortGroupId` / `uploadGroupId` (titles «کوتاه‌مدت» and «بارگذاری ۱۴۰۵»). Same script shape; first lines:

```yaml
      { "title": "کوتاه‌مدت", "parentGroupId": "{{rootGroupId}}" }
```
```yaml
      { "title": "بارگذاری ۱۴۰۵", "parentGroupId": "{{rootGroupId}}" }
```

- [ ] **Step 4: `04-resolve-loan-types.yml`** — `GET /v1/loan-types/search?code=1404`, store id; then per-code steps (or repeat the search per code) storing `lt1404Id`, `lt202601Id`, `lt202602Id`, `lt202603Id`:

```yaml
http:
  method: GET
  url: "{{baseUrl}}/v1/loan-types/search"
  params:
    - { name: code, value: "1404", type: query }
  auth: inherit
runtime:
  scripts:
    - type: before-request
      code: |-
        const { stampHeaders } = require('./scripts/headers.js');
        stampHeaders(req, bru);
    - type: after-response
      code: |-
        const b = res.getBody() || {};
        const items = (b.resultData) || (b.data && (b.data.items||b.data)) || b.items || [];
        const lt = (Array.isArray(items) ? items : []).find(i => String(i.code?.value ?? i.code) === '1404');
        if (lt) bru.setVar('lt1404Id', lt.id);
    - type: tests
      code: |-
        test('1404 resolved', () => expect(bru.getVar('lt1404Id')).to.be.a('string').and.not.empty);
```
(Repeat for `202601/202602/202603` in steps `04b/04c/04d` or duplicate files. Match the items-extraction to the real envelope shape — adapt after a first run against the live `find-all`/`search` response.)

- [ ] **Step 5: `05..08-assign-*.yml`** — `PUT /v1/loan-types/:loanTypeId/group` with body `{ "version": 0, "groupId": "{{shortGroupId}}" }` for 1404; `{{uploadGroupId}}` for 202601/2/3. Path param binding like `02-get-facility`:

```yaml
http:
  method: PUT
  url: "{{baseUrl}}/v1/loan-types/:loanTypeId/group"
  params:
    - { name: loanTypeId, value: "{{lt1404Id}}", type: path }
  body:
    type: json
    data: |-
      { "version": 0, "groupId": "{{shortGroupId}}" }
  auth: inherit
runtime:
  scripts:
    - type: before-request
      code: |-
        const { stampHeaders } = require('./scripts/headers.js');
        stampHeaders(req, bru);
    - type: tests
      code: |-
        test('assign accepted', () => expect(res.getStatus()).to.be.within(200, 204));
```
(`version` is the loan type's current optimistic version — read it first from `GET /v1/loan-types/:id` and `bru.setVar` if the live value isn't 0; add a resolve-version step if needed.)

- [ ] **Step 6: `09-get-tree.yml`** — `GET /v1/loan-type-groups/tree`, assert structure:

```yaml
http: { method: GET, url: "{{baseUrl}}/v1/loan-type-groups/tree", auth: inherit }
runtime:
  scripts:
    - type: before-request
      code: |-
        const { stampHeaders } = require('./scripts/headers.js');
        stampHeaders(req, bru);
    - type: tests
      code: |-
        test('tree 200', () => expect(res.getStatus()).to.equal(200));
        const b = res.getBody() || {};
        const root = (b.resultData) || (b.data) || {};
        test('has children', () => expect(root.children).to.be.an('array').that.is.not.empty);
        const titles = (root.children||[]).map(c => c.title);
        test('short + upload groups present', () => {
          expect(titles).to.include('کوتاه‌مدت'); expect(titles).to.include('بارگذاری ۱۴۰۵');
        });
        const short = (root.children||[]).find(c => c.title === 'کوتاه‌مدت') || {};
        test('1404 under short group', () => {
          expect((short.loanTypes||[]).some(lt => String(lt.code) === '1404')).to.equal(true);
        });
```

- [ ] **Step 7: Makefile target.** Add to `.PHONY` and:

```makefile
e2e-loan-type-groups: ## Bruno: loan-type-group tree journey
	@tools/bru.sh e2e/09-loan-type-groups
```

- [ ] **Step 8: Run the journey** (needs nova + SSO; FCB not required — pure Nova catalog):

Run: `cd /home/m.amirabdollahi/workspaces/nova-testkit && make e2e-loan-type-groups`
Expected: all steps pass (idempotent on re-run — re-creating groups will 201 fresh ids; the journey self-resolves ids each run).

- [ ] **Step 9: Commit** `feat(LN-59559): add loan-type-group e2e tree journey`.

---

### Task 24: k6 read scenario

**Files:** Create `nova-testkit/k6/journeys/loan-type-groups-read.js`; modify `k6/journeys/registry.js`.

- [ ] **Step 1: Write the scenario.**

```js
import http from 'k6/http';
import { check } from 'k6';
import { queryHeaders } from '../lib/headers.js';
import { resolveTarget } from '../lib/target.js';

const TARGET = resolveTarget(__ENV);

export function loanTypeGroupsRead() {
  const res = http.get(`${TARGET.baseUrl}/v1/loan-type-groups/tree`, { headers: queryHeaders(__ENV) });
  check(res, { 'GET /v1/loan-type-groups/tree is 200': (r) => r.status === 200 });
}
```
(Confirm `resolveTarget`/`queryHeaders` exports from `k6/lib/target.js` + `k6/lib/headers.js`; match the existing journeys' import style.)

- [ ] **Step 2: Register** in `k6/journeys/registry.js`: import + add `'loan-type-groups-read': loanTypeGroupsRead,`.
- [ ] **Step 3: Smoke** `make load-smoke` (or the registry-driven runner) hitting the new scenario; expect 200s.
- [ ] **Step 4: Commit** `feat(LN-59559): add k6 loan-type-group tree read scenario`.

---

## Phase 6 — close-out

### Task 25: Cross-repo verification + docs

- [ ] **Step 1:** `cd base-loan && mvn -q install` then `cd nova && mvn -q install` — both green (reactor + ArchUnit).
- [ ] **Step 2:** `cd nova-testkit && make test` (offline harness unit) + `make verify-drift` clean.
- [ ] **Step 3:** Confirm the four DB rows now group correctly: query `10.100.7.20:5433/nova` → `SELECT loan_type_code, group_id FROM loan_types ORDER BY loan_type_code;` shows `1404` + `202601/2/3` with non-null `group_id`; `SELECT id, title, parent_group_id FROM loan_type_groups;` shows root + two children.
- [ ] **Step 4:** Update `nova/CLAUDE.md` module index if it enumerates aggregates (invoke `claude-md-management:revise-claude-md` first). Optional.
- [ ] **Step 5:** Jira close-out per workspace rule — ask the user how long to log on LN-59559, add worklog (Persian comment + AI-provenance footer), transition to terminal state. Do this only after the user confirms the work is complete.

---

## Self-review

- **Spec coverage:** command (T6,8–12) · event (T2,4) · persistence (T13–15) · REST (T18–20) · query (T7,16,17) · groupId source-of-truth (T4,11,12) · flat-aggregate/read-tree (T3,17) · nova-testkit curated journey + auto-gen + k6 (T22–24) · no FCB (none present). All spec sections map to tasks.
- **Known inconsistency to fix during impl (flagged in T17):** `LoanTypeRefDto` needs a `groupId` field for tree grouping — extend it in T7 and T16 (the T17 test forces it). Resolve when reached.
- **Type consistency:** command names (`Create/Rename/Move/AssignLoanTypeToGroup/RemoveLoanTypeFromGroup`), handler/step/data names, `Workflow.singleWrite(writePublishing(step))`, `PublishingWriteActivity<D>` → `StepResult<List<DomainEvent<?>>>` → `.map(Aggregate::domainEvents)`, port names (`LoanTypeGroupRepository`, `LoanTypeGroupQueryRepository`), DTO names (`LoanTypeGroupTreeDto/NodeDto/LoanTypeRefDto`), table names (`loan_type_groups`, `loan_type_groups_outbox_events/_metadata`) are used consistently across tasks.
- **Codebase-specific FQNs to confirm-by-reading** (called out inline where they appear): exact `Result` accessor, `CommandResponseFactory` method names, `PartialResponse`/`PersistentEntity`/`Steps`/`PublishingWriteActivity` packages, `LoanErrorCategory` constant. Each task that touches one instructs the implementer to copy it verbatim from the named sibling file — no guessed signatures shipped as final.
