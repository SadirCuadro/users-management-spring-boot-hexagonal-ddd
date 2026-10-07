# Stage 1: Build
FROM maven:3.9-eclipse-temurin-17-alpine AS build

WORKDIR /build

# Cache de dependencias
COPY pom.xml ./
RUN mvn dependency:go-offline -B || true

# Compilación y empaquetado del artefacto
COPY src ./src
RUN mvn clean package -DskipTests -B --no-transfer-progress

# Stage 2: Runtime ligero
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Crear usuario y grupo de sistema sin privilegios para mayor seguridad
RUN addgroup -S spring && adduser -S spring -G spring

# Copiar el ejecutable empaquetado desde la fase de build
COPY --from=build --chown=spring:spring /build/target/*.jar app.jar

USER spring:spring

# Puerto por defecto y configuración para entornos con puerto dinámico (Render, Heroku, etc.)
ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT:-8080} -jar /app/app.jar"]
