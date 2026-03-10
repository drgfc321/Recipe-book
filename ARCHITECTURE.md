# Recipe Book — Project Architecture Report

## How the System Works (High-Level)

```
┌─────────────────────────────────────────────────────────────────────────┐
│                           USER (Browser)                                │
│                                                                         │
│   Vaadin renders server-side Java components as interactive HTML/JS     │
│   in the browser automatically — no manual REST calls from frontend     │
└──────────────────────────────┬──────────────────────────────────────────┘
                               │ WebSocket (Vaadin Push)
                               ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                     FRONTEND  (Spring Boot + Vaadin)                    │
│                          localhost:8081                                  │
│                                                                         │
│  Views (Java UI)  →  Services  →  ApiClient  ──GraphQL POST──►         │
│                                                                         │
└──────────────────────────────┬──────────────────────────────────────────┘
                               │ HTTP POST /graphql (JSON + JWT Bearer)
                               ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                     BACKEND  (Quarkus + SmallRye)                       │
│                          localhost:8080                                  │
│                                                                         │
│  GraphQL Resolvers  →  Services  →  Entities (Panache ORM)  →  DB      │
│                                                                         │
└──────────────────────────────┬──────────────────────────────────────────┘
                               │ JDBC
                               ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                     PostgreSQL  (localhost:5432)                         │
│                     Database: recipebook                                 │
└─────────────────────────────────────────────────────────────────────────┘
```

### How Vaadin Works
- **Server-side rendering**: All UI logic runs in Java on the server. You write `new Button("Save")` in Java, and Vaadin sends the HTML to the browser via WebSocket.
- **No REST API on the frontend**: The frontend doesn't expose endpoints. Users interact with Vaadin components in the browser; events (clicks, typing) are sent to the server, which updates the UI.
- **Routes**: `@Route("pantry")` maps the URL `/pantry` to a Java view class. `MainLayout.class` wraps all routes in a shared sidebar + header.

### How Quarkus Works
- **CDI-based**: Uses `@ApplicationScoped` for singletons, `@Inject` for dependency injection.
- **Panache ORM**: Entities extend `PanacheEntity` — provides `findById()`, `listAll()`, `persist()`, `delete()` for free. No repository classes needed.
- **SmallRye GraphQL**: Annotate a class with `@GraphQLApi`, methods with `@Query`/`@Mutation`, and the schema is auto-generated. No `.graphql` schema files.
- **JWT Security**: `@Authenticated` on methods requires a valid JWT. `JsonWebToken` bean gives you the user's claims.
- **Flyway Migrations**: Database schema managed via versioned SQL files in `db/migration/` (8 migrations). Replaced Hibernate auto-DDL.
- **Caffeine Caching**: `@CacheResult` / `@CacheInvalidateAll` on services. Named caches: `weekly-mealplan`, `ingredients-cache`, `recommendations-cache`.

---

## Authentication Flow

```
  Browser                    Frontend (8081)                 Backend (8080)
    │                            │                               │
    │  email + password          │                               │
    ├───────────────────────────►│                               │
    │                            │  GraphQL: login(email, pass)  │
    │                            ├──────────────────────────────►│
    │                            │                               │  verify password
    │                            │                               │  generate JWT (24h)
    │                            │  { token, username, role }    │
    │                            │◄──────────────────────────────┤
    │                            │                               │
    │                            │  store JWT in VaadinSession   │
    │  redirect to Dashboard     │                               │
    │◄───────────────────────────┤                               │
    │                            │                               │
    │  (subsequent requests)     │                               │
    │                            │  Authorization: Bearer {JWT}  │
    │                            ├──────────────────────────────►│
```

### OAuth Authentication Flow (Google / GitHub)

