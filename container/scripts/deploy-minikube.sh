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

echo -e "\n${YELLOW}Creating/updating secrets...${NC}"
./scripts/create-secrets.sh

echo -e "\n${YELLOW}Applying Kubernetes resources...${NC}"
kubectl apply -k k8s/base/

echo -e "\n${YELLOW}Patching deployment...${NC}"
kubectl patch deployment trade-loan-service -p '{"spec":{"template":{"spec":{"containers":[{"name":"trade-loan-service","imagePullPolicy":"Never"}]}}}}'

echo -e "\n${YELLOW}Waiting for deployment...${NC}"
kubectl rollout restart deployment/trade-loan-service
kubectl wait --for=condition=available --timeout=300s deployment/trade-loan-service

echo -e "\n${GREEN}✓ Deployment successful!${NC}\n"
kubectl get pods -l app=trade-loan-service

echo -e "\n${GREEN}=== Access Commands ===${NC}"
echo -e "Logs: ${YELLOW}kubectl logs -f deployment/trade-loan-service${NC}"
echo -e "Port forward: ${YELLOW}kubectl port-forward service/trade-loan-service 8085:8080${NC}"
echo -e "Health: ${YELLOW}curl http://localhost:8085/actuator/health${NC}"