# Nova — developer convenience targets.
# CI uses Maven directly; this is for local use.

set shell := ["/usr/bin/env", "bash", "-c"]
set dotenv-load := false

mvn               := "mvn"
profiles          := "local"
env_file          := "container/.env"
nova_config_dir   := "../nova-config"
jvm_args          := "-XX:+UseZGC -XX:+ZGenerational -XX:+AlwaysPreTouch -Xmx4g -Xss512k --enable-native-access=ALL-UNNAMED"

# Liquibase ops (profile-gated; see container/pom.xml -Pliquibase-ops).
# DB creds come from env_file.
revision   := `sed -n 's/.*-Drevision=\([^ ]*\).*/\1/p' .mvn/maven.config`
lb_cmd     := mvn + " -pl container -Pliquibase-ops -Drevision=" + revision
db_sql_out := "container/target/liquibase-updateSQL.sql"

# ---------------------------------------------------------------------------
# DB_URL example:
#   DB_URL=jdbc:postgresql://10.100.7.20:5433,10.100.7.20:5434/novadb?targetServerType=preferPrimary&loadBalanceHosts=true
# ---------------------------------------------------------------------------

# Show this help
help:
    @just --list

# Full build, unit tests only (CI default gate)
build:
    {{mvn}} clean verify -P'!dev' -DskipITs=true

# Unit tests across the reactor
test:
    {{mvn}} test

# ArchUnit rules only
arch:
    {{mvn}} test -Dtest='**/*ArchitectureTest*' -Dsurefire.failIfNoSpecifiedTests=false

# Install all modules to the local repo (skip tests)
install:
    {{mvn}} install -DskipTests

# Boot NovaApplication like the IDE run config (profile={{profiles}}, feeds {{env_file}}, ZGC VM args)
run:
    #!/usr/bin/env bash
    [[ -f {{env_file}} ]] || { echo "FATAL: {{env_file}} missing — copy container/.env.example"; exit 1; }
    set -a; source {{env_file}}; set +a
    {{mvn}} -pl :trade-loan-container spring-boot:run \
        -Dspring-boot.run.profiles={{profiles}} \
        -Dspring-boot.run.jvmArguments='{{jvm_args}}'

# Generate + install the Consul config schema (trade-loan-container -schema.json)
schema:
    #!/usr/bin/env bash
    {{mvn}} -pl :trade-loan-container -am install -DskipTests
    VERSION=$(mvn -q help:evaluate -Dexpression=revision -DforceStdout 2>/dev/null)
    echo "schema installed: ~/.m2/repository/ir/dotin/loan/trade-loan-container/${VERSION}/trade-loan-container-${VERSION}-schema.json"
    echo "note: 'mvn deploy' publishes it to Nexus automatically (build-helper attach-artifact)"

# Regenerate schema, then validate nova-config KV against it
schema-check: schema
    #!/usr/bin/env bash
    [[ -d {{nova_config_dir}} ]] || { echo "FATAL: {{nova_config_dir}} not found"; exit 1; }
    VERSION=$(mvn -q help:evaluate -Dexpression=revision -DforceStdout 2>/dev/null)
    cd {{nova_config_dir}} && make check SCHEMA_VERSION="${VERSION}"

# Extract i18n message bundles (error codes) into container/src/main/resources/i18n
messages:
    {{mvn}} -pl :trade-loan-container i18n-extractor:extract

# Full E2E suite (requires Docker; Testcontainers compose)
e2e:
    {{mvn}} verify -Pe2e -P'!dev'

# Single E2E test: just e2e-one CompleteFacilityLifecycleRestE2ETest
e2e-one IT:
    {{mvn}} -pl :trade-loan-container -am verify -Pe2e -P'!dev' \
        -Dit.test={{IT}} -Dfailsafe.failIfNoSpecifiedTests=false -Dsurefire.skip=true

# LN-59412 connection-pool pinning regression guard
pool-test:
    just e2e-one ConnectionPoolPinningE2ETest

# Executable Spring Boot jar (k8s profile)
package:
    {{mvn}} clean package -Pk8s,spring-boot-application -pl :trade-loan-container -am -DskipTests

# Regenerate the C4 model (Structurizr) — off the main build, profile-gated
c4:
    {{mvn}} -Pc4-docs -pl documents/c4/java -am -DskipTests process-classes

# Serve the generated C4 workspace in Structurizr Lite (http://localhost:8090)
c4-view:
    cd documents/c4 && ./run-structurizr.sh

# Clean the reactor
clean:
    {{mvn}} clean

# ===========================================================================
# Liquibase operations (profile -Pliquibase-ops). stage/prod policy: RB-0004.
# Each target sources env_file (if present) for DB_* then guards the vars,
# matching the `run` target's env pattern.
# ===========================================================================

# Source .env, guard DB creds, run the given Liquibase goal.
# DB_URL replaces the former DB_HOST + DB_PORT pair.
_lb goal:
    #!/usr/bin/env bash
    if [[ -f {{env_file}} ]]; then set -a; source {{env_file}}; set +a; fi
    : "${DB_URL:?DB_URL unset — populate {{env_file}} (copy container/.env.example) or export DB_URL}"
    : "${DB_NAME:?DB_NAME unset — populate {{env_file}} or export DB_NAME}"
    : "${DB_USERNAME:?DB_USERNAME unset — populate {{env_file}} or export DB_USERNAME}"
    : "${DB_PASSWORD:?DB_PASSWORD unset — populate {{env_file}} or export DB_PASSWORD}"
    {{lb_cmd}} {{goal}}

# Liquibase: list changesets not yet applied
db-status:
    just _lb liquibase:status

# Liquibase: validate the changelog (no DB writes)
db-validate:
    just _lb liquibase:validate

# Liquibase: apply pending changesets to the target DB
db-update:
    just _lb liquibase:update

# Liquibase: dry-run — write pending SQL to {{db_sql_out}} for review (no DB writes)
db-sql:
    just _lb "liquibase:updateSQL -Dliquibase.migrationSqlOutputFile={{db_sql_out}}"
    @echo "wrote dry-run SQL → {{db_sql_out}}"

# Liquibase: tag the current DB state (requires TAG=<name>)
db-tag TAG:
    just _lb "liquibase:tag -Dliquibase.tag={{TAG}}"

# Liquibase: roll back to a tag (requires TAG=<name>)
db-rollback TAG:
    just _lb "liquibase:rollback -Dliquibase.rollbackTag={{TAG}}"

# Liquibase: roll back the last N changesets (requires N=<count>)
db-rollback-count N:
    just _lb "liquibase:rollback -Dliquibase.rollbackCount={{N}}"

# Liquibase: show deployment history of the target DB
db-history:
    just _lb liquibase:history

# Liquibase: force-release a stuck changelog lock
db-release-locks:
    just _lb liquibase:releaseLocks
