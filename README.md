# Полка

Личная коллекция книг, фильмов и игр с поиском, заметками и статусами.
Вход через Keycloak; записи доступны только владельцу.

Java 21 · Spring Boot 4 · Gradle (Groovy) · PostgreSQL · Thymeleaf.

## Запуск

Нужны JDK 21, Docker Compose и Python 3.

```bash
./scripts/start-infra.sh
./gradlew bootRun
```

Приложение: http://localhost:8080. В OAuth redirect URI зарегистрировано имя `localhost`.
Скрипт создаёт `.env` и `.local/credentials.txt` с паролями пользователей
`reader`, `second-reader` и администратора Keycloak.

Keycloak: http://localhost:8180/admin/ — realm `shelf`, клиент `shelf-web`.

## Проверка и сборка

```bash
./gradlew check bootJar
./gradlew spotlessApply
```

Тесты используют H2 и тестовую OIDC-личность.
Отчёт: `build/reports/tests/test/index.html`.
Jar: `build/libs/shelf-project-1.0.0.jar`.

[Архитектура](docs/ARCHITECTURE.md) · [Скриншоты для отчёта](docs/SCREENSHOTS.md)
