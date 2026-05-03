ARG NEXUS_PROXY_REGISTRY=nexus3.dotin.ir:8100
FROM ${NEXUS_PROXY_REGISTRY}/eclipse-temurin:25-jre-noble

ARG NEXUS_APT_REPO_URL=https://nexus3.dotin.ir/repository/Ubuntu
ARG APT_SUITES="noble noble-updates noble-security"

USER root

RUN rm -f /etc/apt/sources.list.d/*.list /etc/apt/sources.list.d/*.sources /etc/apt/sources.list \
 && printf '%s\n' \
      'Types: deb' \
      "URIs: ${NEXUS_APT_REPO_URL}" \
      "Suites: ${APT_SUITES}" \
      'Components: main restricted universe multiverse' \
      'Trusted: yes' \
    > /etc/apt/sources.list.d/ubuntu.sources \
 && echo 'Acquire::https::Verify-Peer "false";' > /etc/apt/apt.conf.d/99insecure-cert \
 && apt-get update \
 && apt-get install -y --no-install-recommends ca-certificates openssl curl \
 && rm -rf /var/lib/apt/lists/*

COPY ca-certificates/*.crt /usr/local/share/ca-certificates/

RUN update-ca-certificates && \
    if ls /usr/local/share/ca-certificates/*.crt 1> /dev/null 2>&1; then \
        for cert in /usr/local/share/ca-certificates/*.crt; do \
            alias=$(basename "$cert" .crt); \
            echo "Importing $alias into Java truststore"; \
            keytool -importcert -trustcacerts -noprompt \
                -alias "$alias" \
                -file "$cert" \
                -cacerts \
                -storepass changeit; \
        done; \
    fi \
 && rm -f /etc/apt/apt.conf.d/99insecure-cert

# 3. Create Application User (Noble uses standard useradd/groupadd)
RUN groupadd -g 1001 appgroup && \
    useradd -u 1001 -g appgroup -m -s /bin/bash appuser

WORKDIR /app

# 4. Copy Artifact
COPY target/*.jar app.jar
RUN chmod 444 app.jar && chown appuser:appgroup app.jar

USER appuser

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://127.0.0.1:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java \
    --add-opens=org.hibernate.validator/org.hibernate.validator.internal.engine=spring.core,spring.beans,spring.context,ALL-UNNAMED \
    --add-opens=org.hibernate.validator/org.hibernate.validator.internal.constraintvalidators.bv=spring.core,spring.beans,ALL-UNNAMED \
    --add-opens=org.hibernate.validator/org.hibernate.validator.internal.constraintvalidators.bv.number.bound=spring.core,spring.beans,spring.context,ALL-UNNAMED \
    --add-opens=org.hibernate.validator/org.hibernate.validator.internal.constraintvalidators.bv.number.sign=spring.core,spring.beans,spring.context,ALL-UNNAMED \
    --add-opens=org.hibernate.validator/org.hibernate.validator.internal.util=spring.core,spring.beans,ALL-UNNAMED \
    --add-opens=org.hibernate.validator/org.hibernate.validator.internal.metadata.core=spring.core,ALL-UNNAMED \
    --add-opens=org.hibernate.validator/org.hibernate.validator.internal.metadata.provider=spring.core,ALL-UNNAMED \
    --add-opens=org.hibernate.validator/org.hibernate.validator.internal.metadata.aggregated=spring.core,ALL-UNNAMED \
    $JAVA_OPTS \
    -jar app.jar"]
