FROM eclipse-temurin:21

ENV TEMPLATES_DIR=/email-templates
ENV TZ=UTC

COPY target/main-0.0.1-SNAPSHOT.jar /main.jar

ENTRYPOINT exec java "${JVM_OPTS}" -jar main.jar
