# Recipe Book — Technical Improvements Backlog

> **Last updated:** 2026-03-07
> **Purpose:** Prioritized list of technical improvements for the Recipe Book project.
> Each item includes: what, why, affected files, and estimated effort.
> Priorities reflect real risk: **Security > Stability > Performance > Polish**.

---

## Table of Contents

1. [CRITICAL — Security](#critical--security)
2. [HIGH — Database](#high--database)
3. [HIGH — Testing](#high--testing)
4. [HIGH — CI/CD](#high--cicd)
5. [HIGH — Docker & Deployment](#high--docker--deployment)
6. [MEDIUM — Monitoring & Observability](#medium--monitoring--observability)
7. [MEDIUM — Performance](#medium--performance)
8. [MEDIUM — Code Quality](#medium--code-quality)
9. [LOW — Nice to Have](#low--nice-to-have)

---

## CRITICAL — Security

These items represent **active vulnerabilities**. Fix before any public deployment.

```
┌─────────────────────────────────────────────────────────────────────────────┐
│  #1  Add *.pem to .gitignore                          DONE (2026-03-07)   │
├─────────────────────────────────────────────────────────────────────────────┤
│  Problem:  privateKey.pem and publicKey.pem are committed to git.         │
│            Anyone with repo access can forge valid JWT tokens.            │
│  Fix:      Add *.pem to .gitignore, remove tracked files with            │
│            git rm --cached *.pem                                          │
│  Files:    .gitignore, privateKey.pem, publicKey.pem                      │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│  #2  Rotate JWT keys                                  DONE (2026-03-07)   │
├─────────────────────────────────────────────────────────────────────────────┤
│  Problem:  Current RSA keys are exposed in git history forever.           │
│            Even after .gitignore fix, old keys remain accessible.         │
│  Fix:      Generate new RSA key pair:                                     │
│              openssl genrsa -out privateKey.pem 2048                      │
│              openssl rsa -in privateKey.pem -pubout -out publicKey.pem    │
│            Invalidates all existing sessions (acceptable tradeoff).       │
│  Files:    privateKey.pem, publicKey.pem                                  │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│  #3  Remove hardcoded admin credentials from DataSeeder  DONE (2026-03-07)│
├─────────────────────────────────────────────────────────────────────────────┤
│  Problem:  admin123 is hardcoded as default password in DataSeeder.       │
│            In production, anyone who reads the source knows the           │
│            admin password.                                                │
│  Fix:      Read password from env var ADMIN_PASSWORD with no default.     │
│            Fail startup if not set in prod profile.                       │
│  Files:    backend/src/.../DataSeeder.java, application.properties        │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│  #4  HTTPS enforcement                               DONE (2026-03-07)   │
├─────────────────────────────────────────────────────────────────────────────┤
│  Problem:  Everything runs on plain HTTP. Passwords and JWT tokens        │
│            travel in cleartext over the network.                          │
│  Fix:      Add nginx reverse proxy with TLS termination.                  │
│            Redirect all HTTP → HTTPS. Use Let's Encrypt or               │
│            self-signed certs for dev.                                     │
│  Files:    nginx/nginx.conf, docker-compose.prod.yml,                    │
│            backend/Dockerfile, scripts/generate-certs.*                   │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│  #5  Dynamic CORS from env var                        DONE (2026-03-07)  │
├─────────────────────────────────────────────────────────────────────────────┤
│  Problem:  CORS origin is hardcoded to localhost:8081 in                  │
│            application.properties (lines 15-22). Breaks any deployment    │
│            to a real domain.                                              │
│  Fix:      Use ${CORS_ORIGIN:http://localhost:8081} placeholder.          │
│  Files:    backend/src/main/resources/application.properties              │
└─────────────────────────────────────────────────────────────────────────────┘


## HIGH — Database

Schema management and query performance.

```
┌─────────────────────────────────────────────────────────────────────────────┐
│  #6  Flyway for DB migrations                        DONE (2026-03-07)  │
├─────────────────────────────────────────────────────────────────────────────┤
│  Problem:  hibernate.generation=update is dangerous in production.        │
│            Renaming a column = drop old + create new = data loss.         │
│            No history of schema changes.                                  │
│  Fix:      Add quarkus-flyway extension. Export current schema as         │
│            V1__initial_schema.sql baseline. All future changes as         │
│            versioned SQL files in db/migration/.                          │
│  Files:    pom.xml, application.properties,                               │
│            src/main/resources/db/migration/ (new directory)               │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│  #7  Add indexes on frequently queried columns       DONE (2026-03-07)  │
├─────────────────────────────────────────────────────────────────────────────┤
│  Problem:  Queries filter by user_id + date on several tables but         │
│            no indexes exist. Full table scans as data grows.              │
│  Fix:      Add @Index annotations on:                                     │
│            • mealplan(user_id, meal_date)                                 │
│            • foodlog(user_id, log_date)                                   │
│            • pantryitem(user_id)                                          │
│            • shoppinglistitem(user_id)                                    │
│  Files:    MealPlan.java, FoodLog.java, PantryItem.java,                 │
│            ShoppingListItem.java                                          │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│  #8  Fix N+1 query problems                         DONE (2026-03-07)    │
├─────────────────────────────────────────────────────────────────────────────┤
│  Problem:  @ManyToOne without fetch strategy causes N+1 selects.          │
│            Loading 50 recipes triggers 50+ extra queries for authors,     │
│            categories, etc.                                               │
│  Fix:      Add @NamedEntityGraph on entities or rewrite service           │
│            queries with JPQL JOIN FETCH.                                  │
│  Files:    Recipe.java:49, FoodLog.java:19,36,39,                        │
│            MealPlan.java:21,32, corresponding service classes             │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│  #9  Connection pool tuning                         DONE (2026-03-07)    │
├─────────────────────────────────────────────────────────────────────────────┤
│  Problem:  Agroal connection pool uses defaults. Under load,              │
│            connections may exhaust or idle connections waste resources.    │
│  Fix:      Add explicit config in application.properties:                 │
│            quarkus.datasource.jdbc.min-size=5                             │
│            quarkus.datasource.jdbc.max-size=20                            │
│            quarkus.datasource.jdbc.idle-removal-interval=5M               │
│  Files:    application.properties                                         │
└─────────────────────────────────────────────────────────────────────────────┘
```


## HIGH — Testing

Currently at **zero tests**. This is the biggest gap for long-term maintainability.

```
┌─────────────────────────────────────────────────────────────────────────────┐
│  #10  Unit tests for backend services          DONE (2026-03-08)          │
├─────────────────────────────────────────────────────────────────────────────┤
│  Problem:  Zero tests exist. Any refactoring is a leap of faith.          │
│  Fix:      Start with business-critical services:                         │
│            • MacroCalculationService — math-heavy, easy to unit test      │
│            • ShoppingListService — aggregation logic                      │
│            • RecommendationService — scoring algorithm                    │
│            Use JUnit 5 + Mockito for isolation.                           │
│  Files:    src/test/java/.../ (new test classes)                          │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│  #11  Integration tests for GraphQL API          DONE (2026-03-08)       │
├─────────────────────────────────────────────────────────────────────────────┤
│  Problem:  No tests verify that GraphQL mutations/queries work            │
│            end-to-end with a real database.                               │
│  Fix:      @QuarkusTest + @TestHTTPResource + TestContainers              │
│            (PostgreSQL). Test login, CRUD recipes, meal plan ops.         │
│  Files:    src/test/java/.../ (new), pom.xml (TestContainers dep)        │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│  #12  Delete legacy Playwright test specs                       ~ 15 min  │
├─────────────────────────────────────────────────────────────────────────────┤
│  Problem:  8 of 14 Playwright spec files test "D&D Campaign Manager"     │
│            — a completely different project. They clutter the repo        │
│            and confuse anyone looking at the test suite.                  │
│  Fix:      Delete the 8 irrelevant spec files. Keep only Recipe Book     │
│            related specs (if any are valid).                              │
│  Files:    frontend/src/test/playwright/*.spec.ts (8 files to delete)    │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│  #13  Fresh Playwright E2E tests                                ~ 4-6h    │
├─────────────────────────────────────────────────────────────────────────────┤
│  Problem:  After cleanup (#12), no E2E tests remain.                      │
│  Fix:      Write new specs covering critical user flows:                  │
│            • Login / register                                             │
│            • Create and view a recipe                                     │
│            • Create meal plan for a week                                  │
│            • Generate shopping list from meal plan                        │
│  Files:    frontend/src/test/playwright/ (new spec files)                │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│  #14  Code coverage with JaCoCo                                 ~ 30 min  │
├─────────────────────────────────────────────────────────────────────────────┤
│  Problem:  No visibility into what code is tested.                        │
│  Fix:      Add JaCoCo Maven plugin. Generate HTML report.                 │
│            Set minimum 60% coverage on service classes.                   │
│            Fail build if below threshold.                                 │
│  Files:    pom.xml                                                        │
└─────────────────────────────────────────────────────────────────────────────┘
```


## HIGH — CI/CD

No automation exists. Every build and deploy is manual.

```
┌─────────────────────────────────────────────────────────────────────────────┐
│  #15  GitHub Actions — build + test                             ~ 1-2h    │
├─────────────────────────────────────────────────────────────────────────────┤
│  Problem:  No CI. Broken code can be pushed without anyone knowing.       │
│  Fix:      Create .github/workflows/ci.yml:                               │
│            1. Checkout                                                     │
│            2. Setup Java 21                                                │
│            3. Maven build backend                                          │
│            4. Maven build frontend                                         │
│            5. Run tests                                                    │
│            Trigger on push to main and PRs.                               │
│  Files:    .github/workflows/ci.yml (new)                                │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│  #16  GitHub Actions — Docker build + push                      ~ 1-2h    │
├─────────────────────────────────────────────────────────────────────────────┤
│  Problem:  No automated image building or publishing.                     │
│  Fix:      Second workflow: build Docker images for backend +             │
│            frontend, push to GitHub Container Registry (ghcr.io).         │
│            Trigger on tags (v*) or manual dispatch.                       │
│  Files:    .github/workflows/docker.yml (new)                            │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│  #17  docker-compose.prod.yml                        DONE (2026-03-07)   │
├─────────────────────────────────────────────────────────────────────────────┤
│  Problem:  No production-ready compose file. Current compose is           │
│            dev-only with exposed ports, no TLS, no restart policies.      │
│  Fix:      Create docker-compose.prod.yml with:                           │
│            • backend + frontend + postgres + nginx                        │
│            • Environment variables from .env file                         │
│            • restart: unless-stopped on all services                      │
│            • Named volumes for data persistence                           │
│  Files:    docker-compose.prod.yml                                       │
└─────────────────────────────────────────────────────────────────────────────┘
```


## HIGH — Docker & Deployment

```
┌─────────────────────────────────────────────────────────────────────────────┐
│  #18  Containerize backend                           DONE (2026-03-07)   │
├─────────────────────────────────────────────────────────────────────────────┤
│  Problem:  Backend runs only locally. Templates exist at                  │
│            backend/src/main/docker/Dockerfile.jvm but are not             │
│            integrated into docker-compose.                                │
│  Fix:      Multi-stage Dockerfile: Maven build → JRE slim runtime.        │
│            Add as service in docker-compose.                              │
│  Files:    backend/Dockerfile, docker-compose.prod.yml                    │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│  #19  Containerize frontend                          DONE (2026-03-07)   │
├─────────────────────────────────────────────────────────────────────────────┤
│  Problem:  frontend/Dockerfile exists but is not referenced in            │
│            docker-compose. Frontend runs manually via npm/mvn.            │
│  Fix:      Add frontend service to docker-compose, referencing            │
│            existing Dockerfile.                                           │
│  Files:    docker-compose.prod.yml, frontend/Dockerfile                  │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│  #20  Nginx reverse proxy                            DONE (2026-03-07)   │
├─────────────────────────────────────────────────────────────────────────────┤
│  Problem:  Backend and frontend exposed on separate ports.                │
│            No single entry point, no TLS termination.                     │
│  Fix:      Nginx config:                                                  │
│            • :443 → TLS termination                                       │
│            • /graphql, /q/* → backend:8080                                │
│            • /* → frontend:8081                                           │
│  Files:    nginx/nginx.conf, docker-compose.prod.yml                     │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│  #21  Health checks on app services                             ~ 30 min  │
├─────────────────────────────────────────────────────────────────────────────┤
│  Problem:  Docker has no way to know if app is actually healthy.          │
│            Container can be "running" but app crashed internally.         │
│  Fix:      Add HEALTHCHECK in Dockerfiles. Quarkus already supports      │
│            /q/health with smallrye-health extension.                      │
│  Files:    backend/Dockerfile, docker-compose.yml                        │
└─────────────────────────────────────────────────────────────────────────────┘
```


## MEDIUM — Monitoring & Observability

Zero visibility into what the app is doing in production.

```
┌─────────────────────────────────────────────────────────────────────────────┐
│  #22  Quarkus health checks                                     ~ 15 min  │
├─────────────────────────────────────────────────────────────────────────────┤
│  Problem:  No health endpoints. Can't tell if app is ready to serve.      │
│  Fix:      Add quarkus-smallrye-health extension.                         │
│            Instantly provides /q/health/ready and /q/health/live.         │
│  Files:    pom.xml                                                        │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│  #23  Metrics with Micrometer + Prometheus                      ~ 30 min  │
├─────────────────────────────────────────────────────────────────────────────┤
│  Problem:  No metrics. Can't tell request volume, latency, or             │
│            error rates.                                                    │
│  Fix:      Add quarkus-micrometer-registry-prometheus.                    │
│            Exposes /q/metrics with request counts, latency histograms,    │
│            JVM memory/GC stats.                                           │
│  Files:    pom.xml, application.properties                               │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│  #24  Grafana dashboard                                         ~ 2-3h    │
├─────────────────────────────────────────────────────────────────────────────┤
│  Problem:  Even with metrics exposed, nobody looks at raw /metrics.       │
│  Fix:      Add Prometheus + Grafana as Docker services.                   │
│            Pre-configure a dashboard: request rate, p95 latency,          │
│            error rate, JVM heap, DB connection pool.                      │
│  Files:    docker-compose.yml, prometheus/prometheus.yml (new),           │
│            grafana/dashboards/ (new)                                      │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│  #25  Structured logging (JSON)                                 ~ 30 min  │
├─────────────────────────────────────────────────────────────────────────────┤
│  Problem:  Plain text logs are hard to parse and search.                  │
│  Fix:      Set quarkus.log.console.json=true in prod profile.            │
│            Add quarkus-logging-json for correlation IDs per request.      │
│  Files:    application.properties, pom.xml                               │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│  #26  Access logging                                            ~ 5 min   │
├─────────────────────────────────────────────────────────────────────────────┤
│  Problem:  No record of HTTP requests. Can't audit who accessed what.     │
│  Fix:      Add quarkus.http.access-log.enabled=true.                     │
│  Files:    application.properties                                         │
└─────────────────────────────────────────────────────────────────────────────┘
```


## MEDIUM — Performance

```
┌─────────────────────────────────────────────────────────────────────────────┐
│  #27  Cache frequent queries                                    ~ 1h      │
├─────────────────────────────────────────────────────────────────────────────┤
│  Problem:  Every page load hits the database, even for data that          │
│            rarely changes (ingredients list, recommendations).            │
│  Fix:      Add @CacheResult (Quarkus cache) on:                          │
│            • listIngredients — ingredient list is near-static             │
│            • recipeRecommendations — expensive computation                │
│            • getWeeklyMealPlan — same plan queried multiple times/day     │
│  Files:    Corresponding service classes, pom.xml (quarkus-cache)        │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│  #28  Hibernate L2 Cache                                        ~ 30 min  │
├─────────────────────────────────────────────────────────────────────────────┤
│  Problem:  Entities like Ingredient are loaded from DB on every request   │
│            despite rarely changing.                                       │
│  Fix:      Enable second-level cache with Caffeine. Mark Ingredient      │
│            as @Cacheable. Configure region settings in                    │
│            application.properties.                                        │
│  Files:    pom.xml, application.properties, Ingredient.java              │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│  #29  Paginate RecommendationService                            ~ 1h      │
├─────────────────────────────────────────────────────────────────────────────┤
│  Problem:  RecommendationService calls Recipe.listAll() — loads ALL       │
│            recipes into memory. Won't scale past a few hundred recipes.   │
│  Fix:      Add pagination, limit to top-20 results. Use a DB-level       │
│            pre-filter before scoring in Java.                             │
│  Files:    RecommendationService.java                                     │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│  #30  Optimize ShoppingListService                              ~ 1h      │
├─────────────────────────────────────────────────────────────────────────────┤
│  Problem:  generateShoppingList() iterates meal plans and lazily loads    │
│            plan.recipe.ingredients — classic N+1 query pattern.           │
│  Fix:      Replace with a single JPQL query using JOIN FETCH on           │
│            MealPlan → Recipe → Ingredients.                               │
│  Files:    ShoppingListService.java                                       │
└─────────────────────────────────────────────────────────────────────────────┘
```


## MEDIUM — Code Quality

```
┌─────────────────────────────────────────────────────────────────────────────┐
│  #31  Checkstyle + SpotBugs                                     ~ 30 min  │
├─────────────────────────────────────────────────────────────────────────────┤
│  Problem:  No automated code quality enforcement. Style varies across     │
│            files, common bug patterns go undetected.                      │
│  Fix:      Add Maven plugins for Checkstyle and SpotBugs.                │
│            Enforce at compile time. Use Google Java Style as baseline.    │
│  Files:    pom.xml, checkstyle.xml (new)                                 │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│  #32  Custom exception hierarchy                                ~ 1-2h    │
├─────────────────────────────────────────────────────────────────────────────┤
│  Problem:  Generic catch (Exception e) blocks swallow errors.             │
│            No distinction between "not found", "validation failed",       │
│            and "unauthorized".                                            │
│  Fix:      Create hierarchy:                                              │
│            RecipeBookException                                            │
│              ├── NotFoundException                                        │
│              ├── ValidationException                                      │
│              └── AuthorizationException                                   │
│            Replace generic catches in RecipeImportService.java:44,57      │
│            and RecipeImportGraphQL.java:82.                               │
│  Files:    New exception classes, RecipeImportService.java,               │
│            RecipeImportGraphQL.java                                       │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│  #33  Centralized GraphQL error handler                         ~ 1h      │
├─────────────────────────────────────────────────────────────────────────────┤
│  Problem:  GraphQL errors return raw Java exceptions to the client.       │
│            No structured error codes or user-friendly messages.           │
│  Fix:      Implement @GraphQLExceptionHandler that maps custom            │
│            exceptions to structured responses with error codes.           │
│  Files:    New handler class, GraphQL resource classes                    │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│  #34  Clean up legacy technical debt                            ~ 30 min  │
├─────────────────────────────────────────────────────────────────────────────┤
│  Problem:  Leftovers from a previous D&D Campaign Manager project:       │
│            • i18n keys in messages.properties (campaigns, characters,     │
│              dice references)                                             │
│            • Unused npm deps: ol, proj4, @vaadin/map in                  │
│              frontend/package.json                                        │
│  Fix:      Delete the D&D i18n keys. Remove unused npm dependencies.     │
│  Files:    messages.properties, frontend/package.json                    │
└─────────────────────────────────────────────────────────────────────────────┘
```


## LOW — Nice to Have

Lower priority items. Implement after the above categories are addressed.

```
┌─────────────────────────────────────────────────────────────────────────────┐
│  #35  Rate limiting on API                                      ~ 1h      │
├─────────────────────────────────────────────────────────────────────────────┤
│  Problem:  No rate limiting. A single client can flood the API.           │
│  Fix:      Add rate limiting: max 100 req/min per user on /graphql.      │
│            Use nginx rate limiting or Quarkus extension.                  │
│  Files:    nginx.conf or application.properties                          │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│  #36  Automated database backups                                ~ 1h      │
├─────────────────────────────────────────────────────────────────────────────┤
│  Problem:  No backups. A single bad migration or disk failure means       │
│            total data loss.                                               │
│  Fix:      Docker cron job that runs pg_dump daily, rotates and keeps     │
│            the last 7 backups. Store in a mounted volume.                 │
│  Files:    backup/backup.sh (new), docker-compose.yml                    │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│  #37  API versioning                                            ~ 30 min  │
├─────────────────────────────────────────────────────────────────────────────┤
│  Problem:  No versioning on the GraphQL endpoint. Future breaking         │
│            changes will break all clients simultaneously.                 │
│  Fix:      Prefix endpoint as /v1/graphql. Document versioning           │
│            policy.                                                        │
│  Files:    application.properties, frontend API client config            │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│  #38  Frontend error boundary                                   ~ 1h      │
├─────────────────────────────────────────────────────────────────────────────┤
│  Problem:  When backend is down, frontend shows a generic Vaadin          │
│            error. No retry option, no helpful message.                    │
│  Fix:      Add a catch-all error page with "Backend unavailable"          │
│            message and a retry button.                                    │
│  Files:    Frontend error handler view (new)                             │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│  #39  Environment profiles                                      ~ 1h      │
├─────────────────────────────────────────────────────────────────────────────┤
│  Problem:  Single application.properties for all environments.            │
│            Dev settings leak into prod and vice versa.                    │
│  Fix:      Split into:                                                    │
│            • application-dev.properties (debug, drop-and-create)         │
│            • application-prod.properties (TLS, validate schema)          │
│            • application-test.properties (H2 or TestContainers)          │
│  Files:    backend/src/main/resources/ (new property files)              │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│  #40  Dependabot / Renovate                                     ~ 15 min  │
├─────────────────────────────────────────────────────────────────────────────┤
│  Problem:  Dependencies never update automatically. Vaadin 25-rc2         │
│            will become 25.0 stable — need to track that.                  │
│  Fix:      Add .github/dependabot.yml or install Renovate GitHub app.    │
│            Configure for Maven + npm ecosystems.                          │
│  Files:    .github/dependabot.yml (new)                                  │
└─────────────────────────────────────────────────────────────────────────────┘
```


---

## Summary

| Priority | Items | Total Estimated Effort |
|----------|-------|----------------------|
| **CRITICAL** | #1–#5 | ~2–3 hours |
| **HIGH — Database** | #6–#9 | ~5–7 hours |
| **HIGH — Testing** | #10–#14 | ~13–19 hours |
| **HIGH — CI/CD** | #15–#17 | ~4–7 hours |
| **HIGH — Docker** | #18–#21 | ~3–5 hours |
| **MEDIUM — Monitoring** | #22–#26 | ~3–5 hours |
| **MEDIUM — Performance** | #27–#30 | ~3–4 hours |
| **MEDIUM — Code Quality** | #31–#34 | ~3–5 hours |
| **LOW** | #35–#40 | ~4–5 hours |
| | **TOTAL** | **~40–60 hours** |

### Recommended Order of Execution

1. **Security fixes first** (#1–#5) — protect the app from active vulnerabilities
2. **Environment profiles** (#39) — needed before most other work
3. **Flyway** (#6) — lock down the schema before adding more features
4. **Basic tests** (#10, #12) — safety net before refactoring
5. **CI/CD** (#15) — automate what you have so far
6. **Docker & deployment** (#17–#21) — make it deployable
7. **Everything else** — in priority order within each category
