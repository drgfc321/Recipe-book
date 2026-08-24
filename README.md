# Recipe Book

A full-stack recipe management and meal planning application with macronutrient tracking, pantry management, shopping lists, and smart recipe recommendations.

> **Development note:** Built with AI-assisted development (Claude Code). Architecture, technology selection, data modelling, and code review were my own — including decisions such as replacing Hibernate auto-DDL with versioned Flyway migrations, choosing GraphQL over REST for the API layer, and the design of the meal-plan generation algorithm. See [ARCHITECTURE.md](ARCHITECTURE.md) for the reasoning behind the main technical choices.

## Features

- **Recipe Management** — Create, edit, and delete recipes with ingredients, categories, difficulty levels, prep/cook times, and step-by-step instructions
- **Ingredient Management** — Full CRUD for ingredients with per-100g macronutrient data (calories, protein, carbs, fat)
- **Macronutrient Tracking** — Automatic macro calculation per recipe and per serving based on ingredient quantities
- **Meal Planning** — Weekly calendar with 4 daily meal slots and daily/weekly macro summaries
- **Meal Plan Auto-Generation** — Smart algorithm fills weekly slots based on macro targets, pantry, and variety scoring
- **Food Log** — Daily nutrition tracker with progress bars vs personal targets (recipe, ingredient, or custom entries)
- **BMR/TDEE Calculator** — Multi-step wizard (Mifflin-St Jeor) to set personalized calorie and macro targets
- **Variable Nutrition Targets** — Different macro goals for Training / Rest / Default day types
- **Nutrition Trends** — Historical line + bar charts (ApexCharts) for calories, protein, carbs, fat over time
- **Shopping Lists** — Auto-generated from meal plans, aggregated by category, with pantry subtraction and progress tracking
- **Pantry Management** — Track available ingredients with quantities and expiration dates
- **Recipe Recommendations** — Ranked by pantry match percentage with missing ingredient details
- **Recipe Import** — Bulk import from JSON files with Romanian nutrition data lookup; PDF image extraction
- **OAuth Login** — Sign in with Google or GitHub; auto-creates account and links by email
- **User Profile** — Edit username, email, upload avatar image
- **Password Change & Reset** — In-app password change + email-based forgot/reset password flow
- **Authentication** — JWT-based (RSA256, 24h) with user registration and login
- **Internationalization** — Full EN + RO translations throughout the app
- **58 Integration Tests** — GraphQL tests with real PostgreSQL via Quarkus Dev Services
- **Flyway Migrations** — 8 versioned SQL migrations (replaced Hibernate auto-DDL)
- **Caffeine Caching** — On recipes, ingredients, meal plans, and recommendations
- **HTTPS / nginx** — Production reverse proxy with TLS termination
- **Health Checks** — Docker HEALTHCHECK on all services (PostgreSQL, backend, frontend, nginx)

## Tech Stack

| Layer    | Technology                    | Port |
|----------|-------------------------------|------|
| Frontend | Vaadin 25 + Spring Boot 4.0   | 8081 |
| Backend  | Quarkus 3 + Hibernate Panache | 8080 |
| Database | PostgreSQL 16                 | 5432 |
| DB Admin | Adminer                       | 8082 |
| API      | GraphQL (SmallRye)            |      |
| Auth     | SmallRye JWT                  |      |

## Prerequisites

- Docker Desktop
- Java 21+
- Maven 3.9+

## Quick Start

1. **Clone the repository**
   ```bash
   git clone https://github.com/drgfc321/Recipe-book.git
   cd Recipe-book
   ```

2. **Set up environment variables**
   ```bash
   cp .env.example .env
   # Edit .env if you want to change default values
   ```

3. **Start the database**
   ```bash
   docker compose up -d
   ```

4. **Start the backend** (Terminal 1)
   ```bash
   cd backend
   ./mvnw quarkus:dev
   ```

