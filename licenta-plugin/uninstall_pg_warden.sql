DROP EXTENSION IF EXISTS pg_warden CASCADE;


DROP ROLE IF EXISTS warden_admin;

RAISE NOTICE 'pg_warden extension and warden_admin role have been dropped if they existed.'; 