```
╔══════════════════════════════════════════════════════════════════╗
║                                                                  ║
║     ██████╗ ███████╗ ██████╗██╗██████╗ ███████╗                  ║
║     ██╔══██╗██╔════╝██╔════╝██║██╔══██╗██╔════╝                  ║
║     ██████╔╝█████╗  ██║     ██║██████╔╝█████╗                    ║
║     ██╔══██╗██╔══╝  ██║     ██║██╔═══╝ ██╔══╝                    ║
║     ██║  ██║███████╗╚██████╗██║██║     ███████╗                  ║
║     ╚═╝  ╚═╝╚══════╝ ╚═════╝╚═╝╚═╝     ╚══════╝                  ║
║                  ██████╗  ██████╗  ██████╗ ██╗  ██╗               ║
║                  ██╔══██╗██╔═══██╗██╔═══██╗██║ ██╔╝               ║
║                  ██████╔╝██║   ██║██║   ██║█████╔╝                ║
║                  ██╔══██╗██║   ██║██║   ██║██╔═██╗                ║
║                  ██████╔╝╚██████╔╝╚██████╔╝██║  ██╗               ║
║                  ╚═════╝  ╚═════╝  ╚═════╝ ╚═╝  ╚═╝               ║
║                                                                  ║
║         Plan meals. Track nutrition. Cook smarter.               ║
║                                                                  ║
╚══════════════════════════════════════════════════════════════════╝
```

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

## WHAT IS THIS?

Recipe Book is a full-stack web app for managing recipes, planning weekly meals, tracking what you eat, and keeping your pantry organized. It calculates nutrition automatically, generates smart shopping lists from your meal plan, and recommends recipes based on what you already have at home. Everything runs locally with a dark-themed, modern interface.

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

## QUICK START

Get up and running in 4 steps:

```
┌─────────────────────────────────────────────────────────┐
│  You need: Java 21, Maven 3.x, Node.js, Docker, Git    │
└─────────────────────────────────────────────────────────┘
```

**Step 1** — Start the database

```bash
docker-compose up -d
```

**Step 2** — Start the backend (Terminal 1)

```bash
cd backend
./mvnw quarkus:dev
```

**Step 3** — Start the frontend (Terminal 2)

```bash
cd frontend
./mvnw
```

**Step 4** — Open your browser

```
http://localhost:8081
```

```
┌─────────────────────────────────────────────────────────┐
│  Default login:                                         │
│    Email:     admin@recipebook.com                      │
│    Password:  admin123                                  │
│                                                         │
│  25 ingredients and 5 recipes are pre-loaded for you.   │
└─────────────────────────────────────────────────────────┘
```

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

## HOW IT WORKS

### System Architecture

```
┌──────────────────────────────────────────────────────────────┐
│                      YOUR BROWSER                            │
│            (sees HTML — no JavaScript to write)              │
└──────────────────────────┬───────────────────────────────────┘
                           │ WebSocket
                           ▼
┌──────────────────────────────────────────────────────────────┐
│               FRONTEND  (Vaadin + Spring Boot)               │
│                        :8081                                  │
│                                                              │
│   Java code on the server builds the UI for you.             │
│   Vaadin sends HTML to the browser automatically.            │
│   No React, no Angular, no Vue — just Java.                  │
└──────────────────────────┬───────────────────────────────────┘
                           │ GraphQL over HTTP
                           ▼
┌──────────────────────────────────────────────────────────────┐
│                BACKEND  (Quarkus + Java 21)                   │
│                        :8080                                  │
│                                                              │
│   Business logic, authentication (JWT), macro calculations.  │
│   Exposes a GraphQL API consumed by the frontend.            │
└──────────────────────────┬───────────────────────────────────┘
                           │ SQL (Hibernate)
                           ▼
┌──────────────────────────────────────────────────────────────┐
│                  POSTGRESQL 16  (Docker)                      │
│                        :5432                                  │
│                                                              │
│   12 tables. All user data is scoped by userId.              │
└──────────────────────────────────────────────────────────────┘
```

### Why Vaadin is Special

> Most web apps have a JavaScript frontend that talks to a REST API.
> Vaadin is different: the UI lives on the **server** as Java objects.
> When a user clicks a button, the event travels to the server, Java handles it,
> and Vaadin pushes the updated HTML back to the browser.
>
> This means: **one language (Java) for everything** — no JS/TS to maintain.

