FROM openjdk:25-jdk
WORKDIR /app

COPY target/*.jar app.jar

COPY ~/truststore.jks /app/truststore.jks

ENV SPRING_PROFILES_ACTIVE=k8s
ENV JAVA_OPTS="-Djavax.net.ssl.trustStore=/app/truststore.jks -Djavax.net.ssl.trustStorePassword=changeit"

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]