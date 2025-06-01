# Database Table Protection System

A comprehensive database security management system that provides table-level protection for PostgreSQL databases. This system prevents unauthorized modifications (DELETE, UPDATE, ALTER, DROP) to protected tables and maintains an audit log of all attempted operations.

## 🚀 Features

- **Table Protection**: Prevent DELETE, UPDATE, ALTER, and DROP operations on protected tables
- **Whitelist Approach**: All tables are protected by default unless explicitly unprotected
- **Operation Logging**: Automatic logging of all protected operations to audit file
- **Role-Based Access**: Dedicated `table_manager_admin` role for protection management
- **Easy Management**: Simple SQL functions to protect/unprotect tables
- **Spring Boot Integration**: Web application framework for UI-based management (in development)

## 📋 Components

### 1. PostgreSQL Extension (`licenta-plugin/`)
A native C extension that implements the core protection mechanism using PostgreSQL hooks.

### 2. Spring Boot Application (`licenta-aplicatie/`)
A Java web application for managing table protections through a user-friendly interface (currently in development).

## 🛠️ Installation

### Prerequisites
- PostgreSQL 16 or later
- GCC compiler for building the C extension
- Java 17+ and Maven for the Spring Boot application

### Installing the PostgreSQL Extension

```bash
cd licenta-plugin
./install.sh
```

Or manually:
```bash
cd licenta-plugin
make
sudo make install
```

Then in your PostgreSQL database:
```sql
CREATE EXTENSION pg_log;
```

### Running the Spring Boot Application

```bash
cd licenta-aplicatie
./mvnw spring-boot:run
```

## 📖 Usage

### Basic Table Protection

```sql
-- Protect a table (remove from unprotected list)
SELECT pg_protect_table('sensitive_data');

-- Unprotect a table (add to unprotected list)
SELECT pg_unprotect_table('temporary_data');

-- View all logged operations
SELECT * FROM pg_all_queries();

-- Check protection status
SELECT * FROM pg_unprotected_tables;
```

### Example Workflow

```sql
-- Create a test table
CREATE TABLE employee_salaries (
    id SERIAL PRIMARY KEY,
    employee_name VARCHAR(100),
    salary DECIMAL(10,2)
);

-- Table is protected by default
-- This will fail:
DELETE FROM employee_salaries; -- ERROR: Operations on protected table are not allowed

-- Unprotect the table
SELECT pg_unprotect_table('employee_salaries');

-- Now operations work
DELETE FROM employee_salaries; -- Success

-- Re-protect the table
SELECT pg_protect_table('employee_salaries');
```

## 🔒 Security Model

- **Default Protection**: All tables are protected by default
- **Role-Based Access**: Only users with `table_manager_admin` role can manage protections
- **Audit Logging**: All protected operations are logged to `/tmp/pg_protected_ops.log`
- **Whitelist Approach**: Only explicitly unprotected tables allow modifications

## 🏗️ Architecture

### PostgreSQL Extension
- Uses `ProcessUtility_hook` for DDL command interception
- Uses `post_parse_analyze_hook` for DML command interception
- Maintains `pg_unprotected_tables` for whitelist management
- Logs operations to filesystem for audit trail

### Spring Boot Application (In Development)
- Spring Security for authentication
- Spring Data JPA for database access
- RESTful API for table protection management
- Web UI for monitoring and configuration

## 📝 Development

### Building from Source

**PostgreSQL Extension:**
```bash
cd licenta-plugin
make clean && make
```

**Spring Boot Application:**
```bash
cd licenta-aplicatie
./mvnw clean package
```

### Running Tests

**PostgreSQL Extension:**
```bash
psql -U postgres -d test_db -f licenta-plugin/test.sql
```

**Spring Boot Application:**
```bash
cd licenta-aplicatie
./mvnw test
```

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## 📄 License

This project is licensed under the PostgreSQL License - see the LICENSE file for details.

## 🔮 Future Enhancements

- [ ] Complete Spring Boot application implementation
- [ ] Web-based dashboard for protection management
- [ ] Real-time operation monitoring
- [ ] Email alerts for protection violations
- [ ] Integration with existing authentication systems
- [ ] Support for column-level protection
- [ ] Configurable log destinations

## 📞 Support

For issues, questions, or contributions, please open an issue on GitHub.