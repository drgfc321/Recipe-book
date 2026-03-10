#!/bin/bash
# PostgreSQL backup script for Recipe Book
# Runs pg_dump, compresses output, rotates old backups (keeps last 7)

set -euo pipefail

BACKUP_DIR="/backups"
RETENTION_DAYS=7
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
BACKUP_FILE="${BACKUP_DIR}/recipebook_${TIMESTAMP}.sql.gz"

echo "[$(date)] Starting backup..."

pg_dump -h "$PGHOST" -U "$PGUSER" -d "$PGDATABASE" | gzip > "$BACKUP_FILE"

echo "[$(date)] Backup created: $BACKUP_FILE ($(du -h "$BACKUP_FILE" | cut -f1))"

# Remove backups older than retention period
find "$BACKUP_DIR" -name "recipebook_*.sql.gz" -mtime +"$RETENTION_DAYS" -delete

REMAINING=$(find "$BACKUP_DIR" -name "recipebook_*.sql.gz" | wc -l)
echo "[$(date)] Backup complete. $REMAINING backup(s) retained."
