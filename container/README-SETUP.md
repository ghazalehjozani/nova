# Trade Loan Service - Local Setup

## Prerequisites
- Java 25+
- Maven 3.9.11+
- Docker & Docker Compose

## First Time Setup

### 1. Environment Variables
```bash
# Copy template
cp .env.example .env

# Edit .env with your passwords
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
