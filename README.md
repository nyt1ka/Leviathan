# Leviathan

Leviathan is an internal company messenger project for Android with a Node.js backend.

## Current backend stage

This first deployable version includes:

- Express HTTP API
- `/health` health-check for Railway
- `/api/v1/status`
- WebSocket endpoint at `/ws`
- PostgreSQL connection helper
- Redis connection helper
- Dockerfile
- Railway configuration

This is intentionally a deployment baseline. Authentication, user/device management,
message persistence, file uploads, and end-to-end encryption will be added next.

## Railway

The app listens on `process.env.PORT`, which Railway injects automatically.

Required variables for the API service:

- `DATABASE_URL`
- `REDIS_URL`
- `NODE_ENV=production`

If PostgreSQL and Redis are attached inside the same Railway project, use their Railway
reference variables rather than manually copying passwords.

## GitHub browser upload

1. Open `nyt1ka/Leviathan` on GitHub.
2. Open the ZIP locally and extract it.
3. In the repository, choose **Add file → Upload files**.
4. Upload the CONTENTS of the extracted `Leviathan` folder, not the folder itself.
5. Commit directly to `main`.
6. Return to ChatGPT and say `Загрузил`.

## Security roadmap

The next implementation stage should add:

- account provisioning for company employees
- password hashing with Argon2id
- short-lived access tokens + rotating refresh tokens
- device registration
- end-to-end encrypted message envelopes
- attachment encryption before upload
- per-device key management
- rate limiting and audit logging

Do not use this baseline for sensitive production messaging until those security layers
are implemented and reviewed.
