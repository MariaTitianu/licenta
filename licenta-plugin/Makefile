MODULE_big = pg_log
OBJS = log.o
EXTENSION = pg_log
DATA = pg_log--1.0.sql

PG_CONFIG = /usr/lib/postgresql/16/bin/pg_config
PGXS := $(shell $(PG_CONFIG) --pgxs)
PG_CPPFLAGS = -I/usr/include/postgresql/16/server -I/usr/include/postgresql/internal
include $(PGXS)
