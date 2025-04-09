-- Create the tables
CREATE TABLE protected (
    id SERIAL PRIMARY KEY,
    name TEXT NOT NULL
);

CREATE TABLE unprotected (
    id SERIAL PRIMARY KEY,
    name TEXT NOT NULL
);

-- Create the pg_protected_tables table needed by the protection mechanism
CREATE TABLE IF NOT EXISTS public.pg_protected_tables (
    table_name TEXT PRIMARY KEY,
    protected_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    protected_by TEXT
);

-- Mark the protected table as protected
SELECT pg_protect_table('protected');

-- Populate the protected table
INSERT INTO protected (name) VALUES 
('Critical Data 1'),
('Sensitive Information'),
('Protected Record A'),
('Protected Record B'),
('Important Data');

-- Populate the unprotected table
INSERT INTO unprotected (name) VALUES 
('Regular Data 1'),
('Public Information'),
('Unprotected Record X'),
('Unprotected Record Y'),
('Normal Data');
