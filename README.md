# FundFlow

A fundraising platform built as a resume/portfolio project: donors discover and support
campaigns, organizers create and manage campaigns, and admins verify campaigns before they
go live. Built with Java/Spring Boot on the backend and plain HTML/CSS/JS + Bootstrap on the
frontend, running entirely on localhost.

## Features

- **3 roles**: Donor, Organizer, Admin — each with their own dashboard and permissions
- **Campaign verification workflow**: DRAFT → PENDING → APPROVED / REJECTED / BLOCKED → COMPLETED
- **Mock donation & payment flow**: donate → mock payment gateway → transaction ledger →
  campaign total updated → in-app notifications. No real money moves; architecture keeps a
  clean `PaymentService` interface so a real gateway (e.g. Razorpay) can be added later
  without touching the rest of the app.
- **In-app notifications** for donors, organizers, and admins (no email/SMS)
- **Two AI features** (Spring AI + local Ollama model, no paid API):
  - AI campaign description generator (organizer-only, never auto-publishes)
  - FundFlow help assistant (answers "how do I donate", "what does approval mean", etc.)
- **JWT authentication** with Spring Security, role-based route protection
- Local SVG placeholder assets throughout — nothing depends on external image URLs

## Tech stack

| Layer      | Technology |
|------------|------------|
| Backend    | Java 21, Spring Boot 3.3.4, Spring Security, JWT (jjwt), Spring Data JPA/Hibernate |
| Database   | Oracle XE |
| AI         | Spring AI + Ollama (local model, e.g. Mistral) |
| Frontend   | HTML, CSS, JavaScript, Bootstrap 5 (via CDN) |
| Build      | Maven |
| Payments   | Mock only (`MockPaymentService`) — no Razorpay yet |

## Project structure

```
fundflow-backend/
  src/main/java/com/fundflow/
    config/        SecurityConfig, AiConfig, DataSeeder (creates default admin)
    controller/    REST controllers (Auth, Campaign, Donation, SavedCampaign,
                   Notification, User, Admin, Organizer, Ai)
    service/       Interfaces + impl/ for all business logic
    repository/    Spring Data JPA repositories
    entity/        JPA entities (User, Role, Campaign, Donation, Payment,
                   TransactionLog, Notification, CampaignUpdate, SavedCampaign, ...)
    dto/           Request/response DTOs, grouped by domain
    security/      JWT filter, JwtUtil, CustomUserDetails(Service)
    exception/     Custom exceptions + GlobalExceptionHandler
    payment/       PaymentService interface + MockPaymentServiceImpl
    ai/            CampaignDescriptionAiService, AssistantAiService
  src/main/resources/
    application.yml
    db/schema.sql  Oracle DDL + role seed data
  src/test/java/   Unit tests (Mockito, no DB required)

fundflow-frontend/
  index.html, explore.html, campaign-details.html, about.html, login.html,
  register.html, notifications.html, profile.html
  donor/           dashboard, donations, saved
  organizer/       dashboard, campaigns, create-campaign, campaign-manage
  admin/           dashboard, campaigns (verification+management), users, donations
  payment/         donate, result (receipt)
  ai/              assistant
  assets/
    css/style.css  design system
    js/api.js      fetch wrapper + JWT/session handling
    js/nav.js      shared role-aware navbar + notification bell
    images/        local SVG campaign placeholders
    logo/logo.svg  single swappable logo file
```

## Setup

