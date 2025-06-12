FROM maven:3.9.9-eclipse-temurin-17 AS builder

RUN groupadd -r appuser && useradd -r -g appuser appuser

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src

RUN mvn clean package -DskipTests

# ------------------------------------------------------------
FROM eclipse-temurin:17-jre

RUN groupadd -r appuser && useradd -r -g appuser appuser

WORKDIR /app

COPY --from=builder /app/target/totp-sender-*.jar app.jar

RUN mkdir -p logs migrations && chown -R appuser:appuser /app

USER appuser

EXPOSE 8080


ENTRYPOINT ["sh", "-c", "java -jar app.jar"]
