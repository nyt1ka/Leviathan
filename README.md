# Leviathan Backend v0.3.0

## Пользователи
- Самостоятельная регистрация: `POST /auth/register`
- Новые аккаунты получают `PENDING`
- OWNER/ADMIN подтверждает: `POST /admin/users/:id/approve`
- OWNER/ADMIN может отклонять, блокировать и разблокировать
- OWNER может назначать ADMIN/USER

## Первый владелец
1. В Railway задайте `OWNER_BOOTSTRAP_TOKEN` — длинную случайную строку.
2. Один раз вызовите `POST /auth/bootstrap-owner` с заголовком `X-Bootstrap-Token`.
3. После успешного создания OWNER удалите `OWNER_BOOTSTRAP_TOKEN` из Railway.

## Сессии и устройства
- Access JWT: 15 минут
- Refresh token: 30 дней, ротация при каждом refresh
- Refresh-токены в БД хранятся только в SHA-256 виде
- Пользователь может видеть и отзывать свои устройства
- Блокировка пользователя отзывает активные сессии

## Важно
Это ещё не финальная production-версия. E2EE сообщений и файлов будет реализована следующим этапом.