### Tech Stack at a Glance

```
┌────────────┬────────────────────────┐
│ Backend    │ Quarkus 3.30, Java 21  │
│ Frontend   │ Vaadin 25, Spring Boot │
│ Database   │ PostgreSQL 16          │
│ API        │ SmallRye GraphQL       │
│ Auth       │ JWT (RSA256, 24h)      │
│ OAuth      │ Google + GitHub        │
│ DB Migrate │ Flyway                 │
│ Caching    │ Caffeine               │
│ i18n       │ EN + RO                │
│ Build      │ Maven                  │
│ Containers │ Docker Compose         │
└────────────┴────────────────────────┘
```

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

## FEATURES TOUR

### Recipes

```
┌─ RECIPES ──────────────────────────────────────────────┐
│                                                        │
│  Create, browse, and manage your recipe collection.    │
│                                                        │
│  - Card grid view with image, difficulty, and macros   │
│  - Filter by category, difficulty, or ingredient       │
│  - Image upload (JPEG, PNG, WebP, up to 5 MB)         │
│  - Auto-calculated nutrition from ingredients          │
│  - Full detail page with instructions and macro bars   │
│                                                        │
│  Categories: Breakfast, Lunch, Dinner, Dessert, Snack  │
│  Difficulty: Easy, Medium, Hard                        │
│                                                        │
└────────────────────────────────────────────────────────┘
```

### Ingredients

```
┌─ INGREDIENTS ──────────────────────────────────────────┐
│                                                        │
│  The nutritional building blocks for everything else.  │
│                                                        │
│  - CRUD with data grid                                 │
│  - Nutrition data per 100g (calories, protein,         │
│    carbs, fat)                                         │
│  - 9 categories: Dairy, Meat, Vegetables, Fruits,      │
│    Grains, Spices, Oils, Beverages, Other              │
│  - 25 pre-seeded ingredients with real nutrition data  │
│                                                        │
└────────────────────────────────────────────────────────┘
```

### Meal Planning

```
┌─ MEAL PLANNING ────────────────────────────────────────┐
│                                                        │
│  Organize your week on a visual calendar.              │
│                                                        │
│  - 7-day weekly view (Mon → Sun)                       │
│  - 4 meal slots per day: Breakfast, Lunch, Dinner,     │
│    Snack                                               │
│  - Click a cell → pick a recipe from your collection   │
│  - Each cell shows the recipe name + calorie badge     │
│  - Daily macro totals calculated automatically         │
│  - Navigate between weeks                              │
│                                                        │
└────────────────────────────────────────────────────────┘
```

### Pantry

```
┌─ PANTRY ───────────────────────────────────────────────┐
│                                                        │
│  Track what's in your kitchen right now.               │
│                                                        │
│  - Add ingredients with quantity, unit, expiry date    │
│  - Auto-merges duplicate ingredients (adds quantity)   │
│  - Expiry warning banner for items going bad soon      │
│  - Feeds into recommendations and shopping lists       │
│  - One entry per ingredient per user                   │
│                                                        │
└────────────────────────────────────────────────────────┘
```

### Shopping List

```
┌─ SHOPPING LIST ────────────────────────────────────────┐
│                                                        │
│  Never forget an ingredient at the store.              │
│                                                        │
│  - Auto-generate from your meal plan                   │
│  - Subtracts what you already have in your pantry      │
│  - Check off items as you shop                         │
│  - Share via QR code — scan on your phone!             │
│  - Copy link fallback if QR doesn't work               │
│  - Items grouped by category                           │
│                                                        │
└────────────────────────────────────────────────────────┘
```

### Food Log

```
┌─ FOOD LOG ─────────────────────────────────────────────┐
│                                                        │
│  Track what you actually eat, not just what you plan.  │
│                                                        │
│  - Daily view with date navigation                     │
│  - See your planned meals and check them off           │
│  - Log from 3 sources:                                 │
│      Recipe  → pick from your collection               │
│      Ingredient → log a single food item               │
│      Custom  → manually enter name + macros            │
│  - Progress bars: calories, protein, carbs, fat        │
│  - Compare against your personal nutrition targets     │
│  - "Log All" button for quick day logging              │
│                                                        │
└────────────────────────────────────────────────────────┘
```

