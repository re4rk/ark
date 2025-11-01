#!/bin/bash

# Self-signed SSL certificate generator for development

SSL_DIR="$(dirname "$0")/ssl"
CERT_FILE="${SSL_DIR}/cert.pem"
KEY_FILE="${SSL_DIR}/key.pem"

# Create ssl directory if it doesn't exist
mkdir -p "${SSL_DIR}"

# Check if certificates already exist
if [ -f "${CERT_FILE}" ] && [ -f "${KEY_FILE}" ]; then
    echo "SSL certificates already exist at ${SSL_DIR}"
    read -p "Do you want to regenerate them? (y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        exit 0
    fi
fi

# Generate self-signed certificate
echo "Generating self-signed SSL certificate..."
openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
    -keyout "${KEY_FILE}" \
    -out "${CERT_FILE}" \
    -subj "/C=KR/ST=Seoul/L=Seoul/O=ARK/OU=Development/CN=localhost"

if [ $? -eq 0 ]; then
    echo "SSL certificate generated successfully!"
    echo "Certificate: ${CERT_FILE}"
    echo "Private Key: ${KEY_FILE}"
    echo ""
    echo "Note: This is a self-signed certificate for development only."
    echo "For production, use certificates from a trusted CA (e.g., Let's Encrypt)."
else
    echo "Error: Failed to generate SSL certificate"
    exit 1
fi

