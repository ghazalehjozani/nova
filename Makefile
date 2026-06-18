# Nova — developer convenience targets.
# CI uses Maven directly; this is for local use.

SHELL := /usr/bin/env bash
MVN ?= mvn
PROFILES ?= local
ENV_FILE ?= container/.env
NOVA_CONFIG_DIR ?= ../nova-config
VERSION := $(shell $(MVN) -q help:evaluate -Dexpression=revision -DforceStdout 2>/dev/null)
JVM_ARGS ?= -XX:+UseZGC -XX:+ZGenerational -XX:+AlwaysPreTouch -Xmx4g -Xss512k --enable-native-access=ALL-UNNAMED

# Liquibase ops (profile-gated; see container/pom.xml -Pliquibase-ops). DB creds come from $(ENV_FILE)/env.
REVISION ?= 2026.6.9-SNAPSHOT
LB := $(MVN) -pl container -Pliquibase-ops -Drevision=$(REVISION)
DB_SQL_OUT ?= container/target/liquibase-updateSQL.sql

.PHONY: help build test arch install run schema schema-check messages e2e e2e-one pool-test package clean c4 c4-view \
	db-status db-validate db-update db-sql db-tag db-rollback db-rollback-count db-history db-release-locks

help: ## Show this help
	@awk 'BEGIN {FS = ":.*##"; printf "Usage: make \033[36m<target>\033[0m\n\nTargets:\n"} \
		/^[a-zA-Z0-9_-]+:.*?##/ { printf "  \033[36m%-14s\033[0m %s\n", $$1, $$2 }' $(MAKEFILE_LIST)

build: ## Full build, unit tests only (CI default gate)
	$(MVN) clean verify -P'!dev' -DskipITs=true

test: ## Unit tests across the reactor
	$(MVN) test

arch: ## ArchUnit rules only
	$(MVN) test -Dtest='**/*ArchitectureTest*' -Dsurefire.failIfNoSpecifiedTests=false

install: ## Install all modules to the local repo (skip tests)
	$(MVN) install -DskipTests

run: ## Boot NovaApplication like the IDE run config (profile=$(PROFILES), feeds $(ENV_FILE), ZGC VM args)
	@[[ -f $(ENV_FILE) ]] || { echo "FATAL: $(ENV_FILE) missing — copy container/.env.example"; exit 1; }
	set -a; source $(ENV_FILE); set +a; \
	$(MVN) -pl :trade-loan-container spring-boot:run \
		-Dspring-boot.run.profiles=$(PROFILES) \
		-Dspring-boot.run.jvmArguments='$(JVM_ARGS)'

schema: ## Generate + install the Consul config schema (trade-loan-container -schema.json)
	$(MVN) -pl :trade-loan-container -am install -DskipTests
	@echo "schema installed: ~/.m2/repository/ir/dotin/loan/trade-loan-container/$(VERSION)/trade-loan-container-$(VERSION)-schema.json"
	@echo "note: 'mvn deploy' publishes it to Nexus automatically (build-helper attach-artifact)"

schema-check: schema ## Regenerate schema, then validate nova-config KV against it
	@[[ -d $(NOVA_CONFIG_DIR) ]] || { echo "FATAL: $(NOVA_CONFIG_DIR) not found"; exit 1; }
	$(MAKE) -C $(NOVA_CONFIG_DIR) check SCHEMA_VERSION=$(VERSION)

messages: ## Extract i18n message bundles (error codes) into container/src/main/resources/i18n
	$(MVN) -pl :trade-loan-container i18n-extractor:extract

e2e: ## Full E2E suite (requires Docker; Testcontainers compose)
	$(MVN) verify -Pe2e -P'!dev'

e2e-one: ## Single E2E test: make e2e-one IT=CompleteFacilityLifecycleRestE2ETest
	@[[ -n "$(IT)" ]] || { echo "usage: make e2e-one IT=<TestClassName>"; exit 1; }
	$(MVN) -pl :trade-loan-container -am verify -Pe2e -P'!dev' \
		-Dit.test=$(IT) -Dfailsafe.failIfNoSpecifiedTests=false -Dsurefire.skip=true

pool-test: ## LN-59412 connection-pool pinning regression guard
	$(MAKE) e2e-one IT=ConnectionPoolPinningE2ETest

package: ## Executable Spring Boot jar (k8s profile)
	$(MVN) clean package -Pk8s,spring-boot-application -pl :trade-loan-container -am -DskipTests

c4: ## Regenerate the C4 model (Structurizr) — off the main build, profile-gated
	$(MVN) -Pc4-docs -pl documents/c4/java -am -DskipTests process-classes

c4-view: ## Serve the generated C4 workspace in Structurizr Lite (http://localhost:8090)
	cd documents/c4 && ./run-structurizr.sh

clean: ## Clean the reactor
	$(MVN) clean

# ---------------------------------------------------------------------------
# Liquibase operations (profile -Pliquibase-ops). stage/prod policy: RB-0004.
# Each target sources $(ENV_FILE) (if present) for DB_* then guards the vars,
# matching the `run` target's env pattern.
# ---------------------------------------------------------------------------
# $(call lb,<goal> [extra args]) — source .env, guard DB creds, run the goal.
define lb
	@if [[ -f $(ENV_FILE) ]]; then set -a; source $(ENV_FILE); set +a; fi; \
	: "$${DB_HOST:?DB_HOST unset — populate $(ENV_FILE) (copy container/.env.example) or export DB_*}"; \
	: "$${DB_PORT:?DB_PORT unset — populate $(ENV_FILE) or export DB_*}"; \
	: "$${DB_NAME:?DB_NAME unset — populate $(ENV_FILE) or export DB_*}"; \
	: "$${DB_USERNAME:?DB_USERNAME unset — populate $(ENV_FILE) or export DB_*}"; \
	: "$${DB_PASSWORD:?DB_PASSWORD unset — populate $(ENV_FILE) or export DB_*}"; \
	$(LB) $(1)
endef

db-status: ## Liquibase: list changesets not yet applied
	$(call lb,liquibase:status)

db-validate: ## Liquibase: validate the changelog (no DB writes)
	$(call lb,liquibase:validate)

db-update: ## Liquibase: apply pending changesets to the target DB
	$(call lb,liquibase:update)

db-sql: ## Liquibase: dry-run — write pending SQL to $(DB_SQL_OUT) for review (no DB writes)
	$(call lb,liquibase:updateSQL -Dliquibase.migrationSqlOutputFile=$(DB_SQL_OUT))
	@echo "wrote dry-run SQL → $(DB_SQL_OUT)"

db-tag: ## Liquibase: tag the current DB state (requires TAG=<name>)
	@[[ -n "$(TAG)" ]] || { echo "usage: make db-tag TAG=<name>"; exit 1; }
	$(call lb,liquibase:tag -Dliquibase.tag=$(TAG))

db-rollback: ## Liquibase: roll back to a tag (requires TAG=<name>)
	@[[ -n "$(TAG)" ]] || { echo "usage: make db-rollback TAG=<name>"; exit 1; }
	$(call lb,liquibase:rollback -Dliquibase.rollbackTag=$(TAG))

db-rollback-count: ## Liquibase: roll back the last N changesets (requires N=<count>)
	@[[ -n "$(N)" ]] || { echo "usage: make db-rollback-count N=<count>"; exit 1; }
	$(call lb,liquibase:rollback -Dliquibase.rollbackCount=$(N))

db-history: ## Liquibase: show deployment history of the target DB
	$(call lb,liquibase:history)

db-release-locks: ## Liquibase: force-release a stuck changelog lock
	$(call lb,liquibase:releaseLocks)