### Recommendations

```
┌─ RECOMMENDATIONS ──────────────────────────────────────┐
│                                                        │
│  "What can I cook with what I have?"                   │
│                                                        │
│  - Scores every recipe by pantry match percentage      │
│  - Cards sorted best-match first                       │
│  - Expand to see which ingredients you're missing      │
│  - One-click: add missing items to shopping list       │
│                                                        │
└────────────────────────────────────────────────────────┘
```

### Recipe Import

```
┌─ RECIPE IMPORT ────────────────────────────────────────┐
│                                                        │
│  Bulk-add recipes from external sources.               │
│                                                        │
│  - Import from JSON file (batch creation)              │
│  - Auto-creates ingredients with Romanian nutrition     │
│    data lookup                                         │
│  - Extract images from PDF cookbooks                   │
│  - Matches PDF images to recipe names automatically    │
│                                                        │
└────────────────────────────────────────────────────────┘
```

### OAuth Login

```
┌─ OAUTH LOGIN ─────────────────────────────────────────┐
│                                                        │
│  Sign in with your existing Google or GitHub account.  │
│                                                        │
│  - One-click social login buttons on the login page    │
│  - Automatic account creation on first OAuth login     │
│  - Links to existing account if email matches          │
│  - Fetches profile picture from OAuth provider         │
│  - Falls back to standard email + password login       │
│                                                        │
│  Providers: Google, GitHub                              │
│                                                        │
└────────────────────────────────────────────────────────┘
```

### User Profile

```
┌─ USER PROFILE ────────────────────────────────────────┐
│                                                        │
│  View and edit your account details.                   │
│                                                        │
│  - Change username and email                           │
│  - Upload a custom avatar image                        │
│  - View account info (role, join date, auth provider)  │
│  - Change password dialog (for local accounts)         │
│                                                        │
│  Route: /profile                                       │
│                                                        │
└────────────────────────────────────────────────────────┘
```

### Password Change & Reset

```
┌─ PASSWORD CHANGE & RESET ─────────────────────────────┐
│                                                        │
│  Change your password or recover a forgotten one.      │
│                                                        │
│  - In-app password change (current + new password)     │
│  - "Forgot Password" flow on the login page            │
│  - Email with secure reset link (15-min token expiry)  │
│  - Token verification + new password form              │
│                                                        │
│  Routes: /forgot-password, /reset-password             │
│                                                        │
└────────────────────────────────────────────────────────┘
```

### BMR / TDEE Calculator

```
┌─ BMR / TDEE CALCULATOR ───────────────────────────────┐
│                                                        │
│  Calculate your daily calorie needs with a wizard.     │
│                                                        │
│  - Multi-step wizard: gender → age → height → weight   │
│    → activity level → fitness goal                     │
│  - Mifflin-St Jeor equation for BMR                   │
│  - Activity multiplier for TDEE                        │
│  - Goal adjustment: Lose (−500), Maintain, Gain (+300) │
│  - Auto-calculates protein, carbs, fat targets         │
│  - Saves results to your nutrition targets             │
│                                                        │
│  Route: /bmr-wizard                                    │
│                                                        │
└────────────────────────────────────────────────────────┘
```

### Variable Nutrition Targets (Day Types)

```
┌─ VARIABLE NUTRITION TARGETS ──────────────────────────┐
│                                                        │
│  Different macro targets for different days.           │
│                                                        │
│  - 3 day types: Training, Rest, Default                │
│  - Set separate calorie/protein/carb/fat targets each  │
│  - Assign day types on the meal plan calendar          │
│  - Food log compares against that day's targets        │
│  - Settings page to manage all target profiles         │
│                                                        │
│  Route: /nutrition-settings                            │
│                                                        │
└────────────────────────────────────────────────────────┘
```

### Nutrition Trends

```
┌─ NUTRITION TRENDS ────────────────────────────────────┐
│                                                        │
│  See your nutrition history over time.                 │
│                                                        │
│  - Line chart: daily calorie intake over weeks         │
│  - Bar chart: protein / carbs / fat breakdown          │
│  - Powered by ApexCharts (rendered in Vaadin)          │
│  - Date range selection for custom periods             │
│  - Visual comparison against your targets              │
│                                                        │
│  Route: /nutrition-history                             │
│                                                        │
└────────────────────────────────────────────────────────┘
```

