# --- build ---
FROM maven:3-eclipse-temurin-21 AS build
WORKDIR /build

# Copiar primero el pom aprovecha la caché de capas: las dependencias solo se
# vuelven a descargar si cambia el pom, no en cada cambio de código.
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -q clean package -DskipTests

# --- runtime ---
FROM eclipse-temurin:21-jre
WORKDIR /app

# Ejecutar como root dentro del contenedor es un hallazgo automático en cualquier
# revisión de seguridad.
RUN useradd --system --create-home --uid 10001 spring
USER spring

COPY --from=build /build/target/*.jar app.jar

EXPOSE 8082
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
