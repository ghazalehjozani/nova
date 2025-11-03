#!/bin/bash
set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

cd "$PROJECT_ROOT"

echo -e "${GREEN}=== Trade Loan Service - Minikube Deployment ===${NC}\n"

if [ ! -f .env ]; then
    echo -e "${RED}Error: .env file not found${NC}"
    exit 1
fi

set -a
source .env
set +a

echo -e "${GREEN}✓${NC} Environment loaded"

if ! minikube status | grep -q "Running"; then
    echo -e "${YELLOW}Starting Minikube...${NC}"
    minikube start --cpus=4 --memory=8192 --driver=docker
fi

echo -e "\n${YELLOW}Configuring Docker...${NC}"
eval $(minikube docker-env)

echo -e "\n${YELLOW}Building application...${NC}"
mvn clean package -Pk8s,spring-boot-application -DskipTests

echo -e "\n${YELLOW}Building Docker image...${NC}"
docker build -t trade-loan-service:latest .

DOCKER_HOST_IP=$(minikube ssh "ip route show default" | awk '/default/ {print $3}')

echo -e "\n${YELLOW}Creating secrets...${NC}"
DB_URL="jdbc:postgresql://${DOCKER_HOST_IP}:${DB_PORT}/${DB_NAME}"
KAFKA_SERVERS="${DOCKER_HOST_IP}:9092"
REDIS_HOST="${DOCKER_HOST_IP}"

kubectl create secret generic trade-loan-service-secrets \
  --from-literal=spring.datasource.url="$DB_URL" \
  --from-literal=spring.datasource.username="$DB_USERNAME" \
  --from-literal=spring.datasource.password="$DB_PASSWORD" \
  --from-literal=platform.messaging.encryption.password="$ENCRYPTION_PASSWORD" \
  --dry-run=client -o yaml | kubectl apply -f -

echo -e "\n${YELLOW}Applying kustomize base...${NC}"
kubectl apply -k k8s/base/

echo -e "\n${YELLOW}Patching environment variables...${NC}"
kubectl set env deployment/trade-loan-service \
  KAFKA_BOOTSTRAP_SERVERS="$KAFKA_SERVERS" \
  KAFKA_CONSUMER_GROUP="$KAFKA_CONSUMER_GROUP" \
  REDIS_PASSWORD="$REDIS_PASSWORD" \
  SPRING_DATA_REDIS_HOST="$REDIS_HOST" \
  OTEL_EXPORTER_OTLP_TRACES_ENDPOINT="$OTEL_EXPORTER_OTLP_TRACES_ENDPOINT" \
  OTEL_EXPORTER_OTLP_METRICS_ENDPOINT="$OTEL_EXPORTER_OTLP_METRICS_ENDPOINT" \
  OAUTH2_CLIENT_ID="$OAUTH2_CLIENT_ID" \
  OAUTH2_CLIENT_SECRET="$OAUTH2_CLIENT_SECRET" \
  FCB_BASE_URL="$FCB_BASE_URL" \
  FCB_USERNAME="$FCB_USERNAME" \
  FCB_PASSWORD="$FCB_PASSWORD"

kubectl patch deployment trade-loan-service -p '{"spec":{"template":{"spec":{"containers":[{"name":"trade-loan-service","imagePullPolicy":"Never"}]}}}}'

echo -e "\n${YELLOW}Waiting for deployment...${NC}"
kubectl wait --for=condition=available --timeout=300s deployment/trade-loan-service

echo -e "\n${GREEN}✓ Deployment successful!${NC}\n"
kubectl get pods -l app=trade-loan-service

echo -e "\n${GREEN}=== Access Commands ===${NC}"
echo -e "Logs: ${YELLOW}kubectl logs -f deployment/trade-loan-service${NC}"
echo -e "Port forward: ${YELLOW}kubectl port-forward service/trade-loan-service 8085:8080${NC}"
echo -e "Health: ${YELLOW}curl http://localhost:8085/actuator/health${NC}"