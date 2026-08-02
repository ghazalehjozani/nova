# Nova — developer convenience targets.
# CI uses Maven directly; this is for local use.

# ------------------------------------------------------------------------------
# Variables (can be overridden by environment)
# ------------------------------------------------------------------------------
MVN          := env_var_or_default('MVN', 'mvn')
PROFILES     := env_var_or_default('PROFILES', 'local')
ENV_FILE     := env_var_or_default('ENV_FILE', 'container/.env')
NOVA_CONFIG_DIR := env_var_or_default('NOVA_CONFIG_DIR', '../nova-config')
JVM_ARGS     := env_var_or_default('JVM_ARGS', '-XX:+UseZGC -XX:+ZGenerational -XX:+AlwaysPreTouch -Xmx4g -Xss512k --enable-native-access=ALL-UNNAMED')
REVISION     := env_var_or_default('REVISION', '2026.6.9-SNAPSHOT')
DB_SQL_OUT   := env_var_or_default('DB_SQL_OUT', 'container/target/liquibase-updateSQL.sql')

VERSION      := shell("{{MVN}} -q help:evaluate -Dexpression=revision -DforceStdout 2>/dev/null")
LB           := "{{MVN}} -pl container -Pliquibase-ops -Drevision={{REVISION}}"

# Common boilerplate for Liquibase operations: source .env and guard DB_* vars.
lb_prefix := """
if [[ -f {{ENV_FILE}} ]]; then set -a; source {{ENV_FILE}}; set +a; fi
: "${DB_HOST:?DB_HOST unset — populate {{ENV_FILE}} (copy container/.env.example) or export DB_*}"
: "${DB_PORT:?DB_PORT unset — populate {{ENV_FILE}} or export DB_*}"
: "${DB_NAME:?DB_NAME unset — populate {{ENV_FILE}} or export DB_*}"
: "${DB_USERNAME:?DB_USERNAME unset — populate {{ENV_FILE}} or export DB_*}"
: "${DB_PASSWORD:?DB_PASSWORD unset — populate {{ENV_FILE}} or export DB_*}"
"""

# ------------------------------------------------------------------------------
# Targets
# ------------------------------------------------------------------------------

[doc('Show this help')]
help:
    @just --list

[doc('Full build, unit tests only (CI default gate)')]
build:
    {{MVN}} clean verify -P'!dev' -DskipITs=true

[doc('Unit tests across the reactor')]
test:
    {{MVN}} test

[doc('ArchUnit rules only')]
arch:
    {{MVN}} test -Dtest='**/*ArchitectureTest*' -Dsurefire.failIfNoSpecifiedTests=false

[doc('Install all modules to the local repo (skip tests)')]
install:
    {{MVN}} install -DskipTests

[doc('Boot NovaApplication like the IDE run config (profile=$(PROFILES), feeds $(ENV_FILE), ZGC VM args)')]
run:
    @#!/usr/bin/env bash
    [[ -f {{ENV_FILE}} ]] || { echo "FATAL: {{ENV_FILE}} missing — copy container/.env.example"; exit 1; }
    set -a; source {{ENV_FILE}}; set +a; \
    {{MVN}} -pl :trade-loan-container spring-boot:run \
        -Dspring-boot.run.profiles={{PROFILES}} \
        -Dspring-boot.run.jvmArguments='{{JVM_ARGS}}'

[doc('Generate + install the Consul config schema (trade-loan-container -schema.json)')]
schema:
    {{MVN}} -pl :trade-loan-container -am install -DskipTests
    @echo "schema installed: ~/.m2/repository/ir/dotin/loan/trade-loan-container/{{VERSION}}/trade-loan-container-{{VERSION}}-schema.json"
    @echo "note: 'mvn deploy' publishes it to Nexus automatically (build-helper attach-artifact)"

[doc('Regenerate schema, then validate nova-config KV against it')]
schema-check: schema
    @#!/usr/bin/env bash
    [[ -d {{NOVA_CONFIG_DIR}} ]] || { echo "FATAL: {{NOVA_CONFIG_DIR}} not found"; exit 1; }
    make -C {{NOVA_CONFIG_DIR}} check SCHEMA_VERSION={{VERSION}}

