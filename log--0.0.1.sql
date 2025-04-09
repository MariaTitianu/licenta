-- Create a table to store protected tables
CREATE TABLE pg_protected_tables (
    table_name TEXT PRIMARY KEY,
    protected_at TIMESTAMP DEFAULT now(),
    protected_by TEXT DEFAULT current_user
);

-- Add appropriate permissions
GRANT SELECT ON pg_protected_tables TO PUBLIC;
REVOKE INSERT, UPDATE, DELETE ON pg_protected_tables FROM PUBLIC;

-- Function to retrieve ALTER TABLE logs
CREATE FUNCTION pg_all_queries(OUT query TEXT, OUT pid TEXT)
RETURNS SETOF record
AS 'MODULE_PATHNAME', 'pg_all_queries'
LANGUAGE C STRICT VOLATILE;

-- Function to protect a table
CREATE FUNCTION pg_protect_table(tablename TEXT)
RETURNS BOOLEAN
AS 'MODULE_PATHNAME', 'pg_protect_table'
LANGUAGE C STRICT VOLATILE;

-- Function to unprotect a table
CREATE FUNCTION pg_unprotect_table(tablename TEXT)
RETURNS BOOLEAN
AS 'MODULE_PATHNAME', 'pg_unprotect_table'
LANGUAGE C STRICT VOLATILE;

-- Function to list all protected tables
CREATE FUNCTION pg_list_protected_tables()
RETURNS SETOF pg_protected_tables
AS 'SELECT * FROM pg_protected_tables ORDER BY table_name'
LANGUAGE SQL STABLE;

-- Comment on extension
COMMENT ON EXTENSION log IS 'Logs and prevents DELETE/UPDATE operations on protected tables';