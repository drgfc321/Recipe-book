-- Indexes for frequently queried columns (IMPROVEMENTS.md #7)

-- food_log: getDailyFoodLog queries by (user_id, log_date) ORDER BY meal_slot[, loggedAt]
-- Covers filter + eliminates sort for ORDER BY meal_slot; partial sort benefit for meal_slot, loggedAt
CREATE INDEX IF NOT EXISTS idx_food_log_user_date
    ON food_log (user_id, log_date, meal_slot);

-- shopping_list_item: all queries filter by (user_id, week_start_date)
CREATE INDEX IF NOT EXISTS idx_shopping_list_item_user_week
    ON shopping_list_item (user_id, week_start_date);