### 1. Prerequisites
- JDK 21
- Maven (or use the included setup once you have Java — Maven Wrapper isn't included, install Maven separately)
- Oracle Database XE (any recent version with pluggable DB support)
- [Ollama](https://ollama.com) installed locally, for the AI features

### 2. Oracle setup
1. Install Oracle XE and make sure it's running on `localhost:1521` (default).
2. Create a dedicated schema/user for the app, e.g.:
   ```sql
   CREATE USER fundflow_user IDENTIFIED BY your_password;
   GRANT CONNECT, RESOURCE, DBA TO fundflow_user; -- DBA is generous; scope down for anything beyond local dev
   ALTER USER fundflow_user QUOTA UNLIMITED ON USERS;
   ```
3. Connect as `fundflow_user` and run `fundflow-backend/src/main/resources/db/schema.sql`
   — this creates all tables and seeds the three roles (`ROLE_DONOR`, `ROLE_ORGANIZER`, `ROLE_ADMIN`).
4. Update `application.yml`'s datasource URL if your service name isn't `XEPDB1`.

### 3. Ollama + Mistral setup
1. Install Ollama: https://ollama.com/download
2. Pull the model:
   ```
   ollama pull mistral
   ```
3. Make sure Ollama is running (it usually starts a background service on `localhost:11434`
   automatically after install; otherwise run `ollama serve`).
4. The AI features (description generator, help assistant) will fail gracefully with an
   error message in the UI if Ollama isn't running — everything else in the app works fine
   without it.

### 4. Backend
```bash
cd fundflow-backend

# set these before running, or edit application.yml directly
export DB_USERNAME=fundflow_user
export DB_PASSWORD=your_password
export JWT_SECRET=some-long-random-string-at-least-32-chars
export ADMIN_EMAIL=admin@fundflow.local
export ADMIN_PASSWORD=Admin@12345

mvn spring-boot:run
```
The API starts on `http://localhost:8080`. On first startup, `DataSeeder` automatically
creates the admin account above (only if no admin exists yet — safe to leave running).

**Note on Spring AI dependency:** this project uses `spring-ai-bom`/`spring-ai-starter-model-ollama`
version `1.0.0`. I couldn't verify this exact coordinate against Maven Central from the
environment I built this in — if `mvn compile` complains about resolving it, check
https://mvnrepository.com/artifact/org.springframework.ai for the current artifact name and
latest version, and update the `spring-ai.version` property and/or artifact id in `pom.xml`.

### 5. Frontend
No build step — it's static HTML/CSS/JS. Just serve the folder so relative paths and CORS
work correctly (opening the file directly via `file://` will break API calls):
```bash
cd fundflow-frontend
python3 -m http.server 5500
```
Then open `http://localhost:5500`. (Any static server works — VS Code's Live Server
extension, `npx serve`, etc.)

## Test credentials

| Role      | Email                  | Password       | How to get it |
|-----------|------------------------|----------------|----------------|
| Admin     | `admin@fundflow.local` | `Admin@12345`  | Auto-created on first backend startup (or whatever you set `ADMIN_EMAIL`/`ADMIN_PASSWORD` to) |
| Donor     | *(your choice)*        | *(your choice)*| Register via the UI, choose "Donate to campaigns" |
| Organizer | *(your choice)*        | *(your choice)*| Register via the UI, choose "Start a campaign" |

## Main API endpoints

| Method | Path | Access |
|--------|------|--------|
| POST | `/api/auth/register` | Public |
| POST | `/api/auth/login` | Public |
| GET  | `/api/campaigns/explore` | Public |
| GET  | `/api/campaigns/{id}` | Public (non-approved only visible to owner/admin) |
| POST | `/api/campaigns` | Organizer |
| PUT  | `/api/campaigns/{id}` | Organizer (own, DRAFT/REJECTED only) |
| POST | `/api/campaigns/{id}/submit` | Organizer |
| POST | `/api/campaigns/{id}/review` | Admin (APPROVE/REJECT/BLOCK) |
| POST | `/api/campaigns/{id}/complete` | Admin |
| GET  | `/api/campaigns/mine` | Organizer |
| GET  | `/api/campaigns/admin` / `/admin/pending` | Admin |
| POST | `/api/donations` | Donor |
| GET  | `/api/donations/my` / `/stats` | Donor |
| GET  | `/api/donations/campaign/{id}` | Organizer (own) / Admin |
| GET  | `/api/admin/users` / `/stats` / `/donations` | Admin |
| GET/POST/DELETE | `/api/saved-campaigns` | Donor |
| GET/PUT | `/api/notifications` | Any authenticated user |
| POST | `/api/ai/generate-description` | Organizer |
| POST | `/api/ai/assistant` | Public |

## Known issues / limitations

- **Spring AI dependency coordinates are unverified** (see setup note above) — I don't have
  Maven Central access in the environment I built this in.
- **No integration/controller tests** — unit tests cover service-layer business logic
  (Mockito, no DB), but there are no `@SpringBootTest`/`@WebMvcTest` tests, since those would
  need a live Oracle connection to run reliably.
- **No campaign image upload** — organizers pick from a small set of local placeholder
  images via dropdown; there's no file upload endpoint.
- **No pagination** — list endpoints (explore, admin lists, donation history) return
  everything at once. Fine for a demo dataset, would need pagination at real scale.
- **Frontend has no build step / bundler** — plain multi-page HTML on purpose, to keep it
  simple to explain; this means some markup repeats across pages instead of using shared
  components.
- **CORS is wide open to localhost** for local dev convenience — tighten before any real
  deployment.
- **No password reset / email verification flow** — out of scope for this version.
- I could not run a real `mvn compile` end-to-end in the environment I built this in (no
  Maven Central access), so while I did a careful manual/static review, please treat your
  first local build as the real compilation check.

## Future work

- Real Razorpay integration (architecture already separates `PaymentService` from
  `Donation`/`Transaction` to make this a clean swap)
- Campaign image upload
- Pagination and search improvements
- Email notifications alongside in-app ones
