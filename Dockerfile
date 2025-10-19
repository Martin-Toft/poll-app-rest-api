
FROM gradle:8.10.2-jdk21-alpine AS builder

WORKDIR /home/gradle/app

COPY settings.gradle.kts build.gradle.kts gradlew ./
COPY gradle ./gradle
RUN gradle --no-daemon dependencies || true

COPY src ./src
RUN gradle --no-daemon clean bootJar

RUN cp build/libs/*.jar app.jar

FROM eclipse-temurin:21-jre-alpine

RUN addgroup -g 1000 app && adduser -D -u 1000 -G app app
USER app
WORKDIR /app

COPY --from=builder --chown=app:app /home/gradle/app/app.jar ./app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]