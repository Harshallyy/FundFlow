# FundFlow

FundFlow is an India-focused fundraising demo built with Java 21, Spring Boot, Oracle, and a lightweight static frontend. Donors can support reviewed campaigns, organizers can manage fundraisers, and admins control campaign approval.

## Purpose and features

The project demonstrates a straightforward, role-based fundraising workflow with INR display, campaign progress, saved campaigns, donation history, mock payments, and in-app notifications. Campaign review emails to admins are optional. Four approved sample campaigns are seeded locally when no approved campaigns exist and demo mode is enabled.

## Roles and core flow

- **Donor:** explore and save approved campaigns, make a mock donation, and review donation history.
- **Organizer:** create a draft, submit it for review, and manage approved campaign updates.
- **Admin:** approve, reject, or block submitted campaigns and view platform activity.

Donations use a mock payment result; no real payment is processed. Campaign totals update through the existing backend records.

## Architecture and technology

The backend uses Spring Boot REST controllers, Spring Security with JWT, service-layer business logic, Spring Data JPA, and Oracle Database. The frontend is a responsive multi-page HTML/CSS/JavaScript app using Bootstrap and a shared API helper. This keeps the codebase small and easy to follow.

The existing Oracle schema includes users, roles, campaigns, donations, payments, notifications, saved campaigns, transaction logs, updates, and campaign documents. Apply `fundflow-backend/src/main/resources/db/schema.sql` to the Oracle database before starting the backend; Hibernate validates the schema rather than creating it.

**Tech stack:** Java 21, Spring Boot 3.3.4, Spring Security/JWT, Spring Data JPA/Hibernate, Oracle, Maven, HTML, CSS, JavaScript, and Bootstrap 5.

## AI and email

The app includes a campaign-description helper and a FundFlow help assistant. Admin campaign-review emails are sent through Resend when its sender and API key are configured. Both integrations are optional to the core fundraising flow.

## Setup

Prerequisites: JDK 21, Maven, Oracle Database, and Python for serving the static frontend.

In PowerShell, configure the local backend environment:

```powershell
$env:DB_USERNAME = "fundflow_user"
$env:DB_PASSWORD = "your-database-password"
$env:JWT_SECRET = "a-long-random-secret"
$env:ADMIN_EMAIL = "admin@fundflow.local"
$env:ADMIN_PASSWORD = "your-local-admin-password"
$env:APP_DEMO_ENABLED = "true"
```

Optional email settings: `RESEND_API_KEY` and `RESEND_FROM_EMAIL`.

Run the backend from `fundflow-backend`:

```powershell
mvn clean test
mvn spring-boot:run
```

Serve the frontend from `fundflow-frontend` in another terminal:

```powershell
py -m http.server 5500
```

Open `http://localhost:5500`; the backend uses `http://localhost:8080` by default.

## Environment variables

`DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `ADMIN_EMAIL`, `ADMIN_PASSWORD`, and `APP_DEMO_ENABLED` configure the database, token signing, admin seed, and demo data. `RESEND_API_KEY` and `RESEND_FROM_EMAIL` enable review emails.

## SEO, responsiveness, and performance

Public pages include page titles and metadata; account and dashboard pages are marked `noindex`. Before deployment, replace the local URLs in `fundflow-frontend/robots.txt` and `fundflow-frontend/sitemap.xml` with the public site URL. The frontend uses local assets, CSS transitions, and no heavy client framework.

## Future scope

Potential extensions include a real payment provider, campaign image uploads, and expanded search and pagination.
