#!/bin/bash
# Runs on primary first boot: create replication user + allow replica connections
set -e

psql -v ON_ERROR_STOP=1 -U "$POSTGRES_USER" -d "$POSTGRES_DB" <<-SQL
  CREATE USER ${REPLICATION_USER} WITH REPLICATION ENCRYPTED PASSWORD '${REPLICATION_PASSWORD}';
SQL

cat >> "$PGDATA/pg_hba.conf" <<-HBA
host replication ${REPLICATION_USER} 0.0.0.0/0 scram-sha-256
HBA
