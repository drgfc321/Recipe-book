# RECIPE BOOK — COMPREHENSIVE PROJECT DOCUMENTATION

> **Purpose**: This file is the single source of truth for understanding the entire Recipe Book project. It is designed to be loaded as context in AI-assisted development sessions, giving complete project understanding instantly. Written 2026-03-06.

---

## TABLE OF CONTENTS

1. [Project Overview](#1-project-overview)
2. [Architecture](#2-architecture)
3. [Infrastructure & Deployment](#3-infrastructure--deployment)
4. [Database](#4-database)
5. [Backend — Entities](#5-backend--entities)
6. [Backend — GraphQL API (Complete Reference)](#6-backend--graphql-api-complete-reference)
7. [Backend — REST Endpoints](#7-backend--rest-endpoints)
8. [Backend — Services (Business Logic)](#8-backend--services-business-logic)
9. [Backend — DTOs (All Response Types)](#9-backend--dtos-all-response-types)
10. [Frontend — Services](#10-frontend--services)
11. [Frontend — Views & Components](#11-frontend--views--components)
12. [Frontend — Styling & Theme](#12-frontend--styling--theme)
13. [Patterns & Conventions](#13-patterns--conventions)
14. [Key Algorithms](#14-key-algorithms)
15. [File Inventory](#15-file-inventory)
16. [Security](#16-security)
17. [i18n & Localization](#17-i18n--localization)
18. [Shopping List QR Sharing System](#18-shopping-list-qr-sharing-system)
19. [Recipe Import & PDF Processing](#19-recipe-import--pdf-processing)
20. [Testing](#20-testing)
21. [Frontend Build System](#21-frontend-build-system)
22. [Error Handling & Notifications](#22-error-handling--notifications)
23. [End-to-End Feature Flows](#23-end-to-end-feature-flows)
24. [Legacy / Technical Debt](#24-legacy--technical-debt)
25. [Known State & Project Notes](#25-known-state--project-notes)

---

## 1. PROJECT OVERVIEW

### Description & Purpose

Recipe Book is a full-stack meal planning and nutrition tracking web application. Users can manage recipes, ingredients, plan weekly meals, track food intake against nutrition targets, manage a pantry, generate smart shopping lists, get recipe recommendations based on pantry contents, and import recipes from JSON/PDF sources.

### Tech Stack Summary

| Layer | Technology | Version |
|-------|-----------|---------|
| **Backend Runtime** | Quarkus | 3.30.4 |
| **Backend Language** | Java | 21 |
| **API** | SmallRye GraphQL | (bundled with Quarkus) |
| **Auth** | SmallRye JWT (RSA256) | (bundled with Quarkus) |
| **ORM** | Hibernate ORM Panache | (bundled with Quarkus) |
| **Database** | PostgreSQL | 16 (Alpine) |
| **Frontend Framework** | Vaadin Flow | 25.0.0-rc2 |
| **Frontend Runtime** | Spring Boot | 4.0.0 |
| **Frontend Language** | Java (server-side UI) | 21 |
| **JS Bundler** | Vite | 7.2.7 |
| **JS Runtime** | React (Vaadin internals) | 19.2.3 |
| **QR Code** | ZXing | 3.5.3 |
| **PDF Processing** | Apache PDFBox | 3.0.4 |
| **DB Admin** | Adminer | latest |
| **Build Tool** | Maven | 3.x (wrapper) |
| **Testing** | Playwright | (via npm) |

### Ports & Access Points

| Service | URL | Port |
|---------|-----|------|
| Backend API | http://localhost:8080 | 8080 |
| GraphQL UI | http://localhost:8080/q/graphql-ui | 8080 |
| Frontend | http://localhost:8081 | 8081 |
| Adminer (DB UI) | http://localhost:8082 | 8082 |
| PostgreSQL | localhost:5432 | 5432 |

### Repository Info

| Property | Value |
|----------|-------|
| GitHub URL | https://github.com/drgfc321/Recipe-book-.git |
| Local branch | `master` (pushes to remote `main`) |
| Feature branch | `feature/meal-planning` |
| Other branch | `ui-visual-improvements` |

### Prerequisites

- Java 21 (JDK)
- Maven 3.x (or use included wrapper `./mvnw`)
- Node.js (for frontend Vite build)
- Docker & Docker Compose (for PostgreSQL + Adminer)
- Git

---

## 2. ARCHITECTURE

### High-Level System Diagram

```
┌──────────────────────────────────────────────────────────────────┐
│                        USER'S BROWSER                            │
│  (Vaadin renders server-side HTML — no SPA, no REST frontend)    │
└───────────────────────────┬──────────────────────────────────────┘
                            │ WebSocket + HTTP
                            ▼
┌──────────────────────────────────────────────────────────────────┐
│                    FRONTEND (Vaadin + Spring Boot)                │
│                         Port 8081                                 │
│                                                                   │
│  ┌─────────────┐  ┌────────────┐  ┌───────────────────────────┐  │
│  │  MainLayout  │  │  Views     │  │  Feature Packages          │ │
│  │  (AppShell)  │  │  Login     │  │  recipe/  ingredient/      │ │
│  │  Navigation  │  │  Register  │  │  mealplan/  pantry/        │ │
│  │  Auth Guard  │  │  Dashboard │  │  shopping/  foodlog/       │ │
│  └─────────────┘  └────────────┘  │  recommendation/            │ │
│                                    └───────────────────────────┘  │
│  ┌──────────────────────────────────────────────────────────────┐ │
│  │  Services: ApiClient (GraphQL HTTP) + AuthService (JWT)      │ │
│  └──────────────────────────────────────────────────────────────┘ │
└───────────────────────────┬──────────────────────────────────────┘
                            │ HTTP POST /graphql (JSON)
                            │ + Authorization: Bearer <JWT>
                            ▼
┌──────────────────────────────────────────────────────────────────┐
│                    BACKEND (Quarkus)                              │
│                         Port 8080                                 │
│                                                                   │
│  ┌──────────────┐  ┌──────────────────────────────────────────┐  │
│  │  GraphQL     │  │  Services                                 │ │
│  │  Resolvers   │──│  MacroCalc, Pantry, MealPlan, Shopping,   │ │
│  │  (9 classes) │  │  FoodLog, Recommendation, NutritionTarget,│ │
│  └──────────────┘  │  RecipeImport, PdfExtractor, DataSeeder   │ │
│                     └──────────────────────────────────────────┘  │
│  ┌──────────────┐  ┌──────────────┐  ┌───────────────────────┐  │
│  │  REST        │  │  Entities    │  │  DTOs                  │ │
│  │  ImageResource│  │  (9 tables)  │  │  (22 response types)  │ │
│  └──────────────┘  └──────┬───────┘  └───────────────────────┘  │
└───────────────────────────┼──────────────────────────────────────┘
                            │ JDBC (Hibernate Panache)
                            ▼
┌──────────────────────────────────────────────────────────────────┐
│                    PostgreSQL 16                                  │
│                    Port 5432                                      │
│                    Database: recipebook                           │
└──────────────────────────────────────────────────────────────────┘
```

### How Vaadin Works (Key Concept)

Vaadin is a **server-side UI framework**. There is NO JavaScript frontend code written by the developer. UI components are Java classes that run on the server. Vaadin automatically renders them in the browser via WebSocket. This means:

- **No REST API consumption** from the frontend — the frontend Java code calls the backend's GraphQL API directly via HTTP (using `ApiClient`)
- **No SPA routing** — Vaadin handles routing server-side via `@Route` annotations
- **State lives on the server** — UI state is maintained in the Java session, not in the browser
- **Component model** — UI is built by composing Java objects like `VerticalLayout`, `Button`, `Grid`, `Dialog`, etc.

### How Quarkus Backend Works

- **CDI (Contexts and Dependency Injection)** — Services are `@ApplicationScoped` beans injected via `@Inject`
- **Panache ORM** — Entities extend `PanacheEntity` which provides `id`, `persist()`, `find()`, `list()`, `delete()` etc. Active Record pattern.
- **SmallRye GraphQL** — Classes annotated with `@GraphQLApi` expose `@Query` and `@Mutation` methods
- **SmallRye JWT** — `@Authenticated` annotation + `JsonWebToken` injection for auth enforcement
- **Transactions** — `@Transactional` on mutations that write data

### Authentication Flow

```
1. User submits email + password
2. Frontend AuthService calls backend GraphQL: mutation { login(email, password) }
3. Backend verifies bcrypt hash
4. Backend generates JWT (RSA256 signed, 24h TTL) with claims: userId, username, email, role
5. Frontend stores JWT token + user info in Vaadin VaadinSession
6. All subsequent GraphQL calls include: Authorization: Bearer <token>
7. Backend @Authenticated methods inject JsonWebToken, extract userId from subject claim
8. Data is scoped to user via userId filtering on all queries
```

### Data Flow: Browser → Frontend → Backend → DB

```
Browser (User clicks "Add Recipe")
  → Vaadin WebSocket → Frontend Java (RecipeFormDialog.java)
    → ApiClient.execute(graphqlQuery, variables)
      → HTTP POST http://localhost:8080/graphql
        → RecipeGraphQL.createRecipe(input)
          → MacroCalculationService.toResponse(recipe)
            → Hibernate Panache → PostgreSQL
              → Returns RecipeResponse DTO
            ← JSON response through GraphQL
          ← ApiClient parses JSON to Java record
        ← View updates UI components
      ← Vaadin pushes HTML diff to browser
    ← Browser renders updated UI
```

### Feature Interconnection Diagram

```
┌─────────┐     uses      ┌────────────┐    aggregates    ┌──────────────┐
│ Recipes │◄──────────────│  Meal Plan  │───────────────►│ Shopping List │
└────┬────┘               └─────┬──────┘                  └──────────────┘
     │                          │                                ▲
     │ has                      │ logs to                        │ subtracts
     ▼                          ▼                                │
┌────────────┐           ┌──────────┐                     ┌─────┴──────┐
│ Ingredients │◄─────────│ Food Log │                     │   Pantry    │
└─────┬──────┘           └──────────┘                     └─────┬──────┘
      │                        │                                │
      │ nutrition data         │ tracks against                 │ matches
      ▼                        ▼                                ▼
┌───────────────┐    ┌───────────────────┐          ┌──────────────────┐
│ Macro Calc    │    │ Nutrition Targets  │          │ Recommendations  │
└───────────────┘    └───────────────────┘          └──────────────────┘
```

### Navigation Map

```
/ (root) → redirects to /login if not authenticated, /dashboard if authenticated

/login          → LoginView
/register       → RegisterView
/dashboard      → DashboardView (home page)
/recipes        → RecipeListView (card grid with filters)
/recipes/{id}   → RecipeDetailView (full recipe page)
/ingredients    → IngredientListView (data grid)
/meal-plan      → MealPlanView (weekly calendar)
/pantry         → PantryView (data grid with expiring banner)
/shopping-list  → ShoppingListView (checklist with QR sharing)
/food-log       → FoodLogView (daily tracking)
/recommendations → RecommendationView (pantry-based suggestions)

/share/{id}     → SharedListController (REST, public HTML page for shared shopping lists)
```

---

## 3. INFRASTRUCTURE & DEPLOYMENT

### Docker Compose Services

**File**: `docker-compose.yml`

| Service | Image | Container Name | Port Mapping |
|---------|-------|---------------|--------------|
| postgres | `postgres:16-alpine` | recipebook-postgres | `${POSTGRES_PORT:-5432}:5432` |
| adminer | `adminer:latest` | recipebook-adminer | `${ADMINER_PORT:-8082}:8080` |

- PostgreSQL has a healthcheck: `pg_isready -U <user> -d <db>` every 10s
- Volume `postgres_data` persists data across restarts
- Both services restart `unless-stopped`

### Environment Variables (.env.example)

```properties
# Database Configuration
POSTGRES_DB=recipebook
POSTGRES_USER=recipebook_user
POSTGRES_PASSWORD=change_this_password
POSTGRES_PORT=5432

# Adminer (Database Web UI)
ADMINER_PORT=8082

# Backend (Quarkus)
BACKEND_PORT=8080

# Frontend (Vaadin)
FRONTEND_PORT=8081

# JWT Configuration
JWT_SECRET=generate-a-secure-random-key-here
JWT_EXPIRATION_HOURS=24

# Application
APP_ENV=development
```

### Backend Configuration (application.properties)

**File**: `backend/src/main/resources/application.properties`

| Property | Value | Purpose |
|----------|-------|---------|
| `quarkus.application.name` | `recipebook-backend` | App name |
| `quarkus.http.port` | `8080` | HTTP port |
| `quarkus.datasource.db-kind` | `postgresql` | Database type |
| `quarkus.datasource.username` | `${POSTGRES_USER:recipebook_user}` | DB username |
| `quarkus.datasource.password` | `${POSTGRES_PASSWORD:recipebook_secret}` | DB password |
| `quarkus.datasource.jdbc.url` | `jdbc:postgresql://localhost:${POSTGRES_PORT:5432}/${POSTGRES_DB:recipebook}` | JDBC URL |
| `quarkus.hibernate-orm.database.generation` | `update` | Auto-update schema |
| `quarkus.hibernate-orm.log.sql` | `true` | Log SQL queries |
| `quarkus.http.cors` | `true` | Enable CORS |
| `quarkus.http.cors.origins` | `http://localhost:8081,http://127.0.0.1:8081` | Allowed origins |
| `quarkus.http.cors.methods` | `GET,POST,PUT,DELETE,OPTIONS` | Allowed methods |
| `quarkus.http.cors.headers` | `accept,authorization,content-type,x-requested-with` | Allowed headers |
| `quarkus.http.cors.exposed-headers` | `location,info` | Exposed headers |
| `quarkus.http.cors.access-control-max-age` | `24H` | CORS cache duration |
| `quarkus.http.cors.access-control-allow-credentials` | `true` | Allow credentials |
| `mp.jwt.verify.issuer` | `recipebook` | JWT issuer |
| `mp.jwt.verify.publickey.location` | `publicKey.pem` | Public key for verification |
| `smallrye.jwt.sign.key.location` | `privateKey.pem` | Private key for signing |
| `smallrye.jwt.new-token.lifespan` | `86400` | Token lifespan (seconds = 24h) |
| `jwt.duration.hours` | `24` | Token duration (app-level config) |
| `quarkus.http.auth.permission.public.paths` | `/graphql,/graphql/*,/q/graphql-ui,/q/graphql-ui/*,/api/images/*` | Public paths |
| `quarkus.http.auth.permission.public.policy` | `permit` | Permit public access |
| `quarkus.smallrye-graphql.ui.always-include` | `true` | Always show GraphQL UI |
| `app.upload.dir` | `./uploads/images` | Image upload directory |
| `quarkus.http.limits.max-body-size` | `10M` | Max request body size |
| `quarkus.log.level` | `INFO` | Default log level |
| `quarkus.log.category."com.recipebook".level` | `DEBUG` | App log level |

### Frontend Configuration (application.properties)

**File**: `frontend/src/main/resources/application.properties`

| Property | Value | Purpose |
|----------|-------|---------|
| `server.port` | `${PORT:8081}` | Frontend port |
| `logging.level.org.atmosphere` | `warn` | Suppress Atmosphere logs |
| `vaadin.launch-browser` | `true` | Auto-open browser in dev mode |
| `vaadin.allowed-packages` | `com.vaadin,org.vaadin,com.flowingcode,com.recipebook` | Scanned packages |
| `api.backend.url` | `${BACKEND_URL:http://localhost:8080}` | Backend API URL |

### JWT Setup

- **Algorithm**: RSA256 (asymmetric)
- **Private key**: `backend/src/main/resources/privateKey.pem` (RSA 2048-bit, PKCS#8)
- **Public key**: `backend/src/main/resources/publicKey.pem` (X.509)
- **Issuer**: `recipebook`
- **Token lifespan**: 86400 seconds (24 hours)
- **Claims**: `sub` (userId as string), `upn` (username), `email`, `role`, `groups` (["USER"] or ["ADMIN"]), `iss`, `iat`, `exp`

### Image Upload Configuration

- **Upload directory**: `./uploads/images` (relative to backend working dir)
- **Max file size**: 5 MB (validated in ImageResource, body limit 10M in Quarkus)
- **Allowed types**: JPEG (`image/jpeg`), PNG (`image/png`), WebP (`image/webp`)
- **Filename**: UUID-generated to prevent conflicts
- **Serving**: GET `/api/images/{filename}` with 24h cache header

### How to Run (Development)

```bash
# 1. Start database
docker-compose up -d

# 2. Start backend (terminal 1)
cd backend
./mvnw quarkus:dev

# 3. Start frontend (terminal 2)
cd frontend
./mvnw spring-boot:run
# or simply: ./mvnw (default goal is spring-boot:run)
```

### Production Build

```bash
# Backend
cd backend
./mvnw package -DskipTests
# JAR at: backend/target/quarkus-app/

# Frontend
cd frontend
./mvnw -Pproduction package
# JAR at: frontend/target/frontend-1.0-SNAPSHOT.jar

# Docker (frontend)
docker build -t recipe-book-frontend:latest frontend/
```

### Backend Dockerfiles

Located at `backend/src/main/docker/`:

| File | Base Image | Description |
|------|-----------|-------------|
| `Dockerfile.jvm` | `ubi9/openjdk-21:1.23` | Standard JVM build (port 8080, user 185) |
| `Dockerfile.legacy-jar` | `ubi9/openjdk-21:1.23` | Legacy JAR format |
| `Dockerfile.native` | `ubi9/ubi-minimal:9.5` | GraalVM native executable |
| `Dockerfile.native-micro` | `quay.io/quarkus/quarkus-micro-image:2.0` | Minimal native image |

### Frontend Dockerfile

Multi-stage build:
1. **Build stage**: `eclipse-temurin:21-jdk` + Maven + `mvn -Pproduction package`
2. **Runtime stage**: `eclipse-temurin:21-jre-alpine` running the production JAR

### Vaadin Feature Flags

**File**: `frontend/src/main/resources/vaadin-featureflags.properties`
```properties
com.vaadin.experimental.themeComponentStyles = true
```

### Useful Commands

```bash
# Docker
docker-compose up -d              # Start PostgreSQL + Adminer
docker-compose down               # Stop services
docker-compose down -v            # Stop + delete data volume

# Database
docker exec -i recipebook-postgres pg_dump -U recipebook_user recipebook > backup.sql
docker exec -i recipebook-postgres psql -U recipebook_user recipebook < backup.sql

# Backend dev mode (live reload)
cd backend && ./mvnw quarkus:dev

# Frontend dev mode (live reload)
cd frontend && ./mvnw

# Run tests
cd tests && npx playwright test
```

---

## 4. DATABASE

### All Tables (9 Total)

#### Table: `user_` (mapped from `User` entity)

| Column | Type | Constraints | Default |
|--------|------|------------|---------|
| `id` | bigint | PK, auto-generated | sequence |
| `email` | varchar | UNIQUE, NOT NULL | — |
| `username` | varchar | UNIQUE, NOT NULL | — |
| `password_hash` | varchar | NOT NULL | — |
| `role` | varchar | NOT NULL | `'USER'` |
| `language` | varchar | NOT NULL | `'en'` |
| `created_at` | timestamp | — | `now()` |
| `last_login` | timestamp | — | null |

#### Table: `ingredient` (mapped from `Ingredient` entity)

| Column | Type | Constraints | Default |
|--------|------|------------|---------|
| `id` | bigint | PK, auto-generated | sequence |
| `name` | varchar | UNIQUE, NOT NULL | — |
| `category` | varchar(enum) | NOT NULL | — |
| `calories_per_100g` | double | — | 0.0 |
| `protein_per_100g` | double | — | 0.0 |
| `carbs_per_100g` | double | — | 0.0 |
| `fat_per_100g` | double | — | 0.0 |

#### Table: `recipe` (mapped from `Recipe` entity)

| Column | Type | Constraints | Default |
|--------|------|------------|---------|
| `id` | bigint | PK, auto-generated | sequence |
| `name` | varchar | NOT NULL | — |
| `description` | TEXT | — | — |
| `category` | varchar(enum) | NOT NULL | — |
| `difficulty` | varchar(enum) | NOT NULL | — |
| `prep_time` | int | — | 0 |
| `cook_time` | int | — | 0 |
| `servings` | int | — | 0 |
| `instructions` | TEXT | — | — |
| `image_url` | varchar | — | — |
| `owner_id` | bigint | FK → user_.id | — |
| `createdAt` | timestamp | — | `now()` |

#### Table: `recipeingredient` (mapped from `RecipeIngredient` entity)

| Column | Type | Constraints | Default |
|--------|------|------------|---------|
| `id` | bigint | PK, auto-generated | sequence |
| `recipe_id` | bigint | FK → recipe.id | — |
| `ingredient_id` | bigint | FK → ingredient.id | — |
| `quantity` | double | NOT NULL | — |
| `unit` | varchar(enum) | NOT NULL | — |

#### Table: `mealplan` (mapped from `MealPlan` entity)

| Column | Type | Constraints | Default |
|--------|------|------------|---------|
| `id` | bigint | PK, auto-generated | sequence |
| `user_id` | bigint | FK → user_.id | — |
| `meal_date` | date | NOT NULL | — |
| `meal_slot` | varchar(enum) | NOT NULL | — |
| `recipe_id` | bigint | FK → recipe.id | — |

**Unique constraint**: `(user_id, meal_date, meal_slot)` — one recipe per slot per day per user.

#### Table: `pantryitem` (mapped from `PantryItem` entity)

| Column | Type | Constraints | Default |
|--------|------|------------|---------|
| `id` | bigint | PK, auto-generated | sequence |
| `user_id` | bigint | FK → user_.id | — |
| `ingredient_id` | bigint | FK → ingredient.id | — |
| `quantity` | double | — | — |
| `unit` | varchar(enum) | — | — |
| `expiration_date` | date | — | — |

**Unique constraint**: `(user_id, ingredient_id)` — one pantry entry per ingredient per user.

#### Table: `shoppinglistitem` (mapped from `ShoppingListItem` entity)

| Column | Type | Constraints | Default |
|--------|------|------------|---------|
| `id` | bigint | PK, auto-generated | sequence |
| `user_id` | bigint | FK → user_.id | — |
| `ingredient_id` | bigint | FK → ingredient.id | — |
| `ingredient_name` | varchar | — | — |
| `quantity` | double | — | — |
| `unit` | varchar(enum) | — | — |
| `purchased` | boolean | NOT NULL | `false` |
| `week_start_date` | date | — | — |

#### Table: `usernutritiontarget` (mapped from `UserNutritionTarget` entity)

| Column | Type | Constraints | Default |
|--------|------|------------|---------|
| `id` | bigint | PK, auto-generated | sequence |
| `user_id` | bigint | FK → user_.id, UNIQUE, NOT NULL | — |
| `daily_calories` | int | — | `2000` |
| `daily_protein` | double | — | `150.0` |
| `daily_carbs` | double | — | `250.0` |
| `daily_fat` | double | — | `65.0` |

#### Table: `foodlog` (mapped from `FoodLog` entity)

| Column | Type | Constraints | Default |
|--------|------|------------|---------|
| `id` | bigint | PK, auto-generated | sequence |
| `user_id` | bigint | FK → user_.id, NOT NULL | — |
| `log_date` | date | NOT NULL | — |
| `meal_slot` | varchar(enum) | NOT NULL | — |
| `servings` | double | NOT NULL | `1.0` |
| `logged_at` | timestamp | — | — |
| `recipe_id` | bigint | FK → recipe.id | — |
| `ingredient_id` | bigint | FK → ingredient.id | — |
| `ingredient_quantity` | double | — | — |
| `custom_name` | varchar | — | — |
| `custom_calories` | double | — | — |
| `custom_protein` | double | — | — |
| `custom_carbs` | double | — | — |
| `custom_fat` | double | — | — |
| `source_meal_plan_id` | bigint | — | — |

### Entity Relationship Diagram

```
                    ┌──────────┐
                    │   User   │
                    └────┬─────┘
         ┌───────────┬───┼───────┬───────────┬──────────────┐
         │           │   │       │           │              │
         ▼           ▼   │       ▼           ▼              ▼
    ┌────────┐  ┌────────┴──┐ ┌──────────┐ ┌────────────┐ ┌──────────────────┐
    │ Recipe │  │ MealPlan   │ │ PantryItem│ │ShoppingList│ │UserNutritionTarget│
    └───┬────┘  └────────────┘ └──────────┘ │    Item    │ └──────────────────┘
        │                          │         └──────┬─────┘
        │                          │                │
        ▼                          ▼                ▼
  ┌───────────────┐          ┌────────────┐   ┌────────────┐
  │RecipeIngredient│─────────│ Ingredient │◄──┘            │
  └───────────────┘          └────────────┘                │
                                   ▲                       │
                                   │                       │
                             ┌─────┴────┐                  │
                             │ FoodLog  │──────────────────┘
                             └──────────┘
```

### Enums

**Unit**: `GRAMS`, `KILOGRAMS`, `MILLILITERS`, `LITERS`, `PIECES`, `TABLESPOONS`, `TEASPOONS`, `CUPS`

**RecipeCategory**: `BREAKFAST`, `LUNCH`, `DINNER`, `DESSERT`, `SNACK`, `OTHER`

**Difficulty**: `EASY`, `MEDIUM`, `HARD`

**IngredientCategory**: `DAIRY`, `MEAT`, `VEGETABLES`, `FRUITS`, `GRAINS`, `SPICES`, `OILS`, `BEVERAGES`, `OTHER`

**MealSlot**: `BREAKFAST`, `LUNCH`, `DINNER`, `SNACK`

**FoodSourceType** (DTO enum): `RECIPE`, `INGREDIENT`, `CUSTOM`

### Sequences & ID Generation

All entities extend `PanacheEntity` which provides a `public Long id` field with `@GeneratedValue` (default Panache strategy). Panache uses `allocationSize=50` for sequences by default.

### Data Seeder

`DataSeeder.java` runs at startup (`@Observes StartupEvent`) and creates:

**Default Admin User**:
- Username: `chef_admin`
- Email: `admin@recipebook.com`
- Password: `admin123` (bcrypt hashed)
- Role: `ADMIN`

**25 Seed Ingredients** (with nutritional data per 100g):
- Proteins: Chicken Breast, Ground Beef, Eggs, Salmon, Bacon
- Dairy: Butter, Milk, Parmesan, Mozzarella, Sour Cream
- Vegetables: Onion, Garlic, Tomato, Bell Pepper, Potato, Carrot, Cabbage
- Grains: Spaghetti, Rice, Flour, Bread
- Others: Olive Oil, Salt, Pepper, Sugar

**5 Seed Recipes**:
1. Ciorba de Burta (tripe soup) — LUNCH, MEDIUM
2. Sarmale (cabbage rolls) — DINNER, HARD
3. Pasta Carbonara — DINNER, EASY
4. Chicken Stir-Fry — LUNCH, EASY
5. Scrambled Eggs — BREAKFAST, EASY

---

## 5. BACKEND — ENTITIES

All entities are in `backend/src/main/java/com/recipebook/entity/` and extend `PanacheEntity`.

### User

```java
@Entity @Table(name = "user_")
public class User extends PanacheEntity {
    @Column(unique = true, nullable = false) public String email;
    @Column(unique = true, nullable = false) public String username;
    @Column(name = "password_hash", nullable = false) public String passwordHash;
    @Column(nullable = false) public String role = "USER";
    @Column(nullable = false) public String language = "en";
    @Column(name = "created_at") public LocalDateTime createdAt = LocalDateTime.now();
    @Column(name = "last_login") public LocalDateTime lastLogin;
}
```

### Ingredient

```java
@Entity
public class Ingredient extends PanacheEntity {
    @Column(unique = true, nullable = false) public String name;
    @Enumerated(EnumType.STRING) @Column(nullable = false) public IngredientCategory category;
    @Column(name = "calories_per_100g") public double caloriesPer100g;
    @Column(name = "protein_per_100g") public double proteinPer100g;
    @Column(name = "carbs_per_100g") public double carbsPer100g;
    @Column(name = "fat_per_100g") public double fatPer100g;
}
```

### Recipe

```java
@Entity
public class Recipe extends PanacheEntity {
    @Column(nullable = false) public String name;
    @Column(columnDefinition = "TEXT") public String description;
    @Enumerated(EnumType.STRING) @Column(nullable = false) public RecipeCategory category;
    @Enumerated(EnumType.STRING) @Column(nullable = false) public Difficulty difficulty;
    @Column(name = "prep_time") public int prepTime;
    @Column(name = "cook_time") public int cookTime;
    public int servings;
    @Column(columnDefinition = "TEXT") public String instructions;
    @Column(name = "image_url") public String imageUrl;
    @ManyToOne public User owner;
    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<RecipeIngredient> ingredients = new ArrayList<>();
    public LocalDateTime createdAt = LocalDateTime.now();
}
```

### RecipeIngredient

```java
@Entity
public class RecipeIngredient extends PanacheEntity {
    @ManyToOne public Recipe recipe;
    @ManyToOne public Ingredient ingredient;
    @Column(nullable = false) public double quantity;
    @Enumerated(EnumType.STRING) @Column(nullable = false) public Unit unit;
}
```

### MealPlan

```java
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "meal_date", "meal_slot"}))
public class MealPlan extends PanacheEntity {
    @ManyToOne @JoinColumn(name = "user_id") public User user;
    @Column(name = "meal_date", nullable = false) public LocalDate date;
    @Enumerated(EnumType.STRING) @Column(name = "meal_slot", nullable = false) public MealSlot mealSlot;
    @ManyToOne public Recipe recipe;
}
```

### PantryItem

```java
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "ingredient_id"}))
public class PantryItem extends PanacheEntity {
    @ManyToOne public User user;
    @ManyToOne public Ingredient ingredient;
    public double quantity;
    @Enumerated(EnumType.STRING) public Unit unit;
    @Column(name = "expiration_date") public LocalDate expirationDate;
}
```

### ShoppingListItem

```java
@Entity
public class ShoppingListItem extends PanacheEntity {
    @ManyToOne public User user;
    @ManyToOne public Ingredient ingredient;
    @Column(name = "ingredient_name") public String ingredientName;
    public double quantity;
    @Enumerated(EnumType.STRING) public Unit unit;
    @Column(nullable = false) public boolean purchased = false;
    @Column(name = "week_start_date") public LocalDate weekStartDate;
}
```

### UserNutritionTarget

```java
@Entity
public class UserNutritionTarget extends PanacheEntity {
    @OneToOne @JoinColumn(name = "user_id", unique = true, nullable = false) public User user;
    @Column(name = "daily_calories") public int dailyCalories = 2000;
    @Column(name = "daily_protein") public double dailyProtein = 150.0;
    @Column(name = "daily_carbs") public double dailyCarbs = 250.0;
    @Column(name = "daily_fat") public double dailyFat = 65.0;
}
```

### FoodLog

```java
@Entity
public class FoodLog extends PanacheEntity {
    @ManyToOne @JoinColumn(name = "user_id", nullable = false) public User user;
    @Column(name = "log_date", nullable = false) public LocalDate date;
    @Enumerated(EnumType.STRING) @Column(name = "meal_slot", nullable = false) public MealSlot mealSlot;
    @Column(nullable = false) public double servings = 1.0;
    @Column(name = "logged_at") public LocalDateTime loggedAt;
    @ManyToOne public Recipe recipe;
    @ManyToOne public Ingredient ingredient;
    @Column(name = "ingredient_quantity") public Double ingredientQuantity;
    @Column(name = "custom_name") public String customName;
    @Column(name = "custom_calories") public Double customCalories;
    @Column(name = "custom_protein") public Double customProtein;
    @Column(name = "custom_carbs") public Double customCarbs;
    @Column(name = "custom_fat") public Double customFat;
    @Column(name = "source_meal_plan_id") public Long sourceMealPlanId;
}
```

---

## 6. BACKEND — GRAPHQL API (Complete Reference)

All GraphQL resolvers are in `backend/src/main/java/com/recipebook/graphql/`.

### Auth (AuthGraphQL.java)

| Type | Operation | Signature | Auth Required | Notes |
|------|-----------|-----------|:---:|-------|
| Mutation | `register` | `(email: String!, username: String!, password: String!, language: String): AuthResponse!` | No | Creates user + returns JWT |
| Mutation | `login` | `(email: String!, password: String!): AuthResponse!` | No | Verifies bcrypt, returns JWT |
| Query | `me` | `(): User!` | Yes | Returns current user from JWT subject |
| Query | `users` | `(): [User]!` | Yes | Lists all users |
| Mutation | `deleteUser` | `(id: Long!): boolean` | Yes | Deletes user by ID |

**AuthResponse**: `{ token, type("Bearer"), userId, username, email, role }`

### Recipes (RecipeGraphQL.java)

| Type | Operation | Signature | Auth Required | Notes |
|------|-----------|-----------|:---:|-------|
| Query | `recipes` | `(category: RecipeCategory, difficulty: Difficulty, search: String, ingredientIds: [Long]): [RecipeResponse]!` | No | Filterable list |
| Query | `recipe` | `(id: Long!): RecipeResponse!` | No | Single recipe with full details |
| Mutation | `createRecipe` | `(input: RecipeInput!): RecipeResponse!` | Yes | Sets owner from JWT |
| Mutation | `updateRecipe` | `(id: Long!, input: RecipeInput!): RecipeResponse!` | Yes | Replaces ingredients |
| Mutation | `deleteRecipe` | `(id: Long!): boolean` | Yes | Deletes recipe |

**RecipeInput**: `{ name!, description, category!, difficulty!, prepTime, cookTime, servings, instructions, imageUrl, ingredients: [RecipeIngredientInput] }`

**RecipeIngredientInput**: `{ ingredientId!, quantity!, unit! }`

### Ingredients (IngredientGraphQL.java)

| Type | Operation | Signature | Auth Required | Notes |
|------|-----------|-----------|:---:|-------|
| Query | `ingredients` | `(search: String, category: IngredientCategory): [Ingredient]!` | No | Filterable list |
| Query | `ingredient` | `(id: Long!): Ingredient!` | No | Single ingredient |
| Mutation | `createIngredient` | `(input: IngredientInput!): Ingredient!` | Yes | — |
| Mutation | `updateIngredient` | `(id: Long!, input: IngredientInput!): Ingredient!` | Yes | — |
| Mutation | `deleteIngredient` | `(id: Long!): boolean` | Yes | — |

**IngredientInput**: `{ name!, category!, caloriesPer100g, proteinPer100g, carbsPer100g, fatPer100g }`

### Meal Plan (MealPlanGraphQL.java)

| Type | Operation | Signature | Auth Required | Notes |
|------|-----------|-----------|:---:|-------|
| Query | `mealPlansByDateRange` | `(startDate: LocalDate!, endDate: LocalDate!): [MealPlanResponse]!` | Yes | Date range filter |
| Query | `weeklyMealPlan` | `(weekStart: LocalDate!): WeeklyMealPlanResponse!` | Yes | Full week with macros |
| Query | `dailyMacroSummary` | `(date: LocalDate!): MacroInfo!` | Yes | Single day macros |
| Mutation | `assignMealPlan` | `(input: MealPlanInput!): MealPlanResponse!` | Yes | Upserts (date+slot unique) |
| Mutation | `removeMealPlan` | `(date: LocalDate!, mealSlot: MealSlot!): boolean` | Yes | Removes meal from slot |

**MealPlanInput**: `{ date!, mealSlot!, recipeId! }`

### Pantry (PantryGraphQL.java)

| Type | Operation | Signature | Auth Required | Notes |
|------|-----------|-----------|:---:|-------|
| Query | `pantryItems` | `(): [PantryItemResponse]!` | Yes | All user's pantry items |
| Query | `expiringPantryItems` | `(withinDays: int = 3): [PantryItemResponse]!` | Yes | Items expiring soon |
| Mutation | `addPantryItem` | `(input: PantryItemInput!): PantryItemResponse!` | Yes | Auto-merges if ingredient exists |
| Mutation | `updatePantryItem` | `(id: Long!, input: PantryItemUpdateInput!): PantryItemResponse!` | Yes | Partial update |
| Mutation | `removePantryItem` | `(id: Long!): boolean` | Yes | Deletes item |

**PantryItemInput**: `{ ingredientId!, quantity!, unit!, expirationDate }`

**PantryItemUpdateInput**: `{ quantity, unit, expirationDate }`

### Shopping List (ShoppingListGraphQL.java)

| Type | Operation | Signature | Auth Required | Notes |
|------|-----------|-----------|:---:|-------|
| Query | `shoppingList` | `(weekStart: LocalDate!): ShoppingListResponse!` | Yes | Get list for week |
| Mutation | `generateShoppingList` | `(weekStart: LocalDate!): ShoppingListResponse!` | Yes | Auto-generate from meal plan |
| Mutation | `addShoppingListItem` | `(input: ShoppingListItemInput!): ShoppingListItemResponse!` | Yes | Manual add |
| Mutation | `toggleShoppingListItem` | `(id: Long!): ShoppingListItemResponse!` | Yes | Toggle purchased |
| Mutation | `removeShoppingListItem` | `(id: Long!): boolean` | Yes | Delete item |
| Mutation | `clearShoppingList` | `(weekStart: LocalDate!): boolean` | Yes | Clear all for week |

**ShoppingListItemInput**: `{ ingredientId!, quantity!, unit!, weekStart! }`

### Food Log (FoodLogGraphQL.java)

| Type | Operation | Signature | Auth Required | Notes |
|------|-----------|-----------|:---:|-------|
| Query | `dailyFoodLog` | `(date: LocalDate!): DailyFoodLogResponse!` | Yes | Full day with macros + targets + planned meals |
| Mutation | `logFood` | `(input: FoodLogInput!): FoodLogResponse!` | Yes | Multi-source logging |
| Mutation | `updateFoodLog` | `(id: BigInteger!, servings: Float!): FoodLogResponse!` | Yes | Update servings |
| Mutation | `removeFoodLog` | `(id: BigInteger!): boolean` | Yes | Delete entry |

**FoodLogInput**: `{ date!, mealSlot!, recipeId, ingredientId, ingredientQuantity, customName, customCalories, customProtein, customCarbs, customFat, mealPlanId, servings }`

Three log sources:
1. **Recipe**: Set `recipeId` + `servings` — macros calculated from recipe per-serving × servings
2. **Ingredient**: Set `ingredientId` + `ingredientQuantity` — macros from ingredient per-100g × quantity
3. **Custom**: Set `customName` + `customCalories/Protein/Carbs/Fat` — direct manual entry

### Nutrition Target (NutritionTargetGraphQL.java)

| Type | Operation | Signature | Auth Required | Notes |
|------|-----------|-----------|:---:|-------|
| Query | `nutritionTarget` | `(): UserNutritionTargetResponse!` | Yes | Get current targets (creates defaults if none) |
| Mutation | `updateNutritionTarget` | `(calories: int!, protein: double!, carbs: double!, fat: double!): UserNutritionTargetResponse!` | Yes | Update all targets |

### Recommendations (RecommendationGraphQL.java)

| Type | Operation | Signature | Auth Required | Notes |
|------|-----------|-----------|:---:|-------|
| Query | `recipeRecommendations` | `(filter: RecommendationFilterInput): [RecipeRecommendationResponse]!` | Yes | Sorted by match % |
| Mutation | `addMissingToShoppingList` | `(recipeId: Long!, weekStart: LocalDate!): boolean` | Yes | Adds missing ingredients |

**RecommendationFilterInput**: `{ category, difficulty, maxMissingIngredients }`

### Recipe Import (RecipeImportGraphQL.java)

| Type | Operation | Signature | Auth Required | Notes |
|------|-----------|-----------|:---:|-------|
| Mutation | `importRecipesFromFile` | `(filePath: String = "recipes_import.json"): ImportResultDTO!` | Yes | Imports from JSON file |
| Mutation | `extractPdfImages` | `(filePath: String = "carte.pdf"): String!` | Yes | Extracts images from PDF |

**ImportResultDTO**: `{ imported: int, failed: int, errors: [String] }`

---

## 7. BACKEND — REST ENDPOINTS

### ImageResource (`/api/images`)

**File**: `backend/src/main/java/com/recipebook/rest/ImageResource.java`

#### POST `/api/images` — Upload Image

- **Consumes**: `multipart/form-data`
- **Parameter**: `file` (InputStream + metadata)
- **Validation**:
  - Allowed content types: `image/jpeg`, `image/png`, `image/webp`
  - Max file size: 5 MB
  - Rejects if content type is invalid
- **Behavior**:
  - Generates UUID filename with original extension
  - Creates upload directory if not exists
  - Saves file to `{app.upload.dir}/{uuid}.{ext}`
  - Returns JSON: `{ "url": "/api/images/{uuid}.{ext}" }`
- **Response**: `201 Created` or `400 Bad Request`

#### GET `/api/images/{filename}` — Serve Image

- **Path parameter**: `filename`
- **Validation**:
  - Sanitizes filename (removes path separators to prevent traversal)
  - Checks file exists
- **Behavior**:
  - Detects content type from file extension
  - Returns file bytes with `Cache-Control: max-age=86400` (24 hours)
- **Response**: `200 OK` or `404 Not Found`

---

## 8. BACKEND — SERVICES (Business Logic)

All services are in `backend/src/main/java/com/recipebook/service/`.

### PasswordService

- `hashPassword(String plainPassword)`: Returns bcrypt hash using `BCrypt.hashpw()`
- `verifyPassword(String plainPassword, String hashedPassword)`: Returns boolean via `BCrypt.checkpw()`

### TokenService

- `generateToken(User user)`: Creates JWT with:
  - Issuer: `recipebook`
  - Subject: `user.id.toString()`
  - UPN: `user.username`
  - Claims: email, role, userId
  - Groups: `Set.of(user.role)`
  - Duration: configurable via `jwt.duration.hours` (default 24)
  - Signed with RSA private key

### MacroCalculationService

**Unit Conversion Table** (internal map to grams):

| Unit | Grams Equivalent |
|------|-----------------|
| GRAMS | 1.0 |
| KILOGRAMS | 1000.0 |
| MILLILITERS | 1.0 |
| LITERS | 1000.0 |
| TABLESPOONS | 15.0 |
| TEASPOONS | 5.0 |
| CUPS | 240.0 |
| PIECES | 100.0 |

- `toGrams(double quantity, Unit unit)`: Converts any unit to grams using the table
- `toResponse(Recipe recipe)`: Builds a full `RecipeResponse` with:
  - Per-ingredient macro calculation: `(quantity_in_grams / 100) × nutrient_per_100g`
  - Total recipe macros: sum of all ingredient macros
  - Per-serving macros: total / servings (if servings > 0)

### PantryService

- `getPantryItems(Long userId)`: Returns all pantry items for user, converts to response
- `getExpiringItems(Long userId, int withinDays)`: Finds items with `expirationDate <= today + withinDays`
- `addPantryItem(Long userId, PantryItemInput input)`: **Auto-merge logic** — if user already has that ingredient, converts both quantities to grams, sums them, and updates. Otherwise creates new entry.
- `updatePantryItem(Long userId, Long id, PantryItemUpdateInput input)`: Partial update of quantity/unit/expiration
- `removePantryItem(Long userId, Long id)`: Deletes pantry item
- `toResponse(PantryItem)`: Converts to DTO with `expiringSoon` flag (true if expiration within 3 days)

### MealPlanService

- `getMealPlansByDateRange(Long userId, LocalDate start, LocalDate end)`: Queries meal plans filtered by user and date range
- `getWeeklyMealPlan(Long userId, LocalDate weekStart)`: Builds `WeeklyMealPlanResponse` with:
  - 7 `DailyMealPlanResponse` objects (one per day)
  - Each day has meals list + daily macro total
  - Weekly total macros + average daily macros
- `getDailyMacroSummary(Long userId, LocalDate date)`: Calculates total macros for one day
- `assignMealPlan(Long userId, MealPlanInput input)`: Upserts — finds existing entry for (user, date, slot) or creates new. Links recipe.
- `removeMealPlan(Long userId, LocalDate date, MealSlot slot)`: Deletes the specific meal plan entry

### ShoppingListService

- `getShoppingList(Long userId, LocalDate weekStart)`: Returns existing shopping list for that week
- `generateShoppingList(Long userId, LocalDate weekStart)`: **Core algorithm**:
  1. Clears existing items for that week
  2. Gets weekly meal plan (7 days from weekStart)
  3. Aggregates all recipe ingredients (summing quantities in grams for same ingredient)
  4. Gets user's pantry items
  5. For each aggregated ingredient: `needed = aggregated - pantry` (converted to grams)
  6. If needed > 0, creates shopping list item with that deficit quantity
  7. Returns the generated list with progress stats
- `addShoppingListItem(Long userId, ShoppingListItemInput input)`: Manual item add
- `toggleShoppingListItem(Long userId, Long id)`: Flips `purchased` boolean
- `removeShoppingListItem(Long userId, Long id)`: Deletes item
- `clearShoppingList(Long userId, LocalDate weekStart)`: Deletes all items for that week
- `buildResponse(...)`: Calculates `totalItems`, `purchasedItems`, `progressPercent`

### FoodLogService

- `getDailyFoodLog(Long userId, LocalDate date)`: Returns `DailyFoodLogResponse` with:
  - All food log entries for that date with actual macros
  - Total actual macros (sum of all entries)
  - User's nutrition targets
  - Planned meal statuses (which planned meals are logged vs unlogged)
- `logFood(Long userId, FoodLogInput input)`: Creates food log entry from one of 3 sources:
  - **Recipe**: Looks up recipe, sets `recipe` field
  - **Ingredient**: Looks up ingredient, sets `ingredient` + `ingredientQuantity`
  - **Custom**: Sets `customName` + custom macro fields
  - Sets `sourceMealPlanId` if logging from a planned meal
- `updateFoodLog(Long userId, Long id, double servings)`: Updates servings count
- `removeFoodLog(Long userId, Long id)`: Deletes food log entry
- `toResponse(FoodLog)`: Calculates `actualMacros` based on source:
  - Recipe: per-serving macros × servings
  - Ingredient: (quantity_in_grams / 100) × nutrient_per_100g × servings
  - Custom: custom values × servings

### NutritionTargetService

- `getTarget(Long userId)`: Returns targets, creates defaults if none exist
- `updateTarget(Long userId, int cal, double protein, double carbs, double fat)`: Updates all 4 targets
- `createDefaultTarget(Long userId)`: Creates entry with 2000 cal, 150g protein, 250g carbs, 65g fat

### RecommendationService

- `getRecommendations(Long userId, RecommendationFilterInput filter)`:
  1. Gets all user's pantry items (mapped to ingredientId → quantity in grams)
  2. Gets all recipes (optionally filtered by category/difficulty)
  3. Analyzes each recipe against pantry
  4. Filters by `maxMissingIngredients` if set
  5. Sorts by `matchPercent` descending
- `getMissingIngredients(Long userId, Long recipeId)`: Returns list of missing ingredients for a specific recipe
- `analyzeRecipe(Recipe, Map<Long,Double> pantryMap)`: Private method that:
  - For each recipe ingredient, compares required quantity (in grams) vs pantry quantity
  - Calculates `matchPercent = matchedCount / totalIngredients × 100`
  - Builds list of `MissingIngredientResponse` with required/available/needed quantities

### RecipeImportService

- `importFromJson(String filePath, Long userId)`: Reads JSON file containing recipe array:
  - Skips recipes whose name already exists in DB
  - Creates ingredients (using `findOrCreateIngredient`)
  - Creates recipe with all ingredients linked
  - Returns `ImportResult(imported, failed, errors[])`
- `findOrCreateIngredient(String name)`: Looks up by name, creates if not found using `RomanianNutritionData.lookup()` for macro values

### RomanianNutritionData

- Static `Map<String, NutritionEntry>` with 180+ ingredients
- Each entry: `{ calories, protein, carbs, fat, category }` per 100g
- Values are based on USDA nutritional data adapted for Romanian cuisine
- `lookup(String name)`: Case-insensitive matching with partial match fallback
- `getAll()`: Returns entire map

### PdfImageExtractorService

- `extractAndMatch(String pdfPath, String outputDir)`: Processes PDF cookbook:
  1. Opens PDF with PDFBox
  2. Iterates each page
  3. Extracts the largest image from each page (minimum 100×100 pixels)
  4. Saves as JPEG to output directory
  5. Extracts page text
  6. Matches text against existing recipe names (normalized comparison)
  7. Returns extraction results with recipe-to-image mappings
- `normalize(String text)`: Removes Romanian diacritics (ă→a, î→i, ș→s, ț→t), lowercases, strips non-alphanumeric
- `findMatchingRecipe(String pageText, List<Recipe> recipes)`: Longest-match-wins strategy

### DataSeeder

- `@Observes StartupEvent` — runs once at application boot
- Checks if data exists (by checking User count) before seeding
- Creates admin user, 25 ingredients, 5 recipes with ingredient links
- Uses `@Transactional`

---

## 9. BACKEND — DTOs (All Response Types)

All DTOs are in `backend/src/main/java/com/recipebook/dto/`.

### AuthResponse
```
token: String
type: String = "Bearer"
userId: Long
username: String
email: String
role: String
```

### LoginRequest
```
@NotBlank email: String
@NotBlank password: String
```

### RegisterRequest
```
@NotBlank @Email email: String
@NotBlank @Size(min=3, max=50) username: String
@NotBlank @Size(min=6) password: String
language: String = "en"
```

### ErrorMessage
```
message: String
```

### MacroInfo
```
calories: double
protein: double
carbs: double
fat: double
```

### RecipeResponse
```
id: Long
name: String
description: String
category: RecipeCategory
difficulty: Difficulty
prepTime: int
cookTime: int
servings: int
instructions: String
imageUrl: String
ownerId: Long
ownerUsername: String
ingredients: List<RecipeIngredientResponse>
totalMacros: MacroInfo
perServingMacros: MacroInfo
createdAt: LocalDateTime
```

### RecipeIngredientResponse
```
ingredientId: Long
ingredientName: String
ingredientCategory: IngredientCategory
quantity: double
unit: Unit
macros: MacroInfo
```

### RecipeRequest
```
@NotBlank name: String
description: String
@NotNull category: RecipeCategory
@NotNull difficulty: Difficulty
@Positive prepTime: int
@Positive cookTime: int
@Positive servings: int
instructions: String
imageUrl: String
@Valid ingredients: List<RecipeIngredientRequest>
```

### RecipeIngredientRequest
```
@NotNull ingredientId: Long
@NotNull @Positive quantity: Double
@NotNull unit: Unit
```

### PantryItemResponse
```
id: Long
ingredientId: Long
ingredientName: String
ingredientCategory: IngredientCategory
quantity: double
unit: Unit
expirationDate: LocalDate
expiringSoon: boolean
```

### MealPlanResponse
```
id: Long
date: LocalDate
mealSlot: MealSlot
recipe: RecipeResponse
```

### DailyMealPlanResponse
```
date: LocalDate
meals: List<MealPlanResponse>
totalMacros: MacroInfo
```

### WeeklyMealPlanResponse
```
weekStart: LocalDate
weekEnd: LocalDate
days: List<DailyMealPlanResponse>
totalMacros: MacroInfo
averageDailyMacros: MacroInfo
```

### ShoppingListItemResponse
```
id: Long
ingredientId: Long
ingredientName: String
ingredientCategory: IngredientCategory
quantity: double
unit: Unit
purchased: boolean
```

### ShoppingListResponse
```
weekStart: LocalDate
items: List<ShoppingListItemResponse>
totalItems: int
purchasedItems: int
progressPercent: double
```

### MissingIngredientResponse
```
ingredientId: Long
ingredientName: String
ingredientCategory: IngredientCategory
requiredQuantity: double
pantryQuantity: double
neededQuantity: double
unit: Unit
```

### RecipeRecommendationResponse
```
recipe: RecipeResponse
matchPercent: double
totalIngredients: int
matchedIngredients: int
missingCount: int
missingIngredients: List<MissingIngredientResponse>
```

### FoodLogResponse
```
id: Long
date: LocalDate
mealSlot: MealSlot
servings: double
loggedAt: LocalDateTime
sourceType: FoodSourceType
recipe: RecipeResponse
ingredientName: String
ingredientQuantity: Double
customName: String
actualMacros: MacroInfo
```

### DailyFoodLogResponse
```
date: LocalDate
entries: List<FoodLogResponse>
totalActualMacros: MacroInfo
targets: UserNutritionTargetResponse
plannedMeals: List<PlannedMealStatus>
```

### PlannedMealStatus
```
mealPlanId: Long
mealSlot: MealSlot
recipe: RecipeResponse
logged: boolean
foodLogId: Long
```

### UserNutritionTargetResponse
```
calories: double
protein: double
carbs: double
fat: double
```

### FoodSourceType (Enum)
```
RECIPE, INGREDIENT, CUSTOM
```

---

## 10. FRONTEND — SERVICES

### ApiClient

**File**: `frontend/src/main/java/com/recipebook/service/ApiClient.java`

- **Purpose**: GraphQL HTTP client that communicates with the backend
- **Injection**: `@Value("${api.backend.url}")` for backend URL
- **Uses**: Spring `WebClient` for HTTP calls
- **JWT Handling**: Reads token from `VaadinSession` attribute `"jwt_token"`, adds as `Authorization: Bearer` header
- **Core method**: `execute(String query, Map<String, Object> variables)` → sends POST to `/graphql` with `{ query, variables }` body
- **Error parsing**: Extracts `errors[0].message` from GraphQL error responses
- **Response parsing**: Uses Jackson `ObjectMapper` to deserialize `data.<fieldName>` from response JSON

### AuthService

**File**: `frontend/src/main/java/com/recipebook/service/AuthService.java`

- `login(String email, String password)`: Calls `login` mutation, stores token + user info in `VaadinSession`
- `register(String email, String username, String password, String language)`: Calls `register` mutation, stores session data
- `logout()`: Clears session, invalidates `VaadinSession`
- `isLoggedIn()`: Checks if `jwt_token` exists in session
- `getCurrentUser()`: Returns `UserInfo` from session attributes
- `getToken()`: Returns JWT token string from session
- **Session attributes stored**: `jwt_token`, `user_id`, `username`, `email`, `role`

---

## 11. FRONTEND — VIEWS & COMPONENTS

All frontend views are in `frontend/src/main/java/com/recipebook/`.

### MainLayout (views/MainLayout.java)

- **Route**: Used as layout for all views via `@Route(layout = MainLayout.class)`
- **Components**: `AppLayout` with drawer navigation
- **Navigation items**: Dashboard, Recipes, Ingredients, Meal Plan, Pantry, Shopping List, Food Log, Recommendations
- **Header**: Shows username + logout button when authenticated
- **Auth guard**: `beforeEnter()` checks `AuthService.isLoggedIn()`, redirects to `/login` if not authenticated (except login/register routes)

### DashboardView (views/DashboardView.java)

- **Route**: `/dashboard`
- **Stats cards**: Recipe count, ingredient count, category count, pantry item count
- **Nutrition summary**: Today's macros vs targets (from food log + nutrition targets)
- **Recent recipes**: Last few recipes added
- **Quick action buttons**: Add Recipe, View Meal Plan, Check Pantry

### LoginView (views/LoginView.java)

- **Route**: `/login`
- **Form fields**: Email, Password
- **Actions**: Login button, link to Register
- **On success**: Navigates to `/dashboard`
- **Standalone**: No MainLayout (no sidebar)

### RegisterView (views/RegisterView.java)

- **Route**: `/register`
- **Form fields**: Email, Username, Password, Language (ComboBox: English/Romanian)
- **Actions**: Register button, link to Login
- **On success**: Navigates to `/dashboard`
- **Standalone**: No MainLayout

### RecipeListView (recipe/RecipeListView.java)

- **Route**: `/recipes`
- **Layout**: Card grid displaying recipes
- **Filters**: Category dropdown, Difficulty dropdown, Search text field, Ingredient multi-select
- **Card content**: Image (or placeholder), name, category badge, difficulty badge, macro badges, prep+cook time
- **Actions**: Click card → navigate to detail, "Add Recipe" button opens form dialog
- **Image display**: Shows recipe image from `/api/images/{url}` or placeholder icon

### RecipeDetailView (recipe/RecipeDetailView.java)

- **Route**: `/recipes/{id}`
- **Sections**: Image, title, owner, description, info chips (category, difficulty, times, servings), macro bar, ingredients grid, instructions
- **Macro bar**: 4 colored badges (calories/protein/carbs/fat) for total and per-serving
- **Ingredients grid**: Columns for name, category, quantity+unit, individual macros
- **Actions**: Edit button (opens form dialog), Delete button (with confirmation), Back button

### RecipeFormDialog (recipe/RecipeFormDialog.java)

- **Type**: Vaadin `Dialog`
- **Form sections** (styled with `.form-section` CSS):
  1. **Basic Info** (primary): Name, Description (textarea), Category (combo), Difficulty (combo)
  2. **Timing & Servings** (success): Prep time, Cook time, Servings (all integer fields)
  3. **Image** (warning): File upload with preview, or manual URL input
  4. **Instructions** (primary): Textarea
  5. **Ingredients** (success): Dynamic list — each row has ComboBox (ingredient), NumberField (quantity), ComboBox (unit), remove button. "Add Ingredient" button.
- **Image upload**: Uploads via multipart POST to `/api/images`, shows preview
- **Save**: Calls createRecipe or updateRecipe mutation

### IngredientListView (ingredient/IngredientListView.java)

- **Route**: `/ingredients`
- **Layout**: Vaadin Grid (data table)
- **Columns**: Name, Category (colored badge), Calories/100g, Protein/100g, Carbs/100g, Fat/100g, Actions
- **Filters**: Search field, Category dropdown
- **Actions**: Edit (opens form dialog), Delete, "Add Ingredient" button

### IngredientFormDialog (ingredient/IngredientFormDialog.java)

- **Form sections**:
  1. **Basic Info**: Name, Category (combo)
  2. **Nutrition per 100g**: Calories, Protein, Carbs, Fat (number fields)

### MealPlanView (mealplan/MealPlanView.java)

- **Route**: `/meal-plan`
- **Layout**: Weekly calendar grid (CSS Grid: 8 columns × 5 rows)
  - Column 1: Slot labels (Breakfast/Lunch/Dinner/Snack + Macros)
  - Columns 2-8: Days (Mon-Sun)
- **Week navigation**: Previous/Next week buttons, "Today" button, week date range display
- **Filled cells**: Recipe name, calorie badge, remove button (on hover), colored left border per slot
- **Empty cells**: Dashed border, "+" icon, click to open recipe picker
- **Daily macros row**: Shows total calories + P/C/F pills for each day
- **Weekly summary bar**: Average daily macros + total weekly macros
- **Recipe picker dialog**: Lists all recipes with search, click to assign

### PantryView (pantry/PantryView.java)

- **Route**: `/pantry`
- **Expiring banner**: Warning bar showing count of items expiring within 3 days
- **Grid columns**: Ingredient name, Category, Quantity + Unit, Expiration date (red if expiring), Actions
- **Actions**: Edit (opens form dialog), Delete, "Add Item" button

### PantryFormDialog (pantry/PantryFormDialog.java)

- **Form fields**: Ingredient (ComboBox), Quantity (number), Unit (ComboBox), Expiration date (DatePicker)

### ShoppingListView (shopping/ShoppingListView.java)

- **Route**: `/shopping-list`
- **Week selector**: Previous/Next week buttons
- **Actions**: Generate from Meal Plan button, Clear List button, Share (QR) button
- **Progress bar**: Shows purchased/total items percentage
- **Item list**: Grouped by ingredient category, each item has checkbox (purchased toggle), name, quantity+unit, delete button
- **QR sharing**: Generates QR code via ZXing, stores list in `SharedListStore`, detects LAN IP for URL, shows QR dialog with copy fallback

### FoodLogView (foodlog/FoodLogView.java)

- **Route**: `/food-log`
- **Date selector**: Previous/Next day buttons, "Today" button
- **Nutrition summary**: 4 progress bars (calories, protein, carbs, fat) showing actual vs target
- **Planned meals section**: Shows meals from meal plan with checkbox to "log as eaten", "Log All" button
- **Logged entries**: Grouped by meal slot, shows source (recipe/ingredient/custom), macros, servings editor, delete button
- **Log dialog**: 3 tabs:
  1. **Recipe tab**: Search recipes, select, set servings
  2. **Ingredient tab**: Search ingredients, select, set quantity + unit
  3. **Custom tab**: Manual name + calorie/protein/carbs/fat input
- **Meal slot selector**: Dropdown to choose which slot to log to

### RecommendationView (recommendation/RecommendationView.java)

- **Route**: `/recommendations`
- **Filters**: Category dropdown, Difficulty dropdown, Max missing ingredients slider
- **Card layout**: Each card shows:
  - Recipe name + image
  - Match percentage progress bar (green gradient)
  - "X of Y ingredients available" text
  - Collapsible missing ingredients section (shows name, needed quantity, pantry quantity)
  - "Add Missing to Shopping List" button
  - Click card → navigate to recipe detail

### MacroBar (ui/MacroBar.java)

- **Type**: Reusable component
- **Renders**: 4 colored badges in a horizontal layout:
  - Calories (red/error color)
  - Protein (bronze/primary color)
  - Carbs (green/success color)
  - Fat (gold/warning color)
- **Each badge**: Label on top, formatted value below (e.g., "1234 kcal", "45.2g")

---

## 12. FRONTEND — STYLING & THEME

### Theme Configuration

**File**: `frontend/src/main/frontend/themes/recipe-book/theme.json`
```json
{
  "lumoImports": ["typography", "color", "spacing", "badge", "utility"]
}
```

### Color Palette (CSS Custom Properties)

| Variable | Value | Usage |
|----------|-------|-------|
| `--lumo-base-color` | `#1a1f2b` | Dark navy background |
| `--lumo-primary-color` | `#c08050` | Bronze/copper primary |
| `--lumo-primary-text-color` | `#c08050` | Primary text |
| `--lumo-success-color` | `#5a9e7e` | Green accent |
| `--lumo-error-color` | `#c75050` | Red accent |
| `--lumo-body-text-color` | `#d8dce4` | Light gray text |
| `--lumo-secondary-text-color` | `#8890a0` | Muted text |
| `--lumo-tertiary-text-color` | `#687080` | Even more muted |
| `--recipe-warning-color` | `#c0903c` | Gold/amber warning |
| `--macro-protein` | `#c08050` | Protein badge color |
| `--macro-carbs` | `#5a9e7e` | Carbs badge color |
| `--macro-fat` | `#c0903c` | Fat badge color |

### Macro Color Coding

| Macro | Color | CSS Variable | Hex |
|-------|-------|-------------|-----|
| Calories | Red | `--lumo-error-color` | `#c75050` |
| Protein | Bronze | `--lumo-primary-color` | `#c08050` |
| Carbs | Green | `--lumo-success-color` | `#5a9e7e` |
| Fat | Gold | `--recipe-warning-color` | `#c0903c` |

### Card & Surface Colors

| Element | Background |
|---------|-----------|
| Page background | `#1a1f2b` |
| Card/surface | `#232838` |
| Form section | `rgba(216,220,228,0.04)` |
| Hover state | `rgba(216,220,228,0.06)` |

### Meal Slot Colors

| Slot | Color |
|------|-------|
| Breakfast | `#c0903c` (gold) |
| Lunch | `#5a9e7e` (green) |
| Dinner | `#c08050` (bronze) |
| Snack | `#8890a0` (gray) |

### Font Choices

- **Body text**: Lumo default (system font stack)
- **Labels/Tags**: `'JetBrains Mono', monospace` (imported from Google Fonts)
- **Section labels**: JetBrains Mono, 12px, uppercase, letter-spacing 1.2px

### Component Override Files

Located in `themes/recipe-book/components/`:

| File | Component | Key Overrides |
|------|-----------|--------------|
| `vaadin-app-layout.css` | AppLayout | Host styling |
| `vaadin-combo-box.css` | ComboBox | Input field, label dark styling |
| `vaadin-dialog-overlay.css` | Dialog | Dark container, glow effects, custom scrollbar |
| `vaadin-text-field.css` | TextField | Dark input, transparent border, bronze focus |
| `vaadin-text-area.css` | TextArea | Same as TextField |
| `vaadin-integer-field.css` | IntegerField | Same pattern |
| `vaadin-number-field.css` | NumberField | Same pattern |

All component overrides follow the pattern: dark background (`#1c2130`), transparent borders, bronze focus states (`rgba(192,128,80,...)`).

---

## 13. PATTERNS & CONVENTIONS

### Backend Patterns

1. **Entity Pattern**: Extend `PanacheEntity`, public fields, `@Column` for mapping
2. **GraphQL Resolver Pattern**: `@GraphQLApi` class with `@Query`/`@Mutation` methods
3. **Auth Pattern**: `@Authenticated` + `@Inject JsonWebToken jwt` + `Long.parseLong(jwt.getSubject())` for userId
4. **Input DTO Pattern**: Plain Java class with `@NonNull` on required fields, used as GraphQL input type
5. **Response DTO Pattern**: Plain Java class with public fields, built in service layer
6. **Service Pattern**: `@ApplicationScoped` + `@Inject` dependencies, `@Transactional` on write methods
7. **Macro Calculation**: Always convert to grams first, then `(grams / 100) × per100g`
8. **User Scoping**: All user-specific queries filter by `userId` extracted from JWT

### Frontend Patterns

1. **View Pattern**: `@Route("path") @Menu(order=N, icon="icon-name")` class extending `VerticalLayout`
2. **Service Pattern**: `@Service` Spring bean using `ApiClient` for GraphQL calls
3. **Record DTO Pattern**: Java `record` types for GraphQL response mapping (immutable)
4. **Dialog Pattern**: Custom `Dialog` subclass with form sections, Save/Cancel buttons
5. **Auth Guard**: `MainLayout.beforeEnter()` checks `AuthService.isLoggedIn()`
6. **Filter Pattern**: ComboBox/TextField at top of view, triggers data reload on value change
7. **Notification Pattern**: `Notification.show("message")` for success, error with `NotificationVariant`

### Naming Conventions

| Context | Convention | Example |
|---------|-----------|---------|
| Entity class | PascalCase, singular | `Recipe`, `PantryItem` |
| Table name | lowercase, matches entity (except `user_`) | `recipe`, `pantryitem` |
| Column name | snake_case via `@Column(name=...)` | `prep_time`, `meal_slot` |
| GraphQL query | camelCase | `weeklyMealPlan`, `pantryItems` |
| GraphQL mutation | camelCase | `createRecipe`, `toggleShoppingListItem` |
| GraphQL input type | PascalCase + `Input` suffix | `RecipeInput`, `FoodLogInput` |
| DTO response | PascalCase + `Response` suffix | `RecipeResponse`, `PantryItemResponse` |
| Frontend view | PascalCase + `View` suffix | `RecipeListView`, `MealPlanView` |
| Frontend service | PascalCase + `Service` suffix | `RecipeService`, `AuthService` |
| CSS class | kebab-case | `recipe-card`, `meal-plan-grid` |
| CSS modifier | BEM-like with `--` | `form-section--primary` |

### Error Handling Approach

- **Backend**: GraphQL errors returned in `errors` array. Services return `null` for not-found cases, throw exceptions for validation failures.
- **Frontend**: `ApiClient` parses `errors[0].message` from GraphQL response. Views catch exceptions and show `Notification.show(error, ...)`.
- **No global error handler** — each view handles its own errors inline.

---

## 14. KEY ALGORITHMS

### Macro Calculation Pipeline

```
Ingredient (per 100g values)
    ↓
RecipeIngredient (quantity + unit)
    ↓ Convert to grams: quantity × unitToGrams[unit]
    ↓ Calculate: (grams / 100) × per100gValue
    = Individual ingredient macros
    ↓
Sum all ingredients
    = Total recipe macros
    ↓
Divide by servings
    = Per-serving macros
    ↓
Multiply by servings consumed (FoodLog)
    = Actual consumed macros
    ↓
Sum all food logs for a day
    = Daily actual macros
    ↓
Compare against UserNutritionTarget
    = Progress bars in FoodLogView
```

### Shopping List Generation Algorithm

```
Input: weekStart date, userId

1. CLEAR existing shopping list for this week
2. GET weekly meal plan (weekStart → weekStart + 6 days)
3. AGGREGATE ingredients:
   For each meal in the week:
     For each ingredient in the meal's recipe:
       Convert quantity to grams
       aggregated[ingredientId] += quantityInGrams
4. GET pantry items:
   For each pantry item:
     Convert quantity to grams
     pantry[ingredientId] = quantityInGrams
5. CALCULATE deficit:
   For each aggregated ingredient:
     needed = aggregated[ingredientId] - pantry[ingredientId]
     If needed > 0:
       CREATE ShoppingListItem(ingredient, needed, GRAMS)
6. RETURN shopping list with progress stats
```

### Recommendation Scoring Algorithm

```
Input: userId, optional filters (category, difficulty, maxMissing)

1. GET pantry → Map<ingredientId, quantityInGrams>
2. GET recipes (filtered by category/difficulty if specified)
3. For each recipe:
   matched = 0, missing = []
   For each recipe ingredient:
     requiredGrams = convert(quantity, unit) to grams
     pantryGrams = pantry.get(ingredientId) or 0
     If pantryGrams >= requiredGrams:
       matched++
     Else:
       missing.add({ ingredient, required, available, needed })
   matchPercent = (matched / totalIngredients) × 100
4. FILTER by maxMissingIngredients if set
5. SORT by matchPercent DESC
6. RETURN list of recommendations
```

### Pantry Auto-Merge Algorithm

```
When adding pantry item for ingredientId:
1. CHECK if user already has a PantryItem for that ingredientId
2. If YES:
   a. Convert existing quantity to grams: existingGrams = toGrams(existing.quantity, existing.unit)
   b. Convert new quantity to grams: newGrams = toGrams(input.quantity, input.unit)
   c. existing.quantity = existingGrams + newGrams
   d. existing.unit = GRAMS (normalized)
   e. If input has expirationDate, use the earlier date
   f. persist existing
3. If NO:
   Create new PantryItem
```

### PDF Image Extraction & Recipe Matching

```
1. OPEN PDF with PDFBox
2. For each page:
   a. GET all images on the page
   b. FIND largest image (by pixel area, minimum 100×100)
   c. SAVE as JPEG: outputDir/page_{N}.jpg
   d. EXTRACT text from page
   e. NORMALIZE text: remove diacritics (ă→a, ș→s, ț→t), lowercase, strip punctuation
   f. For each recipe in database:
      NORMALIZE recipe name
      If normalized page text CONTAINS normalized recipe name:
        Record match (prefer longest recipe name match)
3. RETURN results: { page, imagePath, matchedRecipeName }
```

### Food Log Actual Macros Calculation

```
For each FoodLog entry:
  If sourceType == RECIPE:
    Get recipe's per-serving macros (via MacroCalculationService)
    actualMacros = perServingMacros × entry.servings
  Else if sourceType == INGREDIENT:
    grams = toGrams(ingredientQuantity, unit) [uses 100g for pieces]
    actualMacros.calories = (grams / 100) × ingredient.caloriesPer100g × servings
    actualMacros.protein  = (grams / 100) × ingredient.proteinPer100g × servings
    (same for carbs, fat)
  Else if sourceType == CUSTOM:
    actualMacros.calories = customCalories × servings
    actualMacros.protein  = customProtein × servings
    (same for carbs, fat)
```

---

## 15. FILE INVENTORY

### Backend Files (by package)

#### `com.recipebook.entity` (10 files)
| File | Purpose |
|------|---------|
| `User.java` | User account entity |
| `Ingredient.java` | Ingredient with nutrition data |
| `Recipe.java` | Recipe with metadata and relationships |
| `RecipeIngredient.java` | Join entity: recipe ↔ ingredient with quantity |
| `MealPlan.java` | Meal assignment: date + slot + recipe |
| `PantryItem.java` | User's pantry inventory item |
| `ShoppingListItem.java` | Shopping list entry |
| `UserNutritionTarget.java` | Daily nutrition goals |
| `FoodLog.java` | Food consumption log entry |
| `Unit.java` | Enum: measurement units |
| `RecipeCategory.java` | Enum: recipe categories |
| `Difficulty.java` | Enum: recipe difficulty levels |
| `IngredientCategory.java` | Enum: ingredient categories |
| `MealSlot.java` | Enum: meal time slots |

#### `com.recipebook.graphql` (19 files)
| File | Purpose |
|------|---------|
| `AuthGraphQL.java` | Auth queries/mutations |
| `RecipeGraphQL.java` | Recipe CRUD |
| `IngredientGraphQL.java` | Ingredient CRUD |
| `MealPlanGraphQL.java` | Meal plan management |
| `PantryGraphQL.java` | Pantry management |
| `ShoppingListGraphQL.java` | Shopping list management |
| `FoodLogGraphQL.java` | Food logging |
| `NutritionTargetGraphQL.java` | Nutrition targets |
| `RecommendationGraphQL.java` | Recipe recommendations |
| `RecipeImportGraphQL.java` | Recipe import + PDF extraction |
| `RecipeInput.java` | Input type for recipes |
| `RecipeIngredientInput.java` | Input type for recipe ingredients |
| `IngredientInput.java` | Input type for ingredients |
| `MealPlanInput.java` | Input type for meal plans |
| `PantryItemInput.java` | Input type for pantry items |
| `PantryItemUpdateInput.java` | Input type for pantry updates |
| `ShoppingListItemInput.java` | Input type for shopping list items |
| `FoodLogInput.java` | Input type for food logs |
| `RecommendationFilterInput.java` | Input type for recommendation filters |

#### `com.recipebook.service` (13 files)
| File | Purpose |
|------|---------|
| `PasswordService.java` | Bcrypt password hashing |
| `TokenService.java` | JWT token generation |
| `DataSeeder.java` | Startup data initialization |
| `MacroCalculationService.java` | Macro math + unit conversion |
| `PantryService.java` | Pantry CRUD + auto-merge |
| `MealPlanService.java` | Meal plan CRUD + weekly aggregation |
| `ShoppingListService.java` | Shopping list generation + CRUD |
| `FoodLogService.java` | Food logging + daily summaries |
| `NutritionTargetService.java` | Nutrition target CRUD |
| `RecommendationService.java` | Recipe recommendation engine |
| `RecipeImportService.java` | JSON recipe import |
| `PdfImageExtractorService.java` | PDF image extraction + matching |
| `RomanianNutritionData.java` | Hardcoded nutrition lookup (180+ items) |

#### `com.recipebook.dto` (22 files)
| File | Purpose |
|------|---------|
| `AuthResponse.java` | Login/register response |
| `LoginRequest.java` | Login input validation |
| `RegisterRequest.java` | Register input validation |
| `ErrorMessage.java` | Error response wrapper |
| `MacroInfo.java` | Macro nutrient values |
| `RecipeResponse.java` | Full recipe response |
| `RecipeIngredientResponse.java` | Recipe ingredient detail |
| `RecipeRequest.java` | Recipe creation request |
| `RecipeIngredientRequest.java` | Recipe ingredient request |
| `PantryItemResponse.java` | Pantry item detail |
| `MealPlanResponse.java` | Single meal plan entry |
| `DailyMealPlanResponse.java` | Day's meal plan |
| `WeeklyMealPlanResponse.java` | Week's meal plan |
| `ShoppingListItemResponse.java` | Shopping list item |
| `ShoppingListResponse.java` | Full shopping list |
| `MissingIngredientResponse.java` | Missing ingredient detail |
| `RecipeRecommendationResponse.java` | Recipe recommendation |
| `FoodLogResponse.java` | Food log entry |
| `DailyFoodLogResponse.java` | Day's food log |
| `PlannedMealStatus.java` | Planned meal logging status |
| `UserNutritionTargetResponse.java` | Nutrition target values |
| `FoodSourceType.java` | Enum: food log source type |

#### `com.recipebook.rest` (1 file)
| File | Purpose |
|------|---------|
| `ImageResource.java` | Image upload/serve REST endpoint |

### Frontend Files (by package)

#### Core (1 file)
| File | Purpose |
|------|---------|
| `Application.java` | Spring Boot entry point, `@Theme("recipe-book")` |

#### `com.recipebook.views` (4 files)
| File | Purpose |
|------|---------|
| `MainLayout.java` | App shell with navigation + auth guard |
| `DashboardView.java` | Home page with stats + nutrition |
| `LoginView.java` | Login form |
| `RegisterView.java` | Registration form |

#### `com.recipebook.recipe` (7 files)
| File | Purpose |
|------|---------|
| `RecipeListView.java` | Recipe card grid |
| `RecipeDetailView.java` | Full recipe page |
| `RecipeFormDialog.java` | Create/edit recipe dialog |
| `RecipeService.java` | GraphQL calls for recipes |
| `RecipeResponse.java` | Record DTO |
| `RecipeIngredientResponse.java` | Record DTO |
| `MacroInfo.java` | Record DTO |
| `IngredientOption.java` | Record DTO for ingredient picker |

#### `com.recipebook.ingredient` (4 files)
| File | Purpose |
|------|---------|
| `IngredientListView.java` | Ingredient grid |
| `IngredientFormDialog.java` | Create/edit ingredient dialog |
| `IngredientService.java` | GraphQL calls |
| `IngredientResponse.java` | Record DTO |

#### `com.recipebook.mealplan` (5 files)
| File | Purpose |
|------|---------|
| `MealPlanView.java` | Weekly calendar view |
| `MealPlanService.java` | GraphQL calls |
| `MealPlanResponse.java` | Record DTO |
| `DailyMealPlanResponse.java` | Record DTO |
| `WeeklyMealPlanResponse.java` | Record DTO |

#### `com.recipebook.pantry` (4 files)
| File | Purpose |
|------|---------|
| `PantryView.java` | Pantry grid with expiry banner |
| `PantryFormDialog.java` | Add/edit pantry item dialog |
| `PantryService.java` | GraphQL calls |
| `PantryItemResponse.java` | Record DTO |

#### `com.recipebook.shopping` (6 files)
| File | Purpose |
|------|---------|
| `ShoppingListView.java` | Shopping list with QR sharing |
| `ShoppingListService.java` | GraphQL calls |
| `SharedListStore.java` | In-memory shared list cache |
| `SharedListController.java` | REST endpoint for shared lists |
| `ShoppingListResponse.java` | Record DTO |
| `ShoppingListItemResponse.java` | Record DTO |

#### `com.recipebook.foodlog` (6 files)
| File | Purpose |
|------|---------|
| `FoodLogView.java` | Daily food log with planned meals |
| `FoodLogService.java` | GraphQL calls |
| `FoodLogResponse.java` | Record DTO |
| `DailyFoodLogResponse.java` | Record DTO |
| `PlannedMealStatus.java` | Record DTO |
| `FoodSourceType.java` | Enum |
| `UserNutritionTargetResponse.java` | Record DTO |

#### `com.recipebook.recommendation` (4 files)
| File | Purpose |
|------|---------|
| `RecommendationView.java` | Recommendation cards |
| `RecommendationService.java` | GraphQL calls |
| `RecipeRecommendationResponse.java` | Record DTO |
| `MissingIngredientResponse.java` | Record DTO |

#### `com.recipebook.service` (2 files)
| File | Purpose |
|------|---------|
| `ApiClient.java` | GraphQL HTTP client |
| `AuthService.java` | Auth + session management |

#### `com.recipebook.ui` (1 file)
| File | Purpose |
|------|---------|
| `MacroBar.java` | Reusable macro display component |

#### `com.recipebook.dto` (4 files)
| File | Purpose |
|------|---------|
| `AuthResponse.java` | Auth response record |
| `LoginRequest.java` | Login request record |
| `RegisterRequest.java` | Register request record |
| `UserInfo.java` | Session user info record |

#### `com.recipebook.i18n` (1 file)
| File | Purpose |
|------|---------|
| `TranslationProvider.java` | i18n translation service |

### Config & Resource Files

| File | Purpose |
|------|---------|
| `backend/src/main/resources/application.properties` | Backend configuration |
| `backend/src/main/resources/privateKey.pem` | JWT signing key |
| `backend/src/main/resources/publicKey.pem` | JWT verification key |
| `backend/src/main/resources/import.sql` | Empty SQL template |
| `frontend/src/main/resources/application.properties` | Frontend configuration |
| `frontend/src/main/resources/vaadin-featureflags.properties` | Vaadin feature flags |
| `frontend/src/main/resources/i18n/messages.properties` | English translations |
| `frontend/src/main/resources/i18n/messages_ro.properties` | Romanian translations |

### Infrastructure Files

| File | Purpose |
|------|---------|
| `docker-compose.yml` | PostgreSQL + Adminer services |
| `.env.example` | Environment variable template |
| `.env` | Active environment variables |
| `.gitignore` | Git ignore rules |
| `backend/pom.xml` | Backend Maven build |
| `frontend/pom.xml` | Frontend Maven build |
| `frontend/package.json` | Frontend npm dependencies |
| `frontend/Dockerfile` | Frontend Docker build |
| `backend/src/main/docker/Dockerfile.jvm` | Backend JVM Docker |
| `backend/src/main/docker/Dockerfile.legacy-jar` | Backend legacy JAR Docker |
| `backend/src/main/docker/Dockerfile.native` | Backend native Docker |
| `backend/src/main/docker/Dockerfile.native-micro` | Backend micro native Docker |

### Theme Files

| File | Purpose |
|------|---------|
| `themes/recipe-book/theme.json` | Lumo imports config |
| `themes/recipe-book/styles.css` | Main stylesheet (593 lines) |
| `themes/recipe-book/components/vaadin-app-layout.css` | AppLayout overrides |
| `themes/recipe-book/components/vaadin-combo-box.css` | ComboBox overrides |
| `themes/recipe-book/components/vaadin-dialog-overlay.css` | Dialog overrides |
| `themes/recipe-book/components/vaadin-text-field.css` | TextField overrides |
| `themes/recipe-book/components/vaadin-text-area.css` | TextArea overrides |
| `themes/recipe-book/components/vaadin-integer-field.css` | IntegerField overrides |
| `themes/recipe-book/components/vaadin-number-field.css` | NumberField overrides |

### Data & Documentation Files

| File | Purpose |
|------|---------|
| `README.md` | Project readme |
| `ARCHITECTURE.md` | Architecture documentation |
| `ROADMAP.md` | Development roadmap |
| `frontend/CLAUDE.md` | AI guidance (outdated starter template) |
| `recipes_import.json` | Recipe import data file |
| `carte.pdf` | PDF cookbook (~156 MB) |
| `carte_extract.txt` | Extracted text from PDF |
| `backup_2026-03-01.sql` | Database backup |
| `Screenshot 2026-03-01 200536.png` | App screenshot |
| `Screenshot 2026-03-01 212143.png` | App screenshot |

### Total File Count Summary

| Category | Count |
|----------|-------|
| Backend Java (entities) | 14 |
| Backend Java (graphql) | 19 |
| Backend Java (services) | 13 |
| Backend Java (dto) | 22 |
| Backend Java (rest) | 1 |
| **Backend Java Total** | **69** |
| Frontend Java (views) | 4 |
| Frontend Java (features) | 40 |
| Frontend Java (services) | 2 |
| Frontend Java (other) | 6 |
| **Frontend Java Total** | **52** |
| Theme/CSS files | 9 |
| Config/properties files | 5 |
| Infrastructure files | 7 |
| Docker files | 5 |
| Test files (TypeScript) | 16 |
| Documentation/Data files | 10 |
| **Grand Total (custom files)** | **~173** |

---

## 16. SECURITY

### JWT Authentication

- **Algorithm**: RS256 (RSA with SHA-256)
- **Key size**: 2048-bit RSA
- **Private key**: `backend/src/main/resources/privateKey.pem` (PKCS#8 format)
- **Public key**: `backend/src/main/resources/publicKey.pem` (X.509 format)
- **Token structure**:
  ```json
  {
    "iss": "recipebook",
    "sub": "<userId>",
    "upn": "<username>",
    "email": "<email>",
    "role": "<role>",
    "userId": <userId>,
    "groups": ["USER"],
    "iat": <issued_at>,
    "exp": <expiry>
  }
  ```
- **Lifespan**: 86400 seconds (24 hours)

### Path Permissions

| Path Pattern | Policy |
|-------------|--------|
| `/graphql`, `/graphql/*` | `permit` (public, auth enforced per-method) |
| `/q/graphql-ui`, `/q/graphql-ui/*` | `permit` (GraphQL playground) |
| `/api/images/*` | `permit` (public image serving) |
| All other paths | Default deny |

### Password Security

- **Hashing**: bcrypt via `BCrypt.hashpw()` with automatic salt generation
- **Verification**: `BCrypt.checkpw()` — constant-time comparison
- **Minimum length**: 6 characters (enforced by `@Size(min=6)` on RegisterRequest)

### User-Scoped Data Isolation

All user-specific data queries include a `userId` filter:
```java
Long userId = Long.parseLong(jwt.getSubject());
PantryItem.find("user.id", userId).list();
```
This ensures users can only access their own data for: pantry, meal plans, shopping lists, food logs, nutrition targets.

### Image Upload Validation

1. **Content type check**: Only `image/jpeg`, `image/png`, `image/webp` allowed
2. **Size limit**: 5 MB (code validation) + 10 MB (Quarkus body limit)
3. **Filename sanitization**: UUID-generated filename (no user-controlled naming)
4. **Path traversal prevention**: Filename stripped of `/` and `\` characters before serving
5. **Directory isolation**: Images stored in dedicated `./uploads/images/` directory

### CORS Policy

| Setting | Value |
|---------|-------|
| Allowed origins | `http://localhost:8081`, `http://127.0.0.1:8081` |
| Allowed methods | `GET`, `POST`, `PUT`, `DELETE`, `OPTIONS` |
| Allowed headers | `accept`, `authorization`, `content-type`, `x-requested-with` |
| Exposed headers | `location`, `info` |
| Max age | 24 hours |
| Credentials | Allowed |

### GraphQL Auth Pattern

```java
@GraphQLApi
public class SomeGraphQL {
    @Inject JsonWebToken jwt;

    @Query
    @Authenticated  // Rejects unauthenticated requests
    public SomeResponse someQuery() {
        Long userId = Long.parseLong(jwt.getSubject());
        // ... user-scoped query
    }
}
```

Unauthenticated queries (like `recipes`, `ingredients`) omit `@Authenticated`.

---

## 17. i18n & LOCALIZATION

### TranslationProvider

**File**: `frontend/src/main/java/com/recipebook/i18n/TranslationProvider.java`

- Implements `com.vaadin.flow.i18n.I18NProvider`
- Supports 2 locales: `en` (English), `ro` (Romanian)
- Loads from resource bundles: `i18n/messages.properties`, `i18n/messages_ro.properties`
- Falls back to `"!key!"` for missing translations
- Supports `MessageFormat` parameters

### Resource Bundles

**File**: `frontend/src/main/resources/i18n/messages.properties` (English)
**File**: `frontend/src/main/resources/i18n/messages_ro.properties` (Romanian)

Translation keys cover: app name, navigation labels, login/register forms, dashboard text, action buttons, messages.

### KNOWN ISSUE: Legacy D&D Content

The i18n bundles still contain keys from a D&D Campaign Manager application (the project's predecessor):
- `campaigns.*`, `characters.*`, `dice.*` translation keys are present
- These keys are unused by the Recipe Book UI
- They were not cleaned up during the migration from D&D to Recipe Book

### User Language Preference

- Stored on `User.language` field (default: `"en"`)
- Set during registration via the language ComboBox
- The `RegisterRequest` includes a `language` field

---

## 18. SHOPPING LIST QR SHARING SYSTEM

### Overview

Users can share their shopping list via QR code or copyable link, allowing someone else (e.g., a family member) to view the list on their phone without needing an account.

### Components

#### SharedListStore (frontend/shopping/SharedListStore.java)

- **In-memory storage**: `ConcurrentHashMap<String, SharedList>`
- **TTL**: 24 hours per entry
- **ID generation**: `UUID.randomUUID().toString().substring(0, 8)` (8-char short ID)
- **Cleanup**: Automatic expiration check on access
- **Thread-safe**: Uses `ConcurrentHashMap`

#### SharedListController (frontend/shopping/SharedListController.java)

- **Endpoint**: `GET /share/{id}`
- **Returns**: Full HTML page (no template engine, inline HTML string)
- **Styling**: Dark theme matching the app's aesthetic
- **Features**:
  - Items grouped by category
  - Interactive checkboxes (client-side JavaScript)
  - Native Share API button (mobile)
  - Copy-to-clipboard fallback
  - HTML-escaped content for XSS prevention

#### QR Code Generation (in ShoppingListView)

- Uses ZXing library (`com.google.zxing`)
- Generates QR code as Base64-encoded PNG
- Displays in a dialog overlay
- URL format: `http://<LAN_IP>:8081/share/<id>`

#### LAN IP Detection

- `InetAddress.getLocalHost().getHostAddress()` to detect the machine's LAN IP
- Allows sharing within local network (e.g., phone on same WiFi)
- Falls back to `localhost` if detection fails

---

## 19. RECIPE IMPORT & PDF PROCESSING

### RecipeImportService

**File**: `backend/src/main/java/com/recipebook/service/RecipeImportService.java`

- Reads `recipes_import.json` (configurable path, default at project root)
- JSON format: Array of recipe objects with name, description, category, difficulty, times, servings, instructions, ingredients array
- **Duplicate detection**: Skips recipes where name already exists (case-insensitive)
- **Ingredient creation**: Uses `findOrCreateIngredient()`:
  1. Search by name in database
  2. If not found, look up in `RomanianNutritionData` for macro values
  3. Create new ingredient with nutrition data
- **Returns**: `ImportResult { imported, failed, errors[] }`

### PdfImageExtractorService

**File**: `backend/src/main/java/com/recipebook/service/PdfImageExtractorService.java`

- Uses Apache PDFBox 3.0.4
- Processes `carte.pdf` (156 MB Romanian cookbook)
- **Per page**:
  1. Extract all images using PDFBox's `PDResources`
  2. Select largest image (by width × height, minimum 100×100)
  3. Save as JPEG to output directory
  4. Extract text content
  5. Match against recipe names in database

### Text Normalization (for matching)

```
Input:  "Ciorbă de Burtă"
Step 1: Remove diacritics → "Ciorba de Burta"
Step 2: Lowercase → "ciorba de burta"
Step 3: Strip non-alphanumeric → "ciorbadeburta"
```

Romanian diacritics handled: ă→a, â→a, î→i, ș→s, ț→t (and uppercase variants).

### RomanianNutritionData

**File**: `backend/src/main/java/com/recipebook/service/RomanianNutritionData.java`

- Static lookup table with 180+ common Romanian/international ingredients
- Each entry: `{ caloriesPer100g, proteinPer100g, carbsPer100g, fatPer100g, category }`
- Values sourced from USDA nutritional database
- **Lookup strategy**:
  1. Exact match (case-insensitive)
  2. Partial match (key contained in query, or query contained in key)
  3. Returns `null` if no match (ingredient created with zero macros)

---

## 20. TESTING

### Playwright E2E Test Setup

**Config**: `tests/playwright.config.ts`

```typescript
// Key settings:
baseURL: 'http://localhost:8081'
testDir: '.'
timeout: 30000
retries: 0
```

### Test Directory Structure

```
tests/
├── playwright.config.ts
├── package.json
├── api/
│   ├── test-utils.ts          ← Auth helper (getAuthToken, authHeaders)
│   ├── auth.spec.ts           ← Login/register API tests
│   ├── users.spec.ts          ← User management tests
│   ├── campaigns.spec.ts      ← [LEGACY D&D]
│   ├── characters.spec.ts     ← [LEGACY D&D]
│   ├── sessions.spec.ts       ← [LEGACY D&D]
│   └── dice.spec.ts           ← [LEGACY D&D]
├── ui/
│   ├── ui-test-utils.ts
│   ├── fixtures/
│   │   └── auth.fixture.ts
│   ├── auth/
│   │   ├── login.spec.ts      ← Login UI tests
│   │   └── register.spec.ts   ← Register UI tests
│   ├── dashboard/
│   │   └── dashboard.spec.ts  ← Dashboard UI tests
│   ├── navigation/
│   │   └── main-layout.spec.ts ← Navigation UI tests
│   ├── campaigns/             ← [LEGACY D&D]
│   │   ├── campaigns-list.spec.ts
│   │   └── campaign-detail.spec.ts
│   ├── characters/            ← [LEGACY D&D]
│   │   └── characters.spec.ts
│   └── dice/                  ← [LEGACY D&D]
│       └── dice-roller.spec.ts
```

### Test Utilities

**`tests/api/test-utils.ts`**:
- `getAuthToken()`: Registers a test user via GraphQL, caches token
- `authHeaders()`: Returns `{ Authorization: 'Bearer <token>' }` headers

### KNOWN ISSUE: Legacy Tests

Most test files test D&D Campaign Manager features (campaigns, characters, dice roller) that no longer exist in the Recipe Book application. Only these tests are relevant:
- `api/auth.spec.ts` — Tests register/login GraphQL mutations
- `api/users.spec.ts` — Tests user management
- `ui/auth/login.spec.ts` — Tests login page UI
- `ui/auth/register.spec.ts` — Tests register page UI
- `ui/dashboard/dashboard.spec.ts` — Tests dashboard UI
- `ui/navigation/main-layout.spec.ts` — Tests navigation

### How to Run Tests

```bash
cd tests
npm install
npx playwright test              # Run all tests
npx playwright test api/         # Run API tests only
npx playwright test ui/          # Run UI tests only
npx playwright test --headed     # Run with browser visible
```

**Prerequisites**: Both backend (port 8080) and frontend (port 8081) must be running.

---

## 21. FRONTEND BUILD SYSTEM

### Vaadin + Spring Boot Integration

- **Entry point**: `Application.java` with `@SpringBootApplication` and `@Theme("recipe-book")`
- Implements `AppShellConfigurator` for HTML shell customization
- Vaadin auto-generates frontend resources in `frontend/src/main/frontend/generated/`

### Build Pipeline

```
Maven → Vaadin Maven Plugin → Node.js → Vite → Bundle
         (v25.0.0-rc2)        (npm)    (v7.2.7)
```

### Key Dependencies (frontend/package.json)

| Package | Version | Purpose |
|---------|---------|---------|
| `react` | 19.2.3 | Vaadin's rendering layer |
| `react-dom` | 19.2.3 | React DOM |
| `react-router` | 7.10.1 | Client-side routing |
| `@vaadin/*` | 25.0.0-rc1 | Vaadin web components |
| `vite` | 7.2.7 | Build tool |
| `typescript` | 5.9.3 | TypeScript compiler |
| `lit` | 3.3.1 | Web component library |
| `ol` | 10.6.1 | OpenLayers maps [UNUSED] |
| `proj4` | 2.15.0 | Map projections [UNUSED] |

### Auto-Generated Files

The `frontend/src/main/frontend/generated/` directory contains Vaadin auto-generated files:
- `routes.tsx` — Auto-generated routes from `@Route` annotations
- `Flow.tsx` — Vaadin Flow integration
- `ReactAdapter.tsx` — React adapter for Flow components
- `jar-resources/` — Vaadin internal connectors and helpers
- `copilot/` — Vaadin Copilot dev tool
- Theme generated files (`theme-recipe-book.*.js`)

These files are regenerated on each build and should not be manually edited.

### Production Build

```bash
cd frontend
./mvnw -Pproduction package
# Output: target/frontend-1.0-SNAPSHOT.jar
# The -Pproduction profile triggers:
#   1. Vite production build (minification, tree-shaking)
#   2. Service worker generation (workbox)
#   3. Brotli compression
```

---

## 22. ERROR HANDLING & NOTIFICATIONS

### Backend: GraphQL Error Responses

GraphQL errors follow the standard format:
```json
{
  "errors": [
    {
      "message": "User not found",
      "locations": [...],
      "path": [...]
    }
  ],
  "data": null
}
```

Services handle errors by:
- Returning `null` for not-found entities (GraphQL resolvers handle the null)
- Throwing `RuntimeException` for validation failures (becomes GraphQL error)
- Logging errors at service level

### Frontend: ApiClient Error Parsing

```java
// In ApiClient.execute():
if (responseBody.has("errors")) {
    String message = responseBody.get("errors").get(0).get("message").asText();
    throw new RuntimeException(message);
}
```

### Vaadin Notification Patterns

```java
// Success notification
Notification.show("Recipe created successfully!");

// Error notification
Notification notification = Notification.show("Failed to save: " + error.getMessage());
notification.addThemeVariants(NotificationVariant.LUMO_ERROR);

// Warning (using custom positioning)
Notification.show("Item expiring soon!", 3000, Notification.Position.TOP_END);
```

### Validation Patterns

- **Backend DTOs**: `@NotBlank`, `@NotNull`, `@Email`, `@Size`, `@Positive` (Jakarta Validation)
- **GraphQL Inputs**: `@NonNull` annotation on required fields
- **Frontend forms**: Vaadin component validation (required fields, min/max values)
- **No global error boundary** — each view handles errors locally

---

## 23. END-TO-END FEATURE FLOWS

### Flow 1: User Registration → Create Recipe → Plan Meal → Generate Shopping List

```
1. User opens http://localhost:8081 → redirected to /login
2. Clicks "Register" → fills email, username, password, language → Submit
3. Backend creates User, returns JWT → Frontend stores in session
4. Redirected to /dashboard → sees empty stats
5. Navigates to /recipes → clicks "Add Recipe"
6. Fills recipe form: name, category, difficulty, times, servings
7. Adds ingredients: selects ingredient from ComboBox, sets quantity+unit
8. Uploads image via drag-and-drop → multipart POST to /api/images
9. Saves → createRecipe mutation → recipe appears in grid
10. Navigates to /meal-plan → clicks empty cell (e.g., Monday Lunch)
11. Recipe picker opens → searches and selects the recipe → assignMealPlan mutation
12. Calendar cell fills with recipe name + calorie badge
13. Navigates to /shopping-list → clicks "Generate from Meal Plan"
14. generateShoppingList mutation: aggregates ingredients, subtracts pantry, creates deficit items
15. Shopping list appears with items grouped by category
16. Clicks "Share" → QR code generated → scans with phone → sees shared list
```

### Flow 2: Add Pantry → Get Recommendations → Add Missing to List

```
1. Navigate to /pantry → click "Add Item"
2. Select ingredient, set quantity, unit, expiration → addPantryItem mutation
3. Pantry auto-merges if same ingredient exists
4. Navigate to /recommendations
5. recipeRecommendations query: compares pantry vs all recipes
6. Cards sorted by match percentage (highest first)
7. Expand "Missing Ingredients" on a card → sees what's needed
8. Click "Add Missing to Shopping List" → addMissingToShoppingList mutation
9. Missing ingredients added to shopping list for current week
```

### Flow 3: Plan Meal → Log as Eaten → View Nutrition

```
1. Meal plan has recipes assigned for today
2. Navigate to /food-log → sees today's date
3. "Planned Meals" section shows meals from meal plan
4. Click checkbox next to a meal → logFood mutation (source=RECIPE, sourceMealPlanId set)
5. Entry appears in logged entries with actual macros
6. Progress bars update: calories X/2000, protein Xg/150g, etc.
7. Can also click "Log All" to log all planned meals at once
8. Can add extra food via "Log a Meal" button → 3 tabs (recipe/ingredient/custom)
```

### Flow 4: Import Recipes from JSON → Extract PDF Images

```
1. Place recipes_import.json at project root
2. Call importRecipesFromFile mutation (via GraphQL UI or code)
3. Service reads JSON, creates ingredients (with RomanianNutritionData lookup), creates recipes
4. Returns: { imported: 15, failed: 2, errors: ["..."] }
5. Call extractPdfImages mutation with carte.pdf path
6. Service opens PDF, extracts largest image per page, matches against recipe names
7. Images saved to uploads/images/, recipe imageUrl updated
```

### Flow 5: Share Shopping List via QR

```
1. Navigate to /shopping-list with items generated
2. Click "Share" button
3. Frontend creates entry in SharedListStore (8-char UUID, 24h TTL)
4. Detects LAN IP (e.g., 192.168.1.100)
5. Generates QR code for URL: http://192.168.1.100:8081/share/abc12345
6. Shows QR in dialog + copy button
7. Recipient scans QR → browser opens shared list HTML page
8. Page shows items with checkboxes (client-side only, no sync)
```

---

## 24. LEGACY / TECHNICAL DEBT

### i18n Bundles Reference D&D Campaign Manager

The translation files (`messages.properties`, `messages_ro.properties`) contain keys like:
- `campaigns.title`, `campaigns.create`, `campaigns.delete`
- `characters.title`, `characters.create`, `characters.ability_scores`
- `dice.title`, `dice.roll`, `dice.advantage`

These are leftovers from the original D&D Campaign Manager application. The Recipe Book doesn't use these keys, but they haven't been cleaned up.

### Playwright Tests Test D&D Features

8 out of 14 test spec files test features that no longer exist:
- `api/campaigns.spec.ts`, `api/characters.spec.ts`, `api/sessions.spec.ts`, `api/dice.spec.ts`
- `ui/campaigns/campaigns-list.spec.ts`, `ui/campaigns/campaign-detail.spec.ts`
- `ui/characters/characters.spec.ts`, `ui/dice/dice-roller.spec.ts`

### frontend/CLAUDE.md is Outdated

References:
- H2 database (project uses PostgreSQL)
- Example feature package (deleted)
- Default theme (project uses custom "recipe-book" theme)
- Port 8080 for frontend (actually 8081)

### Empty import.sql

`backend/src/main/resources/import.sql` is the default Quarkus template (empty/commented). All seeding is done via `DataSeeder.java`.

### Unused npm Dependencies

`frontend/package.json` includes:
- `ol` (OpenLayers) 10.6.1 — map library, no map views exist
- `proj4` 2.15.0 — map projections, unused
- `@vaadin/map` — map component, unused

These were included by the Vaadin starter template.

### Pre-Release Framework Versions

- **Vaadin 25.0.0-rc2** — Release candidate, not stable
- **Spring Boot 4.0.0** — Major version, may have breaking changes

---

## 25. KNOWN STATE & PROJECT NOTES

### Current Branches

| Branch | Description |
|--------|------------|
| `master` (local) / `main` (remote) | Main development branch |
| `feature/meal-planning` | Current working branch |
| `ui-visual-improvements` | UI styling branch |

### Recent Commits (Feature Evolution)

```
5ced435 Add PDF image extraction and ingredient filter for recipes
29452aa Add recipe import service and fix double scrollbar on detail page
59d6419 Add food logging system with nutrition targets and toggle button fix
c34f1a3 Add shareable shopping list with QR code and copy fallback
9b1e283 Add pantry, shopping list, and recommendations UI with dashboard updates
```

### DataSeeder Creates

On first startup (when no users exist):
1. **Admin user**: `chef_admin` / `admin@recipebook.com` / `admin123` (role: ADMIN)
2. **25 ingredients** with full nutritional data (proteins, dairy, vegetables, grains, etc.)
3. **5 Romanian/international recipes** with ingredient links

### Default Credentials

| Field | Value |
|-------|-------|
| Username | `chef_admin` |
| Email | `admin@recipebook.com` |
| Password | `admin123` |
| Role | `ADMIN` |

### Data Files in Repository

| File | Size | Description |
|------|------|-------------|
| `backup_2026-03-01.sql` | ~KB | PostgreSQL database backup |
| `recipes_import.json` | ~KB | Recipe import data (JSON array) |
| `carte.pdf` | ~156 MB | Romanian cookbook PDF for image extraction |
| `carte_extract.txt` | ~KB | Text extracted from carte.pdf |
| `Screenshot 2026-03-01 200536.png` | ~KB | Application screenshot |
| `Screenshot 2026-03-01 212143.png` | ~KB | Application screenshot |

### Existing Documentation Files

| File | Status | Notes |
|------|--------|-------|
| `README.md` | Active | Quick start guide, feature list |
| `ARCHITECTURE.md` | Active | System design, diagrams |
| `ROADMAP.md` | Active | Feature roadmap |
| `frontend/CLAUDE.md` | Outdated | References H2, example feature, wrong port |
| `DOCUMENTATION.md` | **This file** | Comprehensive reference |

### What's NOT Yet Implemented (from ROADMAP gaps)

Based on ROADMAP.md, these features are planned but not yet built:
- User profile editing
- Recipe sharing between users
- Recipe rating/review system
- Meal plan templates (save/load weekly templates)
- Grocery store aisle organization
- Recipe scaling (adjust servings dynamically)
- Nutritional goal wizard (BMR/TDEE calculator)
- Multi-user household support
- Mobile-responsive optimizations (PWA)
- Recipe photo gallery (multiple images per recipe)

---

*End of documentation. This file supersedes README.md, ARCHITECTURE.md, and ROADMAP.md for context purposes. Those files remain untouched.*