### Meal Plan Auto-Generation

```
┌─ MEAL PLAN AUTO-GENERATION ───────────────────────────┐
│                                                        │
│  Let the app fill your meal plan intelligently.        │
│                                                        │
│  - One-click auto-generate for a full week             │
│  - Smart scoring: macro fit (40-55%) + category match  │
│    (20%) + pantry preference (0-25%) + variety (15%)   │
│  - Respects day-type nutrition targets                  │
│  - Slot calorie distribution: Breakfast 25%,           │
│    Lunch 30%, Dinner 30%, Snack 15%                    │
│  - Option to keep existing meals or replace all        │
│  - Prefers recipes you can make with pantry items      │
│                                                        │
└────────────────────────────────────────────────────────┘
```

### Internationalization (i18n)

```
┌─ INTERNATIONALIZATION ────────────────────────────────┐
│                                                        │
│  Full app translation in two languages.                │
│                                                        │
│  - English (EN) — default                              │
│  - Romanian (RO) — complete translation                │
│  - Language preference saved per user                   │
│  - All UI labels, messages, and notifications          │
│    translated via resource bundles                      │
│                                                        │
└────────────────────────────────────────────────────────┘
```

### How Features Connect

```
                          ┌──────────┐
                          │ RECIPES  │
                          └────┬─────┘
                               │
            ┌──────────────────┼──────────────────┐
            │                  │                  │
            ▼                  ▼                  ▼
      ┌───────────┐     ┌──────────┐       ┌──────────────┐
      │INGREDIENTS│     │MEAL PLAN │       │  FOOD LOG    │
      └─────┬─────┘     └────┬─────┘       └──────┬───────┘
            │                 │                     │
            │    ┌────────────┤              tracks against
            │    │            │                     │
            │    │  auto-     ▼                     ▼
            │    │ generate  ┌──────────────┐  ┌──────────────────┐
            │    │           │SHOPPING LIST │  │NUTRITION TARGETS │
            │    │           └──────┬───────┘  └────────┬─────────┘
            │    │            subtracts                  │
            │    │                 │              set by │
            ▼    ▼                 ▼                     ▼
      ┌──────────┐          ┌──────────┐         ┌───────────┐
      │  PANTRY  │──────────│RECOMMEND.│         │BMR WIZARD │
      └──────────┘ matches  └──────────┘         └───────────┘

      ┌────────────────────────────────────────────────────┐
      │  OAUTH ──► USER ──► PROFILE ──► PASSWORD CHANGE    │
      │                       │                             │
      │                       ▼                             │
      │               NUTRITION TRENDS                      │
      │           (historical charts via Food Log)          │
      └────────────────────────────────────────────────────┘
```

> For complete API details on any feature, see `DOCUMENTATION.md` Sections 6-11.

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

## USER JOURNEYS

### Journey 1: "I want to plan my week"

```
  Plan meals         Generate list       Go shopping         Cook & log
 ┌──────────┐      ┌──────────────┐     ┌──────────┐      ┌──────────┐
 │ Open     │      │ Click        │     │ Share    │      │ Check    │
 │ Meal Plan│─────►│ "Generate    │────►│ list via │─────►│ off meals│
 │ Assign   │      │  from Plan"  │     │ QR code  │      │ in Food  │
 │ recipes  │      │              │     │ to phone │      │ Log      │
 │ to slots │      │ Pantry items │     │          │      │          │
 └──────────┘      │ subtracted   │     └──────────┘      │ See your │
                   │ automatically│                       │ daily    │
                   └──────────────┘                       │ nutrition│
                                                          └──────────┘

  Mon-Sun            Smart deficit         Scan & shop        Track macros
  calendar           calculation           on the go          vs targets
```

### Journey 2: "What can I cook tonight?"

```
  Check pantry       Get suggestions      Fill the gaps
 ┌──────────┐      ┌──────────────┐     ┌──────────────┐
 │ Update   │      │ Open         │     │ Click "Add   │
 │ pantry   │─────►│ Recommend-   │────►│ Missing to   │
 │ with     │      │ ations page  │     │ Shopping     │
 │ what you │      │              │     │ List"        │
 │ have     │      │ See recipes  │     │              │
 └──────────┘      │ ranked by    │     │ Go buy just  │
                   │ match %      │     │ what's needed│
                   └──────────────┘     └──────────────┘
```

