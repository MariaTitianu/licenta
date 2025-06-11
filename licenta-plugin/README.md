# pg_warden PostgreSQL Extension

A PostgreSQL extension for protecting tables against modifications (DELETE, UPDATE, ALTER, and DROP operations).

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
   CREATE EXTENSION pg_warden;
   ```

## Usage

### Protect a table

```sql
SELECT warden_protect('your_table_name');
```

### Unprotect a table

```sql
SELECT warden_unprotect('your_table_name');
```

### View logged operations

```sql
SELECT * FROM warden_all_queries();
```

## Features

- Logs all protected operations to `/tmp/pg_warden_ops.log`
- Prevents DELETE, UPDATE, ALTER TABLE, and DROP TABLE operations on protected tables
- Provides functions to manage table protection
- Requires `warden_admin` role to protect/unprotect tables

## License

Copyright (c) 2024 