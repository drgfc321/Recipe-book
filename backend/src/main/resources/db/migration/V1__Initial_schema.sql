-- Sequences (PanacheEntity uses GenerationType.SEQUENCE, allocationSize=50)
CREATE SEQUENCE IF NOT EXISTS users_SEQ START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE IF NOT EXISTS recipe_SEQ START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE IF NOT EXISTS ingredient_SEQ START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE IF NOT EXISTS recipe_ingredient_SEQ START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE IF NOT EXISTS meal_plan_SEQ START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE IF NOT EXISTS shopping_list_item_SEQ START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE IF NOT EXISTS pantry_item_SEQ START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE IF NOT EXISTS user_nutrition_target_SEQ START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE IF NOT EXISTS food_log_SEQ START WITH 1 INCREMENT BY 50;

-- 1. users
CREATE TABLE IF NOT EXISTS users (
    id            BIGINT NOT NULL PRIMARY KEY,
    email         VARCHAR(255) NOT NULL UNIQUE,
    username      VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(255) NOT NULL,
    language      VARCHAR(255) NOT NULL,
    created_at    TIMESTAMP(6),
    last_login    TIMESTAMP(6)
);

-- 2. ingredient
CREATE TABLE IF NOT EXISTS ingredient (
    id                BIGINT NOT NULL PRIMARY KEY,
    name              VARCHAR(255) NOT NULL UNIQUE,
    category          VARCHAR(255) NOT NULL,
    calories_per_100g FLOAT(53) NOT NULL,
    protein_per_100g  FLOAT(53) NOT NULL,
    carbs_per_100g    FLOAT(53) NOT NULL,
    fat_per_100g      FLOAT(53) NOT NULL
);

-- 3. recipe (FK: owner_id -> users, no @JoinColumn so default name)
CREATE TABLE IF NOT EXISTS recipe (
    id           BIGINT NOT NULL PRIMARY KEY,
    name         VARCHAR(255) NOT NULL,
    description  TEXT,
    category     VARCHAR(255) NOT NULL,
    difficulty   VARCHAR(255) NOT NULL,
    prep_time    INTEGER NOT NULL,
    cook_time    INTEGER NOT NULL,
    servings     INTEGER NOT NULL,
    instructions TEXT,
    image_url    VARCHAR(255),
    owner_id     BIGINT REFERENCES users(id),
    created_at   TIMESTAMP(6)
);

-- 4. recipe_ingredient (FKs: recipe_id, ingredient_id)
CREATE TABLE IF NOT EXISTS recipe_ingredient (
    id            BIGINT NOT NULL PRIMARY KEY,
    recipe_id     BIGINT REFERENCES recipe(id),
    ingredient_id BIGINT REFERENCES ingredient(id),
    quantity      FLOAT(53) NOT NULL,
    unit          VARCHAR(255) NOT NULL
);

-- 5. meal_plan (FK: user_id explicit @JoinColumn, recipe_id default)
CREATE TABLE IF NOT EXISTS meal_plan (
    id        BIGINT NOT NULL PRIMARY KEY,
    user_id   BIGINT REFERENCES users(id),
    meal_date DATE NOT NULL,
    meal_slot VARCHAR(255) NOT NULL,
    recipe_id BIGINT REFERENCES recipe(id),
    UNIQUE (user_id, meal_date, meal_slot)
);

-- 6. shopping_list_item (FKs: user_id, ingredient_id)
CREATE TABLE IF NOT EXISTS shopping_list_item (
    id              BIGINT NOT NULL PRIMARY KEY,
    user_id         BIGINT REFERENCES users(id),
    ingredient_id   BIGINT REFERENCES ingredient(id),
    ingredient_name VARCHAR(255),
    quantity        FLOAT(53) NOT NULL,
    unit            VARCHAR(255),
    purchased       BOOLEAN NOT NULL,
    week_start_date DATE
);

