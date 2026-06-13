# recon-ops — Nova ↔ FCB Reconciliation Ops (Claude Code plugin)

A docs + config Claude Code plugin that packages the **Nova ↔ FCB reconciliation MCP ops surface**
(LN-59442) so an operator or AI can drive reconciliation through MCP. The MCP tools themselves are
**server-side**, implemented in pangaea (`pangaea-ai-mcp-ops`) and nova
(`adapters/driving/mcp`); this plugin only points Claude Code at the running service's ops endpoint and
ships the runbook skills. It contains **no Java and no build files**.

## What it gives you

- An HTTP MCP server entry **`nova-recon-ops`** wired to the service's `/v1/ops/mcp` endpoint (`.mcp.json`).
- Two skills:
  - **`recon-ops`** — concepts, the tool catalog, and the triage / look-up / non-money-converge / suppress runbooks.
  - **`recon-convergence-playbook`** — the operator-gated write paths: maker-checker `ops_recon_remediate` (REPLAY_FORWARD), multi-step/multi-tranche convergence, and how to read the sealed write outcomes.

### Tools exposed by the endpoint

Served at `POST /v1/ops/mcp` (JSON-RPC, MCP streamable-HTTP, stateless). For `facility-state`, `opaqueKey`
is the facility UUID.

| Tool                                   | Kind        | Purpose                                                                                              |
|----------------------------------------|-------------|------------------------------------------------------------------------------------------------------|
| `ops_recon_summary`                    | read        | Per-status discrepancy counts. Start here.                                                           |
| `ops_recon_search`                     | read        | Cursor-paginated search (filters: status, verdict, autonomyTier, reconciliationType).                |
| `ops_recon_get`                        | read        | One `DiscrepancyView` incl. the dossier + `dossierHash`.                                             |
| `loan_recon_get_by_application_number` | read (nova) | Resolve a loan application number (e.g. `1-1404-10088-279`) to its `facility-state` discrepancy.     |
| `ops_recon_force_converge`             | write       | Re-drive the earliest missing **non-money** forward step.                                            |
| `ops_recon_mark_resolved`              | write       | Close/suppress a discrepancy handled out-of-band.                                                    |
| `ops_recon_remediate`                  | write       | Operator-approved maker-checker remediation (e.g. `REPLAY_FORWARD`) for money/terminal/aged orphans. |

## Install

This plugin is **personal-scope / project-scope** friendly. Pick one:

**A. Load directly from this directory (dev / one session):**

```bash
claude --plugin-dir /home/m.amirabdollahi/workspaces/pangaea/reconciliation/claude-recon-ops-plugin
```

**B. As a skills-directory plugin (loads automatically next session):**

Copy or symlink this directory into a skills directory so it is discovered as `recon-ops@skills-dir`:

```bash
# personal (every project)
ln -s /home/m.amirabdollahi/workspaces/pangaea/reconciliation/claude-recon-ops-plugin \
      ~/.claude/skills/recon-ops
# then, in a new session:
/reload-plugins
```

**C. Via a marketplace** — add this directory as a plugin source in your `marketplace.json`, then
`claude plugin install recon-ops@<marketplace>`.

After loading, run `claude plugin validate <dir>` (or `/plugin validate`) to confirm the manifest, skills,
and `.mcp.json` parse cleanly.

## Configure (two environment variables)

The `.mcp.json` server entry uses environment-variable expansion:

```json
{
  "mcpServers": {
    "nova-recon-ops": {
      "type": "http",
      "url": "${RECON_OPS_MCP_URL:-http://localhost:8085/v1/ops/mcp}",
      "headers": { "Authorization": "Bearer ${MCP_OPS_TOKEN}" }
    }
  }
}
```

- **`MCP_OPS_TOKEN`** — the **ops static bearer** for the service. It is the same value the service reads
  for the ops endpoint, defined in the service env: `nova/container/.env` -> `MCP_OPS_TOKEN` (bound in
  Consul via `${MCP_OPS_TOKEN}`). This is **required and has no default** — if unset, Claude Code fails to
  parse the config. There is a *separate* `MCP_BUSINESS_TOKEN` for the business read endpoint `/v1/mcp`;
  do **not** use it here.
- **`RECON_OPS_MCP_URL`** — the running service's ops endpoint. Defaults to
  `http://localhost:8085/v1/ops/mcp` (nova's `SERVER_PORT=8085` + `/v1/ops/mcp`). Override to point at a
  remote instance, e.g. `https://nova.stage.internal/v1/ops/mcp`.

Export them before launching Claude Code (or set them in your shell profile / CI secret store):

```bash
export MCP_OPS_TOKEN='<the ops static bearer from the service env>'
export RECON_OPS_MCP_URL='http://localhost:8085/v1/ops/mcp'   # optional; this is the default
```

## Security

- `MCP_OPS_TOKEN` authenticates **only** the `/v1/ops/mcp` endpoint (its own scoped Spring Security filter
  chain, realm `mcp-ops`, principal `mcp-ops-client`, authority `ROLE_MCP_OPS`). It cannot authenticate the
  service's main REST / business endpoints.
- Ops write tools require the MCP ops operator identity (`ROLE_MCP_OPS`). Platform JWTs carry only `SCOPE_*`
  authorities (a disjoint set), so a JWT cannot satisfy the operator gate via this authority.
- The ops endpoint is **fail-closed**: a blank/unset token on the service side locks it (every request
  `401`s).
- `MCP_OPS_TOKEN` is a **privileged operator credential** — it can move money via `ops_recon_remediate`.
  Keep it secret; never commit it, log it, or paste it into chat. It lives only in the git-ignored service
  `.env`.

## Service-side prerequisites for writes

The write tools are gated on the service side:

- `platform.reconciliation.management.read-only=false` — otherwise the write tools are not registered.
- `platform.reconciliation.management.remediation.enabled=true` — required for `ops_recon_remediate`
  (else it returns a `NotEnabled` outcome).
- `reconciliation.replay-forward-enabled: true` (nova) — required for `REPLAY_FORWARD` remediation
  (startup-read; applies on the next restart).

## Notes

- Tool results: parse the JSON payload from the MCP result `content[0].text`. A `structuredContent.value`
  may be present for some tools and null for others — rely on `content[0].text`.
- Write tools return a `{ correlationId, result: <outcome> }` envelope; the `correlationId` is stamped onto
  the service's outbox/audit pipeline so side effects trace back to the MCP call.
- `type: "http"` is the streamable-HTTP transport (Claude Code also accepts the alias `"streamable-http"`).
