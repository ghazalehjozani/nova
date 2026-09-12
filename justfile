# Nova developer, verification, and publication workflows.
# Registry contract: authenticated pulls use 7110; authenticated pushes use 7111.

set shell := ["/usr/bin/env", "bash", "-c"]
set dotenv-load := false

mod? docker 'docker'

mvn := env_var_or_default("MVN", "mvn")
profiles := "local"
env_file := "container/.env"
nova_config_dir := "/workspace/config/nova-config"
image_name := "expenditures/nova-service"
pull_registry := "nexus3.dotin.ir:7111"
push_registry := "nexus3.dotin.ir:7110"
maven_release_url := "https://nexus.dotin.ir/nexus/content/repositories/CoreRelease"
maven_snapshot_url := "https://nexus.dotin.ir/nexus/content/repositories/CoreSnapshots"
jvm_args := "-XX:+UseZGC -XX:+AlwaysPreTouch -Xmx4g -Xss512k --enable-native-access=ALL-UNNAMED"
revision := `sed -n 's/.*-Drevision=\([^ ]*\).*/\1/p' .mvn/maven.config`
lb_cmd := mvn + " -pl container -Pliquibase-ops -Drevision=" + revision
db_sql_out := "container/target/liquibase-updateSQL.sql"

[private]
default: help

# Show available recipes.
help:
    @just --list

# Verify the local publication toolchain and required secret/file inputs.
[group('build')]
doctor:
    #!/usr/bin/env bash
    set -Eeuo pipefail
    [[ -f "{{ env_file }}" ]] || { echo "FATAL: {{ env_file }} missing; copy container/.env.example" >&2; exit 1; }
    set -a; source "{{ env_file }}"; set +a
    for command in {{ mvn }} just docker git; do
        command -v "$command" >/dev/null || { echo "FATAL: $command not found" >&2; exit 1; }
    done
    : "${NEXUS_PULL_USERNAME:?set NEXUS_PULL_USERNAME in {{ env_file }}}"
    : "${NEXUS_PULL_PASSWORD:?set NEXUS_PULL_PASSWORD in {{ env_file }}}"
    : "${NEXUS_PUSH_USERNAME:?set NEXUS_PUSH_USERNAME in {{ env_file }}}"
    : "${NEXUS_PUSH_PASSWORD:?set NEXUS_PUSH_PASSWORD in {{ env_file }}}"
    : "${NEXUS_MAVEN_USERNAME:?set NEXUS_MAVEN_USERNAME in {{ env_file }}}"
    : "${NEXUS_MAVEN_PASSWORD:?set NEXUS_MAVEN_PASSWORD in {{ env_file }}}"
    : "${IMAGE_CA_CERTS_DIR:?set IMAGE_CA_CERTS_DIR in {{ env_file }}}"
    : "${NOVA_KEYSTORE_PATH:?set NOVA_KEYSTORE_PATH in {{ env_file }}}"
    settings="${MAVEN_SETTINGS:-$HOME/.m2/settings.xml}"
    [[ -f "$settings" ]] || { echo "FATAL: Maven settings not found: $settings" >&2; exit 1; }
    [[ -f "$NOVA_KEYSTORE_PATH" ]] || { echo "FATAL: keystore not found: $NOVA_KEYSTORE_PATH" >&2; exit 1; }
    compgen -G "$IMAGE_CA_CERTS_DIR/*.crt" >/dev/null || { echo "FATAL: no .crt files in $IMAGE_CA_CERTS_DIR" >&2; exit 1; }
    docker info >/dev/null
    echo "publication prerequisites are available"

# Full build with unit tests; integration tests remain profile-gated.
[group('build')]
build:
    {{ mvn }} clean verify -P'!dev' -DskipITs=true

# Run unit tests across the reactor.
[group('build')]
test:
    {{ mvn }} test

# Run ArchUnit rules only.
[group('build')]
arch:
    {{ mvn }} test -Dtest='**/*ArchitectureTest*' -Dsurefire.failIfNoSpecifiedTests=false

# Install all modules in the local Maven repository without tests.
[group('build')]
install:
    {{ mvn }} install -DskipTests

# Start Nova with the local profile and container/.env.
[group('run')]
run:
    #!/usr/bin/env bash
    set -Eeuo pipefail
    [[ -f "{{ env_file }}" ]] || { echo "FATAL: {{ env_file }} missing; copy container/.env.example" >&2; exit 1; }
    set -a; source "{{ env_file }}"; set +a
    {{ mvn }} -pl :trade-loan-container spring-boot:run \
        -Dspring-boot.run.profiles={{ profiles }} \
        -Dspring-boot.run.jvmArguments='{{ jvm_args }}'