```
  Browser                    Frontend (8081)                 Backend (8080)          OAuth Provider
    │                            │                               │                       │
    │  click "Login with Google" │                               │                       │
    ├───────────────────────────►│                               │                       │
    │                            │  GET /api/oauth/google/url    │                       │
    │                            ├──────────────────────────────►│                       │
    │                            │  { authUrl }                  │                       │
    │                            │◄──────────────────────────────┤                       │
    │  redirect to Google        │                               │                       │
    │◄───────────────────────────┤                               │                       │
    │                            │                               │                       │
    │  user consents ────────────┼───────────────────────────────┼──────────────────────►│
    │                            │                               │                       │
    │  redirect with ?code=...   │                               │                       │
    ├───────────────────────────►│  /oauth-callback              │                       │
    │                            │  POST /api/oauth/google/cb    │                       │
    │                            ├──────────────────────────────►│  exchange code        │
    │                            │                               ├──────────────────────►│
    │                            │                               │  { access_token }     │
    │                            │                               │◄──────────────────────┤
    │                            │                               │  fetch profile        │
    │                            │                               ├──────────────────────►│
    │                            │                               │  { email, name, pic } │
    │                            │                               │◄──────────────────────┤
    │                            │                               │                       │
    │                            │                               │  find/create user     │
    │                            │                               │  generate JWT         │
    │                            │  { token, username, role }    │                       │
    │                            │◄──────────────────────────────┤                       │
    │                            │  store JWT in VaadinSession   │                       │
    │  redirect to Dashboard     │                               │                       │
    │◄───────────────────────────┤                               │                       │
```

---

## Backend Structure (Quarkus)

