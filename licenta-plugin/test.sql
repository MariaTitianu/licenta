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

SELECT * FROM protected;



DELETE FROM protected WHERE id > 2;


UPDATE protected SET name = 'Modified' WHERE id = 1;

ALTER TABLE protected ADD COLUMN description TEXT;


DROP TABLE protected;


-- SELECT pg_unprotect_table('protected');


-- DELETE FROM protected WHERE id > 2;
-- UPDATE protected SET name = 'Modified' WHERE id = 1;
-- ALTER TABLE protected ADD COLUMN description TEXT;


-- SELECT pg_protect_table('protected');

-- UPDATE protected SET name = 'Another change' WHERE id = 1;
