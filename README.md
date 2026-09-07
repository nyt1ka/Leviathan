# Leviathan v0.2

Добавлена самостоятельная регистрация пользователей.

Схема:
- пользователь регистрируется сам;
- аккаунт получает статус PENDING;
- администратор одобряет пользователя;
- после одобрения статус ACTIVE;
- только ACTIVE может войти.

Эндпоинты:
- POST /auth/register
- POST /auth/login
- GET /admin/pending
- POST /admin/users/:id/approve

Перед production обязательно задать JWT_SECRET в Railway.