### Journey 3: "Share my grocery list"

```
  ┌──────────────┐     ┌─────────────┐     ┌───────────────┐
  │ Shopping List │────►│ Click Share │────►│ QR Code       │
  │ is ready     │     │ button      │     │ appears in    │
  └──────────────┘     └─────────────┘     │ dialog        │
                                           │               │
                                           │ Scan with     │
                                           │ phone camera  │
                                           │       │       │
                                           └───────┼───────┘
                                                   ▼
                                           ┌───────────────┐
                                           │ Opens in phone│
                                           │ browser with  │
                                           │ checkable     │
                                           │ items list    │
                                           └───────────────┘
```

### Journey 4: "I want to set smart nutrition goals"

```
  Calculate BMR       Set day types        Track progress
 ┌──────────┐      ┌──────────────┐     ┌──────────────────┐
 │ Open     │      │ Go to        │     │ Log food daily   │
 │ BMR      │─────►│ Nutrition    │────►│ in Food Log      │
 │ Wizard   │      │ Settings     │     │                  │
 │          │      │              │     │ See progress     │
 │ Enter    │      │ Set Training │     │ bars vs targets  │
 │ weight,  │      │ / Rest /     │     │                  │
 │ height,  │      │ Default      │     │ Open Nutrition   │
 │ activity │      │ targets      │     │ Trends for       │
 │ & goal   │      │              │     │ weekly/monthly   │
 └──────────┘      └──────────────┘     │ charts           │
                                        └──────────────────┘
  Mifflin-St Jeor    Different macros      ApexCharts line
  equation            per day type          + bar charts
```

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

## PROJECT MAP

```
recipe-book/
│
├── backend/                        ← Quarkus backend (Java 21)
│   └── src/main/java/com/recipebook/
│       ├── entity/                 ← 12 entities + 4 enums (16 files)
│       ├── graphql/                ← 12 GraphQL resolvers + 12 inputs (24)
│       ├── service/                ← 18 business logic services
│       ├── dto/                    ← 27 response/request types
│       ├── rest/                   ← Image upload + OAuth callback
│       └── exception/              ← Custom exception hierarchy (5)
│   └── src/main/resources/
│       └── db/migration/           ← 8 Flyway SQL migrations
│   └── src/test/                   ← 58 tests across 10 test classes
│
├── frontend/                       ← Vaadin frontend (Spring Boot)
│   └── src/main/
│       ├── java/com/recipebook/
│       │   ├── views/              ← Layout, Dashboard, Login, Register,
│       │   │                         Profile, BMR Wizard, Settings,
│       │   │                         OAuth, Forgot/Reset Password (11)
│       │   ├── recipe/             ← Recipe views + service (8)
│       │   ├── ingredient/         ← Ingredient views + service (4)
│       │   ├── mealplan/           ← Meal plan calendar + service (5)
│       │   ├── pantry/             ← Pantry management + service (4)
│       │   ├── shopping/           ← Shopping list + QR sharing (6)
│       │   ├── foodlog/            ← Food logging + nutrition (7)
│       │   ├── recommendation/     ← Recipe recommendations (4)
│       │   ├── nutritionhistory/   ← Nutrition trend charts (3)
│       │   ├── service/            ← ApiClient, AuthService, BMR,
│       │   │                         NutritionTarget, ErrorHandler (6)
│       │   ├── dto/                ← Auth + BMR + OAuth DTOs (7)
│       │   ├── ui/                 ← Shared components (MacroBar)
│       │   └── i18n/               ← TranslationProvider (EN + RO)
│       ├── resources/i18n/         ← messages.properties, messages_ro
│       └── frontend/themes/
│           └── recipe-book/        ← Dark theme (9 CSS files)
│
├── docker-compose.yml              ← PostgreSQL + Adminer (dev)
├── docker-compose.prod.yml         ← Full stack + nginx (production)
├── nginx/                          ← Reverse proxy + TLS certs
├── .env.example                    ← Environment template
├── DOCUMENTATION.md                ← Full technical reference
└── GUIDE.md                        ← You are here!
```