5. **Start the frontend** (Terminal 2)
   ```bash
   cd frontend
   ./mvnw
   ```

6. **Open the app** at [http://localhost:8081](http://localhost:8081)

## Access Points

| Service    | URL                                | Credentials                                  |
|------------|------------------------------------|----------------------------------------------|
| Frontend   | http://localhost:8081              | Register a new account                       |
| Backend    | http://localhost:8080              | GraphQL API                                  |
| GraphQL UI | http://localhost:8080/q/graphql-ui | Interactive GraphQL explorer                 |
| Adminer    | http://localhost:8082              | Credentials from `.env` (see `.env.example`) |
| PostgreSQL | localhost:5432                     | Credentials from `.env` (see `.env.example`) |

## Project Structure

```
recipe-book/
├── docker-compose.yml          # PostgreSQL + Adminer (development)
├── docker-compose.prod.yml     # Full stack with nginx (production)
├── .env.example                # Environment variable template
├── nginx/                      # Nginx reverse proxy config + certs
├── scripts/                    # Utility scripts (cert generation)
├── backend/                    # Quarkus GraphQL API (92 Java files)
│   └── src/main/java/com/recipebook/
│       ├── entity/             # 12 entities + 4 enums
│       ├── graphql/            # 12 resolvers + 12 input types
│       ├── service/            # 18 business logic services
│       ├── dto/                # 27 response/request DTOs
│       ├── rest/               # Image upload + OAuth callback
│       └── exception/          # Custom exception hierarchy
│   └── src/main/resources/
│       └── db/migration/       # 8 Flyway SQL migrations
│   └── src/test/               # 58 integration tests (10 classes)
├── frontend/                   # Vaadin UI (68 Java files)
│   └── src/main/java/com/recipebook/
│       ├── views/              # MainLayout, Dashboard, Login, Register,
│       │                       # Profile, BMR Wizard, Settings, OAuth,
│       │                       # Forgot/Reset Password
│       ├── recipe/             # Recipe list, detail, and form views
│       ├── ingredient/         # Ingredient list and form views
│       ├── mealplan/           # Meal plan calendar + auto-generate
│       ├── pantry/             # Pantry management
│       ├── shopping/           # Shopping list + QR sharing
│       ├── foodlog/            # Food logging + nutrition tracking
│       ├── recommendation/     # Recipe recommendations
│       ├── nutritionhistory/   # Nutrition trend charts (ApexCharts)
│       ├── service/            # ApiClient, AuthService, BMR, etc.
│       ├── i18n/               # TranslationProvider (EN + RO)
│       └── dto/                # Auth + BMR + OAuth DTOs
└── e2e/                        # Playwright E2E tests
```

## Production Deployment (Docker + HTTPS)

The production stack runs all services behind an nginx reverse proxy with TLS termination. Only ports 80 and 443 are exposed.

1. **Generate self-signed certificates**
   ```powershell
   powershell -File scripts/generate-certs.ps1
   ```
   Or from Git Bash:
   ```bash
   ./scripts/generate-certs.sh
   ```

2. **Configure environment**
   ```bash
   cp .env.example .env
   # Edit .env — set POSTGRES_PASSWORD, ADMIN_PASSWORD, etc.
   ```

3. **Build and start**
   ```bash
   docker compose -f docker-compose.prod.yml up --build -d
   ```

4. **Access** at [https://localhost](https://localhost) (accept the self-signed certificate warning)

| Service    | URL                            |
|------------|--------------------------------|
| App        | https://localhost              |
| GraphQL    | https://localhost/graphql      |
| GraphQL UI | https://localhost/q/graphql-ui |

## Docker Commands

```bash
# Development — start database only
docker compose up -d

# Development — stop
docker compose down

# Production — start full stack
docker compose -f docker-compose.prod.yml up --build -d

# Production — stop
docker compose -f docker-compose.prod.yml down

# View logs
docker compose logs -f

# Reset database (delete all data)
docker compose down -v
docker compose up -d
```
