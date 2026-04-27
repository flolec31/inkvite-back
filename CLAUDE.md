# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Housekeeping

After completing any task, ask yourself: does this change introduce new endpoints, env vars, stack choices, conventions, or setup steps? If yes, update `CLAUDE.md` and/or `README.md` accordingly. Skip if nothing externally observable changed.

## Commands

```bash
# Build
./gradlew build

# After adding, removing, or updating any dependency (including plugins):
# regenerate dependency verification checksums, otherwise CI will fail
./gradlew --write-verification-metadata sha256 --refresh-dependencies dependencies buildEnvironment

# Run tests
./gradlew test

# Run a single test class
./gradlew test --tests "com.inkvite.inkviteback.SomeTest"

# Run the app locally (with Testcontainers instead of Docker Compose)
./gradlew bootRun --args='--spring.profiles.active=test'
# Or via the test main entry point:
./gradlew bootTestRun
```

## Architecture

This is a Spring Boot application using Kotlin + Spring Boot 4 + Java 24 + Gradle (Kotlin DSL).

**Key stack choices:**
- **Spring Data JPA** with PostgreSQL at runtime.
- **Liquibase** for database migrations — changesets live under `src/main/resources/db/changelog/`, one folder per feature branch (e.g. `1-tattoo-artist-registration-email-validation/`), SQL format.
- **Spring Security** — all endpoints secured by default; `SecurityConfig` permits `/auth/**`, `/swagger-ui/**`, `/v3/api-docs/**`, `GET /appointment/verify`, `POST /appointment/{slug}` and `POST /appointment/{slug}/reference` (public client-facing endpoints). Always scope `permitAll()` with `HttpMethod` when the route has an authenticated sibling.
- **JWT authentication** (`spring-boot-starter-oauth2-resource-server`) — stateless, HMAC-SHA256. Access tokens (15 min) issued as JWTs; refresh tokens (30 days) stored in `refresh_token` table. `JwtConfig` binds `app.jwt.secret` (env: `APP_JWT_SECRET`, min 256-bit base64) and `app.jwt.access-token-expiry`. `JwtServiceImpl` uses `NimbusJwtEncoder` with an explicit `JwsHeader.with(MacAlgorithm.HS256)` — required for Nimbus key selection. `JwtAuthenticationEntryPoint` must be registered on **both** `exceptionHandling` and `oauth2ResourceServer` DSL blocks to cover both missing-token and invalid-token paths.
- **springdoc-openapi** (`springdoc-openapi-starter-webmvc-ui`) for Swagger UI at `/swagger-ui.html`.
- **Resend** (`resend-java`) for transactional email via `EmailServiceImpl`. API key configured via `application.yaml` (bound with `@ConfigurationProperties` in `ResendConfig`).
- **RestClient** (not WebClient/RestTemplate) for outbound HTTP calls.
- **Spring application events** for decoupling: e.g. `AuthServiceImpl` publishes `VerificationEmailRequested`, `EmailEventListener` handles it.

**Package structure** (under `com.inkvite.inkviteback`):
- `artist` — `TattooArtist` entity, repository, service, profile photo upload
- `appointment` — `Appointment` entity, repository, service, controller; handles both public client submission and JWT-protected artist views
- `auth` — registration/verification controllers, services, token entity, DTOs, events
- `client` — `TattooClient` entity, repository, service (created implicitly on appointment submission)
- `common` — global exception handler (`GlobalExceptionHandler`), shared pagination DTO
- `email` — `EmailService`, Resend client, event listener
- `security` — `SecurityConfig`, password encoder
- `storage` — `StorageService` / `StorageServiceImpl` — S3-compatible file storage via AWS SDK v2

**Testing approach:**
- Tests use Testcontainers for PostgreSQL **and MinIO** via `TestcontainersConfiguration` (in `src/test`), imported with `@Import(TestcontainersConfiguration::class)`. MinIO container is started and its S3 URL, access key, and secret key are injected via `DynamicPropertyRegistry`.
- `TestInkviteBackApplication` allows running the full app locally with Testcontainers in place of a real database.
- Integration tests use `@SpringBootTest` + `@AutoConfigureMockMvc`; external services (e.g. `EmailService`) are `@MockitoBean`.
- Docker Compose (`compose.yaml`) runs Postgres 17 + MinIO for manual dev; not used in tests.

**Kotlin compiler flags:**
- `-Xjsr305=strict`: null-safety annotations from Java are treated as strict.
- `-Xannotation-default-target=param-property`: annotations on constructor parameters apply to both the parameter and the backing property (important for JPA/Jackson).

**Kotlin conventions:**
- Use the `$$` string prefix for `@Value` annotations to avoid escaping `$`: `@Value($$"${some.property}")` not `@Value("\${some.property}")`.

**JPA entities** must be in classes annotated with `@Entity`, `@MappedSuperclass`, or `@Embeddable` — the `allOpen` plugin makes these open automatically so JPA proxying works without `open` keywords.

## External services

- **SonarCloud**: project `flolec31_inkvite-back`, org `florianleca` — tracks code quality on the main branch and decorates PRs. Requires `SONAR_TOKEN` secret in GitHub.
- **Resend**: transactional email provider. API key stored in `application-local.yaml` (gitignored). See `ResendConfig` for the `@ConfigurationProperties` binding.
- **MinIO / S3**: object storage for appointment reference photos and artist profile photos. Endpoint, bucket, access key, and secret key are all configured via `app.storage.*` properties (env vars: `APP_STORAGE_ENDPOINT`, `APP_STORAGE_BUCKET`, `APP_STORAGE_ACCESS_KEY`, `APP_STORAGE_SECRET_KEY`). Local dev uses the MinIO service in `compose.yaml` (API on port 9000, console on 9001).