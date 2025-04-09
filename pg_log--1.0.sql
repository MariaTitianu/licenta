-- complain if script is sourced in psql, rather than via CREATE EXTENSION
\echo Use "CREATE EXTENSION pg_log" to load this file. \quit

-- Create table to store unprotected tables
CREATE TABLE pg_unprotected_tables (
    table_name TEXT PRIMARY KEY,
    unprotect_time TIMESTAMP NOT NULL DEFAULT now(),
    unprotected_by TEXT NOT NULL
);

-- Register the C functions
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