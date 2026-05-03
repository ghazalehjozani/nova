ARG NEXUS_PROXY_REGISTRY=nexus3.dotin.ir:8100
FROM ${NEXUS_PROXY_REGISTRY}/eclipse-temurin:25-jre-alpine

RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

WORKDIR /app

RUN apk add --no-cache ca-certificates gcompat java-lz4 libc6-compat libstdc++ openssl

COPY ca-certificates/ /usr/local/share/ca-certificates/

RUN update-ca-certificates && \
    if ls /usr/local/share/ca-certificates/*.crt 1> /dev/null 2>&1; then \
        for cert in /usr/local/share/ca-certificates/*.crt; do \
            alias=$(basename "$cert" .crt); \
            echo "Importing $alias into Java truststore"; \
            keytool -importcert -trustcacerts -noprompt \
                -alias "$alias" \
                -file "$cert" \
                -cacerts \
                -storepass changeit \
        done; \
    else \
        echo "No certificates to import"; \
    fi

COPY target/*.jar app.jar
RUN chmod 444 app.jar

ENV SPRING_PROFILES_ACTIVE=dev,dev-elk,kafka-fcb,elk \
    JAVA_OPTS=""

USER appuser

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD wget -qO- http://127.0.0.1:8080/actuator/health || exit 1

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
