#!/bin/bash
# Run from container root: ./scripts/sync-configs.sh

set -e
K8S_BASE="k8s/base"

mkdir -p "$K8S_BASE"

# Sync application.yml to ConfigMap (if exists)
if [ -f "src/main/resources/application.yml" ]; then
cat > "${K8S_BASE}/configmap-trade-loan.yml" <<EOF
apiVersion: v1
kind: ConfigMap
metadata:
  name: trade-loan-service-config
  namespace: default
data:
  application.yml: |
$(cat "src/main/resources/application.yml" | sed 's/^/    /')
EOF
echo "Synced application.yml"
else
echo "No application.yml found - skipping"
fi

# Validate YAML
if command -v kubectl &> /dev/null; then
    kubectl apply --dry-run=client -f "${K8S_BASE}/configmap-trade-loan.yml" > /dev/null 2>&1 && \
    echo "YAML valid" || echo "YAML validation failed"
fi

echo "Done!"