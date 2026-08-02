# Trade Loan Service - Local Setup

## Prerequisites
- Java 25+
- Maven 3.9.11+
- Docker & Docker Compose

## Local infrastructure (PostgreSQL / broker / Redis Cluster)

PostgreSQL and Artemis live in the sibling repo [`../../nova-dev-stack`](../../nova-dev-stack). Start only
those compatible services:

```bash
cd ../../nova-dev-stack
cp .env.example .env && nano .env       # DB_* and ARTEMIS_* must match this app's .env
docker compose up -d postgres artemis
```

Nova is Cluster-only. The Sentinel services in `nova-dev-stack` are intentionally incompatible and must
not be configured as Nova seed nodes. Supply a reachable Redis Cluster separately and set
`REDIS_CLUSTER_NODES`, `REDIS_USERNAME`, `REDIS_PASSWORD`, and `REDIS_TLS_ENABLED` in `container/.env`.
Every address returned by `CLUSTER SHARDS` must be reachable from the Nova process.

The automated E2E profile provisions its own single-primary Redis Cluster fixture; run it through
`make e2e` instead of reusing the Sentinel stack.

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
