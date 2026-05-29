# Trade Loan AsyncAPI Documentation

## Validation

```bash
# Validate AsyncAPI specification
npx -y @asyncapi/cli validate documents/asyncapi/asyncapi.yaml

# Generate HTML documentation
npx -y @asyncapi/cli generate from-file documents/asyncapi/asyncapi.yaml \
  --param htmlTemplate=single-page \
  -o documents/asyncapi/index.html
```