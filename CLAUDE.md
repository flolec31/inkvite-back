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

> **⚠️ Agents/Claude: never run the app (`bootRun`, `bootTestRun`, or any task that boots Spring) — only the human does.**
> A plain `bootRun` boots against the developer's **persistent local Postgres** (via `spring-boot-docker-compose`) and **applies Liquibase migrations** to it. If a changeset is later edited (which is normal on an in-progress feature branch), Liquibase then fails startup with a checksum-mismatch (`ValidationFailedException`) because the stored checksum no longer matches the edited file. Agents must verify work with `./gradlew test`/`build` (Testcontainers, ephemeral DB) only. These app-run tasks are hard-denied in `.claude/settings.json`.

## Architecture

This is a Spring Boot application using Kotlin + Spring Boot 4 + Java 24 + Gradle (Kotlin DSL).

**Key stack choices:**
- **Spring Data JPA** with PostgreSQL at runtime.
- **Liquibase** for database migrations — changesets live under `src/main/resources/db/changelog/`, one folder per feature branch (e.g. `1-tattoo-artist-registration-email-validation/`), SQL format.
- **Spring Security** — all endpoints secured by default; `SecurityConfig` permits `/auth/**` (which covers both `/auth/artist/*` and `/auth/client/*`), `/swagger-ui/**`, `/v3/api-docs/**`, `GET /appointment/verify`, `POST /appointment/{slug}`, `POST /appointment/{slug}/reference` and `POST /appointment/{slug}/links` (public client-facing endpoints). `POST /auth/artist/change-password` is the one exception carved out of that space — its `.hasRole("ARTIST")` matcher is registered *before* the `/auth/**` `permitAll()` matcher, since Spring Security's `authorizeHttpRequests` uses first-match-wins ordering. `GET /client/appointment/{appointmentId}` and `GET /client/appointment/{appointmentId}/messages` are each declared with `.hasRole("CLIENT")` *before* the `anyRequest().hasRole("ARTIST")` catch-all (same first-match-wins reason); note the `/messages` sub-path needs its own matcher because a `{appointmentId}` path variable only matches a single segment. Always scope `permitAll()` with `HttpMethod` when the route has an authenticated sibling.
- **RBAC** — every JWT carries a `type` claim (`artist` or `client`, see the `Role` enum in `auth`). The `JwtAuthenticationConverter` bean in `SecurityConfig` maps that claim to a single `ROLE_ARTIST`/`ROLE_CLIENT` `GrantedAuthority`; artist-only routes are declared with `.hasRole("ARTIST")` (e.g. `GET /artists/me`, `POST /auth/artist/change-password`, and the catch-all `anyRequest()`).
- **JWT authentication** (`spring-boot-starter-oauth2-resource-server`) — stateless, HMAC-SHA256. Access tokens (15 min) issued as JWTs for both artists and clients. Refresh tokens are opaque UUIDs stored in a single unified `refresh_token` table (columns: `token` PK, `subject_id`, `subject_type` — no FK, since the subject can be either a `TattooArtist` or `TattooClient`). Refresh-token lifecycle (issue/rotate-on-refresh/revoke/revoke-all) is centralized in `RefreshTokenService`, shared by artist and client auth; refresh tokens live 30 days for artists, 48 hours for clients. `JwtConfig` binds `app.jwt.secret` (env: `APP_JWT_SECRET`, min 256-bit base64) and `app.jwt.access-token-expiry`. `JwtServiceImpl` uses `NimbusJwtEncoder` with an explicit `JwsHeader.with(MacAlgorithm.HS256)` — required for Nimbus key selection. `JwtAuthenticationEntryPoint` must be registered on **both** `exceptionHandling` and `oauth2ResourceServer` DSL blocks to cover both missing-token and invalid-token paths.
- **springdoc-openapi** (`springdoc-openapi-starter-webmvc-ui`) for Swagger UI at `/swagger-ui.html`.
- **Resend** (`resend-java`) for transactional email via `EmailServiceImpl`. API key configured via `application.yaml` (bound with `@ConfigurationProperties` in `ResendConfig`).
- **RestClient** (not WebClient/RestTemplate) for outbound HTTP calls.
- **Spring application events** for decoupling: e.g. `AuthServiceImpl` publishes `VerificationEmailRequested`, `EmailEventListener` handles it.

