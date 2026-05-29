# Trade Loan API Documentation

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
