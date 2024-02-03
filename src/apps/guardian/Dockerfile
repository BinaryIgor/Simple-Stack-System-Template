FROM eclipse-temurin:21

# TODO: db backups using pg_dump?

ENV TZ=UTC

COPY target/guardian-0.0.1-SNAPSHOT.jar /guardian.jar

ENTRYPOINT exec java "${JVM_OPTS}" -jar guardian.jar