[doc('Extract i18n message bundles (error codes) into container/src/main/resources/i18n')]
messages:
    {{MVN}} -pl :trade-loan-container i18n-extractor:extract

[doc('Full E2E suite (requires Docker; Testcontainers compose)')]
e2e:
    {{MVN}} verify -Pe2e -P'!dev'

[doc('Single E2E test: just e2e-one <TestClassName>')]
e2e-one IT:
    @#!/usr/bin/env bash
    [[ -n "{{IT}}" ]] || { echo "usage: just e2e-one <TestClassName>"; exit 1; }
    {{MVN}} -pl :trade-loan-container -am verify -Pe2e -P'!dev' \
        -Dit.test={{IT}} -Dfailsafe.failIfNoSpecifiedTests=false -Dsurefire.skip=true

[doc('LN-59412 connection-pool pinning regression guard')]
pool-test:
    @just e2e-one ConnectionPoolPinningE2ETest

[doc('Executable Spring Boot jar (k8s profile)')]
package:
    {{MVN}} clean package -Pk8s,spring-boot-application -pl :trade-loan-container -am -DskipTests

[doc('Regenerate the C4 model (Structurizr) — off the main build, profile-gated')]
c4:
    {{MVN}} -Pc4-docs -pl documents/c4/java -am -DskipTests process-classes

[doc('Serve the generated C4 workspace in Structurizr Lite (http://localhost:8090)')]
c4-view:
    cd documents/c4 && ./run-structurizr.sh

[doc('Clean the reactor')]
clean:
    {{MVN}} clean

# ------------------------------------------------------------------------------
# Liquibase operations (profile -Pliquibase-ops). stage/prod policy: RB-0004.
# Each target sources $(ENV_FILE) (if present) for DB_* then guards the vars.
# ------------------------------------------------------------------------------

[doc('Liquibase: list changesets not yet applied')]
db-status:
    @#!/usr/bin/env bash
    {{lb_prefix}}
    {{LB}} liquibase:status

[doc('Liquibase: validate the changelog (no DB writes)')]
db-validate:
    @#!/usr/bin/env bash
    {{lb_prefix}}
    {{LB}} liquibase:validate

[doc('Liquibase: apply pending changesets to the target DB')]
db-update:
    @#!/usr/bin/env bash
    {{lb_prefix}}
    {{LB}} liquibase:update

[doc('Liquibase: dry-run — write pending SQL to $(DB_SQL_OUT) for review (no DB writes)')]
db-sql:
    @#!/usr/bin/env bash
    {{lb_prefix}}
    {{LB}} liquibase:updateSQL -Dliquibase.migrationSqlOutputFile={{DB_SQL_OUT}}
    @echo "wrote dry-run SQL → {{DB_SQL_OUT}}"

[doc('Liquibase: tag the current DB state (requires TAG=<name>)')]
db-tag TAG:
    @#!/usr/bin/env bash
    [[ -n "{{TAG}}" ]] || { echo "usage: just db-tag <name>"; exit 1; }
    {{lb_prefix}}
    {{LB}} liquibase:tag -Dliquibase.tag={{TAG}}

[doc('Liquibase: roll back to a tag (requires TAG=<name>)')]
db-rollback TAG:
    @#!/usr/bin/env bash
    [[ -n "{{TAG}}" ]] || { echo "usage: just db-rollback <name>"; exit 1; }
    {{lb_prefix}}
    {{LB}} liquibase:rollback -Dliquibase.rollbackTag={{TAG}}

[doc('Liquibase: roll back the last N changesets (requires N=<count>)')]
db-rollback-count N:
    @#!/usr/bin/env bash
    [[ -n "{{N}}" ]] || { echo "usage: just db-rollback-count <count>"; exit 1; }
    {{lb_prefix}}
    {{LB}} liquibase:rollback -Dliquibase.rollbackCount={{N}}

[doc('Liquibase: show deployment history of the target DB')]
db-history:
    @#!/usr/bin/env bash
    {{lb_prefix}}
    {{LB}} liquibase:history

[doc('Liquibase: force-release a stuck changelog lock')]
db-release-locks:
    @#!/usr/bin/env bash
    {{lb_prefix}}
    {{LB}} liquibase:releaseLocks
