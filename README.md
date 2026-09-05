# inkvite-back

[![CI](https://github.com/flolec31/inkvite-back/actions/workflows/ci.yml/badge.svg)](https://github.com/flolec31/inkvite-back/actions/workflows/ci.yml)
[![CD](https://github.com/flolec31/inkvite-back/actions/workflows/cd.yml/badge.svg)](https://github.com/flolec31/inkvite-back/actions/workflows/cd.yml)

Backend API for **Inkvite** — a tattoo appointment booking platform. Artists register and manage their profile; clients submit appointment requests through the artist's public booking page.

## Tech stack

- Kotlin + Spring Boot 4 + Java 24
- PostgreSQL (Spring Data JPA + Liquibase migrations)
- JWT authentication (HMAC-SHA256, stateless, refresh tokens in DB)
- S3-compatible object storage via AWS SDK v2 (MinIO locally)
- Transactional email via [Resend](https://resend.com)
- Gradle (Kotlin DSL) with a version catalog (`gradle/libs.versions.toml`)

## Running locally

**Prerequisites:** Docker (for Postgres + MinIO via Compose), JDK 24.

```bash
# Start Postgres + MinIO
docker compose up -d

# Run the app (picks up application-local.yaml for secrets)
./gradlew bootRun
```

Create `src/main/resources/application-local.yaml` with:

```yaml
resend:
  api-key: re_...

app:
  base-url: http://localhost:8080
  jwt:
    secret: <base64-encoded 256-bit secret>
  storage:
    endpoint: http://localhost:9000
    bucket: inkvite
    access-key: minioadmin
    secret-key: minioadmin
```

Alternatively, run with Testcontainers (no Docker Compose needed):

```bash
./gradlew bootTestRun
```

## Running tests

```bash
./gradlew test
```

Tests spin up Postgres and MinIO via Testcontainers automatically. `EmailService` is mocked — no real emails are sent.

## API overview

| Method | Path                            | Auth   | Description                            |
|--------|---------------------------------|--------|----------------------------------------|
| POST   | `/auth/register`                | public | Register a tattoo artist               |
| GET    | `/auth/verify`                  | public | Verify email address                   |
| POST   | `/auth/resend-verification`     | public | Resend verification email              |
| POST   | `/auth/login`                   | public | Login, returns JWT + refresh token     |
| POST   | `/auth/refresh`                 | public | Rotate refresh token                   |
| POST   | `/auth/logout`                  | public | Invalidate refresh token               |
| POST   | `/auth/forgot-password`         | public | Send password-reset email              |
| POST   | `/auth/reset-password`          | public | Reset password with token              |
| POST   | `/auth/change-password`         | JWT    | Change own password                    |
| GET    | `/artists/{slug}`               | public | Get artist public profile              |
| GET    | `/artists/slug-available`       | public | Check slug availability                |
| GET    | `/artists/me`                   | JWT    | Get own profile                        |
| PATCH  | `/artists/me`                   | JWT    | Update profile                         |
| POST   | `/artists/me/photo`             | JWT    | Upload profile photo                   |
| DELETE | `/artists/me/photo`             | JWT    | Delete profile photo                   |
| POST   | `/appointment/{slug}`           | public | Submit appointment request             |
| POST   | `/appointment/{slug}/reference` | public | Upload reference photo                 |
| GET    | `/appointment/verify`           | public | Verify appointment (client email link) |
| GET    | `/appointment/`                 | JWT    | List own appointments (paginated)      |
| GET    | `/appointment/{id}`             | JWT    | Get appointment details                |
| POST   | `/appointment/{id}/archive`     | JWT    | Archive appointment                    |
| POST   | `/appointment/{id}/unarchive`   | JWT    | Unarchive appointment                  |
| POST   | `/appointment/{id}/mark-new`    | JWT    | Mark appointment as new                |
| POST   | `/support/screenshot`           | JWT    | Upload support message screenshot      |
| POST   | `/support`                      | JWT    | Submit a support/contact message       |

Swagger UI is disabled in production (`springdoc.swagger-ui.enabled: false`). Enable it locally by overriding in `application-local.yaml`.

## Environment variables

| Variable                         | Description                                             |
|----------------------------------|---------------------------------------------------------|
| `APP_JWT_SECRET`                 | Base64-encoded HMAC-SHA256 secret (min 256 bits)        |
| `APP_BASE_URL`                   | Public base URL of the API (used in emails)             |
| `APP_EMAIL_FROM`                 | Sender address (default: `noreply@inkvite.me`)          |
| `APP_SUPPORT_NOTIFICATION_EMAIL` | Recipient for new support/contact message notifications |
| `RESEND_API_KEY`                 | Resend API key                                          |
| `APP_STORAGE_ENDPOINT`           | S3/MinIO endpoint URL                                   |
| `APP_STORAGE_BUCKET`             | Bucket name                                             |
| `APP_STORAGE_ACCESS_KEY`         | Storage access key                                      |
| `APP_STORAGE_SECRET_KEY`         | Storage secret key                                      |