```
┌───────────────────────────────────────────────────────┐
│  Backend:  92 Java files  │  Frontend: 68 Java files  │
│  Tests:    10 test files  │  Migrations: 8 SQL files  │
│  Theme:     9 CSS files   │  Total: ~180+ files       │
└───────────────────────────────────────────────────────┘
```

> For the complete file inventory, see `DOCUMENTATION.md` Section 15.

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

## DATABASE AT A GLANCE

### Entity Relationships

```
                           ┌──────────┐
                           │   User   │
                           └────┬─────┘
    ┌────────┬──────┬───┬───┴───┬──────────┬───────────┬──────────┐
    ▼        ▼      ▼   ▼       ▼          ▼           ▼          ▼
┌────────┐┌────┐┌─────┐┌────────┐┌──────────┐┌────────┐┌─────────┐
│ Recipe ││Meal││Panty││Shopping││Nutrition ││Food   ││UserDay │
│        ││Plan││Item ││List   ││Target   ││Log    ││Type    │
└───┬────┘└────┘└──┬──┘│Item   │└──────────┘└───┬────┘└─────────┘
    │              │   └───┬────┘                │
    ▼              │       │                     │
┌───────────┐      ▼       ▼                     ▼
│  Recipe   │┌────────────────────────────────────────┐
│ Ingredient│►│             Ingredient                │
└───────────┘└────────────────────────────────────────┘
```

**12 tables total** — All user-specific data is scoped by `userId` for multi-user support.

New entities since v1:
- **FoodLog** — tracks what you eat (recipe, ingredient, or custom food)
- **UserNutritionTarget** — per-day-type macro goals (Training / Rest / Default)
- **UserDayType** — assigns a day type (Training/Rest) to a specific date

### Enums Quick Reference

```
┌──────────────────┬──────────────────────────────────────────────┐
│ Unit             │ GRAMS, KG, ML, LITERS, PIECES, TBSP, TSP,   │
│                  │ CUPS                                         │
├──────────────────┼──────────────────────────────────────────────┤
│ RecipeCategory   │ BREAKFAST, LUNCH, DINNER, DESSERT, SNACK,    │
│                  │ OTHER                                        │
├──────────────────┼──────────────────────────────────────────────┤
│ Difficulty       │ EASY, MEDIUM, HARD                           │
├──────────────────┼──────────────────────────────────────────────┤
│ IngredientCat.   │ DAIRY, MEAT, VEGETABLES, FRUITS, GRAINS,     │
│                  │ SPICES, OILS, BEVERAGES, OTHER               │
├──────────────────┼──────────────────────────────────────────────┤
│ MealSlot         │ BREAKFAST, LUNCH, DINNER, SNACK              │
├──────────────────┼──────────────────────────────────────────────┤
│ FoodSourceType   │ RECIPE, INGREDIENT, CUSTOM                   │
├──────────────────┼──────────────────────────────────────────────┤
│ DayType          │ DEFAULT, TRAINING, REST                      │
└──────────────────┴──────────────────────────────────────────────┘

User entity also stores (as strings):
  AuthProvider:  LOCAL, GOOGLE, GITHUB
  ActivityLevel: SEDENTARY, LIGHTLY_ACTIVE, MODERATE, ACTIVE, VERY_ACTIVE
  FitnessGoal:   LOSE, MAINTAIN, GAIN
```

> For full column details on every table, see `DOCUMENTATION.md` Section 4.

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

## THE DARK THEME

The app uses a custom dark theme built on Vaadin's Lumo design system.

### Color Palette

```
┌────────────────────────────────────────────────────────────┐
│                                                            │
│   Background     ████  #1a1f2b  (dark navy)               │
│   Card Surface   ████  #232838  (slightly lighter)        │
│   Primary        ████  #c08050  (warm bronze/copper)      │
│   Success        ████  #5a9e7e  (muted green)             │
│   Error          ████  #c75050  (soft red)                 │
│   Warning        ████  #c0903c  (gold/amber)              │
│   Body Text      ████  #d8dce4  (light gray)              │
│   Muted Text     ████  #8890a0  (secondary gray)          │
│                                                            │
│   Font: System default + JetBrains Mono for labels/tags   │
│                                                            │
└────────────────────────────────────────────────────────────┘
```

