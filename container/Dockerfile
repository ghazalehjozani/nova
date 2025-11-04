FROM eclipse-temurin:25-jre-alpine

WORKDIR /app

RUN apk add --no-cache ca-certificates openssl

COPY ca-certificates/*.crt /usr/local/share/ca-certificates/

RUN update-ca-certificates && \
    for cert in /usr/local/share/ca-certificates/*.crt; do \
        alias=$(basename "$cert" .crt); \
        echo "Importing $alias into Java truststore"; \
        keytool -importcert -trustcacerts -noprompt \
            -alias "$alias" \
            -file "$cert" \
            -keystore /opt/java/openjdk/lib/security/cacerts \
            -storepass changeit; \
    done

COPY target/*.jar app.jar

ENV SPRING_PROFILES_ACTIVE=k8s

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
