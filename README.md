# pg_log PostgreSQL Extension

A PostgreSQL extension for logging ALTER TABLE commands and protecting tables against DELETE and UPDATE operations.

## Prerequisites

- PostgreSQL 16 or compatible version
- PostgreSQL development packages
- GCC compiler and Make

## Installation

1. Install PostgreSQL development packages:
   ```
   # For Debian/Ubuntu
   sudo apt-get install postgresql-server-dev-16
   
   # For RedHat/CentOS
   sudo yum install postgresql16-devel
   ```

2. Build and install the extension:
   ```
   make
   sudo make install
   ```

3. Create the extension in your database:
   ```sql
   CREATE EXTENSION pg_log;
   ```

## Usage

### Protect a table

```sql
SELECT pg_protect_table('your_table_name');
```

### Unprotect a table

```sql
SELECT pg_unprotect_table('your_table_name');
```

### View logged ALTER TABLE commands

```sql
SELECT * FROM pg_all_queries();
```

## Features

- Logs all ALTER TABLE commands to `/tmp/alter_table.log`
- Prevents DELETE and UPDATE operations on protected tables
- Provides functions to manage table protection

## License

Copyright (c) 2024 