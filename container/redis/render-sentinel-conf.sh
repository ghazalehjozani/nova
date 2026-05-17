#!/usr/bin/env bash
set -euo pipefail

# Render per-sentinel conf files from sentinel.conf.tmpl using values in .env.
# Run before `docker compose up`. Produces sentinel-1.conf, sentinel-2.conf, sentinel-3.conf
# next to the template. Sentinel REWRITES its conf at runtime, so the files are mounted RW.

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TMPL="${SCRIPT_DIR}/sentinel.conf.tmpl"
ENV_FILE="${SCRIPT_DIR}/../.env"

[[ -f "$TMPL" ]] || { echo "missing $TMPL" >&2; exit 1; }
[[ -f "$ENV_FILE" ]] || { echo "missing $ENV_FILE" >&2; exit 1; }

set -a
# shellcheck disable=SC1090
source "$ENV_FILE"
set +a

render() {
    local idx="$1"
    local announce_host_var="REDIS_SENTINEL_${idx}_ANNOUNCE_HOST"
    local announce_port_var="REDIS_SENTINEL_${idx}_ANNOUNCE_PORT"
    local announce_host="${!announce_host_var:?$announce_host_var must be set}"
    local announce_port="${!announce_port_var:?$announce_port_var must be set}"
    local master_host="${REDIS_MASTER_MONITOR_HOST:?REDIS_MASTER_MONITOR_HOST must be set}"
    local master_port="${REDIS_MASTER_MONITOR_PORT:-6379}"
    local master_name="${REDIS_MASTER_NAME:?REDIS_MASTER_NAME must be set}"
    local password="${REDIS_PASSWORD:?REDIS_PASSWORD must be set}"

    local out="${SCRIPT_DIR}/sentinel-${idx}.conf"
    sed -e "s|@@ANNOUNCE_IP@@|${announce_host}|g" \
        -e "s|@@ANNOUNCE_PORT@@|${announce_port}|g" \
        -e "s|@@MASTER_HOST@@|${master_host}|g" \
        -e "s|@@MASTER_PORT@@|${master_port}|g" \
        -e "s|@@REDIS_MASTER_NAME@@|${master_name}|g" \
        -e "s|@@REDIS_PASSWORD@@|${password}|g" \
        "$TMPL" > "$out"
    echo "rendered $out (announce ${announce_host}:${announce_port})"
}

render 1
render 2
render 3