**Package structure** (under `com.inkvite.inkviteback`):
- `artist` — `TattooArtist` entity, repository, service, profile photo upload
- `appointment` — `Appointment` entity, repository; service and controller are split by audience: `*Submission*` (public client form submission, reference upload, email verification, and link retrieval), `*Management*` (JWT-protected artist views: list/details, archive), and `*Client*` (`AppointmentClientController` at `GET /client/appointment/{appointmentId}`, `ROLE_CLIENT`, client id from the JWT subject — lets an authenticated client read their own appointment). The client read returns `ClientAppointmentDetailsResponseDto`, a trimmed variant of the artist `AppointmentDetailsResponseDto`: it drops the artist-inbox fields (`new`, `archived`) and swaps `clientName` for `artistName`, and unlike the artist read it has **no** `new`-flip side effect. Ownership is enforced by `AppointmentAccessService.findAppointmentForClient(clientId, appointmentId)`, which throws `AppointmentNotFoundException` (404) both when the appointment is missing/unverified **and** when it belongs to another client (no existence leak), in contrast to the artist-side `findOwnedAppointment` which returns 403 for a wrong-owner match. Link retrieval (`POST /appointment/{slug}/links`, body `{ email }`) lets a client who lost their access link(s) request them by email: it returns `204` **regardless** of whether a match exists (enumeration protection), and only when the client has verified appointments with that artist (archived included) does it publish `AppointmentLinksRequested`, emailing one link per appointment (`{baseUrl}/appointment/{id}`, labeled by submission date `dd/MM/yy` + tattoo description). No rate-limiting yet — noted as future work.
  - Resend templates only substitute string/number variables (no loops/arrays), so the variable-length list is pre-rendered in `EmailServiceImpl` into a single HTML `<ul>` string and injected via the triple-brace `{{{APPOINTMENTS_HTML}}}` variable (raw HTML); the client-supplied tattoo description is truncated to 100 chars (with `...`), wrapped in guillemets, then HTML-escaped. Template `retrieve-appointment-links` variables: `ARTIST_NAME`, `CLIENT_FIRSTNAME`, `APPOINTMENTS_HTML`.
  - Verifying an appointment (`GET /appointment/verify`) publishes two events: `AppointmentNotificationEmailRequested` (notifies the artist) and `AppointmentRequestConfirmationEmailRequested` (confirms to the client and gives them their access link `{baseUrl}/appointment/{id}`). Client confirmation template `confirm-client-appointment-request` variables: `LINK`, `ARTIST_NAME`, `CLIENT_FIRSTNAME`.