```
backend/src/main/java/com/recipebook/
│
├── entity/                         ◄── DATABASE MODELS (Hibernate Panache) — 16 files
│   ├── User.java                       email, username, passwordHash, role, avatarUrl,
│   │                                   authProvider, weightKg, heightCm, birthDate,
│   │                                   gender, activityLevel, fitnessGoal,
│   │                                   passwordResetToken, passwordResetExpiry
│   ├── Recipe.java                     name, category, difficulty, timing, owner→User
│   ├── Ingredient.java                 name, category, macros per 100g
│   ├── RecipeIngredient.java           recipe→Recipe, ingredient→Ingredient, qty, unit
│   ├── PantryItem.java                 user→User, ingredient→Ingredient, qty, expiration
│   ├── MealPlan.java                   user→User, date, mealSlot, recipe→Recipe
│   ├── ShoppingListItem.java          user→User, ingredient, qty, purchased, weekStart
│   ├── FoodLog.java                    user→User, date, mealSlot, recipe/ingredient/custom
│   ├── UserNutritionTarget.java        user→User, dayType, calories, protein, carbs, fat
│   ├── UserDayType.java                user→User, date, dayType (Training/Rest/Default)
│   ├── Difficulty.java                 enum: EASY, MEDIUM, HARD
│   ├── RecipeCategory.java             enum: BREAKFAST, LUNCH, DINNER, DESSERT, SNACK, OTHER
│   ├── IngredientCategory.java         enum: DAIRY, MEAT, VEGETABLES, FRUITS, ...
│   ├── Unit.java                       enum: GRAMS, KILOGRAMS, PIECES, CUPS, ...
│   ├── MealSlot.java                   enum: BREAKFAST, LUNCH, DINNER, SNACK
│   └── DayType.java                    enum: DEFAULT, TRAINING, REST
│
├── graphql/                        ◄── GRAPHQL API LAYER — 24 files
│   ├── AuthGraphQL.java                login/register, me/users, password change/reset
│   ├── RecipeGraphQL.java              recipes CRUD, filtered queries
│   ├── IngredientGraphQL.java          ingredients CRUD, filtered queries (cached)
│   ├── PantryGraphQL.java              pantryItems, expiringItems, add/update/remove
│   ├── MealPlanGraphQL.java            weeklyMealPlan, assign/remove meals
│   ├── ShoppingListGraphQL.java        shoppingList, generate/toggle/clear
│   ├── RecommendationGraphQL.java      recipeRecommendations, addMissingToList
│   ├── BMRGraphQL.java                 BMR/TDEE calculation + save physical data
│   ├── FoodLogGraphQL.java             daily food log CRUD
│   ├── NutritionTargetGraphQL.java     get/update nutrition targets per day type
│   ├── DayTypeGraphQL.java             get/set day types for dates
│   ├── NutritionHistoryGraphQL.java    historical nutrition data for charts
│   ├── RecipeImportGraphQL.java        bulk recipe import from JSON
│   ├── RecipeInput.java                @Input DTO for create/update recipe
│   ├── RecipeIngredientInput.java      @Input DTO for recipe ingredients
│   ├── IngredientInput.java            @Input DTO for create/update ingredient
│   ├── PantryItemInput.java            @Input DTO for add pantry item
│   ├── PantryItemUpdateInput.java      @Input DTO for update pantry item
│   ├── MealPlanInput.java              @Input DTO for assign meal
│   ├── ShoppingListItemInput.java      @Input DTO for add shopping item
│   ├── RecommendationFilterInput.java  @Input DTO for recommendation filters
│   ├── FoodLogInput.java               @Input DTO for food log entries
│   ├── BMRWizardInput.java             @Input DTO for BMR wizard data
│   └── AutoGenerateInput.java          @Input DTO for meal plan auto-generation
│
├── dto/                            ◄── RESPONSE DTOs — 27 files
│   ├── AuthResponse.java               token, userId, username, email, role
│   ├── LoginRequest.java               email, password (validated)
│   ├── RegisterRequest.java            email, username, password, language
│   ├── RecipeResponse.java             full recipe + ingredients + macros
│   ├── RecipeRequest.java              recipe create/update request
│   ├── RecipeIngredientResponse.java   ingredient detail + macros for recipe
│   ├── RecipeIngredientRequest.java    ingredient input for recipes
│   ├── MacroInfo.java                  calories, protein, carbs, fat
│   ├── MealPlanResponse.java           id, date, mealSlot, recipe
│   ├── DailyMealPlanResponse.java      date, meals[], dailyMacros
│   ├── WeeklyMealPlanResponse.java     weekStart/End, days[], totalMacros, avgMacros
│   ├── PantryItemResponse.java         ingredient info + qty + expiration + expiringSoon
│   ├── ShoppingListItemResponse.java   ingredient info + qty + purchased
│   ├── ShoppingListResponse.java       items[] + totalItems + progress%
│   ├── RecipeRecommendationResponse.java  recipe + match% + missing list
│   ├── MissingIngredientResponse.java  what's needed vs what's in pantry
│   ├── BMRResultResponse.java          bmr, tdee, adjustedCalories, macros
│   ├── UserPhysicalDataResponse.java   weight, height, birthDate, gender, etc.
│   ├── UserNutritionTargetResponse.java  dayType + calorie/macro targets
│   ├── FoodLogResponse.java            food log entry with computed macros
│   ├── DailyFoodLogResponse.java       entries + totals + targets + planned meals
│   ├── DailyNutritionSummary.java      date + totals for trend charts
│   ├── FoodSourceType.java             enum: RECIPE, INGREDIENT, CUSTOM
│   ├── PlannedMealStatus.java          planned meal + logged status
│   ├── DayTypeEntry.java               date + dayType pairing
│   ├── OAuthUserProfile.java           OAuth provider profile data
│   └── ErrorMessage.java               error wrapper
│
├── service/                        ◄── BUSINESS LOGIC — 18 files
│   ├── PasswordService.java            BCrypt hash/verify
│   ├── TokenService.java               JWT generation (SmallRye, RSA, 24h expiry)
│   ├── MacroCalculationService.java    unit→grams conversion, macro math per recipe
│   ├── PantryService.java              CRUD + auto-merge + expiration checks
│   ├── MealPlanService.java            weekly plans + daily/weekly macro aggregation
│   ├── ShoppingListService.java        generate from mealplan−pantry, toggle, clear
│   ├── RecommendationService.java      rank recipes by pantry match%, find missing
│   ├── BMRCalculatorService.java       Mifflin-St Jeor BMR + TDEE + macro split
│   ├── NutritionTargetService.java     per-day-type targets with DEFAULT fallback
│   ├── NutritionHistoryService.java    daily nutrition summaries for trend charts
│   ├── DayTypeService.java             assign Training/Rest/Default to dates
│   ├── FoodLogService.java             food log CRUD + daily totals vs targets
│   ├── MealPlanAutoGenerateService.java  greedy algorithm: macro fit + pantry + variety
│   ├── OAuthService.java               Google + GitHub OAuth: token exchange, profiles
│   ├── RecipeImportService.java        JSON bulk import + ingredient matching
│   ├── PdfImageExtractorService.java   extract images from PDF cookbooks
│   ├── RomanianNutritionData.java      Romanian ingredient nutrition lookup
│   └── DataSeeder.java                 seeds admin user + 25 ingredients + 5 recipes
│
├── rest/                           ◄── REST ENDPOINTS
│   ├── ImageResource.java              POST /api/images (upload), GET /api/images/{file}
│   └── OAuthResource.java              GET /api/oauth/{provider}/url, POST callback
│
├── exception/                     ◄── CUSTOM EXCEPTIONS (5 files)
│   ├── RecipeBookException.java        Base exception (replaces raw GraphQLException)
│   ├── NotFoundException.java          Entity not found
│   ├── ValidationException.java        Input validation errors
│   ├── AuthorizationException.java     Permission denied
│   └── OAuthException.java             OAuth flow errors
│
└── resources/
    ├── application.properties          DB config, CORS, JWT keys, ports, OAuth, caching
    ├── privateKey.pem                  JWT signing key
    ├── publicKey.pem                   JWT verification key
    └── db/migration/                   Flyway versioned migrations (V1–V8)
        ├── V1__Initial_schema.sql          Base tables
        ├── V2__Add_indexes.sql             Performance indexes
        ├── V3__Add_avatar_url.sql          User avatar support
        ├── V4__Add_password_reset_token.sql  Password reset fields
        ├── V5__Add_oauth_provider.sql      OAuth provider + providerId
        ├── V6__Add_bmr_fields.sql          Physical data on User
        ├── V7__Add_day_type_targets.sql    UserDayType + UserNutritionTarget
        └── V8__Drop_old_user_nutrition_unique.sql
```

