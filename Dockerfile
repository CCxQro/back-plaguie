###
# Runtime-only Dockerfile for Quarkus in JVM mode.
# Requires `./mvnw package` to generate target/quarkus-app.
###

FROM eclipse-temurin:17-jre

ARG APP_VERSION=dev
ENV APP_VERSION=${APP_VERSION}

WORKDIR /deployments

COPY target/quarkus-app/lib/      lib/
COPY target/quarkus-app/*.jar     ./
COPY target/quarkus-app/app/      app/
COPY target/quarkus-app/quarkus/  quarkus/

EXPOSE 8080

ENTRYPOINT ["java", "-Dquarkus.http.host=0.0.0.0", "-Djava.util.logging.manager=org.jboss.logmanager.LogManager", "-jar", "quarkus-run.jar"]