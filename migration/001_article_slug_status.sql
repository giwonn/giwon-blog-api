-- Run AFTER application starts with ddl-auto: update (which adds the new columns as nullable first)
-- Then run this script to populate data and add constraints

-- 1. Populate slug from id for existing articles
UPDATE articles SET slug = CAST(id AS VARCHAR) WHERE slug IS NULL;

-- 2. Populate status based on existing fields
UPDATE articles SET status = 'LOCKED' WHERE password IS NOT NULL AND status IS NULL;
UPDATE articles SET status = 'PRIVATE' WHERE hidden = true AND status IS NULL;
UPDATE articles SET status = 'PUBLIC' WHERE status IS NULL AND published_at IS NOT NULL AND published_at <= NOW();
UPDATE articles SET status = 'DRAFT' WHERE status IS NULL;

-- 3. Set published_at for articles that were already published
-- (keep existing published_at values, they should already be set)

-- 4. After verifying data:
-- ALTER TABLE articles ALTER COLUMN slug SET NOT NULL;
-- ALTER TABLE articles ALTER COLUMN status SET NOT NULL;
-- ALTER TABLE articles ADD CONSTRAINT articles_slug_unique UNIQUE (slug);

-- 5. Drop old column (after verifying everything works):
-- ALTER TABLE articles DROP COLUMN IF EXISTS hidden;
