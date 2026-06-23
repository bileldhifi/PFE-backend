# Travel Diary — Backend API

Spring Boot REST API for a **travel diary / social travel app**. Users record GPS trips, publish geo-tagged posts with photos, follow other travelers, exchange direct messages, and receive real-time notifications.

| | |
|---|---|
| **Base URL** | `http://localhost:8089/app-backend` |
| **Swagger UI** | [http://localhost:8089/app-backend/swagger-ui/index.html](http://localhost:8089/app-backend/swagger-ui/index.html) |
| **OpenAPI JSON** | `http://localhost:8089/app-backend/v3/api-docs` |
| **Stack** | Java 17 · Spring Boot 3.4 · PostgreSQL · JWT · WebSocket (STOMP) |

---

## Table of contents

- [Features](#features)
- [Architecture](#architecture)
- [Project structure](#project-structure)
- [Getting started](#getting-started)
- [Configuration](#configuration)
- [Authentication](#authentication)
- [API overview](#api-overview)
- [WebSocket (real-time)](#websocket-real-time)
- [Data model](#data-model)
- [File uploads](#file-uploads)
- [Detailed API reference](#detailed-api-reference)

---

## Features

| Domain | Capabilities |
|--------|--------------|
| **Auth** | Register, login, JWT access + refresh tokens, forgot/reset password (email code), change password |
| **Users** | Profile CRUD, avatar upload, travel stats, list all users |
| **Trips** | Start/end trips, list by user, delete, timeline view with stats |
| **GPS tracking** | Single/bulk track points, time-range & geo-radius queries, distance calculation, optimization (skip redundant points) |
| **Posts** | Multipart posts with images, visibility (PUBLIC/PRIVATE), search by city/country, feed from followed users |
| **Social** | Likes, comments, follow/unfollow, followers/following lists |
| **Notifications** | In-app notifications + WebSocket push (like, comment, follow, new post, mention) |
| **Messaging** | 1-to-1 conversations, paginated history, read receipts, WebSocket updates |
| **Media** | Upload photos/videos/audio to posts, serve static files from `/uploads/**` |

---

## Architecture

**Layers**

1. **Controllers** (`control/`) — HTTP mapping, request validation, auth context
2. **Services** (`service/`) — business logic, notifications, WebSocket broadcasts
3. **Repositories** (`repository/`) — JPA data access
4. **Entities** (`entity/`) — JPA domain model
5. **DTOs** (`dto/`) — request/response records
6. **Config** (`config/`) — security, JWT, WebSocket, OpenAPI, static resources, AOP logging

Cross-cutting: **AOP** (`CnfigAOP`) logs entry/exit and execution time for every service method.

---

## Project structure

```
src/main/java/tn/esprit/exam/
├── ExamApplication.java          # Entry point (@EnableScheduling, @EnableAspectJAutoProxy)
├── config/                         # Security, JWT, WebSocket, OpenAPI, static files
├── control/                        # REST controllers (13 controllers)
├── dto/                            # Request/response records
├── entity/                         # JPA entities & enums
├── repository/                     # Spring Data interfaces
└── service/                        # Interfaces + *Impl classes

src/main/resources/
└── application.properties        # DB, JWT, mail, multipart limits

uploads/                            # Created at runtime (post media + avatars)
```

---

## Getting started

### Prerequisites

- **JDK 17+**
- **Maven 3.8+**
- **PostgreSQL 14+** with database `app-backend`

### Database setup

```sql
CREATE DATABASE "app-backend";
```

Default credentials (see [Configuration](#configuration)):

- Host: `localhost:5432`
- User / password: `postgres` / `postgres`

Hibernate `ddl-auto=update` creates/updates tables automatically on startup.

### Run the application

```bash
# Clone and enter the project
cd exam

# Run with Maven wrapper
./mvnw spring-boot:run

# Or build and run the JAR
./mvnw clean package -DskipTests
java -jar target/app-backend-1.0.0.jar
```

Verify:

```bash
curl http://localhost:8089/app-backend/swagger-ui/index.html
```

---

## Configuration

Key settings in `src/main/resources/application.properties`:

| Property | Default | Description |
|----------|---------|-------------|
| `server.port` | `8089` | HTTP port |
| `server.servlet.context-path` | `/app-backend` | All routes are prefixed with this path |
| `spring.datasource.url` | `jdbc:postgresql://localhost:5432/app-backend` | PostgreSQL connection |
| `spring.jpa.hibernate.ddl-auto` | `update` | Auto schema migration |
| `app.jwt.secret` | env `JWT_SECRET` or built-in default | HMAC signing key (**override in production**) |
| `app.jwt.access-expiration` | `900000` (15 min) | Access token TTL (ms) |
| `app.jwt.refresh-expiration` | `604800000` (7 days) | Refresh token TTL (ms) |
| `spring.servlet.multipart.max-file-size` | `20MB` | Max upload size |
| `spring.mail.*` | Mailtrap sandbox | Password reset emails |

**Production checklist**

- Set `JWT_SECRET` via environment variable (never commit real secrets).
- Replace mail credentials with your SMTP provider (SendGrid, etc.).
- Use `spring.jpa.hibernate.ddl-auto=validate` or Flyway/Liquibase for schema control.
- Configure CORS if the frontend runs on a different origin.

---

## Authentication

Protected endpoints require a JWT in the `Authorization` header:

```http
Authorization: Bearer <accessToken>
```

The token subject is the user's **email**. Claims include `role` and `uid` (user UUID).

### Public endpoints (no token)

| Method | Path |
|--------|------|
| POST | `/auth/login`, `/auth/register`, `/auth/forgot-password`, `/auth/reset-password`, `/auth/verify-reset-code` |
| POST | `/users/add` |
| GET | `/uploads/**` |
| GET | `/swagger-ui/**`, `/v3/api-docs/**` |
| * | `/ws/**` (WebSocket handshake) |

All other routes require authentication.

### Token lifecycle

| Token | TTL | Usage |
|-------|-----|-------|
| Access | 15 minutes | API calls |
| Refresh | 7 days | `POST /auth/refresh?refreshToken=...` → new pair |

### Password reset flow

1. `POST /auth/forgot-password?email=user@example.com` → email with 6-digit code + link
2. `POST /auth/verify-reset-code?email=...&code=123456` → returns reset `token`
3. `POST /auth/reset-password?token=...&newPassword=...` → password updated

### Example: login

```bash
curl -X POST http://localhost:8089/app-backend/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"secret123"}'
```

Returns an `AuthResponse` with `accessToken`, `refreshToken`, and `user` (see [docs/API.md](docs/API.md)).

---

## API overview

> Full request/response details: **[docs/API.md](docs/API.md)**

All paths below are relative to `/app-backend`.

### Auth — `/auth`

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/login` | — | Login → tokens + user |
| POST | `/register` | — | Register new user |
| POST | `/forgot-password` | — | Send reset email |
| POST | `/verify-reset-code` | — | Verify 6-digit code |
| POST | `/reset-password` | — | Set new password with token |
| PUT | `/change-password` | ✓ | Change password (authenticated) |
| POST | `/refresh` | — | Refresh tokens |

### Users — `/users`

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/` | ✓ | List all users |
| GET | `/me` | ✓ | Current user profile |
| GET | `/{userId}` | ✓ | User by ID |
| GET | `/{userId}/travel-stats` | ✓ | Aggregated travel statistics |
| POST | `/add` | — | Create user (public) |
| PUT | `/me` | ✓ | Update own profile |
| PUT | `/{userId}` | ✓ | Update user by ID |
| DELETE | `/me` | ✓ | Delete own account |
| DELETE | `/{userId}` | ✓ | Delete user by ID |
| POST | `/me/avatar` | ✓ | Upload avatar (multipart) |
| DELETE | `/me/avatar` | ✓ | Remove avatar |

### Follow — `/users` (social graph)

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/{userId}/follow` | ✓ | Follow user |
| DELETE | `/{userId}/follow` | ✓ | Unfollow user |
| GET | `/{userId}/followers` | ✓ | List followers |
| GET | `/{userId}/following` | ✓ | List following |
| GET | `/{userId}/follow-status` | ✓ | Am I following this user? |

### Trips — `/trips`

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/start/{userId}` | ✓ | Start a new trip |
| PATCH | `/end/{tripId}` | ✓ | End an active trip |
| GET | `/user/{userId}` | ✓ | List user's trips |
| GET | `/{tripId}` | ✓ | Trip details + stats |
| DELETE | `/{tripId}` | ✓ | Delete trip |
| GET | `/{tripId}/timeline` | ✓ | Chronological timeline (track points + posts) |

### Track points — `/trips/{tripId}/track-points`

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/` | ✓ | Add GPS point |
| POST | `/bulk` | ✓ | Batch add points |
| GET | `/` | ✓ | All points for trip |
| GET | `/?startTime=&endTime=` | ✓ | Points in time range |
| GET | `/?lat=&lon=&radius=` | ✓ | Points near location (meters) |
| GET | `/latest` | ✓ | Most recent point |
| GET | `/distance` | ✓ | Total distance (km) |
| DELETE | `/{trackPointId}` | ✓ | Delete a point |

### Posts — `/posts`

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/{tripId}` | ✓ | Create post with images (multipart) |
| GET | `/trip/{tripId}` | ✓ | Posts for a trip |
| GET | `/track-point/{trackPointId}` | ✓ | Posts at a map marker |
| GET | `/public?country=&city=` | ✓ | Search public posts |
| GET | `/{postId}` | ✓ | Single post |
| GET | `/following` | ✓ | Feed from followed users |

### Likes & comments — `/posts`

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/{postId}/like` | ✓ | Like post |
| DELETE | `/{postId}/like` | ✓ | Unlike post |
| GET | `/{postId}/likes` | ✓ | List likers |
| GET | `/{postId}/like-status` | ✓ | Did I like this post? |
| POST | `/{postId}/comments` | ✓ | Add comment |
| DELETE | `/comments/{commentId}` | ✓ | Delete own comment |
| GET | `/{postId}/comments` | ✓ | List comments |

### Media — `/media`

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/upload/{postId}` | ✓ | Attach media to existing post |

### Notifications — `/notifications`

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/` | ✓ | All notifications |
| GET | `/unread` | ✓ | Unread only |
| GET | `/unread/count` | ✓ | Unread count |
| PUT | `/{notificationId}/read` | ✓ | Mark one as read |
| PUT | `/read-all` | ✓ | Mark all as read |
| DELETE | `/{notificationId}` | ✓ | Delete notification |

### Direct messages — `/messages`

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/conversations` | ✓ | List my conversations |
| POST | `/conversations` | ✓ | Create or get existing 1-to-1 chat |
| GET | `/conversations/{conversationId}` | ✓ | Conversation metadata |
| GET | `/conversations/{conversationId}/messages` | ✓ | Paginated messages (`before`, `limit`) |
| POST | `/conversations/{conversationId}/messages` | ✓ | Send message |
| POST | `/conversations/{conversationId}/read` | ✓ | Mark conversation as read |

---

## WebSocket (real-time)

**Endpoint:** `ws://localhost:8089/app-backend/ws` (SockJS supported)

**Protocol:** STOMP over WebSocket

| Setting | Value |
|---------|-------|
| Application prefix | `/app` |
| Broker topics | `/topic`, `/queue` |
| User prefix | `/user` |

### Subscribe topics

| Topic | Event | Payload |
|-------|-------|---------|
| `/topic/notifications/{userId}` | New notification, read updates | `NotificationUpdate` |
| `/topic/posts/{postId}/likes` | Like / unlike | `PostLikeUpdate` |
| `/topic/posts/{postId}/comments` | New comment | `PostCommentUpdate` |
| `/topic/dm/{conversationId}` | New message, read receipt | `DirectMessageUpdate` |

### Client example (JavaScript)

```javascript
const socket = new SockJS('http://localhost:8089/app-backend/ws');
const stomp = Stomp.over(socket);

stomp.connect({}, () => {
  stomp.subscribe(`/topic/notifications/${userId}`, (msg) => {
    console.log(JSON.parse(msg.body));
  });
});
```

---

## Data model

### Enums

| Enum | Values |
|------|--------|
| `Role` | `USER`, `ADMIN` |
| `Visibility` | `PUBLIC`, `PRIVATE` |
| `MediaKind` | `PHOTO`, `VIDEO`, `AUDIO` |
| `NotificationType` | `LIKE`, `COMMENT`, `FOLLOW`, `NEW_POST`, `MENTION` |

---

## File uploads

| Type | Max size | Storage | Public URL |
|------|----------|---------|------------|
| Post images (multipart) | 20 MB (config) | `uploads/{uuid}_{filename}` | `/app-backend/uploads/...` |
| Avatar | 5 MB, images only | `uploads/avatars/{uuid}_{filename}` | `/app-backend/uploads/avatars/...` |
| Post media via `/media/upload` | 20 MB | Same as post images | `/app-backend/uploads/...` |

Static files are served without authentication at `/uploads/**`.

---

## Detailed API reference

See **[docs/API.md](docs/API.md)** for:

- Complete request/response details
- Query parameters and validation rules
- Multipart form field names
- WebSocket payload structures
- Typical usage flows (register → trip → track → post → social)

---

## Dependencies (Maven)

| Dependency | Purpose |
|------------|---------|
| spring-boot-starter-web | REST API |
| spring-boot-starter-data-jpa | ORM |
| spring-boot-starter-security | Auth |
| spring-boot-starter-mail | Password reset emails |
| spring-boot-starter-websocket | STOMP real-time |
| postgresql | Database driver |
| jjwt | JWT tokens |
| springdoc-openapi | Swagger UI |
| lombok | Boilerplate reduction |

---

## License

Academic / PFE project — ESPRIT Tunisia.
