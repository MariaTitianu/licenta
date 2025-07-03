# Backend Architecture and Comparison System

## Overview

The application uses a dual-backend architecture to demonstrate security vulnerabilities and compare performance:

1. **JPA Backend** - Uses Spring Data JPA with parameterized queries (secure)
2. **JDBC Backend** - Uses raw JDBC with string concatenation (intentionally vulnerable to SQL injection)

## Backend Selection Mechanism

### Spring Profiles
The backend type is determined by Spring profiles configured in `application.properties`:
```properties
spring.profiles.active=${ACTIVE_PROFILE:jpa}
```

- Profile `jpa` activates JPA repositories (secure)
- Profile `jdbc` activates JDBC repositories (vulnerable)

### Repository Implementation
Each repository interface has two implementations:
- `@Profile("jpa")` - JPA implementation using Spring Data JPA
- `@Profile("jdbc")` - JDBC implementation with manual SQL

Example:
```java
@Repository
@Profile("jpa")
public interface ProductJpaRepository extends ProductRepository, JpaRepository<Product, Long> {
    // Secure parameterized queries
}

@Repository
@Profile("jdbc")
public class ProductJdbcRepository implements ProductRepository {
    // Vulnerable string concatenation: 
    // String sql = "SELECT * FROM products WHERE id = " + id;
}
```

## Docker Container Configuration

The system runs 6 different backend configurations:

### With pg_warden Extension:
1. **Port 8081**: JPA + Admin user + pg_warden
2. **Port 8082**: JPA + Regular user + pg_warden  
3. **Port 8083**: JDBC + Admin user + pg_warden
4. **Port 8084**: JDBC + Regular user + pg_warden

### Vanilla PostgreSQL (without pg_warden):
5. **Port 8085**: JPA + Admin user + Vanilla PostgreSQL
6. **Port 8086**: JDBC + Admin user + Vanilla PostgreSQL

## Benchmark Comparison Logic

When running benchmarks, the frontend:

1. **Primary Backend Selection**: User selects one of the 6 backends
2. **Comparison Logic** (when "Compare with Vanilla PostgreSQL" is checked):
   - If JPA backend selected (8081/8082) → Compare with JPA Vanilla (8085)
   - If JDBC backend selected (8083/8084) → Compare with JDBC Vanilla (8086)
   - If Vanilla backend selected (8085/8086) → No comparison available

### Key Point: JPA is Always Compared with JPA

The comparison is **like-for-like**:
- JPA + pg_warden is compared with JPA + Vanilla
- JDBC + pg_warden is compared with JDBC + Vanilla

This ensures fair performance comparisons by keeping the data access layer constant and only varying the security extension.

## Benchmark Service Architecture

The `BenchmarkService` class:
1. Uses the injected `ProductRepository` interface
2. The actual implementation (JPA or JDBC) depends on the active Spring profile
3. Runs the same benchmark operations regardless of backend
4. Measures performance metrics including percentiles (p50, p95, p99)

## Performance Impact Analysis

The benchmark results show:
- **Overhead percentage**: How much slower pg_warden operations are compared to vanilla
- **Operation-specific impact**: Different operations (SELECT, INSERT, UPDATE, DELETE) have different overhead
- **Protection state impact**: Protected vs unprotected tables show different performance characteristics

## Security vs Performance Trade-off

- **JPA backends**: Secure by default, slightly slower due to ORM overhead
- **JDBC backends**: Faster but vulnerable to SQL injection in the current implementation
- **pg_warden**: Adds security layer with measurable performance overhead
- **Vanilla**: No security extension, baseline performance

This architecture allows demonstrating:
1. Security vulnerabilities (JDBC string concatenation)
2. Performance impact of security extensions (pg_warden)
3. ORM vs raw SQL performance differences
4. Multi-user security scenarios (admin vs regular user)