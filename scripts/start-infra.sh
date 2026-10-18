#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
python3 scripts/init-local.py
docker compose up -d
printf '\nПроверка готовности Keycloak (до двух минут)…\n'
for attempt in {1..60}; do
  if curl -fsS --max-time 2 http://localhost:8180/realms/shelf/.well-known/openid-configuration >/dev/null 2>&1; then
    printf 'Сервер входа готов. Запусти ./gradlew bootRun\n'
    exit 0
  fi
  sleep 2
done
printf 'Keycloak ещё не готов. Проверь: docker compose logs keycloak\n' >&2
exit 1
