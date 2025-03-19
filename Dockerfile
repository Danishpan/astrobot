# Build stage
FROM gradle:7.6.4-jdk17 AS build
WORKDIR /app
COPY . .
RUN gradle clean build -x test

# Runtime stage
FROM openjdk:17.0.1-jdk-slim
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
