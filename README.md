# TOTP Sender

[![CI/CD Pipeline](https://github.com/vterdunov/totp-sender/actions/workflows/ci.yml/badge.svg)](https://github.com/vterdunov/totp-sender/actions/workflows/ci.yml)

Spring Boot приложение для генерации и валидации одноразовых паролей (OTP) с поддержкой различных каналов доставки.

## Возможности

- **Аутентификация и авторизация** с использованием JWT токенов
- **Ролевая модель** (администратор и пользователь)
- **Генерация и валидация OTP кодов** с настраиваемыми параметрами
- **Различные каналы доставки**: Email, SMS, Telegram, файл
- **Административная панель** для управления пользователями и конфигурацией
- **Автоматическая очистка** просроченных OTP кодов
- **Глобальная обработка ошибок** с детализированными ответами

## Требования

- Java 17+
- PostgreSQL 17
- Maven 3.8+
- SMPPsim (для тестирования SMS)
- Telegram Bot Token (для Telegram уведомлений)

## Быстрый старт

### Запуск с Docker Compose

```bash
# Клонировать репозиторий
git clone https://github.com/vterdunov/totp-sender.git
cd totp-sender

# Запустить PostgreSQL
docker compose up -d

# Собрать и запустить приложение
mvn spring-boot:run
```

### Использование Docker образа

```bash
# Запуск из GitHub Container Registry
docker run -p 8080:8080 ghcr.io/vterdunov/totp-sender:latest
```

### Локальная разработка

```bash
# Установить зависимости
mvn clean install

# Запустить базу данных
docker-compose up -d postgres

# Запустить приложение
mvn spring-boot:run
```

## API Документация

### Аутентификация

#### Регистрация пользователя
```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "user123",
  "password": "password",
  "role": "USER"
}
```

#### Вход в систему
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "user123",
  "password": "password"
}
```

### Пользовательские операции (требуется JWT токен)

#### Генерация OTP кода
```http
POST /api/user/otp/generate
Authorization: Bearer <jwt_token>
Content-Type: application/json

{
  "operationId": "payment_123",
  "channel": "EMAIL",
  "destination": "user@example.com"
}
```

#### Валидация OTP кода
```http
POST /api/user/otp/validate
Authorization: Bearer <jwt_token>
Content-Type: application/json

{
  "code": "123456",
  "operationId": "payment_123"
}
```

### Администраторские операции

#### Получение списка пользователей
```http
GET /api/admin/users
Authorization: Bearer <admin_jwt_token>
```

#### Получение конфигурации OTP
```http
GET /api/admin/otp-config
Authorization: Bearer <admin_jwt_token>
```

#### Обновление конфигурации OTP
```http
PUT /api/admin/otp-config
Authorization: Bearer <admin_jwt_token>
Content-Type: application/json

{
  "codeLength": 6,
  "ttlSeconds": 300
}
```

#### Удаление пользователя
```http
DELETE /api/admin/users/{user_id}
Authorization: Bearer <admin_jwt_token>
```

## Конфигурация каналов

### Email
Создайте файл `src/main/resources/email.properties`:
```properties
email.username=your_email@example.com
email.password=your_email_password
email.from=your_email@example.com
mail.smtp.host=smtp.example.com
mail.smtp.port=587
mail.smtp.auth=true
mail.smtp.starttls.enable=true
```

### SMS (SMPP)
Создайте файл `src/main/resources/sms.properties`:
```properties
smpp.host=localhost
smpp.port=2775
smpp.system_id=smppclient1
smpp.password=password
smpp.system_type=OTP
smpp.source_addr=OTPService
```

### Telegram
Создайте файл `src/main/resources/telegram.properties`:
```properties
telegram.bot.token=YOUR_BOT_TOKEN
telegram.chat.id=YOUR_CHAT_ID
telegram.api.url=https://api.telegram.org/bot
```

## База данных

Проект использует PostgreSQL с тремя основными таблицами:

### users
- Хранит пользователей с зашифрованными паролями
- Поддерживает роли: USER, ADMIN
- Ограничение: только один администратор

### otp_config
- Конфигурация для OTP кодов
- Длина кода (4-8 символов)
- Время жизни в секундах

### otp_codes
- Активные OTP коды
- Статусы: ACTIVE, EXPIRED, USED
- Привязка к пользователю и операции

## Фоновые задачи

Система автоматически очищает просроченные OTP коды каждые 5 минут через ScheduledExecutorService.

## Тестирование

```bash
# Запуск всех тестов
mvn test

# Запуск конкретного теста
mvn test -Dtest=OtpServiceTest

# Генерация отчета о покрытии
mvn jacoco:report
```

## Безопасность

- Пароли хешируются с использованием BCrypt
- JWT токены для аутентификации и авторизации
- Валидация входных данных с Bean Validation
- Защита от несанкционированного доступа
- Логирование всех операций

## Docker

### Сборка образа
```bash
docker build -t totp-sender .
```

### Запуск контейнера
```bash
docker run -p 8080:8080 -e POSTGRES_URL=jdbc:postgresql://host:5432/totp totp-sender
```

## CI/CD

### GitHub Actions
- **Автоматическое тестирование** на каждый push и pull request
- **Сборка Docker образа** и публикация в GitHub Container Registry
- **Поддержка PostgreSQL** в тестовой среде
- **Кэширование Maven зависимостей** для ускорения сборки

### Docker образы
Образы автоматически публикуются в GitHub Container Registry:
```bash
docker pull ghcr.io/vterdunov/totp-sender:latest
docker pull ghcr.io/vterdunov/totp-sender:main
```

## Логирование

Логи записываются в:
- Консоль (для разработки)
- Файл `logs/app.log` (для продакшена)

Уровни логирования:
- ERROR: Ошибки
- WARN: Предупреждения
- INFO: Информационные сообщения
- DEBUG: Отладочная информация

## Мониторинг

Используйте Spring Boot Actuator endpoints:
- `/actuator/health` - Статус здоровья приложения
- `/actuator/metrics` - Метрики производительности
- `/actuator/info` - Информация о приложении

## Разработка

### Требования
- Java 17+
- Maven 3.6+
- PostgreSQL 12+
- Docker (опционально)

### Настройка IDE
1. Импортировать как Maven проект
2. Настроить Java 17
3. Установить плагины для Spring Boot
4. Настроить code style для проекта

### Структура проекта
```
src/
├── main/java/com/example/totpsender/
│   ├── config/          # Конфигурация Spring
│   ├── controller/      # REST контроллеры
│   ├── dto/            # Data Transfer Objects
│   ├── exception/      # Пользовательские исключения
│   ├── model/          # Доменные модели
│   ├── repository/     # Доступ к данным
│   ├── service/        # Бизнес-логика
│   └── util/           # Утилиты
└── test/               # Тесты
```

## Лицензия

MIT License

## Контакты

Для вопросов и предложений создавайте Issues в GitHub репозитории.
