#!/usr/bin/env bash
#
# Serve the generated C4 workspace in Structurizr Lite.
# Works locally and on any server that has the documents/c4 directory + Docker.
#
#   STRUCTURIZR_PORT   host port (default 8090)
#   STRUCTURIZR_IMAGE  image     (default structurizr/structurizr:2026.05.22-noble)
#   DETACH=1           run in the background (no TTY) — used by server deploys
#
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
C4_DIR="$SCRIPT_DIR"
PORT="${STRUCTURIZR_PORT:-8090}"
IMAGE="${STRUCTURIZR_IMAGE:-structurizr/structurizr:2026.05.22-noble}"

# Structurizr Lite runs as a non-root user inside the container and writes the
# workspace + .structurizr/ cache back into the mounted directory. Ensure the
# directory and its contents are world-writable first, otherwise the container
# aborts with a permission error on the bind mount.
mkdir -p "$C4_DIR/.structurizr"
chmod -R a+rwX "$C4_DIR" 2>/dev/null || true

if [[ "${DETACH:-0}" == "1" ]]; then
  RUN_FLAGS=(-d --restart unless-stopped --name nova-c4)
  docker rm -f nova-c4 >/dev/null 2>&1 || true
else
  RUN_FLAGS=(-it --rm)
fi

echo "Structurizr Lite → http://localhost:${PORT}  (workspace: $C4_DIR)"

exec docker run "${RUN_FLAGS[@]}" \
  -p "${PORT}:8080" \
  -v "$C4_DIR:/usr/local/structurizr" \
  "$IMAGE" local
