-- Fix existing users table by updating NULL values to default values
-- Run this script if you encounter the "column contains null values" error

-- Update existing users with default values for count columns
UPDATE users 
SET 
    trips_count = COALESCE(trips_count, 0),
    steps_count = COALESCE(steps_count, 0),
    followers_count = COALESCE(followers_count, 0),
    following_count = COALESCE(following_count, 0)
WHERE 
    trips_count IS NULL 
    OR steps_count IS NULL 
    OR followers_count IS NULL 
    OR following_count IS NULL;

-- Verify the update
SELECT id, username, trips_count, steps_count, followers_count, following_count 
FROM users 
WHERE trips_count IS NULL 
   OR steps_count IS NULL 
   OR followers_count IS NULL 
   OR following_count IS NULL;
