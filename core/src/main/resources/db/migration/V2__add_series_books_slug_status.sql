-- V2: Add series, books tables and article slug/status columns

-- 1. Create series table
CREATE TABLE series (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    thumbnail_url VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- 2. Create books table
CREATE TABLE books (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL UNIQUE,
    author VARCHAR(255) NOT NULL,
    publisher VARCHAR(255),
    thumbnail_url VARCHAR(255),
    description TEXT,
    isbn VARCHAR(255),
    read_start_date DATE,
    read_end_date DATE,
    rating INTEGER,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- 3. Add new columns to articles (all nullable initially for data migration)
ALTER TABLE articles ADD COLUMN slug VARCHAR(255);
ALTER TABLE articles ADD COLUMN status VARCHAR(255);
ALTER TABLE articles ADD COLUMN series_id BIGINT;
ALTER TABLE articles ADD COLUMN order_in_series INTEGER;
ALTER TABLE articles ADD COLUMN book_id BIGINT;
ALTER TABLE articles ADD COLUMN order_in_book INTEGER;

-- 4. Data migration: populate slug from id
UPDATE articles SET slug = CAST(id AS VARCHAR) WHERE slug IS NULL;

-- 5. Data migration: populate status from hidden/publishedAt/password
UPDATE articles SET status = 'LOCKED' WHERE password IS NOT NULL;
UPDATE articles SET status = 'PRIVATE' WHERE hidden = true AND status IS NULL;
UPDATE articles SET status = 'PUBLIC' WHERE status IS NULL AND published_at IS NOT NULL AND published_at <= NOW();
UPDATE articles SET status = 'DRAFT' WHERE status IS NULL;

-- 6. Add NOT NULL constraints after data migration
ALTER TABLE articles ALTER COLUMN slug SET NOT NULL;
ALTER TABLE articles ALTER COLUMN status SET NOT NULL;

-- 7. Add unique constraint on slug
ALTER TABLE articles ADD CONSTRAINT articles_slug_unique UNIQUE (slug);

-- 8. Make published_at nullable (was NOT NULL before, now DRAFT articles may not have it)
ALTER TABLE articles ALTER COLUMN published_at DROP NOT NULL;

-- 9. Drop the old hidden column
ALTER TABLE articles DROP COLUMN IF EXISTS hidden;