### Entity Relationships (Database) — 12 tables

```
┌──────────────────┐  owns   ┌──────────┐  contains  ┌──────────────────┐
│      User        │────────►│  Recipe   │───────────►│ RecipeIngredient │
│                  │         │           │            │                  │
│ id               │         │ id        │            │ recipe_id (FK)   │
│ email            │         │ name      │            │ ingredient_id(FK)│
│ username         │         │ category  │            │ quantity         │
│ passHash         │         │ difficulty│            │ unit             │
│ role             │         │ timing    │            └────────┬─────────┘
│ avatarUrl        │         │ servings  │                     │
│ authProvider     │         │ owner(FK) │                     │ references
│ providerId       │         └───────────┘                     ▼
│ language         │                                   ┌──────────────┐
│ weightKg         │                                   │  Ingredient  │
│ heightCm         │  has pantry                       │              │
│ birthDate        ├──────────────►┌──────────────┐    │ id           │
│ gender           │               │  PantryItem  │───►│ name         │
│ activityLevel    │               │ user_id (FK) │    │ category     │
│ fitnessGoal      │               │ ingredient_id│    │ cal/prot/    │
│ passwordReset-   │               │ quantity     │    │ carbs/fat    │
│   Token/Expiry   │               │ expiration   │    └──────────────┘
│ createdAt        │               └──────────────┘           ▲
│ lastLogin        │                                          │
└──────┬───────────┘  has meal plan                           │
       │              ├──────────────►┌──────────────┐        │
       │              │               │   MealPlan   │        │
       │              │               │ user_id (FK) │        │
       │              │               │ date         │        │
       │              │               │ mealSlot     │        │
       │              │               │ recipe_id(FK)│        │
       │              │               └──────────────┘        │
       │              │                                       │
       │  has shopping list                                   │
       │              ├──────────►┌──────────────────┐        │
       │              │           │ ShoppingListItem │────────┘
       │              │           │ user_id (FK)     │
       │              │           │ ingredient_id    │
       │              │           │ quantity, unit   │
       │              │           │ purchased        │
       │              │           │ weekStartDate    │
       │              │           └──────────────────┘
       │              │
       │  has food log                          (NEW in v2)
       │              ├──────────►┌──────────────────┐
       │              │           │     FoodLog      │
       │              │           │ user_id (FK)     │
       │              │           │ date, mealSlot   │
       │              │           │ recipe/ingredient│
       │              │           │ /custom food     │
       │              │           │ servings         │
       │              │           │ sourceMealPlanId │
       │              │           └──────────────────┘
       │              │
       │  has nutrition targets                 (NEW in v2)
       │              ├──────────►┌──────────────────────┐
       │              │           │ UserNutritionTarget  │
       │              │           │ user_id (FK)         │
       │              │           │ dayType (enum)       │
       │              │           │ calories, protein,   │
       │              │           │ carbs, fat           │
       │              │           └──────────────────────┘
       │              │
       │  has day types                         (NEW in v2)
       │              └──────────►┌──────────────────┐
       │                          │   UserDayType    │
       │                          │ user_id (FK)     │
       │                          │ date             │
       │                          │ dayType (enum)   │
       │                          └──────────────────┘
       │
       │  Unique constraints:
       │    UserNutritionTarget: (user_id, day_type)
       │    UserDayType: (user_id, day_date)
       └─────────────────────────────────────────────
```

### Key Backend Patterns