# Generate and install Nova's attached JSON schema artifact.
[group('publish')]
schema:
    #!/usr/bin/env bash
    set -Eeuo pipefail
    {{ mvn }} -pl :trade-loan-container -am install -DskipTests
    schema_file="container/target/nova.schema.json"
    [[ -s "$schema_file" ]] || { echo "FATAL: schema was not generated: $schema_file" >&2; exit 1; }
    echo "schema installed: ir.dotin.loan:trade-loan-container:{{ revision }}:json:schema"

# Generate the schema and validate every nova-config YAML before GitOps use.
[group('publish')]
schema-check: schema
    #!/usr/bin/env bash
    set -Eeuo pipefail
    [[ -d "{{ nova_config_dir }}" ]] || { echo "FATAL: {{ nova_config_dir }} not found" >&2; exit 1; }
    SCHEMA_VERSION="{{ revision }}" SCHEMA_FILE="$(pwd)/container/target/nova.schema.json" \
        make -C "{{ nova_config_dir }}" check

# Extract i18n message bundles.
[group('build')]
messages:
    {{ mvn }} -pl :trade-loan-container i18n-extractor:extract

# Run the full Testcontainers E2E suite.
[group('test')]
e2e:
    {{ mvn }} verify -Pe2e -P'!dev'

# Run one E2E test, for example: just e2e-one ConnectionPoolPinningE2ETest
[group('test')]
e2e-one IT:
    {{ mvn }} -pl :trade-loan-container -am verify -Pe2e -P'!dev' \
        -Dit.test={{ IT }} -Dfailsafe.failIfNoSpecifiedTests=false -Dsurefire.skip=true

# Run the connection-pool pinning regression guard.
[group('test')]
pool-test:
    just e2e-one ConnectionPoolPinningE2ETest

# Build the executable Spring Boot JAR without tests.
[group('publish')]
package:
    {{ mvn }} clean package -P'!dev,nexus' -pl :trade-loan-container -am -DskipTests

# Publish Maven artifacts, including the attached schema JSON, using the release profile.
[group('publish')]
maven-publish VERSION=revision:
    #!/usr/bin/env bash
    set -Eeuo pipefail
    just _publish-guard "{{ VERSION }}"
    [[ -f "{{ env_file }}" ]] || { echo "FATAL: {{ env_file }} missing" >&2; exit 1; }
    set -a; source "{{ env_file }}"; set +a
    : "${NEXUS_MAVEN_USERNAME:?set NEXUS_MAVEN_USERNAME in {{ env_file }}}"
    : "${NEXUS_MAVEN_PASSWORD:?set NEXUS_MAVEN_PASSWORD in {{ env_file }}}"
    settings="${MAVEN_SETTINGS:-$HOME/.m2/settings.xml}"
    [[ -f "$settings" ]] || { echo "FATAL: Maven settings not found: $settings" >&2; exit 1; }
    {{ mvn }} -s "$settings" -pl :trade-loan-container -am clean install \
        -Drevision="{{ VERSION }}" -DskipTests -P'!dev,nexus'
    [[ -s container/target/nova.schema.json ]] || { echo "FATAL: Nova schema is missing" >&2; exit 1; }
    SCHEMA_VERSION="{{ VERSION }}" SCHEMA_FILE="$(pwd)/container/target/nova.schema.json" \
        MAVEN_SETTINGS="$settings" make -C "{{ nova_config_dir }}" check
    {{ mvn }} -s "$settings" clean deploy -Drevision="{{ VERSION }}" -DskipITs=true \
        -P'!dev,nexus,release'
    [[ -s container/target/nova.schema.json ]] || { echo "FATAL: deployed build did not generate the schema" >&2; exit 1; }
    echo "published Maven reactor and schema classifier for {{ VERSION }}"

