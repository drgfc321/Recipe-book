-- Drop Hibernate-generated unique constraint on user_id alone
-- (replaced by composite unique on user_id + day_type in V7)
ALTER TABLE user_nutrition_target DROP CONSTRAINT IF EXISTS ukdvni17tbpq9kqjk03isaurg95;
