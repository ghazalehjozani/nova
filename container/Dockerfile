FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
COPY target/*.jar app.jar

ENV SPRING_PROFILES_ACTIVE=k8s,spring-boot-application

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]