# Repair/publish the schema classifier when the application artifact already exists.
[group('publish')]
schema-publish VERSION=revision:
    #!/usr/bin/env bash
    set -Eeuo pipefail
    just _publish-guard "{{ VERSION }}"
    [[ -f "{{ env_file }}" ]] || { echo "FATAL: {{ env_file }} missing" >&2; exit 1; }
    set -a; source "{{ env_file }}"; set +a
    : "${NEXUS_MAVEN_USERNAME:?set NEXUS_MAVEN_USERNAME in {{ env_file }}}"
    : "${NEXUS_MAVEN_PASSWORD:?set NEXUS_MAVEN_PASSWORD in {{ env_file }}}"
    settings="${MAVEN_SETTINGS:-$HOME/.m2/settings.xml}"
    [[ -f "$settings" ]] || { echo "FATAL: Maven settings not found: $settings" >&2; exit 1; }
    {{ mvn }} -s "$settings" -pl :trade-loan-container -am clean install \
        -Drevision="{{ VERSION }}" -DskipTests -P'!dev,nexus'
    schema_file="container/target/nova.schema.json"
    [[ -s "$schema_file" ]] || { echo "FATAL: schema was not generated: $schema_file" >&2; exit 1; }
    SCHEMA_VERSION="{{ VERSION }}" SCHEMA_FILE="$(pwd)/container/target/nova.schema.json" \
        MAVEN_SETTINGS="$settings" make -C "{{ nova_config_dir }}" check
    repository_id=releases
    repository_url="${NEXUS_MAVEN_RELEASE_URL:-{{ maven_release_url }}}"
    if [[ "{{ VERSION }}" == *-SNAPSHOT ]]; then
        repository_id=snapshots
        repository_url="${NEXUS_MAVEN_SNAPSHOT_URL:-{{ maven_snapshot_url }}}"
    else
        echo "note: an immutable release repository may reject adding a missing classifier"
    fi
    {{ mvn }} -s "$settings" org.apache.maven.plugins:maven-deploy-plugin:3.1.2:deploy-file \
        -Dfile="$schema_file" \
        -DgroupId=ir.dotin.loan \
        -DartifactId=trade-loan-container \
        -Dversion="{{ VERSION }}" \
        -Dpackaging=json \
        -Dclassifier=schema \
        -DgeneratePom=false \
        -DrepositoryId="$repository_id" \
        -Durl="$repository_url" \
        -P'!dev,nexus,release'
    echo "published schema classifier for {{ VERSION }}"

