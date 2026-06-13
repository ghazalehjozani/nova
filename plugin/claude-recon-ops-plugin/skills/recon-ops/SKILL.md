---
name: recon-ops
description: Use to reconcile Nova<->FCB facility-state discrepancies via the recon MCP ops tools - triage the discrepancy backlog, look up a discrepancy by loan application number, find/converge/remediate ORPHAN or LAGGING facilities, and inspect a discrepancy's operator dossier before acting. Talks to the running service's /v1/ops/mcp endpoint (server "nova-recon-ops").
---

# Reconciliation Ops (Nova <-> FCB)

This skill drives **Nova <-> FCB dual-persistence reconciliation** (LN-59442) through the recon MCP ops
tools. Pangaea runs a generic discrepancy engine; nova implements the `facility-state` reconciliation
type. A ~30s sweep detects divergence, classifies it, and **auto-converges `AUTO_SAFE` rows**; money /
terminal rows are `OPERATOR_GATED` and wait for a human/AI operator here.

The tools are served by the **`nova-recon-ops`** MCP server (this plugin's `.mcp.json`), pointed at the
running service's `/v1/ops/mcp` endpoint. If the tools are missing, the service is down, the URL/token are
unset, or `platform.ai.mcp.security.enabled` locked the endpoint (blank token -> every request 401s). See
the plugin `README.md` for `MCP_OPS_TOKEN` + `RECON_OPS_MCP_URL` setup.

## Core concepts

- **`reconciliationType`** — for nova always **`"facility-state"`**. Pass it to every tool.
- **`opaqueKey`** — the **facility UUID**. For `facility-state`, opaqueKey == the loan facility id.
- **`verdict`** — `ALIGNED` | `LAGGING` | `ORPHAN` | `UNKNOWN`.
  - `ORPHAN` = one store holds state the peer never materialized (e.g. Nova has the facility, FCB has no file).
  - `UNKNOWN` = indeterminate read (peer unreachable/timeout/parse error); **never auto-driven**, ages into operator attention.
- **`direction`** — `NONE` | `SOURCE_AHEAD` | `TARGET_AHEAD` | `UNKNOWN`. For `facility-state`: source = Nova authority, target = FCB. Convergence flows from the ahead pole toward the lagging one.
- **`autonomyTier`** — `AUTO_SAFE` (engine auto-drives up to an attempt cap) | `OPERATOR_GATED` (irreversible/money-moving — operator only).
- **`status`** — `OPEN` -> `CONVERGING` -> `RESOLVED`, with branches `NEEDS_OPERATOR` and `REMEDIATION_PENDING`. `RESOLVED` is the only terminal/closed state.
- **`rootCause`** — `FCB_LAG` | `FCB_APPLY_LOST` | `FCB_BUSINESS_REJECT` | `PARTIAL_APPLY` | `NOVA_PHANTOM` | `UNKNOWN`. `FCB_APPLY_LOST` = a forward event was published but FCB never materialized the file.
- **`recommendedKind`** (RemediationKind) — `REPLAY_FORWARD` | `REVERSE_NOVA` | `MANUAL_DATA_FIX` | `NONE`.
- **`dossier`** (OperatorDossier) — `{ rootCause, confidence (HIGH|MEDIUM|LOW), novaSnapshot, fcbSnapshot, graceElapsed, evidence[], recommended (ProposedRemediation), alternatives[], dossierHash }`. **`dossierHash`** is the maker-checker TOCTOU guard required by `ops_recon_remediate`.

A `DiscrepancyView` (returned by `ops_recon_get`, `loan_recon_get_by_application_number`, and inside each
search page item) carries: `reconciliationType, opaqueKey, verdict, direction, autonomyTier, status,
attemptCount, firstSeen, lastSeen, correlationId, causationId, reasonCode, detail (map: typically
novaStatus, fcbFileStatus, applicationNumber), rootCause, recommendedKind, dossier`.

## The tools

Endpoint: `POST /v1/ops/mcp` (JSON-RPC, MCP streamable-HTTP, stateless). Auth: static bearer `MCP_OPS_TOKEN`.

**Read (always safe):**
- `ops_recon_summary(reconciliationType?)` -> `List<{status, count}>`. **Start here** for a health overview.
- `ops_recon_search(reconciliationType?, status?, verdict?, autonomyTier?, cursor?, size)` -> `{items[], nextCursor}`. `size` clamped to 1..100. All filters optional except `size`; omit a filter to span all values.
- `ops_recon_get(reconciliationType, opaqueKey)` -> `DiscrepancyView`. **Read the dossier + copy `dossierHash` here BEFORE any remediate.** Not-found error if absent.
- `loan_recon_get_by_application_number(applicationNumber)` **[NOVA]** -> `DiscrepancyView`. Resolves a loan application number (format `branch-loanType-customer-seq`, e.g. `"1-1404-10088-279"`) to its facility id, then fetches the `facility-state` discrepancy. Use when the operator has the application number, not the facility UUID.

**Write (MUTATING, operator-gated — see the `recon-convergence-playbook` skill for the full procedure):**
- `ops_recon_force_converge(reconciliationType, opaqueKey)` -> `{correlationId, result: ForceConvergeOutcome}`. Re-drives the earliest not-yet-applied forward step (non-money / `AUTO_SAFE` or operator-forced). Does **not** drive money/terminal paths — use `ops_recon_remediate` for those.
- `ops_recon_mark_resolved(reconciliationType, opaqueKey, note)` -> `{correlationId, result: MarkResolvedOutcome}`. Close/suppress a discrepancy handled out-of-band; no re-drive.
- `ops_recon_remediate(reconciliationType, opaqueKey, action, dossierHash, idempotencyKey, operatorReason)` -> `{correlationId, result: RemediateOutcome}`. Operator-approved remediation (`action` = a RemediationKind by name, e.g. `"REPLAY_FORWARD"`). Maker-checker path for money/terminal/aged `FCB_APPLY_LOST` orphans.

## Reading tool results (important)

- The JSON payload is in the MCP result **`content[0].text`** — parse that. A `structuredContent.value` may be present for some tools and null for others; **rely on `content[0].text`**.
- Write tools wrap their outcome: the result object is `{ correlationId, result: <Outcome> }` (the `StampedResult` envelope). Record the `correlationId` — it is stamped onto the outbox/audit pipeline so the side effects trace back to this MCP call.
- Sealed outcomes translate to errors: a not-found discrepancy or a non-convergeable/conflict state surfaces as a 404/409-equivalent JSON-RPC error.

## Playbook 1 — Triage the backlog

1. `ops_recon_summary("facility-state")`. Read the per-status counts.
2. If `NEEDS_OPERATOR > 0` (or `REMEDIATION_PENDING > 0`): `ops_recon_search("facility-state", status="NEEDS_OPERATOR")` (page with `nextCursor` if needed).
3. For each item, `ops_recon_get("facility-state", <opaqueKey>)` and read `dossier.rootCause`, `dossier.confidence`, `dossier.recommended` and the top-level `recommendedKind`.
4. Route each row:
   - `autonomyTier=AUTO_SAFE`, `verdict=ORPHAN`/`LAGGING`, non-money -> Playbook 3 (force-converge).
   - `recommendedKind=REPLAY_FORWARD` on a money/terminal or aged `FCB_APPLY_LOST` orphan -> `recon-convergence-playbook` skill, Playbook 4 (remediate).
   - Handled outside the system already -> Playbook 5 (mark resolved).
   - `verdict=UNKNOWN` -> do **not** drive; investigate the peer reachability first (it is an inconclusive read, not a confirmed orphan).

## Playbook 2 — Look up by application number

Operator gives an application number, not a UUID:

```
loan_recon_get_by_application_number("1-1404-10088-279")
```

Returns the `facility-state` `DiscrepancyView`. From there, its `opaqueKey` is the facility UUID you pass
to the `ops_recon_*` tools.

## Playbook 3 — Converge a non-money forward orphan (AUTO_SAFE)

For an `AUTO_SAFE` `ORPHAN`/`LAGGING` whose `recommendedKind` is not a money path:

```
ops_recon_force_converge("facility-state", <facilityId>)
```

Re-drives the earliest missing forward step (re-applies on FCB via effect-aware idempotency). It typically
moves to `CONVERGING` and converges within a sweep (~30s). Convergence is **step-wise** — for a multi-step
divergence, re-run after the next sweep until `ops_recon_summary` / `ops_recon_get` shows it `RESOLVED`.
If it returns a `Rejected`/`NotFound` outcome, re-fetch with `ops_recon_get` and reassess (it may already be
`CONVERGING`/`RESOLVED`, or it is a money/terminal path that needs `ops_recon_remediate`).

## Playbook 5 — Suppress / close out-of-band

When the discrepancy was already handled outside the system (e.g. a manual FCB fix) and should just be closed:

```
ops_recon_mark_resolved("facility-state", <facilityId>, "handled out of band: <why>")
```

No re-drive. Always pass a real operator note.

## For money / terminal / aged FCB_APPLY_LOST orphans

These need the maker-checker remediation path (dossierHash + idempotencyKey + operator reason). See the
**`recon-convergence-playbook`** skill (Playbook 4). Do not try to force-converge them — `ops_recon_force_converge`
will not drive money/terminal paths.

## Security (must understand before using)

- `MCP_OPS_TOKEN` authenticates **only** the `/v1/ops/mcp` endpoint (its own scoped Spring Security filter
  chain, realm `mcp-ops`, principal `mcp-ops-client`, authority `ROLE_MCP_OPS`). It cannot authenticate the
  main REST / business endpoints.
- The ops write tools require the MCP ops operator identity (`ROLE_MCP_OPS`). Platform JWTs carry only
  `SCOPE_*` authorities (a disjoint set), so a JWT can never satisfy this operator gate via that authority.
- `MCP_OPS_TOKEN` is a **privileged operator credential** — it can move money via `ops_recon_remediate`.
  Keep it secret; never log it or paste it into chat.
