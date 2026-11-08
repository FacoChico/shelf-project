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

[Архитектура](docs/ARCHITECTURE.md)

## Учебный стенд Grafana в Minikube

Профиль `grafana` открывает только Actuator health и Prometheus на отдельном
порту `8081`. HTTP-приложение остаётся на `8080`. Публичная главная страница
даёт запрос для проверки логов и трасс без входа. В этом стенде используется
H2 в памяти, а закрытые страницы по-прежнему требуют входа через Keycloak.
Временный OIDC-секрет нужен для запуска конфигурации клиента.

На Ubuntu с Docker и Minikube выполните из корня проекта. Нужен запущенный
профиль Minikube `grafana-hw2` и namespace `monitoring`.

```bash
kubectl config use-context grafana-hw2
minikube -p grafana-hw2 image build -t shelf-project:local .

openssl rand -hex 32 | tr -d '\n' |
  kubectl -n monitoring create secret generic shelf-lab \
    --from-file=oidc-client-secret=/dev/stdin

kubectl apply -f infra/minikube/shelf.yaml
kubectl -n monitoring rollout status deployment/shelf-project
kubectl -n monitoring port-forward svc/shelf-project 8080:8080 8081:8081
```

Создайте секрет один раз. Dockerfile сам собирает JAR с Gradle и JDK 21.
После повторной сборки того же тега запустите
`kubectl -n monitoring rollout restart deployment/shelf-project`.
Трассы отправляются на `http://jaeger-otlp:4318/v1/traces`. Если сервис Jaeger
имеет другое имя, задайте его адрес через `JAEGER_OTLP_ENDPOINT`.

Проверка из второго терминала Ubuntu:

```bash
curl -fsS http://127.0.0.1:8080/ -o /dev/null
curl -fsS http://127.0.0.1:8081/actuator/health
curl -fsS http://127.0.0.1:8081/actuator/prometheus | grep '^jvm_memory_used_bytes'
kubectl -n monitoring logs deployment/shelf-project | grep 'Shelf home page requested'
```

В существующем `prometheus-values.yaml` замените задание сбора метрик демо-приложения:

```yaml
extraScrapeConfigs: |
  - job_name: shelf-project
    scrape_interval: 15s
    metrics_path: /actuator/prometheus
    static_configs:
      - targets: ['shelf-project.monitoring.svc.cluster.local:8081']
```

- В Prometheus проверьте `up{job="shelf-project"}`. В запросах панелей и алерта
  используйте `job="shelf-project"`. В JVM (Micrometer), ID 4701, выберите
  `Application = shelf-project`.
- Во Fluentd замените путь сбора логов на
  `/var/log/pods/monitoring_shelf-project-*/*/*.log`. После применения нового
  ConfigMap перезапустите Fluentd, если конфиг подключён через `subPath`.
  В Grafana ищите `message:"Shelf home page requested"`.
- В Jaeger выберите сервис `shelf-project` и найдите трассу запроса к `/`.
  Идентификатор трассы также виден в строке журнала запроса.
