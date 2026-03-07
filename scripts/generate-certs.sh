#!/usr/bin/env bash
set -euo pipefail

# Prevent MSYS/Git-Bash from converting /CN=localhost to a Windows path
export MSYS_NO_PATHCONV=1

CERT_DIR="$(cd "$(dirname "$0")/.." && pwd)/nginx/certs"
CERT_FILE="$CERT_DIR/selfsigned.crt"
KEY_FILE="$CERT_DIR/selfsigned.key"

if [ -f "$CERT_FILE" ] && [ -f "$KEY_FILE" ]; then
    echo "Certificates already exist in $CERT_DIR — skipping generation."
    exit 0
fi

mkdir -p "$CERT_DIR"

openssl req -x509 -nodes -days 365 \
    -newkey rsa:2048 \
    -keyout "$KEY_FILE" \
    -out "$CERT_FILE" \
    -subj "/CN=localhost"

echo "Self-signed certificate generated in $CERT_DIR"