-- 7. pantry_item (FKs: user_id, ingredient_id)
CREATE TABLE IF NOT EXISTS pantry_item (
    id              BIGINT NOT NULL PRIMARY KEY,
    user_id         BIGINT REFERENCES users(id),
    ingredient_id   BIGINT REFERENCES ingredient(id),
    quantity        FLOAT(53) NOT NULL,
    unit            VARCHAR(255),
    expiration_date DATE,
    UNIQUE (user_id, ingredient_id)
);

-- 8. user_nutrition_target (FK: user_id explicit @JoinColumn, unique + not null)
CREATE TABLE IF NOT EXISTS user_nutrition_target (
    id             BIGINT NOT NULL PRIMARY KEY,
    user_id        BIGINT NOT NULL UNIQUE REFERENCES users(id),
    daily_calories INTEGER NOT NULL,
    daily_protein  FLOAT(53) NOT NULL,
    daily_carbs    FLOAT(53) NOT NULL,
    daily_fat      FLOAT(53) NOT NULL
);

-- 9. food_log (FK: user_id explicit @JoinColumn NOT NULL; recipe_id, ingredient_id default)
CREATE TABLE IF NOT EXISTS food_log (
    id                  BIGINT NOT NULL PRIMARY KEY,
    user_id             BIGINT NOT NULL REFERENCES users(id),
    log_date            DATE NOT NULL,
    meal_slot           VARCHAR(255) NOT NULL,
    servings            FLOAT(53) NOT NULL,
    logged_at           TIMESTAMP(6),
    recipe_id           BIGINT REFERENCES recipe(id),
    ingredient_id       BIGINT REFERENCES ingredient(id),
    ingredient_quantity FLOAT(53),
    custom_name         VARCHAR(255),
    custom_calories     FLOAT(53),
    custom_protein      FLOAT(53),
    custom_carbs        FLOAT(53),
    custom_fat          FLOAT(53),
    source_meal_plan_id BIGINT
);

-- Sync sequences past max existing IDs (critical for existing databases)
DO $$
DECLARE
    seq_val BIGINT;
BEGIN
    SELECT COALESCE(MAX(id), 0) + 50 INTO seq_val FROM users;
    IF seq_val > 1 THEN PERFORM setval('users_SEQ', seq_val, false); END IF;

    SELECT COALESCE(MAX(id), 0) + 50 INTO seq_val FROM recipe;
    IF seq_val > 1 THEN PERFORM setval('recipe_SEQ', seq_val, false); END IF;

    SELECT COALESCE(MAX(id), 0) + 50 INTO seq_val FROM ingredient;
    IF seq_val > 1 THEN PERFORM setval('ingredient_SEQ', seq_val, false); END IF;

    SELECT COALESCE(MAX(id), 0) + 50 INTO seq_val FROM recipe_ingredient;
    IF seq_val > 1 THEN PERFORM setval('recipe_ingredient_SEQ', seq_val, false); END IF;

    SELECT COALESCE(MAX(id), 0) + 50 INTO seq_val FROM meal_plan;
    IF seq_val > 1 THEN PERFORM setval('meal_plan_SEQ', seq_val, false); END IF;

    SELECT COALESCE(MAX(id), 0) + 50 INTO seq_val FROM shopping_list_item;
    IF seq_val > 1 THEN PERFORM setval('shopping_list_item_SEQ', seq_val, false); END IF;

    SELECT COALESCE(MAX(id), 0) + 50 INTO seq_val FROM pantry_item;
    IF seq_val > 1 THEN PERFORM setval('pantry_item_SEQ', seq_val, false); END IF;

    SELECT COALESCE(MAX(id), 0) + 50 INTO seq_val FROM user_nutrition_target;
    IF seq_val > 1 THEN PERFORM setval('user_nutrition_target_SEQ', seq_val, false); END IF;

    SELECT COALESCE(MAX(id), 0) + 50 INTO seq_val FROM food_log;
    IF seq_val > 1 THEN PERFORM setval('food_log_SEQ', seq_val, false); END IF;
END $$;
