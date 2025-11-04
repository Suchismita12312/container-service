FROM gradle:7.6.4-jdk17 AS build
WORKDIR /workspace

COPY build.gradle settings.gradle ./
RUN gradle dependencies --no-daemon || true

COPY src ./src
RUN gradle clean bootJar --no-daemon

FROM eclipse-temurin:17-jre
ENV APP_HOME=/app \
    JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"
WORKDIR ${APP_HOME}

COPY --from=build /workspace/build/libs/*-SNAPSHOT.jar app.jar

RUN useradd -r -s /sbin/nologin spring && chown -R spring:spring ${APP_HOME}
USER spring

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]