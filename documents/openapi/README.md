# Trade Loan API Documentation

## Overview

This document contains the OpenAPI 3.1 specification for the Trade Loan Service, a microservice that manages Morabehe (Trade) loans within the enterprise loan management system.

## Service Details

- **Service Name**: trade-loan-service
- **Version**: 1.0.0
- **Base URL**: http://127.0.0.1:8080
- **Authentication**: Bearer JWT (OAuth 2.0)

## Files

- `openapi.yaml` - Complete OpenAPI 3.1 specification with dereferenced components
- `openapi.json` - OpenAPI specification in JSON format
- `README.md` - This file

## Accessing the API

### Base URL
- **Local Development**: `http://localhost:8080`
- **Service**: trade-loan-service

### Authentication
The API uses OAuth 2.0 Bearer JWT tokens for authentication.

### Interactive Documentation
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8080/v3/api-docs`

## API Categories

The service provides endpoints for:

1. **Loan Arrangement Management** - مدیریت شرایط اعطا
2. **Loan Type Management** - مدیریت نوع تسهیلات
3. **Facility Case Opening** - ایجاد پرونده تسهیلات
4. **Facility Approval Submission** - ثبت درخواست تصویب مصوبه
5. **Facility Approval** - تصویب مصوبه
6. **Facility Contract Issuance** - صدور قرارداد تسهیلات
7. **Lump Sum Disbursement** - پرداخت یکجای تسهیلات
8. **Irregular Disbursement** - پرداخت نامنظم تسهیلات
9. **Facility Collateral Management** - مدیریت وثایق تسهیلات
10. **Facility Closure** - بستن تسهیلات (پرداخت شده/معوق)
11. **Queries** - استعلام تسهیلات، اقساط، نوع تسهیلات
12. **Full Loan Facility Lifecycle** - عملیات چرخه کامل تسهیلات
13. **Audit Queries** - Command audit trail
14. **Actuator** - Health checks and metrics

## Request Headers

All API requests should include:
- `X-Request-ID`: Unique request identifier (UUID)
- `Idempotency-Key`: UUID for idempotent execution
- `X-Request-DateTime`: Client timestamp (UTC, ISO 8601)
- `Accept-Language`: Preferred language (fa | en | ar | ru; default: en)
- `Authorization`: Bearer JWT token

## Response Headers

API responses include:
- `X-Request-ID`: Echoed from request
- `Idempotency-Key`: Echoed from request
- `X-Request-DateTime`: Echoed from request
- `X-Idempotency-Replayed`: `true` if served from cache
- `X-Response-DateTime`: Server response timestamp
- `X-Cache`: `HIT` if cached, `MISS` otherwise

## Regeneration

To regenerate this specification:

```bash
# Using Redocly CLI
npx -y @redocly/cli bundle http://localhost:8080/v3/api-docs \
  --output documents/openapi/openapi.yaml \
  --dereferenced

# Or using curl
curl -s http://localhost:8080/v3/api-docs > documents/openapi/openapi.json
```

## Validation

```bash
# Validate with Redocly
npx -y @redocly/cli lint documents/openapi/openapi.yaml

# Generate HTML documentation
npx -y @redocly/cli build-docs documents/openapi/openapi.yaml
```

## Architecture Notes

This service implements:
- **Pattern**: Hexagonal Architecture (Ports & Adapters)
- **Domain Model**: Domain-Driven Design with CQRS
- **Framework**: Spring Boot 3.5.5 with Java 25
- **Security**: OAuth 2.0 + JWT with TPS SSO integration
- **Resilience**: Circuit breakers, retries, and rate limiting
- **Observability**: OpenTelemetry tracing and metrics

## Integration Points

- **FCB (Core Banking)**: Facility case opening and validation
- **Kafka**: Event-driven messaging for async processing
- **PostgreSQL**: Primary data persistence
- **Redis**: Caching and idempotency store
- **TPS SSO**: Authentication and authorization