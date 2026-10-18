#!/usr/bin/env python3
"""Local secret and Keycloak realm initialization. Existing configuration is preserved."""
import json
import os
import secrets
from pathlib import Path

root = Path(__file__).resolve().parents[1]
env_path = root / ".env"
local = root / ".local"
realm_path = local / "keycloak" / "shelf-realm.json"
if env_path.exists():
    if not realm_path.exists():
        raise SystemExit("Локальная конфигурация неполна: отсутствует .local/keycloak/shelf-realm.json. Для повторной инициализации требуется восстановление файла из резервной копии либо переименование .env. Данные БД сохраняются.")
    print("Локальные настройки уже существуют. Секреты и пользователи сохранены.")
    raise SystemExit(0)
if local.exists() and any(local.iterdir()):
    raise SystemExit("Инициализация остановлена: папка .local уже содержит файлы.")
local.mkdir(mode=0o700, exist_ok=True)
realm_path.parent.mkdir(mode=0o700, exist_ok=True)
client_secret = secrets.token_urlsafe(40)
db_password = secrets.token_urlsafe(30)
admin_password = secrets.token_urlsafe(24)
reader_password = secrets.token_urlsafe(18)
other_password = secrets.token_urlsafe(18)
realm = {
    "realm": "shelf", "displayName": "Полка", "enabled": True,
    "sslRequired": "external", "registrationAllowed": False,
    "loginWithEmailAllowed": True, "duplicateEmailsAllowed": False,
    "resetPasswordAllowed": False, "rememberMe": False,
    "bruteForceProtected": True, "failureFactor": 5,
    "ssoSessionIdleTimeout": 1800, "ssoSessionMaxLifespan": 28800,
    "accessTokenLifespan": 300, "passwordPolicy": "length(12)",
    "internationalizationEnabled": True, "supportedLocales": ["ru", "en"], "defaultLocale": "ru",
    "clients": [{
        "clientId": "shelf-web", "name": "Полка", "enabled": True,
        "protocol": "openid-connect", "publicClient": False, "secret": client_secret,
        "standardFlowEnabled": True, "implicitFlowEnabled": False,
        "directAccessGrantsEnabled": False, "serviceAccountsEnabled": False,
        "redirectUris": ["http://localhost:8080/login/oauth2/code/keycloak"],
        "webOrigins": ["http://localhost:8080"],
        "attributes": {"pkce.code.challenge.method": "S256", "post.logout.redirect.uris": "http://localhost:8080/"},
        "defaultClientScopes": ["profile", "email", "roles", "web-origins"],
    }],
    "users": [
        {"username": "reader", "enabled": True, "emailVerified": True,
         "firstName": "Александр", "lastName": "Читатель", "email": "reader@example.test",
         "credentials": [{"type": "password", "value": reader_password, "temporary": False}], "requiredActions": []},
        {"username": "second-reader", "enabled": True, "emailVerified": True,
         "firstName": "Мария", "lastName": "Читатель", "email": "second-reader@example.test",
         "credentials": [{"type": "password", "value": other_password, "temporary": False}], "requiredActions": []},
    ],
}
# Exclusive creation prevents silently replacing existing secrets.
with realm_path.open("x") as output:
    json.dump(realm, output, ensure_ascii=False, indent=2)
# The parent directory is 0700; the mounted file must be readable by Keycloak UID 1000.
os.chmod(realm_path, 0o644)
values = {
    "OIDC_ISSUER_URI": "http://localhost:8180/realms/shelf",
    "OIDC_CLIENT_SECRET": client_secret, "APP_BASE_URL": "http://localhost:8080",
    "APP_BIND_ADDRESS": "127.0.0.1", "APP_PORT": "8080", "COOKIE_SECURE": "false",
    "DB_URL": "jdbc:postgresql://localhost:5433/shelf", "DB_USERNAME": "shelf",
    "DB_PASSWORD": db_password, "POSTGRES_PASSWORD": db_password,
    "KEYCLOAK_ADMIN_PASSWORD": admin_password,
}
with env_path.open("x") as output:
    output.write("\n".join(f"{key}={value}" for key, value in values.items()) + "\n")
os.chmod(env_path, 0o600)
credentials = local / "credentials.txt"
with credentials.open("x") as output:
    output.write(f"Локальный учебный стенд Полка\n\nПриложение: http://localhost:8080\nЛогин: reader\nПароль: {reader_password}\n\nВторой аккаунт: second-reader\nПароль: {other_password}\n\nKeycloak: http://localhost:8180/admin/\nЛогин администратора: shelf-admin\nПароль: {admin_password}\n")
os.chmod(credentials, 0o600)
print("Созданы .env, конфигурация Keycloak и .local/credentials.txt.")
print("Запуск инфраструктуры: docker compose up -d")
