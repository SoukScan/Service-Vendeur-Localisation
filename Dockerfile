# Multi-stage build pour optimiser la taille de l'image

# Stage 1: Build
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Exposer le port 8081
EXPOSE 8081

# Variables d'environnement (peuvent être overridées)
ENV SPRING_PROFILES_ACTIVE=prod
ENV SERVER_PORT=8081

# Démarrer l'application
ENTRYPOINT ["java", "-jar", "app.jar"]

