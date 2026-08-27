# Trade Loan API Documentation

## Breaking change (2026-08-27) — localized-enum schema components were renamed

Every localized enum's schema component is now named by a stable, hand-declared kebab-case identifier instead of its
Java class name, and every `$ref` to it moved with it:

```
#/components/schemas/ApplicantChannel   ->  #/components/schemas/applicant-channel
#/components/schemas/FacilityStatus     ->  #/components/schemas/facility-status
```

The same value is the name in `GET /v1/reference/enums` and the `{enumName}` segment of
`GET /v1/reference/enums/{enumName}`, so a client reading a `$ref` and a client reading the catalog identify an enum
identically. Nothing inside a payload changed — `code` values are still the enum constants' names, labels are still
`Accept-Language`-localized. Only the identifier of the component and the catalog moved.

**What consumers must do:** regenerate any client generated from this document, and replace hard-coded catalog names
(`/v1/reference/enums/ApplicantChannel` -> `/v1/reference/enums/applicant-channel`). An old name now returns `404`.

Why: the previous name was `enumClass.getSimpleName()`, so renaming or moving a Java class silently rewrote a
published identifier — and two classes sharing a simple name renamed *both* catalogs. The declared name is immune to
refactoring; see `platform/pangaea/documents/adr/ADR-0037.stable-declared-names-for-localized-enums.md`.

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
