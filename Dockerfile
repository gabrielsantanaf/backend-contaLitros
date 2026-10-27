# syntax=docker/dockerfile:1

# ---------- Etapa 1: build ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Baixa as dependencias primeiro para aproveitar o cache de camadas
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 mvn -B -q dependency:go-offline

COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -B -q package -DskipTests \
    && cp target/*.jar app.jar

# ---------- Etapa 2: runtime ----------
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
USER spring

COPY --from=build /app/app.jar app.jar

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75"
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
