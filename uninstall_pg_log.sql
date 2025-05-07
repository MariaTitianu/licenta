DROP EXTENSION IF EXISTS pg_log CASCADE;


DROP ROLE IF EXISTS table_manager_admin;

RAISE NOTICE 'pg_log extension and table_manager_admin role have been dropped if they existed.'; 