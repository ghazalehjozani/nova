#!/bin/bash
set -e

if [ ! -f .env ]; then
    echo "Error: .env file not found"
    exit 1
fi

set -a
source .env
set +a

if [[ "$OSTYPE" == "darwin"* ]]; then
    DOCKER_HOST_IP="host.docker.internal"
elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
    DOCKER_HOST_IP=$(minikube ssh "ip route show default" | awk '/default/ {print $3}')
else
    DOCKER_HOST_IP="host.docker.internal"
fi

DB_URL="jdbc:postgresql://${DOCKER_HOST_IP}:${DB_PORT}/${DB_NAME}"

kubectl create secret generic trade-loan-service-secrets \
  --from-literal=spring.datasource.url="$DB_URL" \
  --from-literal=spring.datasource.username="$DB_USERNAME" \
  --from-literal=spring.datasource.password="$DB_PASSWORD" \
  --from-literal=platform.messaging.encryption.password="$ENCRYPTION_PASSWORD" \
  --dry-run=client -o yaml | kubectl apply -f -

echo "✓ Secret created"