### Macro Color Coding

Throughout the app, nutrition macros are consistently color-coded:

```
  Calories ── #c75050 (red)      ██████
  Protein  ── #c08050 (bronze)   ██████
  Carbs    ── #5a9e7e (green)    ██████
  Fat      ── #c0903c (gold)     ██████
```

### Meal Slot Colors

```
  Breakfast ── #c0903c (gold)
  Lunch     ── #5a9e7e (green)
  Dinner    ── #c08050 (bronze)
  Snack     ── #8890a0 (gray)
```

> For complete CSS details and component overrides, see `DOCUMENTATION.md` Section 12.

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

## DEVELOPER CHEAT SHEET

### Common Commands

```
┌────────────────────────────────────────────────────────────────┐
│  START                                                        │
│    docker-compose up -d              Start database            │
│    cd backend && ./mvnw quarkus:dev  Start backend (dev)      │
│    cd frontend && ./mvnw             Start frontend (dev)     │
│                                                                │
│  STOP                                                         │
│    docker-compose down               Stop database             │
│    docker-compose down -v            Stop + delete data        │
│    Ctrl+C                            Stop backend/frontend     │
│                                                                │
│  BUILD                                                        │
│    cd backend && ./mvnw package -DskipTests                   │
│    cd frontend && ./mvnw -Pproduction package                 │
│                                                                │
│  DATABASE                                                     │
│    docker exec -i recipebook-postgres \                       │
│      pg_dump -U recipebook_user recipebook > backup.sql       │
│    docker exec -i recipebook-postgres \                       │
│      psql -U recipebook_user recipebook < backup.sql          │
│                                                                │
│  TEST                                                         │
│    cd backend && ./mvnw test         58 integration tests     │
│    cd tests && npx playwright test   E2E browser tests        │
└────────────────────────────────────────────────────────────────┘
```

### Default Credentials

```
┌─────────┬──────────────────────────┐
│ User    │ chef_admin               │
│ Email   │ admin@recipebook.com     │
│ Pass    │ admin123                 │
│ Role    │ ADMIN                    │
└─────────┴──────────────────────────┘
```

### Port Reference

```
┌──────────┬───────┬──────────────────────────────────┐
│ Service  │ Port  │ URL                              │
├──────────┼───────┼──────────────────────────────────┤
│ Frontend │ 8081  │ http://localhost:8081             │
│ Backend  │ 8080  │ http://localhost:8080             │
│ GraphQL  │ 8080  │ http://localhost:8080/q/graphql-ui│
│ Adminer  │ 8082  │ http://localhost:8082             │
│ Postgres │ 5432  │ localhost:5432                    │
└──────────┴───────┴──────────────────────────────────┘
```

### Where to Find Things

```
┌──────────────────────────┬──────────────────────────────────────┐
│ I want to...             │ Look at...                           │
├──────────────────────────┼──────────────────────────────────────┤
│ Add a new entity         │ backend/.../entity/                  │
│ Add a GraphQL endpoint   │ backend/.../graphql/                 │
│ Change business logic    │ backend/.../service/                 │
│ Add a new page           │ frontend/.../views/ or feature pkg   │
│ Change the theme         │ frontend/.../themes/recipe-book/     │
│ Modify the DB schema     │ Add a Flyway migration in            │
│                          │ backend/.../db/migration/            │
│ Check environment vars   │ .env.example                         │
│ Read the full docs       │ DOCUMENTATION.md                     │
│ See the architecture     │ ARCHITECTURE.md                      │
│ Check the roadmap        │ ROADMAP.md                           │
└──────────────────────────┴──────────────────────────────────────┘
```

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

## WHAT'S NEXT?

Planned features that haven't been built yet:

- **Recipe sharing** — share recipes between users
- **Meal plan templates** — save and reuse weekly plans
- **Recipe scaling** — dynamically adjust servings and quantities
- **Mobile-responsive PWA** — optimized for phone screens
- **Admin dashboard** — user management for admins
- **Notification system** — expiry alerts, meal reminders

> See `ROADMAP.md` for the full development roadmap.

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

*This guide covers the highlights. For the complete technical reference (every entity field, every GraphQL query, every DTO), see [DOCUMENTATION.md](DOCUMENTATION.md).*
