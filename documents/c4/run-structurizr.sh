#!/bin/bash

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
C4_DIR="$SCRIPT_DIR"

echo "Structurizr will available at http://localhost:8090"

docker run -it --rm \
  -p 8090:8080 \
  -v "$C4_DIR:/usr/local/structurizr" \
  structurizr/lite
