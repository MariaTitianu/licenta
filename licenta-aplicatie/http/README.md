# API Demonstration Files

This folder contains HTTP request files that demonstrate all implemented endpoints of the secure-admin Spring Boot application.

## Files Overview

### 1. `products.http`
Complete CRUD operations for the products table:
- GET all products, by ID, by name
- Search by category, name, stock quantity
- POST single/bulk products
- PUT update products
- DELETE single/all products

### 2. `payments.http`
Complete CRUD operations for the customer_payments table:
- GET all payments, by ID, by transaction ID
- Search by customer, card type, amount, date range
- POST single/bulk payments
- PUT update payments
- DELETE single/all payments (likely blocked due to protection)

### 3. `protection.http`
Admin-only table protection management:
- GET protection status, summary, unprotected tables
- POST protect/unprotect individual tables
- POST bulk protect/unprotect operations
- Error handling for non-existent tables

### 4. `logs.http`
Operation logging and audit trails:
- GET recent operations with various filters
- GET blocked vs allowed operations
- GET operations by table name
- GET operation statistics and summaries

### 5. `demonstration-scenarios.http`
**Most Important** - Complete demonstration scenarios that showcase:
- Initial state (products unprotected, payments protected)
- Protection in action (blocking DELETE/UPDATE)
- Risk demonstration (unprotecting sensitive data)
- Audit trail viewing
- Bulk operations testing

## How to Use

### Prerequisites
1. Start PostgreSQL with the pg_warden extension
2. Start the Spring Boot application: `./mvnw spring-boot:run`
3. Application should be running on http://localhost:8081

### Using VS Code or IntelliJ
1. Install the REST Client extension (VS Code) or use built-in HTTP client (IntelliJ)
2. Open any `.http` file
3. Click "Send Request" above each HTTP request

### Using curl
Convert any request to curl format. Example:
```bash
curl -X GET http://localhost:8081/api/protection/summary \
  -H "Accept: application/json"
```

## Recommended Testing Order

1. **Start with `demonstration-scenarios.http`** - This provides a complete walkthrough of the protection capabilities
2. Use `protection.http` to understand admin protection management
3. Use `products.http` and `payments.http` to test CRUD operations
4. Use `logs.http` to view audit trails after performing operations

## Expected Behaviors

### Protected Tables (default: customer_payments)
- ❌ DELETE operations → 403 Forbidden
- ❌ UPDATE operations → 403 Forbidden
- ✅ INSERT operations → 201 Created
- ✅ SELECT operations → 200 OK

### Unprotected Tables (default: products)
- ✅ All CRUD operations work normally
- ✅ Until admin protects the table using protection endpoints

### Error Responses
Protection violations return structured error responses:
```json
{
  "status": 403,
  "error": "TABLE_PROTECTED",
  "message": "Operation blocked by database protection",
  "details": {
    "operation": "DELETE",
    "table": "customer_payments",
    "reason": "Table is protected by pg_warden extension",
    "dbError": "ERROR: Table 'customer_payments' is protected from DELETE operations"
  },
  "timestamp": "2024-12-06T10:30:45.123Z"
}
```

## Database Configuration

The application connects using these defaults (configurable via environment variables):
- Host: localhost:5433
- Database: postgres
- User: warden_admin_user
- Password: warden_admin_pass

## Notes

- All endpoints include CORS headers for frontend integration
- Error handling preserves original PostgreSQL error messages for clarity
- Operation logs are automatically captured by the pg_warden extension
- Admin endpoints require the application to connect as a user with warden_admin role