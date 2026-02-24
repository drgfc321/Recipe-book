# Recipe Book

A full-stack recipe management and meal planning application with macronutrient tracking, pantry management, shopping lists, and smart recipe recommendations.

## Features

- **Recipe Management** — Create, edit, and delete recipes with ingredients, categories, difficulty levels, prep/cook times, and step-by-step instructions
- **Ingredient Management** — Full CRUD for ingredients with per-100g macronutrient data (calories, protein, carbs, fat)
- **Macronutrient Tracking** — Automatic macro calculation per recipe and per serving based on ingredient quantities
- **Meal Planning** — Weekly calendar with 4 daily meal slots (Breakfast, Lunch, Dinner, Snack) and daily/weekly macro summaries
- **Shopping Lists** — Auto-generated from meal plans, aggregated by category, with pantry subtraction and progress tracking
- **Pantry Management** — Track available ingredients with quantities and expiration dates
- **Recipe Recommendations** — Ranked by pantry match percentage with missing ingredient details
- **Authentication** — JWT-based user registration and login

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
   git clone https://github.com/drgfc321/Recipe-book-.git
   cd Recipe-book-
   ```

2. **Set up environment variables**
   ```bash
   cp .env.example .env
   # Edit .env if you want to change default values
   ```

3. **Start the database**
   ```bash
   docker-compose up -d
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

| Service    | URL                    | Credentials                          |
|------------|------------------------|--------------------------------------|
| Frontend   | http://localhost:8081   | Register a new account               |
| Backend    | http://localhost:8080   | GraphQL API                          |
| GraphQL UI | http://localhost:8080/q/graphql-ui | Interactive GraphQL explorer |
| Adminer    | http://localhost:8082   | Server: postgres, User: recipebook_user, Password: recipebook_secret, DB: recipebook |
| PostgreSQL | localhost:5432         | (same credentials as above)          |

## Project Structure

```
recipe-book/
├── docker-compose.yml          # PostgreSQL + Adminer
├── .env.example                # Environment variable template
├── backend/                    # Quarkus GraphQL API
│   └── src/main/java/com/recipebook/
│       ├── entity/             # JPA entities (Recipe, Ingredient, MealPlan, etc.)
│       ├── graphql/            # GraphQL resolvers and input types
│       ├── service/            # Business logic and data seeding
│       └── dto/                # Response/request DTOs
├── frontend/                   # Vaadin UI
│   └── src/main/java/com/recipebook/
│       ├── views/              # MainLayout, Dashboard, Login, Register
│       ├── recipe/             # Recipe list, detail, and form views
│       ├── ingredient/         # Ingredient list and form views
│       └── service/            # ApiClient, AuthService
└── tests/                      # Playwright E2E tests
```

## Docker Commands

```bash
# Start containers
docker-compose up -d

# Stop containers
docker-compose down

# View logs
docker-compose logs -f

# Reset database (delete all data)
docker-compose down -v
docker-compose up -d
```