| Pattern | How it works |
|---------|-------------|
| **Entity** | `class Recipe extends PanacheEntity` → auto `id`, `persist()`, `findById()`, `listAll()` |
| **Query** | `@Query` on method → auto-added to GraphQL schema. `PantryItem.find("user", user).list()` |
| **Mutation** | `@Mutation` + `@Transactional` → write operations with rollback on error |
| **Auth** | `@Authenticated` + `@Inject JsonWebToken jwt` → `jwt.getSubject()` gives userId |
| **Input DTO** | `@Input("PantryItemInput")` class → becomes GraphQL input type automatically |
| **Macro calc** | `MacroCalculationService.toGrams(qty, unit)` converts everything to grams, then `grams * (per100g / 100)` |
| **Caching** | `@CacheResult(cacheName = "...")` caches, `@CacheInvalidateAll` invalidates. Caffeine-backed |
| **Exception** | Custom `RecipeBookException` hierarchy replaces raw `GraphQLException` for typed error handling |
| **OAuth** | `OAuthService` exchanges auth codes for tokens, fetches profiles, finds/creates users |

---

## Frontend Structure (Vaadin + Spring Boot)

```
frontend/src/main/java/com/recipebook/
│
├── Application.java                ◄── ENTRY POINT (@SpringBootApplication + @Theme)
│
├── service/                        ◄── INFRASTRUCTURE (6 files)
│   ├── ApiClient.java                  Central GraphQL HTTP client (WebClient)
│   │                                   • query() / mutate() with JWT from VaadinSession
│   │                                   • Parses errors, extracts data fields
│   ├── AuthService.java               Login/register/logout + session management
│   │                                   • Stores JWT + UserInfo in VaadinSession
│   ├── BMRService.java                 Frontend BMR/TDEE API calls
│   ├── NutritionTargetUIService.java   Frontend nutrition target API calls
│   ├── CustomErrorHandler.java         Global error handling for Vaadin
│   └── CustomVaadinServiceInitListener.java  Service lifecycle hooks
│
├── dto/                            ◄── AUTH + CONFIG DTOs (7 files)
│   ├── AuthResponse.java               token, userId, username, email, role
│   ├── LoginRequest.java               email, password
│   ├── RegisterRequest.java            email, username, password, language
│   ├── UserInfo.java                   id, username, email, role (Serializable)
│   ├── BMRResultResponse.java          BMR/TDEE calculation results
│   ├── UserPhysicalDataResponse.java   weight, height, birthDate, etc.
│   └── OAuthConfig.java                OAuth provider configuration
│
├── views/                          ◄── CORE VIEWS (11 files)
│   ├── MainLayout.java                 AppLayout shell: header + sidebar nav + auth guard
│   ├── DashboardView.java             Route "/" — stats cards, recent recipes, quick actions
│   ├── LoginView.java                 Route "/login" — email + password + OAuth buttons
│   ├── RegisterView.java             Route "/register" — signup form
│   ├── ProfileView.java              Route "/profile" — edit username, email, avatar
│   ├── BMRWizardView.java            Route "/bmr-wizard" — multi-step BMR/TDEE calculator
│   ├── NutritionSettingsView.java     Route "/nutrition-settings" — day type targets
│   ├── ChangePasswordDialog.java      Dialog for in-app password change
│   ├── ForgotPasswordView.java        Route "/forgot-password" — enter email
│   ├── ResetPasswordView.java         Route "/reset-password" — token + new password
│   └── OAuthCallbackView.java         Route "/oauth-callback" — handles OAuth return
│
├── recipe/                         ◄── RECIPE FEATURE
│   ├── RecipeResponse.java             record: full recipe data
│   ├── RecipeIngredientResponse.java   record: ingredient in a recipe
│   ├── MacroInfo.java                  record: calories, protein, carbs, fat
│   ├── IngredientOption.java           record: id, name, category (for dropdowns)
│   ├── RecipeService.java              GraphQL CRUD + ingredient options
│   ├── RecipeListView.java             Route "/recipes" — card grid + filters
│   ├── RecipeDetailView.java           Route "/recipes/{id}" — full recipe page
│   └── RecipeFormDialog.java           Dialog: create/edit recipe with image upload
│
├── ingredient/                     ◄── INGREDIENT FEATURE
│   ├── IngredientResponse.java         record: id, name, category, macros
│   ├── IngredientService.java          GraphQL CRUD with search/filter
│   ├── IngredientListView.java         Route "/ingredients" — data grid + filters
│   └── IngredientFormDialog.java       Dialog: create/edit ingredient
│
├── mealplan/                       ◄── MEAL PLANNING FEATURE
│   ├── MealPlanResponse.java           record: id, date, mealSlot, recipe
│   ├── DailyMealPlanResponse.java      record: date, meals[], dailyMacros
│   ├── WeeklyMealPlanResponse.java     record: week range, days[], total+avg macros
│   ├── MealPlanService.java            GraphQL: weekly plan, assign/remove meals
│   └── MealPlanView.java              Route "/meal-plan" — 7-day calendar grid
│
├── pantry/                         ◄── PANTRY FEATURE
│   ├── PantryItemResponse.java         record: ingredient + qty + expiration
│   ├── PantryService.java              GraphQL: list, expiring, add/update/remove
│   ├── PantryView.java                Route "/pantry" — grid + expiring banner
│   └── PantryFormDialog.java          Dialog: add/edit pantry item
│
├── shopping/                       ◄── SHOPPING LIST FEATURE (6 files)
│   ├── ShoppingListItemResponse.java   record: ingredient + qty + purchased
│   ├── ShoppingListResponse.java       record: items + progress stats
│   ├── ShoppingListService.java        GraphQL: get, generate, toggle, clear
│   ├── ShoppingListView.java          Route "/shopping-list" — weekly checklist
│   ├── SharedListController.java       REST controller for shared list access
│   └── SharedListStore.java            In-memory store for shared list tokens
│
├── recommendation/                 ◄── RECOMMENDATIONS FEATURE
│   ├── RecipeRecommendationResponse.java  record: recipe + match% + missing
│   ├── MissingIngredientResponse.java     record: what's needed to buy
│   ├── RecommendationService.java      GraphQL: recommendations, add missing to list
│   └── RecommendationView.java        Route "/recommendations" — ranked cards
│
├── foodlog/                       ◄── FOOD LOG FEATURE (7 files)
│   ├── FoodLogResponse.java            record: food log entry with macros
│   ├── DailyFoodLogResponse.java       record: day entries + totals + targets
│   ├── FoodSourceType.java             enum: RECIPE, INGREDIENT, CUSTOM
│   ├── PlannedMealStatus.java          record: planned meal + logged status
│   ├── UserNutritionTargetResponse.java  record: targets for the day
│   ├── FoodLogService.java             GraphQL: daily log, add/update/remove entries
│   └── FoodLogView.java               Route "/food-log" — daily tracker + progress bars
│
├── nutritionhistory/              ◄── NUTRITION TRENDS (3 files)
│   ├── DailyNutritionSummary.java      record: date + calorie/macro totals
│   ├── NutritionHistoryService.java    GraphQL: fetch daily summaries for date range
│   └── NutritionHistoryView.java      Route "/nutrition-history" — ApexCharts graphs
│
├── ui/                             ◄── SHARED COMPONENTS
│   └── MacroBar.java                   4 colored macro badges (cal, prot, carbs, fat)
│
├── i18n/
│   └── TranslationProvider.java        EN + RO translations from resource bundles
│
└── src/main/frontend/themes/recipe-book/
    ├── styles.css                      Global dark theme, component styles
    └── components/                     Vaadin component theme overrides
        ├── vaadin-app-layout.css
        ├── vaadin-dialog-overlay.css
        ├── vaadin-text-field.css
        ├── vaadin-text-area.css
        ├── vaadin-integer-field.css
        ├── vaadin-number-field.css
        └── vaadin-combo-box.css
```

