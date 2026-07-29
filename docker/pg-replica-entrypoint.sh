#!/bin/bash
# Streaming replica bootstrap: pg_basebackup from primary, then start as standby.
# Runs as root; drops to postgres via docker-entrypoint.sh after fixing ownership.
set -e

until pg_isready -h pg-primary -p 5432 -U "$POSTGRES_USER" >/dev/null 2>&1; do
  echo "waiting for pg-primary..."; sleep 2
done

if [ ! -s "$PGDATA/PG_VERSION" ]; then
  echo "bootstrapping replica from pg-primary..."
  rm -rf "$PGDATA"/* "$PGDATA"/.[!.]* 2>/dev/null || true
  gosu postgres env PGPASSWORD="$REPLICATION_PASSWORD" pg_basebackup \
    -h pg-primary -p 5432 -U "$REPLICATION_USER" \
    -D "$PGDATA" -Fp -Xs -P -R \
    -C -S replica_1
fi

chown -R postgres:postgres "$PGDATA"
chmod 700 "$PGDATA"

exec docker-entrypoint.sh postgres \
  -c shared_preload_libraries=pg_stat_statements,auto_explain \
  -c pg_stat_statements.track=all \
  -c pg_stat_statements.max=10000 \
  -c pg_stat_statements.track_utility=off \
  -c compute_query_id=on \
  -c auto_explain.log_min_duration=500ms \
  -c auto_explain.log_analyze=on \
  -c auto_explain.log_buffers=on \
  -c auto_explain.log_nested_statements=on \
  -c hot_standby=on
