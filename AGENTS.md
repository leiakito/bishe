# Repository Guidelines

## Project Structure & Module Organization
- `demo/`: Spring Boot service (Java 17) with layered packages `controller`, `service`, `repository`, `entity`, `dto`, `security`, `config`, `util`. Config lives in `src/main/resources/application.properties` (MySQL, JWT, logging); schema/bootstrap SQL in `init.sql`; file uploads under `uploads/`; runtime logs in `logs/`.
- `front/`: Vue 3 + TypeScript + Vite UI. Key dirs: `src/pages/*` role-specific views, `src/layouts/MainLayout.vue`, `src/api` for HTTP calls, `src/stores` (Pinia), `src/utils`, `src/styles`, `src/router`. Static assets in `public/`; build output in `dist/`. Root `docker-compose.yml` orchestrates MySQL + backend + frontend.

## Build, Test, and Development Commands
- Backend (run inside `demo/`): `./mvnw spring-boot:run` starts the API on 8080 using the MySQL URL in `application.properties`; `./mvnw test` runs unit/integration suites; `./mvnw clean package` produces the jar in `target/`. Use `docker-compose up --build` from repo root for containerized MySQL/backend/frontend (override env defaults via compose vars).
- Frontend (run inside `front/`): `pnpm install` to install deps; `pnpm dev` serves Vite on 5173 with `VITE_API_BASE_URL` from `.env`; `pnpm build` creates `dist/`; `pnpm lint` or `pnpm lint:fix` runs ESLint; `pnpm preview` serves the built bundle.

## Coding Style & Naming Conventions
- Java: 4-space indent, lowercase packages, PascalCase classes (`CompetitionController`), interfaces `*Service`/`*Repository`, DTOs under `dto`. Keep business logic in services and return `ResponseEntity` from controllers; apply validation annotations on request bodies; prefer env overrides instead of hardcoded secrets.
- Vue/TS: use `<script setup>` with TypeScript; components/pages in PascalCase `.vue`, functions/variables camelCase. Centralize API paths in `src/api`, shared state in `src/stores`, utilities in `src/utils`. Run `pnpm lint` before committing.

## Testing Guidelines
- Backend tests belong in `demo/src/test/java`, named `*Tests`; use JUnit with Spring Boot test slices/MockMvc for controllers and service-layer coverage. Mock repositories where possible; when DB interaction is required, reuse schemas from `init.sql` and target a local MySQL instance.
- Frontend automated tests are not configured; at minimum run `pnpm lint` and smoke-test key role flows in `src/pages/*`. Add Vitest + Vue Test Utils if you introduce UI/unit coverage.

## Commit & Pull Request Guidelines
- Git history favors short subjects (often Chinese), e.g., `修复提出的问题`; keep messages concise, present-tense, and scoped to one change. Reference related issues or modules when applicable.
- PRs should include a summary, testing performed (`./mvnw test`, `pnpm lint`, manual flows), API/DB/config changes, and screenshots for UI updates. Update docs when endpoints or environment variables change.

## Security & Configuration Tips
- Avoid committing real credentials; override DB/JWT settings through environment variables or compose overrides. Keep frontend `.env` and backend `application.properties` aligned for API base URLs/ports. Logs write to `demo/logs/`; sanitize sensitive data before sharing.
