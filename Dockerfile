# Stage 1: Build
FROM gradle:jdk21 AS build
WORKDIR /app
COPY build.gradle settings.gradle /app/
COPY src /app/src/
RUN gradle bootJar

# Stage 2: Run
FROM eclipse-temurin:21-jre
COPY --from=build /app/*.jar /app/role-command-service.jar
WORKDIR /app
CMD ["java", "-jar", "role-command-service.jar"]
