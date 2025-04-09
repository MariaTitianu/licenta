SET client_min_messages TO 'NOTICE';
INSERT INTO protected (name) VALUES ('Test Row');
DELETE FROM protected WHERE 1=1;