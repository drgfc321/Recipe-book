-- Add day_type column to user_nutrition_target (existing rows become DEFAULT)
ALTER TABLE user_nutrition_target ADD COLUMN day_type VARCHAR(20) NOT NULL DEFAULT 'DEFAULT';

-- Drop old unique constraint on user_id alone
ALTER TABLE user_nutrition_target DROP CONSTRAINT IF EXISTS user_nutrition_target_user_id_key;

-- Add composite unique constraint (user_id, day_type)
ALTER TABLE user_nutrition_target ADD CONSTRAINT uq_user_nutrition_target_user_daytype
    UNIQUE (user_id, day_type);

-- New table for per-date day type assignments
CREATE SEQUENCE IF NOT EXISTS user_day_type_SEQ START WITH 1 INCREMENT BY 50;

CREATE TABLE IF NOT EXISTS user_day_type (
    id       BIGINT NOT NULL PRIMARY KEY,
    user_id  BIGINT NOT NULL REFERENCES users(id),
    day_date DATE NOT NULL,
    day_type VARCHAR(20) NOT NULL,
    UNIQUE (user_id, day_date)
);
