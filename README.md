# Database Table Protection System

A comprehensive database security management system that provides table-level protection for PostgreSQL databases. This system prevents unauthorized modifications (DELETE, UPDATE, ALTER, DROP) to protected tables and maintains an audit log of all attempted operations.

## 🚀 Features

- **Table Protection**: Prevent DELETE, UPDATE, ALTER, and DROP operations on protected tables
- **Whitelist Approach**: All tables are protected by default unless explicitly unprotected
- **Operation Logging**: Automatic logging of all protected operations to audit file
- **Role-Based Access**: Dedicated `warden_admin` role for protection management
- **Easy Management**: Simple SQL functions to protect/unprotect tables
- **Spring Boot Integration**: Web application framework for UI-based management (in development)

## 📋 Components

### 1. PostgreSQL Extension (`licenta-plugin/`)
A native C extension that implements the core protection mechanism using PostgreSQL hooks.

### 2. Spring Boot Application (`licenta-aplicatie/`)
A Java web application for managing table protections through a user-friendly interface (currently in development).

## 🛠️ Installation

### Option 1: Docker (Recommended - One-Click Setup)

#### Linux/macOS:
```bash
./start-app.sh              # Start all services (preserves data)
./start-app.sh --reset-db   # Start and wipe PostgreSQL data
```

#### Windows:
```powershell
# First time only - enable PowerShell script execution:
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser

# Then run:
./start-app.ps1              # Start all services (preserves data)
./start-app.ps1 -ResetDb     # Start and wipe PostgreSQL data
```

This automatically:
- Builds and installs the pg_warden extension
- Creates a PostgreSQL database with demo data
- Sets up pgAdmin for database management
- Starts 6 Spring Boot backend variants
- Starts React frontend on port 3000

**Services:**
- Frontend: http://localhost:3000
- Backend APIs: http://localhost:8081-8086
- PostgreSQL: `localhost:5433` (database: `licenta_db`)
  - Admin user: `warden_admin_user` / `warden_admin_pass`
  - Regular user: `regular_user` / `regular_user_pass`
- pgAdmin: http://localhost:5050
  - Email: `admin@licenta.com`
  - Password: `admin`

### Option 2: Manual Installation

Prerequisites:
- PostgreSQL 16 or later
- GCC compiler for building the C extension
- Java 17+ and Maven for the Spring Boot application

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
CREATE EXTENSION pg_warden;
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
SELECT warden_protect('sensitive_data');

-- Unprotect a table (add to unprotected list)
SELECT warden_unprotect('temporary_data');

-- View all logged operations
SELECT * FROM warden_all_queries();

-- Check protection status
SELECT * FROM warden_unprotected_tables;
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
SELECT warden_unprotect('employee_salaries');

-- Now operations work
DELETE FROM employee_salaries; -- Success

-- Re-protect the table
SELECT warden_protect('employee_salaries');
```

## 🔒 Security Model

- **Default Protection**: All tables are protected by default
- **Role-Based Access**: Only users with `warden_admin` role can manage protections
- **Audit Logging**: All protected operations are logged to `/tmp/pg_warden_ops.log`
- **Whitelist Approach**: Only explicitly unprotected tables allow modifications

## 🏗️ Architecture

### PostgreSQL Extension
- Uses `ProcessUtility_hook` for DDL command interception
- Uses `post_parse_analyze_hook` for DML command interception
- Maintains `warden_unprotected_tables` for whitelist management
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