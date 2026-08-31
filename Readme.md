# Flygo

A flight booking and hotel reservation system with authentication, built with Spring Boot. This README covers everything needed to run the backend locally and integrate against its API.

---

## ⚠️ Security notice

If you're pulling this repo fresh: **do not use any `.env` values that may already exist in git history.** Any secrets previously committed have been rotated. Always generate your own local secrets using the steps below — never reuse a secret you found in a commit.

---

## Tech stack

- Java 21, Spring Boot 4.1.0
- Spring Security, Spring Data JPA
- PostgreSQL 16, Flyway (migrations)
- MapStruct, Lombok
- springdoc-openapi (Swagger UI)

---

## Prerequisites

- Java 21 (JDK)
- PostgreSQL 16.x running locally
- Maven (or use the included `./mvnw` wrapper — no separate install needed)

---

## Local setup

**1. Clone the repo**
```bash
git clone https://github.com/LorenzoOyani/flygo.git
cd flygo
```

**2. Create your local `.env`**

Copy the example file and fill in your own values:
```bash
cp .env.example .env
```
```
JWT_SECRET=<generate with: openssl rand -base64 64>
POSTGRES_DB=flygo
POSTGRES_USER=postgres
POSTGRES_PASSWORD=<your local postgres password>
```

**3. Create the database**
```bash
psql -U postgres -c "CREATE DATABASE flygo;"
```

**4. Configure `src/main/resources/application.properties`**
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/flygo
spring.datasource.username=postgres
spring.datasource.password=${POSTGRES_PASSWORD}
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.hibernate.ddl-auto=validate
```
> `ddl-auto=validate` is intentional — Flyway migrations are the single source of truth for schema. Hibernate only validates entities against the schema; it never auto-alters tables.

**5. Run the app**
```bash
./mvnw spring-boot:run
```
The API will be available at `http://localhost:8080`.

---

## API documentation (Swagger)

Once running, the full interactive API docs are at:

```
http://localhost:8080/swagger-ui/index.html
```

Raw OpenAPI spec (useful for generating a typed client):
```
http://localhost:8080/v3/api-docs
```

**Using the "Authorize" button:** after logging in via `/api/v1/auth/login`, copy the `accessToken` from the response, click **Authorize** at the top of the Swagger page, paste it in (no need to type `Bearer` — Swagger adds that for you), and it will be attached automatically to every protected endpoint you test from then on.

---

## Auth flow overview

| Step | Endpoint | Auth required? | Notes |
|---|---|---|---|
| Register | `POST /api/v1/auth/signup` | No | Returns `accessToken` + `refreshToken` immediately |
| Login | `POST /api/v1/auth/login` | No | Returns `accessToken` + `refreshToken` |
| Get current user | `GET /api/v1/users/me` | Yes (`Bearer <accessToken>`) | Returns the authenticated user's profile |
| Refresh tokens | `POST /api/v1/auth/refresh` | No (uses refresh token in body) | Returns a **new** access + refresh token pair |

**Token lifecycle:**
- `accessToken` is short-lived (currently ~1 hour). Send it as `Authorization: Bearer <accessToken>` on every protected request.
- When a request returns `401 Unauthorized`, the access token has expired (or is invalid). Call `/api/v1/auth/refresh` with the current `refreshToken` to get a new pair.
- **Refresh tokens rotate on every use** — each call to `/refresh` invalidates the old refresh token and issues a brand new one. Always store and use the *latest* refresh token returned; reusing an old, already-rotated one will fail.
- Recommended frontend pattern: wrap your API client with an interceptor that, on a `401`, automatically calls `/refresh`, updates stored tokens, and retries the original request once.

**Token storage:** *(fill in once agreed with your frontend dev — e.g. in-memory + refresh token in httpOnly cookie, or localStorage for MVP simplicity)*

---

## Example requests

**Signup**
```json
POST /api/v1/auth/signup
{
  "fullName": "Ada Lovelace",
  "email": "ada@example.com",
  "password": "SecurePass123"
}
```

**Login**
```json
POST /api/v1/auth/login
{
  "email": "ada@example.com",
  "password": "SecurePass123"
}
```

**Response shape (signup & login)**
```json
{
  "id": "uuid",
  "accessToken": "eyJ...",
  "refreshToken": "base64-url-safe-string",
  "status": "DOCUMENTS_REQUIRED"
}
```

**Refresh**
```json
POST /api/v1/auth/refresh
{
  "refreshToken": "<current refresh token>"
}
```

---

## What's implemented

- ✅ Signup / Login
- ✅ JWT access tokens (signed, expiring)
- ✅ Refresh tokens (hashed at rest, rotated on use, reuse detection revokes all sessions)
- ✅ Rate limiting on `/auth/login` and `/auth/signup`
- ✅ `GET /api/v1/users/me` (protected)
- ✅ Swagger / OpenAPI docs

## Not yet implemented

- ⬜ Password reset / forgot password
- ⬜ Email verification
- ⬜ Admin roles / admin endpoints
- ⬜ Flight & hotel booking endpoints
- ⬜ Payment provider integration

*(Update this list as features land, so it stays accurate.)*

---

## Questions / issues

Ping Lawrence directly, or open an issue on this repo.