#!/bin/bash
set -e

if [ -z "$1" ]; then
    echo "Usage: ./get-secret.sh <key>"
    echo "Example: ./get-secret.sh fcb.username"
    exit 1
fi

KEY=$1

kubectl get secret trade-loan-service-secrets -o jsonpath="{.data.$KEY}" | base64 -d
echo