- `auth` — root holds genuinely shared code: `JwtService`/`JwtServiceImpl`, `RefreshTokenService`/`RefreshTokenServiceImpl` (unified issue/rotate/revoke for both audiences), `RefreshToken` entity + repository, `LoginResponseDto`, shared token exceptions (`TokenNotFoundException`, `TokenExpiredException`, `InvalidRefreshTokenException`) in `auth.exception`, and `auth.controller.AuthControllerAdvice` which maps those shared exceptions (thrown by both artist and client flows). Everything artist-specific lives under `auth.artist.**`: `ArtistAuthController` (`@RequestMapping("/auth/artist")`, endpoints at `/auth/artist/{register,verify,resend-verification,login,refresh,logout,forgot-password,reset-password,change-password}`), `ArtistAuthControllerAdvice` (artist-specific exceptions only), `AuthService`/`AuthServiceImpl`, `VerificationToken`/`PasswordResetToken` entities + repositories, artist DTOs, artist events (`ArtistVerificationEmailRequested`, `PasswordResetEmailRequested`, `PasswordChangedEmailRequested`), and artist-specific exceptions (`EmailAlreadyRegisteredException`, `InvalidCredentialsException`, `AccountNotActivatedException`) in `auth.artist.exception`. Client-facing auth lives under `auth.client.**`: `ClientAuthController` (`@RequestMapping("/auth/client")`) exposes three endpoints. `POST /auth/client/request-code` (body `{ appointmentId }`) resolves the appointment to its client and issues a 6-digit `ClientAccessCode` (table `client_access_code`, PK `client_id`, 10-min expiry via `expiresAt`) using `SecureRandom`, publishing `ClientAccessCodeRequested` to email the code (template `verify-client-access-code`, variables `CLIENT_FIRSTNAME`/`CODE`); it always returns `204`, even for an unknown `appointmentId` (enumeration-safe, no email sent). Resend/brute-force is throttled so the attempt cap can't be reset by re-requesting: a **fresh** code (with `attempts` reset to 0) is minted only when no code exists or the previous one has expired; while a code is still valid, re-requesting re-sends the **same** code and never resets `attempts`, and the email is throttled to once per 60s via `last_sent_at` (`RESEND_COOLDOWN`) — a request inside the cooldown returns `204` without emailing. Net effect: at most 5 guesses per 10-min code lifetime, and at most one email per 60s. `POST /auth/client/verify-code` (body `{ appointmentId, code }`) checks the code against `ClientAccessCode`, enforcing a 10-minute expiry and a max of 5 attempts (the row is deleted once expired, exhausted, or successfully consumed) — on success it issues `{ accessToken, refreshToken }` via `RefreshTokenService.issue(clientId, Role.CLIENT)`; any failure throws `InvalidCodeException` (mapped to 401 by `ClientAuthControllerAdvice`). The `attempts` read-modify-write is guarded by a JPA `@Version` optimistic lock (column `version`) so concurrent wrong-guess requests can't race past the cap. `POST /auth/client/refresh` (body `{ refreshToken }`) rotates it via `RefreshTokenService.rotate` and returns a new pair. There is deliberately no client logout endpoint — clients have no account UI, so a refresh token is simply left to expire (48h); add a logout route only if/when a client-facing "sign out" affordance exists.
- `client` — `TattooClient` entity, repository, service (created implicitly on appointment submission)
- `common` — global exception handling (`GlobalControllerAdvice` for cross-cutting exceptions: bean-validation, upload-size, and `EmailDeliveryException`; `AbstractControllerAdvice` base), shared pagination DTO
- `discussion` — `Message` entity/`MessageSender` enum, repository, service, controller: JWT-protected appointment-scoped message thread. Split by audience like `appointment`: the artist side (`DiscussionController`/`DiscussionService`) exposes `GET`/`POST /appointment/{appointmentId}/messages` (ownership via `AppointmentAccessService.findOwnedAppointment`, 403 on wrong owner); the client side (`DiscussionClientController`/`DiscussionClientService` at `GET /client/appointment/{appointmentId}/messages`, `ROLE_CLIENT`, client id from the JWT subject) is **read-only** — a client reads their own thread, ownership via `AppointmentAccessService.findAppointmentForClient` (404 no existence leak, and unverified appointments 404). Both reads reuse the same `MessageResponseDto` (it carries no artist-only fields — `id`, `sender`, `content`, `imageUrl`, `sentAt`, `readAt`) and the same `messageRepository.findByAppointmentIdOrderBySentAtAsc` + signed-URL mapping. There is deliberately no client POST yet. A message carries text, an image, or both (at least one required). Sending stays JSON (`{ content?, imageKey? }`); the image is uploaded first via `POST /appointment/{appointmentId}/messages/image` (multipart, field `image`) which returns a `messages/{artistId}/{uuid}` key. The service guards that a supplied `imageKey` is prefixed with the caller's own `messages/{artistId}/`. Image URLs are returned as 1h presigned URLs on read. Posting a message (currently always artist-sent) publishes `NewMessageEmailRequested`, emailing the client a generic notification (no message content or link yet — no client-facing page exists to view the thread).
- `email` — `EmailService`, Resend client, event listener
- `security` — `SecurityConfig`, password encoder
- `storage` — `StorageService` / `StorageServiceImpl` — S3-compatible file storage via AWS SDK v2; `ImageValidator` — single source of truth for image validation (JPEG/PNG/WebP, max 5 MB), used by every image entry point (references, support screenshots, message images, and the artist profile photo in `TattooArtistServiceImpl`); `ImageUploadService` — shared validate-and-upload logic (delegates validation to `ImageValidator`, then key-prefixes and stores) used by appointment reference, support screenshot, and message image uploads (prefixes `references/`, `contact-screenshots/`, `messages/`)
- `support` — `SupportMessage` entity/enum, repository, service, controller: artist-submitted contact/support messages (bug/help/idea/other), with optional screenshot via `storage.ImageUploadService`. Submitting publishes `SupportMessageReceivedEmailRequested` (notifies `app.support.notification-email`, env `APP_SUPPORT_NOTIFICATION_EMAIL`) and `SupportMessageConfirmationEmailRequested` (confirms receipt to the submitting artist).

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