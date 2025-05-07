\echo Use "CREATE EXTENSION pg_log" to load this file. \quit


CREATE TABLE pg_unprotected_tables (
    table_name TEXT PRIMARY KEY,
    unprotect_time TIMESTAMP NOT NULL DEFAULT now(),
    unprotected_by TEXT NOT NULL
);


CREATE FUNCTION pg_all_queries() 
RETURNS TABLE (query TEXT, pid TEXT)
AS 'MODULE_PATHNAME', 'pg_all_queries'
LANGUAGE C STRICT;

CREATE FUNCTION pg_protect_table(tablename TEXT)
RETURNS BOOLEAN
AS 'MODULE_PATHNAME', 'pg_protect_table'
LANGUAGE C STRICT;

CREATE FUNCTION pg_unprotect_table(tablename TEXT) 
RETURNS BOOLEAN
AS 'MODULE_PATHNAME', 'pg_unprotect_table'
LANGUAGE C STRICT;

DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'table_manager_admin') THEN
    CREATE ROLE table_manager_admin;
    RAISE NOTICE 'Role table_manager_admin created.';
  ELSE
    RAISE NOTICE 'Role table_manager_admin already exists.';
  END IF;
END $$;

REVOKE EXECUTE ON FUNCTION pg_protect_table(TEXT) FROM PUBLIC;
REVOKE EXECUTE ON FUNCTION pg_unprotect_table(TEXT) FROM PUBLIC;

GRANT EXECUTE ON FUNCTION pg_protect_table(TEXT) TO table_manager_admin;
GRANT EXECUTE ON FUNCTION pg_unprotect_table(TEXT) TO table_manager_admin; 