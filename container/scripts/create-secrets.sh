#!/bin/bash
set -e

if [ ! -f .env ]; then
    echo "Error: .env file not found"
    exit 1
fi

set -a
source .env
set +a

DOCKER_HOST_IP=$(minikube ssh "ip route show default" | awk '/default/ {print $3}')

DB_URL="jdbc:postgresql://${DOCKER_HOST_IP}:${DB_PORT}/${DB_NAME}"
KAFKA_SERVERS="10.100.8.81:9093,10.100.8.82:9093,10.100.8.83:9093"
REDIS_HOST="${DOCKER_HOST_IP}"

kubectl create secret generic trade-loan-service-secrets \
  --from-literal=spring.datasource.url="$DB_URL" \
  --from-literal=spring.datasource.username="$DB_USERNAME" \
  --from-literal=spring.datasource.password="$DB_PASSWORD" \
  --from-literal=spring.data.redis.host="$REDIS_HOST" \
  --from-literal=spring.data.redis.port="$REDIS_PORT" \
  --from-literal=redis.password="$REDIS_PASSWORD" \
  --from-literal=kafka.bootstrap.servers="$KAFKA_SERVERS" \
  --from-literal=kafka.consumer.group="$KAFKA_CONSUMER_GROUP" \
  --from-literal=kafka.user="$KAFKA_USER" \
  --from-literal=kafka.password="$KAFKA_PASSWORD" \
  --from-literal=platform.messaging.encryption.password="$ENCRYPTION_PASSWORD" \
  --from-literal=oauth2.client.id="$OAUTH2_CLIENT_ID" \
  --from-literal=oauth2.client.secret="$OAUTH2_CLIENT_SECRET" \
  --from-literal=fcb.base.url="$FCB_BASE_URL" \
  --from-literal=fcb.username="$FCB_USERNAME" \
  --from-literal=fcb.password="$FCB_PASSWORD" \
  --from-literal=otel.traces.endpoint="$OTEL_EXPORTER_OTLP_TRACES_ENDPOINT" \
  --from-literal=otel.metrics.endpoint="$OTEL_EXPORTER_OTLP_METRICS_ENDPOINT" \
  --dry-run=client -o yaml | kubectl apply -f -

echo "✓ Secrets created/updated"