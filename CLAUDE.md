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

This is a Spring application using Kotlin + Spring Boot 4 + Java 24 + Gradle (Kotlin DSL).

**Key stack choices:**
- **Spring Data JPA** with PostgreSQL at runtime.
- **Spring Security** is on the classpath — all endpoints are secured by default.
- **RestClient** (not WebClient/RestTemplate) for outbound HTTP calls.

**Testing approach:**
- Tests use Testcontainers for PostgreSQL via `TestcontainersConfiguration` (in `src/test`), imported with `@Import(TestcontainersConfiguration::class)`.
- `TestInkviteBackApplication` allows running the full app locally with Testcontainers in place of a real database.
- Docker Compose support (`spring-boot-docker-compose`) is available for dev but `compose.yaml` is currently empty.

**Kotlin compiler flags:**
- `-Xjsr305=strict`: null-safety annotations from Java are treated as strict.
- `-Xannotation-default-target=param-property`: annotations on constructor parameters apply to both the parameter and the backing property (important for JPA/Jackson).

**JPA entities** must be in classes annotated with `@Entity`, `@MappedSuperclass`, or `@Embeddable` — the `allOpen` plugin makes these open automatically so JPA proxying works without `open` keywords.

## External services

- **SonarCloud**: project `flolec31_inkvite-back`, org `florianleca` — tracks code quality on the main branch and decorates PRs. Requires `SONAR_TOKEN` secret in GitHub.