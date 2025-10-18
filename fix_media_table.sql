-- Fix media table to allow null post_id for avatar media
-- Run this script if you encounter the "null value in column post_id violates not-null constraint" error

-- Drop the existing NOT NULL constraint on post_id
ALTER TABLE media ALTER COLUMN post_id DROP NOT NULL;

-- Verify the change
\d media;
