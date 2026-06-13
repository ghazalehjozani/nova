---
name: recon-convergence-playbook
description: Use when converging or remediating a Nova<->FCB facility-state discrepancy that needs an operator-approved write - especially money/terminal/aged FCB_APPLY_LOST orphans via the maker-checker ops_recon_remediate (REPLAY_FORWARD) path, multi-step/multi-tranche convergence, dossierHash/idempotencyKey handling, and interpreting the sealed write outcomes. Pairs with the recon-ops skill (start there for triage/reads).
---

# Reconciliation Convergence & Remediation Playbook

Deep procedures for the **mutating** recon ops tools on the `facility-state` type. Start with the
**`recon-ops`** skill for concepts, the tool catalog, triage, reads, and the non-money force-converge path.
This skill covers the operator-gated write paths and how to read their outcomes.

`reconciliationType` is always `"facility-state"`; `opaqueKey` is the **facility UUID**.

## force_converge vs remediate — pick the right write

| Situation | Tool |
| --- | --- |
| `AUTO_SAFE`, non-money forward `ORPHAN`/`LAGGING` (re-apply a missing forward step) | `ops_recon_force_converge` |
| Money / terminal / irreversible path, or aged `FCB_APPLY_LOST` orphan, `recommendedKind=REPLAY_FORWARD` | `ops_recon_remediate` |
| Already fixed out-of-band, just close it | `ops_recon_mark_resolved` |

`ops_recon_force_converge` re-drives the earliest not-yet-applied **forward** step and **refuses money/terminal
paths**. Money/terminal convergence must go through `ops_recon_remediate` with an explicit `REPLAY_FORWARD`
action and the maker-checker guards below.

## Service-side gates (must be ON, else writes are refused)

- **Any write tool registered at all:** `platform.reconciliation.management.read-only=false`. When read-only
  (the default for hosts that relied on always-on recon REST ops), the write tools are not registered.
- **`ops_recon_remediate` specifically:** `platform.reconciliation.management.remediation.enabled=true`. When
  off, remediate returns a `NotEnabled` outcome.
- **REPLAY_FORWARD on nova:** the nova-side flag `reconciliation.replay-forward-enabled: true` (startup-read;
  applies on the next restart). The nova operator-allowlist (JWT sub allowlist) also gates the underlying
  replay.

## Playbook 4 — Maker-checker remediation (REPLAY_FORWARD)

For a money/terminal or aged `FCB_APPLY_LOST` orphan whose dossier recommends `REPLAY_FORWARD`:

1. **Fetch a fresh dossier** (the "maker" read):
   ```
   ops_recon_get("facility-state", <facilityId>)
   ```
   Inspect `dossier.rootCause`, `dossier.confidence`, `dossier.recommended` (a `ProposedRemediation`:
   `kind, safetyTier, idempotent, idempotencyKeys[], expectedEffect, reversible, blastRadius,
   preconditions[]`), `dossier.alternatives[]`, `dossier.graceElapsed`, and `dossier.evidence[]`. Confirm
   `recommended.kind == REPLAY_FORWARD` (or that an alternative you intend is `REPLAY_FORWARD`).
2. **Copy `dossier.dossierHash`** verbatim from that same response. This is the TOCTOU guard.
3. **Mint a fresh, unique `idempotencyKey`** (a new UUID) for this remediation attempt. A stable replay of the
   same key returns the stored outcome rather than re-running.
4. **Remediate** (the "checker" write):
   ```
   ops_recon_remediate(
     "facility-state",
     <facilityId>,
     "REPLAY_FORWARD",
     <dossierHash>,        // from step 2, this fetch
     <fresh-uuid>,         // from step 3
     "<operator reason>"   // why you approved it
   )
   ```
5. **Read the outcome** (`result` in the `{correlationId, result}` envelope; see below). Record the
   `correlationId`.
6. **Multi-tranche / multi-step money facilities:** remediation is step-wise — it replays the earliest
   missing forward step. Re-run from step 1 (fresh `ops_recon_get` -> fresh `dossierHash` -> fresh
   `idempotencyKey`) after each sweep until `ops_recon_summary` / `ops_recon_get` shows the facility
   `RESOLVED`. Do **not** reuse a prior `dossierHash` or `idempotencyKey` across steps.

### dossierHash is a strict TOCTOU match

`dossierHash` must match the **current** stored dossier exactly. If the row was re-observed by the sweep
between your read and your write, the hash changed and remediate returns a **`DossierStale`** outcome.
Recovery: re-run `ops_recon_get`, copy the new `dossierHash`, and retry with a **new** `idempotencyKey`.
Never hand-edit or guess a hash.

## Reading write outcomes

Every write returns `{ correlationId, result: <SealedOutcome> }` (parse `content[0].text`). The `result`
is a sealed type; branch on its variant:

**`ForceConvergeOutcome`**
- `Converged{detail}` — convergence ran; the discrepancy is now resolved.
- `Requeued{detail}` — the stored row was re-enqueued for the normal processor; not yet resolved. Re-check after a sweep.
- `Rejected{reason}` — not in a convergeable state (e.g. already `CONVERGING`/`RESOLVED`). Re-`get` and reassess.
- `NotFound{}` — no open discrepancy for that type+key.

**`MarkResolvedOutcome`** — `Resolved{detail}` | `Rejected{reason}` | `NotFound{}`.

**`RemediateOutcome`**
- `Accepted{detail}` — remediation accepted/applied; verify with `ops_recon_get` / `ops_recon_summary`.
- `Rejected{reason}` — refused (e.g. action not permitted for this row).
- `DossierStale{reason}` — the `dossierHash` no longer matches the current dossier; re-fetch and retry (see above).
- `NotEnabled{reason}` — `platform.reconciliation.management.remediation.enabled` is off (or the nova replay flag/allowlist gate is closed).
- `NotFound{}` — no open discrepancy for that type+key.

## Convergence is step-wise — expect to iterate

Both `force_converge` and `remediate` re-drive the **earliest** missing forward step, not the whole chain.
A deep multi-step money orphan needs several `remediate` calls **across sweeps** (~30s each), each preceded
by a fresh `ops_recon_get` for a current `dossierHash` and each with a new `idempotencyKey`. Treat
"the summary shows it `RESOLVED`" as the only completion signal.

## Safety reminders

- `UNKNOWN` verdicts are inconclusive reads (peer unreachable/timeout), **not** confirmed orphans — never
  remediate them; resolve the peer-reachability question first.
- `MCP_OPS_TOKEN` is a privileged operator credential that can move money via `ops_recon_remediate`. Keep it
  secret; never log or paste it. The write tools require `ROLE_MCP_OPS`; a platform JWT (`SCOPE_*` only)
  cannot satisfy that gate.
