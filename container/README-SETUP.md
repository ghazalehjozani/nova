# Trade Loan Service - Local Setup

## Prerequisites
- Java 25+
- Maven 3.9.11+
- Docker & Docker Compose

## Local infrastructure (postgres / redis HA / broker)

Lives in the sibling repo [`../../nova-dev-stack`](../../nova-dev-stack), not here. Stand it up first:

```bash
cd ../../nova-dev-stack
cp .env.example .env && nano .env       # DB_*, REDIS_PASSWORD, ARTEMIS_* (must match this app's .env)
./redis/render-sentinel-conf.sh
docker compose up -d                    # postgres + redis HA + Artemis (primary broker)
```

## First Time Setup

### 1. Environment Variables (app — client side)
```bash
# Copy template
cp .env.example .env

# Edit .env with your passwords (shared DB_*/REDIS_PASSWORD/ARTEMIS_* must match nova-dev-stack/.env)
nano .env
```

### Build with K8s profile
mvn clean package -Pk8s,spring-boot-application

### Build Docker image
docker build -t trade-loan-service:latest

### Sync configs
./scripts/sync-configs.sh

### Deploy
kubectl apply -f k8s/base/

## ⚠️ Important
NEVER commit:

```
application-dev.yml
.env
k8s/**/secret*.yml
```
