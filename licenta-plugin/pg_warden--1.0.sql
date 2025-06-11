\echo Use "CREATE EXTENSION pg_warden" to load this file. \quit


CREATE TABLE warden_unprotected_tables (
    table_name TEXT PRIMARY KEY,
    unprotect_time TIMESTAMP NOT NULL DEFAULT now(),
    unprotected_by TEXT NOT NULL
);


CREATE FUNCTION warden_all_queries() 
RETURNS TABLE (query TEXT, pid TEXT)
AS 'MODULE_PATHNAME', 'warden_all_queries'
LANGUAGE C STRICT;

CREATE FUNCTION warden_protect(tablename TEXT)
RETURNS BOOLEAN
AS 'MODULE_PATHNAME', 'warden_protect'
LANGUAGE C STRICT;

CREATE FUNCTION warden_unprotect(tablename TEXT) 
RETURNS BOOLEAN
AS 'MODULE_PATHNAME', 'warden_unprotect'
LANGUAGE C STRICT;

DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'warden_admin') THEN
    CREATE ROLE warden_admin;
    RAISE NOTICE 'Role warden_admin created.';
  ELSE
    RAISE NOTICE 'Role warden_admin already exists.';
  END IF;
END $$;

REVOKE EXECUTE ON FUNCTION warden_protect(TEXT) FROM PUBLIC;
REVOKE EXECUTE ON FUNCTION warden_unprotect(TEXT) FROM PUBLIC;

GRANT EXECUTE ON FUNCTION warden_protect(TEXT) TO warden_admin;
GRANT EXECUTE ON FUNCTION warden_unprotect(TEXT) TO warden_admin; 