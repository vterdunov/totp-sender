# TOTP Sender

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

### 1. Настройка базы данных

Создайте базу данных PostgreSQL:
```sql
CREATE DATABASE totp_sender;
CREATE USER postgres WITH PASSWORD 'postgres';
GRANT ALL PRIVILEGES ON DATABASE totp_sender TO postgres;
```

Выполните миграции:
```bash
psql -h localhost -U postgres -d totp_sender -f migrations/20250607120000_init.sql
```

### 2. Конфигурация

Отредактируйте файлы конфигурации в `src/main/resources/`:

**database.properties:**
```properties
db.url=jdbc:postgresql://localhost:5432/totp_sender
db.username=postgres
db.password=postgres
```

**email.properties:**
```properties
email.username=your_email@example.com
email.password=your_password
email.from=your_email@example.com
mail.smtp.host=smtp.gmail.com
mail.smtp.port=587
mail.smtp.auth=true
mail.smtp.starttls.enable=true
```

**telegram.properties:**
```properties
telegram.bot.token=YOUR_BOT_TOKEN
telegram.chat.id=YOUR_CHAT_ID
telegram.api.url=https://api.telegram.org/bot
```

**sms.properties:**
```properties
smpp.host=localhost
smpp.port=2775
smpp.system_id=smppclient1
smpp.password=password
smpp.system_type=OTP
smpp.source_addr=OTPService
```

### 3. Запуск приложения

```bash
mvn clean compile
mvn spring-boot:run
```

Приложение будет доступно по адресу: `http://localhost:8080`

## API Документация

### Аутентификация

#### Регистрация пользователя
```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "user1",
  "password": "password123",
  "role": "USER"
}
```

**Ответ:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "username": "user1",
  "role": "USER"
}
```

#### Авторизация
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "user1",
  "password": "password123"
}
```

### Пользовательские операции

Все запросы требуют JWT токен в заголовке:
```http
Authorization: Bearer YOUR_JWT_TOKEN
```

#### Генерация OTP кода
```http
POST /api/otp/generate
Content-Type: application/json

{
  "destination": "user@example.com",
  "channel": "EMAIL",
  "operationId": "payment-123"
}
```

**Поддерживаемые каналы:** `EMAIL`, `SMS`, `TELEGRAM`, `FILE`

#### Валидация OTP кода
```http
POST /api/otp/validate
Content-Type: application/json

{
  "code": "123456",
  "operationId": "payment-123"
}
```

### Административные операции

Доступны только пользователям с ролью `ADMIN`.

#### Получение списка пользователей
```http
GET /api/admin/users
Authorization: Bearer ADMIN_JWT_TOKEN
```

#### Удаление пользователя
```http
DELETE /api/admin/users/{userId}
Authorization: Bearer ADMIN_JWT_TOKEN
```

#### Получение конфигурации OTP
```http
GET /api/admin/otp-config
Authorization: Bearer ADMIN_JWT_TOKEN
```

#### Обновление конфигурации OTP
```http
PUT /api/admin/otp-config
Content-Type: application/json
Authorization: Bearer ADMIN_JWT_TOKEN

{
  "codeLength": 6,
  "ttlSeconds": 300
}
```

## Структура базы данных

### Таблица users
- `id` - UUID (PK)
- `username` - VARCHAR (UNIQUE)
- `password_hash` - VARCHAR
- `role` - VARCHAR (USER/ADMIN)
- `created_at`, `updated_at` - TIMESTAMP

### Таблица otp_config
- `id` - UUID (PK)
- `code_length` - INTEGER (4-10)
- `ttl_seconds` - INTEGER (60-3600)
- `created_at`, `updated_at` - TIMESTAMP

### Таблица otp_codes
- `id` - UUID (PK)
- `user_id` - UUID (FK)
- `code` - VARCHAR
- `operation_id` - VARCHAR
- `status` - VARCHAR (ACTIVE/EXPIRED/USED)
- `created_at`, `expires_at`, `used_at` - TIMESTAMP

## Каналы уведомлений

### Email
Использует JavaMail API для отправки через SMTP.

### SMS
Использует протокол SMPP с эмулятором SMPPsim.

Установка SMPPsim:
1. Скачайте SMPPsim
2. Запустите: `./startsmppsim.bat`
3. Используйте параметры по умолчанию

### Telegram
Использует Telegram Bot API.

Настройка бота:
1. Создайте бота через @BotFather
2. Получите токен
3. Найдите chat_id через `/getUpdates`

### Файл
Сохраняет OTP коды в файл `otp_codes.txt` в корне проекта.

## Безопасность

- **JWT токены** с ограниченным временем жизни
- **BCrypt хеширование** паролей
- **Ролевая авторизация** (USER/ADMIN)
- **CORS конфигурация** для web приложений
- **Валидация входных данных** через Bean Validation

## Мониторинг

- **Структурированное логирование** через SLF4J + Logback
- **Автоматическая очистка** просроченных OTP (каждые 5 минут)
- **Глобальная обработка ошибок** с детальными кодами

## Примеры использования

### Создание администратора
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123","role":"ADMIN"}'
```

### Генерация OTP через Email
```bash
curl -X POST http://localhost:8080/api/otp/generate \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"destination":"user@example.com","channel":"EMAIL","operationId":"test-op"}'
```

### Валидация OTP
```bash
curl -X POST http://localhost:8080/api/otp/validate \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"code":"123456","operationId":"test-op"}'
```

## Разработка

### Сборка
```bash
mvn clean compile
mvn package
```

### Тестирование
```bash
mvn test
```

### Запуск в режиме разработки
```bash
mvn spring-boot:run
```
