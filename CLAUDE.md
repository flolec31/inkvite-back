# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

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
- **Spring Security** — all endpoints secured by default; `SecurityConfig` permits `/auth/**`, `/swagger-ui/**`, `/v3/api-docs/**`.
- **springdoc-openapi** (`springdoc-openapi-starter-webmvc-ui`) for Swagger UI at `/swagger-ui.html`.
- **Resend** (`resend-java`) for transactional email via `EmailServiceImpl`. API key configured via `application.yaml` (bound with `@ConfigurationProperties` in `ResendConfig`).
- **RestClient** (not WebClient/RestTemplate) for outbound HTTP calls.
- **Spring application events** for decoupling: e.g. `AuthServiceImpl` publishes `VerificationEmailRequested`, `EmailEventListener` handles it.

**Package structure** (under `com.inkvite.inkviteback`):
- `artist` — `TattooArtist` entity, repository, service
- `auth` — registration/verification controllers, services, token entity, DTOs, events
- `email` — `EmailService`, Resend client, event listener
- `security` — `SecurityConfig`, password encoder

**Testing approach:**
- Tests use Testcontainers for PostgreSQL via `TestcontainersConfiguration` (in `src/test`), imported with `@Import(TestcontainersConfiguration::class)`.
- `TestInkviteBackApplication` allows running the full app locally with Testcontainers in place of a real database.
- Integration tests use `@SpringBootTest` + `@AutoConfigureMockMvc`; external services (e.g. `EmailService`) are `@MockitoBean`.
- Docker Compose (`compose.yaml`) runs a local Postgres 17 for manual dev; not used in tests.

**Kotlin compiler flags:**
- `-Xjsr305=strict`: null-safety annotations from Java are treated as strict.
- `-Xannotation-default-target=param-property`: annotations on constructor parameters apply to both the parameter and the backing property (important for JPA/Jackson).

**Kotlin conventions:**
- Use the `$$` string prefix for `@Value` annotations to avoid escaping `$`: `@Value($$"${some.property}")` not `@Value("\${some.property}")`.

**JPA entities** must be in classes annotated with `@Entity`, `@MappedSuperclass`, or `@Embeddable` — the `allOpen` plugin makes these open automatically so JPA proxying works without `open` keywords.

## External services

- **SonarCloud**: project `flolec31_inkvite-back`, org `florianleca` — tracks code quality on the main branch and decorates PRs. Requires `SONAR_TOKEN` secret in GitHub.
- **Resend**: transactional email provider. API key stored in `application-local.yaml` (gitignored). See `ResendConfig` for the `@ConfigurationProperties` binding.