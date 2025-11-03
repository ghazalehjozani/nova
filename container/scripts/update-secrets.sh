#!/bin/bash
set -e

if [ -z "$1" ]; then
    echo "Usage: ./update-secrets.sh <key> <value>"
    echo "Example: ./update-secrets.sh fcb.username newuser"
    exit 1
fi

KEY=$1
VALUE=$2

kubectl patch secret trade-loan-service-secrets -p "{\"data\":{\"$KEY\":\"$(echo -n $VALUE | base64)\"}}"
kubectl rollout restart deployment/trade-loan-service

echo "✓ Secret '$KEY' updated and deployment restarted"