### Frontend Patterns

| Pattern | How it works |
|---------|-------------|
| **View** | `class PantryView extends VerticalLayout` + `@Route("pantry", layout=MainLayout.class)` |
| **Service** | `@Service` class injects `ApiClient`, defines GraphQL strings, calls `apiClient.query()/mutate()` |
| **Record DTO** | `record PantryItemResponse(Long id, ...)` — Jackson auto-deserializes GraphQL JSON into this |
| **Dialog** | `class PantryFormDialog extends Dialog` — created with `new`, called with `.open()`, runs `onSave` callback |
| **Auth guard** | `MainLayout.beforeEnter()` checks `authService.isLoggedIn()`, redirects to `/login` if not |
| **Filters** | `ComboBox` + `TextField` with `ValueChangeMode.LAZY` → triggers `refreshGrid()` on change |
| **Category colors** | `switch(category)` returns CSS vars like `var(--lumo-error-color)` for badges |
| **i18n** | `TranslationProvider` loads `messages.properties` / `messages_ro.properties` from resource bundles |
| **OAuth** | `OAuthCallbackView` catches redirect from provider, exchanges code for JWT via backend |

---

## How a Feature Flows End-to-End

Example: **User adds a pantry item**

```
  Browser                  PantryView.java              PantryService.java           ApiClient.java             Backend
    │                          │                             │                           │                        │
    │ clicks "Add Item"        │                             │                           │                        │
    ├─────────────────────────►│                             │                           │                        │
    │                          │ new PantryFormDialog()      │                           │                        │
    │                          │ dialog.open()               │                           │                        │
    │◄─────────────────────────┤ shows form                  │                           │                        │
    │                          │                             │                           │                        │
    │ fills form, clicks Save  │                             │                           │                        │
    ├─────────────────────────►│                             │                           │                        │
    │                          │ pantryService.addPantryItem  │                           │                        │
    │                          │     (input map)             │                           │                        │
    │                          ├────────────────────────────►│                           │                        │
    │                          │                             │ apiClient.mutate(          │                        │
    │                          │                             │   ADD_PANTRY_ITEM,         │                        │
    │                          │                             │   {input: {...}})          │                        │
    │                          │                             ├──────────────────────────►│                        │
    │                          │                             │                           │  POST /graphql          │
    │                          │                             │                           │  Authorization: Bearer  │
    │                          │                             │                           ├───────────────────────►│
    │                          │                             │                           │                        │ PantryGraphQL
    │                          │                             │                           │                        │ .addPantryItem()
    │                          │                             │                           │                        │ PantryService
    │                          │                             │                           │                        │ .addPantryItem()
    │                          │                             │                           │                        │ PantryItem.persist()
    │                          │                             │                           │  { data: {...} }       │
    │                          │                             │                           │◄───────────────────────┤
    │                          │                             │  PantryItemResponse       │                        │
    │                          │                             │◄──────────────────────────┤                        │
    │                          │  PantryItemResponse         │                           │                        │
    │                          │◄────────────────────────────┤                           │                        │
    │                          │                             │                           │                        │
    │                          │ onSave.run() → refreshGrid()│                           │                        │
    │                          │ Notification.show("saved")  │                           │                        │
    │ updated grid + toast     │                             │                           │                        │
    │◄─────────────────────────┤                             │                           │                        │
```

