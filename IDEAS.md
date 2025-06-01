# Project Ideas and Architecture Decisions

## Demonstrator Application Architecture (2024-06-02)

### The Idea
Create a demonstrator application that showcases how the PostgreSQL table protection extension (pg_log) works by allowing users to test different attack scenarios and see how the protection mechanism responds.

### Architecture Decision: 4-Container Approach

We decided to use 4 separate backend containers instead of role-switching because:
1. **Simplicity**: Each container has hardcoded database credentials - no authentication complexity
2. **Clear demonstration**: Each container represents exactly one scenario
3. **Better for demos**: Can show all scenarios simultaneously for easy comparison
4. **Easier implementation**: No conditional logic needed for role switching

### Container Structure
```
Container 1: secure-admin
- Uses: Spring Data JPA (secure)
- DB User: table_manager_admin
- Port: 8081
- Purpose: Show admin can protect/unprotect tables with secure code

Container 2: secure-user  
- Uses: Spring Data JPA (secure)
- DB User: regular_user
- Port: 8082
- Purpose: Show regular user cannot modify protected tables even with secure code

Container 3: vulnerable-admin
- Uses: Raw JDBC with SQL injection vulnerabilities
- DB User: table_manager_admin  
- Port: 8083
- Purpose: Show that even admin with vulnerable code is protected by the extension

Container 4: vulnerable-user
- Uses: Raw JDBC with SQL injection vulnerabilities
- DB User: regular_user
- Port: 8084  
- Purpose: Show SQL injection attempts fail on protected tables
```

### Frontend
- Single frontend application with:
  - Login screen (just for show, no real auth)
  - Dropdown/selector to choose which backend to communicate with
  - UI to test various operations (SELECT, INSERT, UPDATE, DELETE)
  - Display of results showing what succeeded/failed

### Key Demonstration Scenarios
1. **Admin protects a table** → User cannot DELETE/UPDATE even with valid queries
2. **SQL injection attempt on protected table** → Fails regardless of user type
3. **Admin unprotects a table** → Operations succeed (showing the protection was active)
4. **Comparison between JPA and vulnerable DAO** → Same protection applies regardless of code quality

### Why This Architecture Works
- **Educational**: Clearly shows the extension protects at the database level, not application level
- **Comprehensive**: Tests all combinations of user types and code quality
- **Simple to implement**: Each container is independent with static configuration
- **Easy to demonstrate**: Switch between backends to show different scenarios instantly