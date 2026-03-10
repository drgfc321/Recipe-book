$CertDir = Join-Path $PSScriptRoot "..\nginx\certs"
$CertFile = Join-Path $CertDir "selfsigned.crt"
$KeyFile = Join-Path $CertDir "selfsigned.key"

if ((Test-Path $CertFile) -and (Test-Path $KeyFile)) {
    Write-Host "Certificates already exist in $CertDir - skipping generation."
    exit 0
}

New-Item -ItemType Directory -Force -Path $CertDir | Out-Null

openssl req -x509 -nodes -days 365 `
    -newkey rsa:2048 `
    -keyout $KeyFile `
    -out $CertFile `
    -subj "/CN=localhost"

Write-Host "Self-signed certificate generated in $CertDir"