---

## Navigation Map

```
┌───────────────────────────────────────────────────────────────────┐
│                        MainLayout (shell)                          │
│  ┌───────────────┐  ┌─────────────────────────────────────────┐   │
│  │  Sidebar       │  │             Content Area                 │   │
│  │               │  │                                           │   │
│  │ Dashboard     │  │  Route "/"             → DashboardView    │   │
│  │ Recipes       │  │  Route "/recipes"      → RecipeListView   │   │
│  │ Ingredients   │  │  Route "/recipes/{id}" → RecipeDetailView │   │
│  │ Meal Planner  │  │  Route "/ingredients"  → IngredientList   │   │
│  │ Pantry        │  │  Route "/meal-plan"    → MealPlanView     │   │
│  │ Shopping      │  │  Route "/pantry"       → PantryView       │   │
│  │ Food Log      │  │  Route "/shopping-list"→ ShoppingListView │   │
│  │ Recommend.    │  │  Route "/food-log"     → FoodLogView      │   │
│  │ Trends        │  │  Route "/recommendations" → RecView       │   │
│  │               │  │  Route "/nutrition-history"→ TrendsView   │   │
│  │ ─────────     │  │  Route "/profile"      → ProfileView      │   │
│  │ Profile       │  │  Route "/bmr-wizard"   → BMRWizardView    │   │
│  │ BMR Wizard    │  │  Route "/nutrition-settings" → Settings   │   │
│  │ Settings      │  │                                           │   │
│  └───────────────┘  └─────────────────────────────────────────┘   │
└───────────────────────────────────────────────────────────────────┘

  Standalone (no sidebar):
    Route "/login"           → LoginView (+ OAuth buttons)
    Route "/register"        → RegisterView
    Route "/forgot-password" → ForgotPasswordView
    Route "/reset-password"  → ResetPasswordView
    Route "/oauth-callback"  → OAuthCallbackView
```

---

## Feature Interconnections

