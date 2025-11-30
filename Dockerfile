# ==========================================
# DOCKERFILE MULTI-STAGE PARA RENDER
# ==========================================

# ----- STAGE 1: Build -----
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app

# Copiar archivos de configuración de Maven primero (para cache de dependencias)
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# Descargar dependencias (se cachea si pom.xml no cambia)
RUN mvn dependency:go-offline -B

# Copiar código fuente
COPY src ./src

# Compilar y empaquetar (sin tests para acelerar)
RUN mvn clean package -DskipTests -B

# ----- STAGE 2: Runtime -----
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Crear usuario no-root por seguridad
RUN addgroup -S spring && adduser -S spring -G spring

# Crear directorio para uploads con permisos correctos
RUN mkdir -p /app/uploads && chown -R spring:spring /app

# Copiar JAR desde stage de build
COPY --from=builder /app/target/backend-0.0.1-SNAPSHOT.jar app.jar

# Cambiar a usuario no-root
USER spring:spring

# Exponer puerto (Render usa la variable PORT)
EXPOSE 8080

# Variables de entorno por defecto (se sobreescriben en Render)
ENV JAVA_OPTS="-Xmx512m -Xms256m"
ENV SERVER_PORT=8080

# Comando de inicio
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dserver.port=$PORT -jar app.jar"]
