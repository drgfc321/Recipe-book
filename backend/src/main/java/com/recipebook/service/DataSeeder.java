package com.recipebook.service;

import com.recipebook.entity.Difficulty;
import com.recipebook.entity.Ingredient;
import com.recipebook.entity.IngredientCategory;
import com.recipebook.entity.Recipe;
import com.recipebook.entity.RecipeCategory;
import com.recipebook.entity.RecipeIngredient;
import com.recipebook.entity.Unit;
import com.recipebook.entity.User;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class DataSeeder {

    private static final Logger LOG = Logger.getLogger(DataSeeder.class);

    @Inject
    PasswordService passwordService;

    @Transactional
    public void onStartup(@Observes StartupEvent event) {
        if (Ingredient.count() > 0) {
            LOG.info("Database already seeded, skipping");
            return;
        }

        LOG.info("Seeding database...");

        // Create seed user
        User admin = new User();
        admin.username = "chef_admin";
        admin.email = "admin@recipebook.com";
        admin.passwordHash = passwordService.hashPassword("admin123");
        admin.role = "ADMIN";
        admin.createdAt = LocalDateTime.now();
        admin.persist();

        // Seed ingredients
        Map<String, Ingredient> ingredients = new HashMap<>();
        ingredients.put("Chicken Breast", createIngredient("Chicken Breast", IngredientCategory.MEAT, 165, 31.0, 0.0, 3.6));
        ingredients.put("Chicken Thigh", createIngredient("Chicken Thigh", IngredientCategory.MEAT, 209, 26.0, 0.0, 10.9));
        ingredients.put("Pork Shoulder", createIngredient("Pork Shoulder", IngredientCategory.MEAT, 236, 17.0, 0.0, 18.0));
        ingredients.put("Ground Pork", createIngredient("Ground Pork", IngredientCategory.MEAT, 263, 16.9, 0.0, 21.2));
        ingredients.put("Beef Tripe", createIngredient("Beef Tripe", IngredientCategory.MEAT, 85, 12.1, 0.0, 3.7));
        ingredients.put("Bacon", createIngredient("Bacon", IngredientCategory.MEAT, 541, 37.0, 1.4, 42.0));
        ingredients.put("Eggs", createIngredient("Eggs", IngredientCategory.DAIRY, 155, 13.0, 1.1, 11.0));
        ingredients.put("Sour Cream", createIngredient("Sour Cream", IngredientCategory.DAIRY, 198, 2.4, 4.6, 19.4));
        ingredients.put("Butter", createIngredient("Butter", IngredientCategory.DAIRY, 717, 0.9, 0.1, 81.0));
        ingredients.put("Parmesan Cheese", createIngredient("Parmesan Cheese", IngredientCategory.DAIRY, 431, 38.5, 4.1, 28.6));
        ingredients.put("Cabbage", createIngredient("Cabbage", IngredientCategory.VEGETABLES, 25, 1.3, 5.8, 0.1));
        ingredients.put("Sauerkraut", createIngredient("Sauerkraut", IngredientCategory.VEGETABLES, 19, 0.9, 4.3, 0.1));
        ingredients.put("Onion", createIngredient("Onion", IngredientCategory.VEGETABLES, 40, 1.1, 9.3, 0.1));
        ingredients.put("Garlic", createIngredient("Garlic", IngredientCategory.VEGETABLES, 149, 6.4, 33.1, 0.5));
        ingredients.put("Carrot", createIngredient("Carrot", IngredientCategory.VEGETABLES, 41, 0.9, 9.6, 0.2));
        ingredients.put("Bell Pepper", createIngredient("Bell Pepper", IngredientCategory.VEGETABLES, 31, 1.0, 6.0, 0.3));
        ingredients.put("Tomato", createIngredient("Tomato", IngredientCategory.VEGETABLES, 18, 0.9, 3.9, 0.2));
        ingredients.put("Broccoli", createIngredient("Broccoli", IngredientCategory.VEGETABLES, 34, 2.8, 7.0, 0.4));
        ingredients.put("Rice", createIngredient("Rice", IngredientCategory.GRAINS, 130, 2.7, 28.2, 0.3));
        ingredients.put("Spaghetti", createIngredient("Spaghetti", IngredientCategory.GRAINS, 158, 5.8, 30.9, 0.9));
        ingredients.put("Flour", createIngredient("Flour", IngredientCategory.GRAINS, 364, 10.3, 76.3, 1.0));
        ingredients.put("Paprika", createIngredient("Paprika", IngredientCategory.SPICES, 282, 14.1, 53.9, 12.9));
        ingredients.put("Black Pepper", createIngredient("Black Pepper", IngredientCategory.SPICES, 251, 10.4, 63.9, 3.3));
        ingredients.put("Salt", createIngredient("Salt", IngredientCategory.SPICES, 0, 0.0, 0.0, 0.0));
        ingredients.put("Olive Oil", createIngredient("Olive Oil", IngredientCategory.OILS, 884, 0.0, 0.0, 100.0));
        ingredients.put("Soy Sauce", createIngredient("Soy Sauce", IngredientCategory.SPICES, 53, 8.1, 4.9, 0.0));
        ingredients.put("Lemon", createIngredient("Lemon", IngredientCategory.FRUITS, 29, 1.1, 9.3, 0.3));

        // Seed recipes

        // 1. Ciorbă de Burtă
        Recipe ciorba = new Recipe();
        ciorba.name = "Ciorbă de Burtă";
        ciorba.description = "Traditional Romanian tripe soup, a beloved comfort food served with sour cream and hot peppers.";
        ciorba.category = RecipeCategory.LUNCH;
        ciorba.difficulty = Difficulty.HARD;
        ciorba.prepTime = 30;
        ciorba.cookTime = 180;
        ciorba.servings = 6;
        ciorba.instructions = "1. Clean the tripe thoroughly and boil in salted water for 2-3 hours until tender.\n"
                + "2. Cut the cooked tripe into thin strips.\n"
                + "3. Sauté diced onion and garlic in olive oil until translucent.\n"
                + "4. Add the tripe strips and broth back to the pot.\n"
                + "5. In a bowl, whisk sour cream with egg yolks and slowly temper with hot broth.\n"
                + "6. Pour the sour cream mixture into the soup, stirring constantly.\n"
                + "7. Season with salt, pepper, and a squeeze of lemon juice.\n"
                + "8. Serve hot with extra sour cream and hot peppers on the side.";
        ciorba.owner = admin;
        ciorba.persist();
        addRecipeIngredient(ciorba, ingredients.get("Beef Tripe"), 500, Unit.GRAMS);
        addRecipeIngredient(ciorba, ingredients.get("Sour Cream"), 200, Unit.GRAMS);
        addRecipeIngredient(ciorba, ingredients.get("Eggs"), 2, Unit.PIECES);
        addRecipeIngredient(ciorba, ingredients.get("Onion"), 1, Unit.PIECES);
        addRecipeIngredient(ciorba, ingredients.get("Garlic"), 4, Unit.PIECES);
        addRecipeIngredient(ciorba, ingredients.get("Olive Oil"), 30, Unit.MILLILITERS);
        addRecipeIngredient(ciorba, ingredients.get("Lemon"), 1, Unit.PIECES);
        addRecipeIngredient(ciorba, ingredients.get("Salt"), 2, Unit.TEASPOONS);
        addRecipeIngredient(ciorba, ingredients.get("Black Pepper"), 1, Unit.TEASPOONS);

        // 2. Sarmale
        Recipe sarmale = new Recipe();
        sarmale.name = "Sarmale";
        sarmale.description = "Romanian cabbage rolls stuffed with a savory mix of ground pork and rice, slow-cooked in sauerkraut.";
        sarmale.category = RecipeCategory.DINNER;
        sarmale.difficulty = Difficulty.HARD;
        sarmale.prepTime = 60;
        sarmale.cookTime = 240;
        sarmale.servings = 8;
        sarmale.instructions = "1. Mix ground pork with cooked rice, sautéed onion, paprika, salt, and pepper.\n"
                + "2. Separate sauerkraut leaves and rinse if too sour.\n"
                + "3. Place a spoonful of filling on each leaf and roll tightly, tucking in the sides.\n"
                + "4. Layer sauerkraut on the bottom of a large pot.\n"
                + "5. Arrange cabbage rolls seam-side down in layers.\n"
                + "6. Add tomato sauce, a bit of water, and remaining sauerkraut between layers.\n"
                + "7. Cover and cook on low heat for 3-4 hours.\n"
                + "8. Serve hot with sour cream and polenta.";
        sarmale.owner = admin;
        sarmale.persist();
        addRecipeIngredient(sarmale, ingredients.get("Ground Pork"), 500, Unit.GRAMS);
        addRecipeIngredient(sarmale, ingredients.get("Sauerkraut"), 1, Unit.KILOGRAMS);
        addRecipeIngredient(sarmale, ingredients.get("Rice"), 150, Unit.GRAMS);
        addRecipeIngredient(sarmale, ingredients.get("Onion"), 2, Unit.PIECES);
        addRecipeIngredient(sarmale, ingredients.get("Tomato"), 3, Unit.PIECES);
        addRecipeIngredient(sarmale, ingredients.get("Paprika"), 2, Unit.TEASPOONS);
        addRecipeIngredient(sarmale, ingredients.get("Salt"), 2, Unit.TEASPOONS);
        addRecipeIngredient(sarmale, ingredients.get("Black Pepper"), 1, Unit.TEASPOONS);
        addRecipeIngredient(sarmale, ingredients.get("Sour Cream"), 100, Unit.GRAMS);

        // 3. Pasta Carbonara
        Recipe carbonara = new Recipe();
        carbonara.name = "Pasta Carbonara";
        carbonara.description = "Classic Italian pasta with crispy bacon, eggs, and Parmesan cheese.";
        carbonara.category = RecipeCategory.DINNER;
        carbonara.difficulty = Difficulty.EASY;
        carbonara.prepTime = 10;
        carbonara.cookTime = 20;
        carbonara.servings = 4;
        carbonara.instructions = "1. Cook spaghetti in salted boiling water until al dente.\n"
                + "2. Meanwhile, cook diced bacon in a pan until crispy.\n"
                + "3. In a bowl, whisk eggs with grated Parmesan and black pepper.\n"
                + "4. Drain pasta, reserving some cooking water.\n"
                + "5. Toss hot pasta with bacon (off heat), then quickly mix in egg mixture.\n"
                + "6. Add a splash of pasta water to create a creamy sauce.\n"
                + "7. Serve immediately with extra Parmesan on top.";
        carbonara.owner = admin;
        carbonara.persist();
        addRecipeIngredient(carbonara, ingredients.get("Spaghetti"), 400, Unit.GRAMS);
        addRecipeIngredient(carbonara, ingredients.get("Bacon"), 200, Unit.GRAMS);
        addRecipeIngredient(carbonara, ingredients.get("Eggs"), 4, Unit.PIECES);
        addRecipeIngredient(carbonara, ingredients.get("Parmesan Cheese"), 100, Unit.GRAMS);
        addRecipeIngredient(carbonara, ingredients.get("Black Pepper"), 1, Unit.TEASPOONS);
        addRecipeIngredient(carbonara, ingredients.get("Salt"), 1, Unit.TEASPOONS);

        // 4. Chicken Stir-Fry
        Recipe stirFry = new Recipe();
        stirFry.name = "Chicken Stir-Fry";
        stirFry.description = "Quick and healthy chicken stir-fry with colorful vegetables and soy sauce.";
        stirFry.category = RecipeCategory.DINNER;
        stirFry.difficulty = Difficulty.EASY;
        stirFry.prepTime = 15;
        stirFry.cookTime = 15;
        stirFry.servings = 4;
        stirFry.instructions = "1. Cut chicken breast into thin strips and season with salt and pepper.\n"
                + "2. Heat olive oil in a wok or large pan over high heat.\n"
                + "3. Stir-fry chicken until golden, about 5 minutes. Remove and set aside.\n"
                + "4. Add sliced bell pepper, broccoli florets, and carrot strips to the pan.\n"
                + "5. Stir-fry vegetables for 3-4 minutes until tender-crisp.\n"
                + "6. Return chicken to the pan, add soy sauce and minced garlic.\n"
                + "7. Toss everything together for 1-2 minutes.\n"
                + "8. Serve over steamed rice.";
        stirFry.owner = admin;
        stirFry.persist();
        addRecipeIngredient(stirFry, ingredients.get("Chicken Breast"), 500, Unit.GRAMS);
        addRecipeIngredient(stirFry, ingredients.get("Bell Pepper"), 2, Unit.PIECES);
        addRecipeIngredient(stirFry, ingredients.get("Broccoli"), 200, Unit.GRAMS);
        addRecipeIngredient(stirFry, ingredients.get("Carrot"), 2, Unit.PIECES);
        addRecipeIngredient(stirFry, ingredients.get("Garlic"), 3, Unit.PIECES);
        addRecipeIngredient(stirFry, ingredients.get("Soy Sauce"), 3, Unit.TABLESPOONS);
        addRecipeIngredient(stirFry, ingredients.get("Olive Oil"), 2, Unit.TABLESPOONS);
        addRecipeIngredient(stirFry, ingredients.get("Rice"), 300, Unit.GRAMS);
        addRecipeIngredient(stirFry, ingredients.get("Salt"), 1, Unit.TEASPOONS);
        addRecipeIngredient(stirFry, ingredients.get("Black Pepper"), 1, Unit.TEASPOONS);

        // 5. Scrambled Eggs with Butter
        Recipe scrambled = new Recipe();
        scrambled.name = "Scrambled Eggs with Butter";
        scrambled.description = "Simple, creamy scrambled eggs cooked slowly in butter for the perfect breakfast.";
        scrambled.category = RecipeCategory.BREAKFAST;
        scrambled.difficulty = Difficulty.EASY;
        scrambled.prepTime = 5;
        scrambled.cookTime = 10;
        scrambled.servings = 2;
        scrambled.instructions = "1. Crack eggs into a bowl and whisk lightly with a fork.\n"
                + "2. Melt butter in a non-stick pan over low heat.\n"
                + "3. Pour in the eggs and let them sit for 30 seconds.\n"
                + "4. Gently stir with a spatula, pushing eggs from edges to center.\n"
                + "5. Continue stirring slowly until eggs are softly set but still moist.\n"
                + "6. Season with salt and pepper, serve immediately.";
        scrambled.owner = admin;
        scrambled.persist();
        addRecipeIngredient(scrambled, ingredients.get("Eggs"), 4, Unit.PIECES);
        addRecipeIngredient(scrambled, ingredients.get("Butter"), 20, Unit.GRAMS);
        addRecipeIngredient(scrambled, ingredients.get("Salt"), 1, Unit.TEASPOONS);
        addRecipeIngredient(scrambled, ingredients.get("Black Pepper"), 1, Unit.TEASPOONS);

        LOG.info("Seeding complete: " + Ingredient.count() + " ingredients, " + Recipe.count() + " recipes");
    }

    private Ingredient createIngredient(String name, IngredientCategory category,
                                         double calories, double protein, double carbs, double fat) {
        Ingredient ingredient = new Ingredient();
        ingredient.name = name;
        ingredient.category = category;
        ingredient.caloriesPer100g = calories;
        ingredient.proteinPer100g = protein;
        ingredient.carbsPer100g = carbs;
        ingredient.fatPer100g = fat;
        ingredient.persist();
        return ingredient;
    }

    private void addRecipeIngredient(Recipe recipe, Ingredient ingredient,
                                      double quantity, Unit unit) {
        RecipeIngredient ri = new RecipeIngredient();
        ri.recipe = recipe;
        ri.ingredient = ingredient;
        ri.quantity = quantity;
        ri.unit = unit;
        ri.persist();
        recipe.ingredients.add(ri);
    }
}