```
                    ┌──────────┐
                    │  Recipes │
                    └────┬─────┘
                         │ uses ingredients from
                         ▼
                    ┌──────────────┐
                    │  Ingredients │◄──────────────────────────┐
                    └──────┬───────┘                           │
                           │ referenced by                     │
                  ┌────────┴──────────┐                        │
                  ▼                   ▼                        │
           ┌───────────┐      ┌─────────────┐                 │
           │  Pantry   │      │  Meal Plan  │                 │
           │ (what you │      │ (what you   │                 │
           │   have)   │      │  plan to    │                 │
           └─────┬─────┘      │   eat)      │                 │
                 │            └──────┬──────┘                  │
                 │                   │                         │
                 │    compared to    │ generates               │
                 │    find gaps      │                         │
                 ▼                   ▼                         │
         ┌────────────────┐  ┌───────────────┐                │
         │ Recommendations│  │ Shopping List │                │
         │ (what you CAN  │  │ (what you    │                │
         │  cook with     │  │  NEED to buy)│                │
         │  your pantry)  │  │              │                │
         └───────┬────────┘  └──────────────┘                │
                 │                   ▲                         │
                 │ add missing ──────┘                         │
                 │ ingredients to list                         │
                 └────────────────────────────────────────────┘
```

### Data flow for Recommendations → Shopping List
1. `RecommendationService` checks pantry vs recipe ingredients
2. Calculates match % and identifies missing ingredients
3. User clicks "Add missing to list" on a recommendation card
4. Backend creates `ShoppingListItem` for each deficit ingredient
5. User opens Shopping List view to see what to buy

### Data flow for Shopping List generation
1. `ShoppingListService.generateShoppingList(weekStart)`
2. Backend fetches all meals for the week from `MealPlan`
3. Aggregates all recipe ingredients → total needed (in grams)
4. Subtracts what's in `PantryItem` → deficit
5. Creates `ShoppingListItem` for each positive deficit

---

## Theme & Visual Identity

```
Color Palette (Dark Mode):
──────────────────────────
  Background:  #1a1f2b  (dark navy)
  Cards:       #232838  (lighter navy)
  Primary:     #c08050  (warm bronze — buttons, links)
  Success:     #5a9e7e  (forest green — carbs, pantry)
  Error:       #c75050  (red — fat, warnings, delete)
  Warning:     #c0903c  (gold — fat, expiring)
  Text:        #d8dce4  (light gray)
  Secondary:   #8890a0  (medium gray)

Macro Colors:
  Calories  → #c75050 (red)
  Protein   → #c08050 (bronze)
  Carbs     → #5a9e7e (green)
  Fat       → #c0903c (gold)

Fonts:
  Body: Lumo default (system sans-serif)
  Labels/Tags: JetBrains Mono (monospace, uppercase, 12px)
```

---

## File Count Summary

| Area | Files | Purpose |
|------|-------|---------|
| **Backend entities** | 16 | 12 database models + 4 enums |
| **Backend GraphQL** | 24 | 12 resolvers + 12 input DTOs |
| **Backend DTOs** | 27 | Response/request objects |
| **Backend services** | 18 | Business logic |
| **Backend REST** | 2 | Image upload + OAuth callback |
| **Backend exceptions** | 5 | Custom exception hierarchy |
| **Backend tests** | 10 | 58 integration tests |
| **Flyway migrations** | 8 | Versioned SQL (V1–V8) |
| **Frontend services** | 6 | ApiClient, AuthService, BMR, etc. |
| **Frontend views** | 11 | Core views + Profile, BMR, Settings, OAuth, Password |
| **Frontend DTOs** | 7 | Auth + BMR + OAuth config |
| **Frontend recipe** | 8 | Service + views + dialogs + DTOs |
| **Frontend ingredient** | 4 | Service + view + dialog + DTO |
| **Frontend mealplan** | 5 | Service + view + DTOs |
| **Frontend pantry** | 4 | Service + view + dialog + DTO |
| **Frontend shopping** | 6 | Service + view + DTOs + sharing |
| **Frontend foodlog** | 7 | Service + view + DTOs |
| **Frontend recommendation** | 4 | Service + view + DTOs |
| **Frontend nutrition history** | 3 | Service + view + DTO |
| **Frontend shared UI** | 1 | MacroBar component |
| **Frontend i18n** | 1+2 | TranslationProvider + EN/RO bundles |
| **CSS/Theme** | 9 | Main styles + component overrides |
| **Backend TOTAL** | 92 | Java files |
| **Frontend TOTAL** | 68 | Java files |
| **GRAND TOTAL** | ~180+ | All source files |
