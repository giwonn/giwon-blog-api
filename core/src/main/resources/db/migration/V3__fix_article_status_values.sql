-- V3: Fix article status values from old enum names to new ones

-- Old Hibernate-generated values → new values
UPDATE articles SET status = 'PUBLIC' WHERE status = 'PUBLISHED';
UPDATE articles SET status = 'PRIVATE' WHERE status = 'HIDDEN';

-- Catch any other unexpected values
UPDATE articles SET status = 'PUBLIC' WHERE status NOT IN ('DRAFT', 'PUBLIC', 'LOCKED', 'PRIVATE');
