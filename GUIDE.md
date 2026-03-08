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
│   9 tables. All user data is scoped by userId.               │
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

### How Features Connect

```
                    ┌──────────┐
                    │ RECIPES  │
                    └────┬─────┘
                         │
          ┌──────────────┼──────────────┐
          │              │              │
          ▼              ▼              ▼
    ┌───────────┐  ┌──────────┐  ┌──────────────┐
    │INGREDIENTS│  │MEAL PLAN │  │  FOOD LOG    │
    └─────┬─────┘  └────┬─────┘  └──────────────┘
          │              │              │
          │              ▼              │ tracks against
          │       ┌──────────────┐     ▼
          │       │SHOPPING LIST │  ┌──────────────────┐
          │       └──────┬───────┘  │NUTRITION TARGETS │
          │              │          └──────────────────┘
          │         subtracts
          │              │
          ▼              ▼
    ┌──────────┐   ┌──────────┐
    │  PANTRY  │───│RECOMMEND.│
    └──────────┘   └──────────┘
      matches ──────►
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

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

## PROJECT MAP

```
recipe-book/
│
├── backend/                        ← Quarkus backend (Java 21)
│   └── src/main/java/com/recipebook/
│       ├── entity/                 ← 9 database entities + enums
│       ├── graphql/                ← 9 GraphQL resolver classes
│       ├── service/                ← Business logic services
│       ├── dto/                    ← Input/Response types (22)
│       └── rest/                   ← Image upload REST endpoint
│
├── frontend/                       ← Vaadin frontend (Spring Boot)
│   └── src/main/
│       ├── java/com/recipebook/
│       │   ├── views/              ← Layout, Dashboard, Login, Register
│       │   ├── recipe/             ← Recipe views + service
│       │   ├── ingredient/         ← Ingredient views + service
│       │   ├── mealplan/           ← Meal plan calendar + service
│       │   ├── pantry/             ← Pantry management + service
│       │   ├── shopping/           ← Shopping list + QR sharing
│       │   ├── foodlog/            ← Food logging + nutrition
│       │   ├── recommendation/     ← Recipe recommendations
│       │   └── service/            ← ApiClient, AuthService
│       └── frontend/themes/
│           └── recipe-book/        ← Dark theme (CSS)
│
├── docker-compose.yml              ← PostgreSQL + Adminer
├── .env.example                    ← Environment template
├── DOCUMENTATION.md                ← Full technical reference (2600 lines)
└── GUIDE.md                        ← You are here!
```

```
┌──────────────────────────────────────────────────────┐
│  Backend: 69 Java files   │  Frontend: 52 Java files │
│  Theme: 9 CSS files       │  Total: ~173 files       │
└──────────────────────────────────────────────────────┘
```

> For the complete file inventory, see `DOCUMENTATION.md` Section 15.

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

## DATABASE AT A GLANCE

### Entity Relationships

```
                       ┌──────────┐
                       │   User   │
                       └────┬─────┘
        ┌────────┬──────┬───┴───┬──────────┬────────────┐
        ▼        ▼      ▼       ▼          ▼            ▼
    ┌────────┐ ┌────┐ ┌─────┐ ┌────────┐ ┌──────────┐ ┌────────┐
    │ Recipe │ │Meal│ │Panty│ │Shopping│ │Nutrition │ │Food   │
    │        │ │Plan│ │Item │ │List    │ │Target    │ │Log    │
    └───┬────┘ └────┘ └──┬──┘ │Item    │ └──────────┘ └───┬────┘
        │                │    └───┬────┘                   │
        ▼                │        │                        │
  ┌───────────┐          ▼        ▼                        ▼
  │  Recipe   │    ┌────────────────────────────────────────┐
  │ Ingredient│───►│              Ingredient                │
  └───────────┘    └────────────────────────────────────────┘
```

**9 tables total** — All user-specific data is scoped by `userId` for multi-user support.

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
└──────────────────┴──────────────────────────────────────────────┘
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
│    cd tests && npx playwright test                            │
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
│ Modify the DB schema     │ Just change the entity — Hibernate   │
│                          │ auto-updates on restart              │
│ Check environment vars   │ .env.example                         │
│ Read the full docs       │ DOCUMENTATION.md                     │
│ See the architecture     │ ARCHITECTURE.md                      │
│ Check the roadmap        │ ROADMAP.md                           │
└──────────────────────────┴──────────────────────────────────────┘
```

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

## WHAT'S NEXT?

Planned features that haven't been built yet:

- **User profile editing** — change username, email, password
- **Recipe sharing** — share recipes between users
- **Meal plan templates** — save and reuse weekly plans
- **Recipe scaling** — dynamically adjust servings and quantities
- **Nutritional goal wizard** — BMR/TDEE calculator to set smart targets
- **Mobile-responsive PWA** — optimized for phone screens

> See `ROADMAP.md` for the full development roadmap.

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

*This guide covers the highlights. For the complete technical reference (every entity field, every GraphQL query, every DTO), see [DOCUMENTATION.md](DOCUMENTATION.md).*
