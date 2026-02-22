# Recipe Book Application - Roadmap

## Vision

A recipe management and meal planning application where users can:
- Manage recipes with ingredients and macronutrient tracking
- Plan weekly meals
- Auto-generate shopping lists from meal plans
- Track pantry inventory
- Get recipe recommendations based on available ingredients

## Tech Stack

- **Backend:** Quarkus 3 (Java 21) + Hibernate ORM Panache + SmallRye JWT
- **Frontend:** Vaadin 25 + Spring Boot 4.0
- **Database:** PostgreSQL 16
- **Build:** Maven, Docker Compose

---

## Features

### 1. Recipe Management
- Create/edit/delete recipes with name, description, category, difficulty, prep/cook time, servings
- Step-by-step instructions
- Ingredient list with quantities and units
- Optional image URL
- Categories: Breakfast, Lunch, Dinner, Dessert, Snack, Other
- Difficulty levels: Easy, Medium, Hard

### 2. Macronutrient Tracking
- Each ingredient stores macros per 100g: calories (kcal), protein (g), carbs (g), fat (g)
- Recipe macros calculated automatically from ingredient quantities
- Per-serving breakdown
- Daily totals in meal planner
- Weekly averages

### 3. Meal Planning
- Weekly calendar view (Mon-Sun)
- 4 meal slots per day: Breakfast, Lunch, Dinner, Snack
- Assign recipes to slots
- Navigate between weeks
- Daily and weekly macro summaries

### 4. Shopping List
- Auto-generated from weekly meal plan
- Aggregates ingredients across all planned recipes
- Subtracts ingredients already in pantry
- Grouped by ingredient category
- Mark items as purchased (checkbox)
- Manual item addition
- Progress tracking

### 5. Pantry Management
- Track available ingredients with quantities
- Optional expiration date tracking
- Expiring soon / expired alerts
- Auto-merge when adding existing ingredients

### 6. Recipe Recommendations
- Ranked by pantry match percentage (best match first)
- Shows missing ingredients for each recipe
- Filter by category, difficulty, max missing ingredients
- "Add missing to shopping list" action

---

## Implementation Steps

1. **Cleanup** — Delete D&D code, rename packages to `com.recipebook`, update configs
2. **Backend Entities** — Enums, Ingredient (with macros), Recipe, RecipeIngredient, MealPlan, ShoppingListItem, PantryItem + data seeder
3. **Recipe & Ingredient API** — REST endpoints + macro calculation
4. **Meal Plan + Shopping List + Pantry + Recommendations API**
5. **Frontend: Recipes UI** — RecipesView, RecipeDetailView with macros
6. **Frontend: Meal Plan UI** — Weekly calendar with daily macro totals
7. **Frontend: Shopping List + Pantry + Recommendations UI** + Dashboard rebuild
8. **Tests** — Backend integration tests for all resources
