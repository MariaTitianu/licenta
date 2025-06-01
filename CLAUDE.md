# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This repository implements a database security management system with two components:

1. **PostgreSQL Extension (`licenta-plugin/`)** - A C-based extension that protects tables from modifications (DELETE, UPDATE, ALTER, DROP)
2. **Spring Boot Application (`licenta-aplicatie/`)** - A Java web application skeleton for managing table protections (currently unimplemented)

## PostgreSQL Extension Commands

### Build and Install
```bash
cd licenta-plugin
make clean && make
sudo make install
# Or use the automated script:
./install.sh
```

### Test the Extension
```bash
psql -U postgres -d your_database -f test.sql
```

### Uninstall
```bash
psql -U postgres -d your_database -f uninstall_pg_log.sql
```

## Spring Boot Application Commands

### Build
```bash
cd licenta-aplicatie
./mvnw clean package
```

### Run Application
```bash
./mvnw spring-boot:run
```

### Run Tests
```bash
./mvnw test
```

### Run a Single Test
```bash
./mvnw test -Dtest=LicentaApplicationTests
```

## Architecture Overview

### PostgreSQL Extension Architecture

The extension uses a **whitelist approach** where all tables are protected by default unless explicitly unprotected:

- **Hooks**: 
  - `ProcessUtility_hook`: Intercepts DDL commands (ALTER, DROP)
  - `post_parse_analyze_hook`: Intercepts DML commands (DELETE, UPDATE)
- **Protection Table**: `pg_unprotected_tables` stores the list of unprotected tables
- **Logging**: Protected operations are logged to `/tmp/pg_protected_ops.log`
- **Security Role**: `table_manager_admin` role controls access to protection functions

**Key Functions**:
- `pg_protect_table(tablename)`: Remove table from unprotected list
- `pg_unprotect_table(tablename)`: Add table to unprotected list
- `pg_all_queries()`: View logged operations

### Spring Boot Application Architecture

The application is currently a skeleton that needs implementation. Expected architecture:
- **Database**: PostgreSQL integration via Spring Data JPA (driver not yet added to pom.xml)
- **Security**: Spring Security for authentication/authorization
- **API**: Spring Web for REST endpoints
- **Integration Point**: Should connect as `table_manager_admin` role to manage protections

## Development Notes

### PostgreSQL Extension
- The extension creates `pg_unprotected_tables` table on first use
- All tables are protected by default (whitelist approach)
- Protection prevents DELETE, UPDATE, ALTER TABLE, and DROP TABLE operations
- The `table_manager_admin` role is created with exclusive access to protection functions

### Spring Boot Application
- Currently only contains boilerplate code
- Needs PostgreSQL driver dependency in pom.xml
- Should implement entities, repositories, services, and controllers for table protection management
- Must configure database connection to use `table_manager_admin` role

## Development Guidelines

### Documentation Requirements
- **Document all changes**: All implementation documentation must be written in `DOCUMENTATION.md` explaining what was implemented, why, and how it works
- **Update CLAUDE.md**: As you implement new features, add relevant commands, architecture details, or important notes to this file
- **Code comments**: Add meaningful comments to complex logic or integration points
- **Commit messages**: Write clear, descriptive commit messages explaining the changes

### Documentation Structure
- **DOCUMENTATION.md**: Primary documentation file for all implementation details, design decisions, and feature explanations
- **CLAUDE.md**: Reference guide for Claude Code with commands and architecture overview (this file)
- **README.md**: Public-facing project overview and setup instructions

### When to Update CLAUDE.md
- After adding new build/test/run commands
- When implementing significant architectural changes
- After creating new integration points between components
- When discovering important configuration requirements
- After resolving complex issues that future developers might encounter