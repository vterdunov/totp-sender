FROM openjdk:17-jdk-slim

# Создаем пользователя для запуска приложения
RUN groupadd -r appuser && useradd -r -g appuser appuser

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем pom.xml и скачиваем зависимости
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline -B

# Копируем исходный код
COPY src ./src

# Собираем приложение
RUN ./mvnw clean package -DskipTests

# Создаем финальный образ
FROM openjdk:17-jre-slim

# Создаем пользователя для запуска приложения
RUN groupadd -r appuser && useradd -r -g appuser appuser

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем jar файл из предыдущего этапа
COPY --from=0 /app/target/totp-sender-*.jar app.jar

# Создаем необходимые директории
RUN mkdir -p logs migrations && chown -R appuser:appuser /app

# Переключаемся на пользователя appuser
USER appuser

# Открываем порт
EXPOSE 8080

# Устанавливаем переменные окружения
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Команда запуска
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
