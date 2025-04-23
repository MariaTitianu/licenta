SET client_min_messages TO 'NOTICE';

CREATE TABLE IF NOT EXISTS protected (
    id SERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO protected (name) VALUES 
    ('Test Row 1'),
    ('Test Row 2'),
    ('Test Row 3'),
    ('Test Row 4'),
    ('Test Row 5');

SELECT pg_protect_table('protected');
--SELECT pg_unprotect_table('protected');
-- Display inserted data
SELECT * FROM protected;

-- SECTION 1: Test protection against various operations
-- All of these should fail by default

-- Try to delete data
DELETE FROM protected WHERE id > 2;

-- Try to update data
UPDATE protected SET name = 'Modified' WHERE id = 1;

-- Try to alter table structure
ALTER TABLE protected ADD COLUMN description TEXT;

-- To test DROP, uncomment:
DROP TABLE protected;

-- SECTION 2: Unprotect the table temporarily
-- Uncomment these lines to test unprotection

-- SELECT pg_unprotect_table('protected');

-- Try operations again, should succeed
-- DELETE FROM protected WHERE id > 2;
-- UPDATE protected SET name = 'Modified' WHERE id = 1;
-- ALTER TABLE protected ADD COLUMN description TEXT;

-- Protect the table again
-- SELECT pg_protect_table('protected');

-- Try to update again, should fail
-- UPDATE protected SET name = 'Another change' WHERE id = 1;