# Build a local OCI image from a temporary, minimal context.
[group('image')]
image-build TAG=revision:
    #!/usr/bin/env bash
    set -Eeuo pipefail
    just _validate-tag "{{ TAG }}"
    [[ -f "{{ env_file }}" ]] || { echo "FATAL: {{ env_file }} missing" >&2; exit 1; }
    set -a; source "{{ env_file }}"; set +a
    : "${NEXUS_PULL_USERNAME:?set NEXUS_PULL_USERNAME in {{ env_file }}}"
    : "${NEXUS_PULL_PASSWORD:?set NEXUS_PULL_PASSWORD in {{ env_file }}}"
    : "${IMAGE_CA_CERTS_DIR:?set IMAGE_CA_CERTS_DIR in {{ env_file }}}"
    : "${NOVA_KEYSTORE_PATH:?set NOVA_KEYSTORE_PATH in {{ env_file }}}"
    [[ -f "$NOVA_KEYSTORE_PATH" ]] || { echo "FATAL: keystore not found: $NOVA_KEYSTORE_PATH" >&2; exit 1; }
    certs=("$IMAGE_CA_CERTS_DIR"/*.crt)
    [[ -e "${certs[0]}" ]] || { echo "FATAL: no .crt files in $IMAGE_CA_CERTS_DIR" >&2; exit 1; }
    {{ mvn }} clean package -P'!dev,nexus' -pl :trade-loan-container -am -DskipTests -Drevision="{{ TAG }}"
    jars=(container/target/*.jar)
    [[ ${#jars[@]} -eq 1 ]] || { echo "FATAL: expected one application JAR, found ${#jars[@]}" >&2; exit 1; }
    context="$(mktemp -d)"
    docker_config="$(mktemp -d)"
    cleanup() { rm -rf "$context" "$docker_config"; }
    trap cleanup EXIT
    mkdir -p "$context/target" "$context/ca-certificates"
    cp container/Dockerfile "$context/Dockerfile"
    cp "${jars[0]}" "$context/target/application.jar"
    cp "$NOVA_KEYSTORE_PATH" "$context/target/keystore.p12"
    cp "${certs[@]}" "$context/ca-certificates/"
    export DOCKER_CONFIG="$docker_config"
    printf '%s' "$NEXUS_PULL_PASSWORD" | docker login "${NEXUS_PULL_REGISTRY:-{{ pull_registry }}}" \
        --username "$NEXUS_PULL_USERNAME" --password-stdin >/dev/null
    source_url="$(git config --get remote.origin.url || true)"
    docker build --pull \
        --build-arg "NEXUS_PROXY_REGISTRY=${NEXUS_PULL_REGISTRY:-{{ pull_registry }}}" \
        --build-arg "NEXUS_APT_REPO_URL=${NEXUS_APT_REPO_URL:-https://nexus3.dotin.ir/repository/Ubuntu}" \
        --label "org.opencontainers.image.created=$(date -u +%Y-%m-%dT%H:%M:%SZ)" \
        --label "org.opencontainers.image.revision=$(git rev-parse HEAD)" \
        --label "org.opencontainers.image.source=$source_url" \
        --label "org.opencontainers.image.version={{ TAG }}" \
        --tag "{{ image_name }}:{{ TAG }}" "$context"
    docker logout "${NEXUS_PULL_REGISTRY:-{{ pull_registry }}}" >/dev/null || true
    just image-smoke "{{ TAG }}"

# Inspect mandatory runtime metadata on a locally built image.
[group('image')]
image-smoke TAG=revision:
    #!/usr/bin/env bash
    set -Eeuo pipefail
    just _validate-tag "{{ TAG }}"
    user="$(docker image inspect --format '{{ "{{.Config.User}}" }}' "{{ image_name }}:{{ TAG }}")"
    healthcheck="$(docker image inspect --format '{{ "{{json .Config.Healthcheck.Test}}" }}' "{{ image_name }}:{{ TAG }}")"
    [[ "$user" == appuser ]] || { echo "FATAL: image must run as appuser" >&2; exit 1; }
    [[ -n "$healthcheck" && "$healthcheck" != null && "$healthcheck" != '[]' ]] || { echo "FATAL: image has no healthcheck" >&2; exit 1; }
    echo "image metadata check passed: {{ image_name }}:{{ TAG }}"

# Pull through the authenticated Nexus group on port 7110 and retag locally.
[group('image')]
image-pull TAG=revision:
    #!/usr/bin/env bash
    set -Eeuo pipefail
    just _validate-tag "{{ TAG }}"
    [[ -f "{{ env_file }}" ]] || { echo "FATAL: {{ env_file }} missing" >&2; exit 1; }
    set -a; source "{{ env_file }}"; set +a
    : "${NEXUS_PULL_USERNAME:?set NEXUS_PULL_USERNAME in {{ env_file }}}"
    : "${NEXUS_PULL_PASSWORD:?set NEXUS_PULL_PASSWORD in {{ env_file }}}"
    registry="${NEXUS_PULL_REGISTRY:-{{ pull_registry }}}"
    docker_config="$(mktemp -d)"
    trap 'rm -rf "$docker_config"' EXIT
    export DOCKER_CONFIG="$docker_config"
    printf '%s' "$NEXUS_PULL_PASSWORD" | docker login "$registry" --username "$NEXUS_PULL_USERNAME" --password-stdin >/dev/null
    docker pull "$registry/{{ image_name }}:{{ TAG }}"
    docker tag "$registry/{{ image_name }}:{{ TAG }}" "{{ image_name }}:{{ TAG }}"
    docker logout "$registry" >/dev/null || true

# Push an existing local image to the authenticated hosted registry on port 7111.
[group('image')]
image-push TAG=revision:
    #!/usr/bin/env bash
    set -Eeuo pipefail
    just _publish-tag-guard "{{ TAG }}"
    just image-smoke "{{ TAG }}"
    [[ -f "{{ env_file }}" ]] || { echo "FATAL: {{ env_file }} missing" >&2; exit 1; }
    set -a; source "{{ env_file }}"; set +a
    : "${NEXUS_PUSH_USERNAME:?set NEXUS_PUSH_USERNAME in {{ env_file }}}"
    : "${NEXUS_PUSH_PASSWORD:?set NEXUS_PUSH_PASSWORD in {{ env_file }}}"
    registry="${NEXUS_PUSH_REGISTRY:-{{ push_registry }}}"
    docker_config="$(mktemp -d)"
    trap 'rm -rf "$docker_config"' EXIT
    export DOCKER_CONFIG="$docker_config"
    printf '%s' "$NEXUS_PUSH_PASSWORD" | docker login "$registry" --username "$NEXUS_PUSH_USERNAME" --password-stdin >/dev/null
    docker tag "{{ image_name }}:{{ TAG }}" "$registry/{{ image_name }}:{{ TAG }}"
    docker push "$registry/{{ image_name }}:{{ TAG }}"
    docker logout "$registry" >/dev/null || true
    echo "published $registry/{{ image_name }}:{{ TAG }}"

# Publish Maven artifacts/schema first, then build and push the matching image.
[group('publish')]
image-publish VERSION=revision:
    just maven-publish "{{ VERSION }}"
    just image-build "{{ VERSION }}"
    just image-push "{{ VERSION }}"

# Regenerate the C4 model.
[group('docs')]
c4:
    {{ mvn }} -Pc4-docs -pl documents/c4/java -am -DskipTests process-classes

# Serve the generated C4 workspace at http://localhost:8090.
[group('docs')]
c4-view:
    cd documents/c4 && ./run-structurizr.sh

# Clean Maven outputs.
[group('build')]
clean:
    {{ mvn }} clean

[private]
_validate-tag TAG:
    #!/usr/bin/env bash
    set -Eeuo pipefail
    [[ "{{ TAG }}" =~ ^[A-Za-z0-9_][A-Za-z0-9_.-]{0,127}$ ]] || { echo "FATAL: invalid OCI tag: {{ TAG }}" >&2; exit 1; }

[private]
_publish-guard VERSION:
    #!/usr/bin/env bash
    set -Eeuo pipefail
    just _publish-tag-guard "{{ VERSION }}"
    if [[ -n "$(git status --porcelain)" && "${ALLOW_DIRTY:-0}" != 1 ]]; then
        echo "FATAL: publication requires a clean worktree; set ALLOW_DIRTY=1 only for an intentional exception" >&2
        exit 1
    fi

[private]
_publish-tag-guard VERSION:
    #!/usr/bin/env bash
    set -Eeuo pipefail
    just _validate-tag "{{ VERSION }}"
    current="{{ revision }}"
    if [[ "{{ VERSION }}" != "$current" && "${ALLOW_VERSION_OVERRIDE:-0}" != 1 ]]; then
        echo "FATAL: version {{ VERSION }} differs from checkout revision $current; checkout that version or set ALLOW_VERSION_OVERRIDE=1" >&2
        exit 1
    fi
    if [[ "{{ VERSION }}" == latest || "{{ VERSION }}" == local ]] && [[ "${ALLOW_MUTABLE_TAG:-0}" != 1 ]]; then
        echo "FATAL: mutable publication tag {{ VERSION }} is forbidden; set ALLOW_MUTABLE_TAG=1 only for an intentional exception" >&2
        exit 1
    fi

# Liquibase operations use DB_* values from container/.env.
[private]
_lb goal:
    #!/usr/bin/env bash
    set -Eeuo pipefail
    if [[ -f "{{ env_file }}" ]]; then set -a; source "{{ env_file }}"; set +a; fi
    : "${DB_URL:?DB_URL unset; populate {{ env_file }} or export DB_URL}"
    : "${DB_USERNAME:?DB_USERNAME unset; populate {{ env_file }} or export DB_USERNAME}"
    : "${DB_PASSWORD:?DB_PASSWORD unset; populate {{ env_file }} or export DB_PASSWORD}"
    {{ lb_cmd }} {{ goal }}

# List unapplied Liquibase changesets.
[group('db')]
db-status:
    just _lb liquibase:status

# Validate the Liquibase changelog without DB writes.
[group('db')]
db-validate:
    just _lb liquibase:validate

# Apply pending Liquibase changesets.
[group('db')]
db-update:
    just _lb liquibase:update

# Write pending SQL for review without applying it.
[group('db')]
db-sql:
    just _lb "liquibase:updateSQL -Dliquibase.migrationSqlOutputFile={{ db_sql_out }}"
    @echo "wrote dry-run SQL to {{ db_sql_out }}"

# Tag the current DB state.
[group('db')]
db-tag TAG:
    just _lb "liquibase:tag -Dliquibase.tag={{ TAG }}"

# Roll back to a Liquibase tag.
[group('db')]
db-rollback TAG:
    just _lb "liquibase:rollback -Dliquibase.rollbackTag={{ TAG }}"

# Roll back the last N Liquibase changesets.
[group('db')]
db-rollback-count N:
    just _lb "liquibase:rollback -Dliquibase.rollbackCount={{ N }}"

# Show Liquibase deployment history.
[group('db')]
db-history:
    just _lb liquibase:history

# Release a stuck Liquibase changelog lock.
[group('db')]
db-release-locks:
    just _lb liquibase:releaseLocks
