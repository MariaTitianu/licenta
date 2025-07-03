# Frontend Implementation Plan - pg_warden Demonstration

## Overview
This document outlines the implementation plan for a React-based demonstration frontend that showcases the pg_warden PostgreSQL security extension's capabilities.

## Project Goals
- Demonstrate table protection mechanisms
- Show SQL injection vulnerability differences (JPA vs JDBC)
- Visualize performance impact through benchmarks
- Provide real-time operation logging
- Enable easy switching between 6 different backend configurations

## Architecture

### Backend Services
The frontend will connect to 6 different Spring Boot containers:
1. **Port 8081**: JPA + Admin + pg_warden
2. **Port 8082**: JPA + User + pg_warden
3. **Port 8083**: JDBC + Admin + pg_warden
4. **Port 8084**: JDBC + User + pg_warden
5. **Port 8085**: JPA + Admin + Vanilla PostgreSQL
6. **Port 8086**: JDBC + Admin + Vanilla PostgreSQL

### Technology Stack
- **Framework**: React 19
- **UI Library**: Material-UI (@mui/material)
- **Data Grid**: MUI X DataGrid
- **Charts**: MUI X Charts
- **Notifications**: React Toastify
- **HTTP Client**: Native fetch API
- **State Management**: React hooks (useState, useEffect)

## Core Features

### 1. Backend Selector
- Dropdown menu to switch between 6 backend configurations
- Visual indicator showing current configuration
- Automatic data refresh on backend switch

### 2. Table Management
- **Products Table**
  - Full CRUD operations using MUI DataGrid
  - Inline editing capabilities
  - Delete with confirmation
- **Customer Payments Table**
  - Read-only display
  - Visual protection status indicator

### 3. Protection Management
- Protection status badges (Protected/Unprotected)
- Protect/Unprotect buttons for each table
- Real-time status updates
- Color coding: Red (protected), Green (unprotected)

### 4. SQL Injection Demonstration
- Input field for custom SQL injection attempts
- Pre-built injection examples:
  - Basic OR: `1 OR 1=1`
  - String bypass: `' OR '1'='1`
  - Delete attempt: `1; DELETE FROM products; --`
- Results display showing success/failure
- Automatic log refresh to show blocked operations

### 5. Operation Logs
- Log viewer showing recent operations
- Refreshed after each data-modifying operation
- Color-coded by status:
  - Green: Successful operations
  - Red: Blocked operations
  - Yellow: Errors
- Filterable by operation type and table

### 6. Benchmark Dashboard
- Run benchmarks on current backend
- Operations tested: SELECT, INSERT, UPDATE, DELETE
- Results visualization:
  - Execution time per operation
  - Success/blocked counts
  - Bar charts comparing performance
- Option to compare with vanilla equivalent

## Component Structure

```
src/
├── components/
│   ├── BackendSelector.js      // Backend switching dropdown
│   ├── ProductsTable.js        // Products CRUD table
│   ├── PaymentsTable.js        // Payments read-only table
│   ├── ProtectionPanel.js      // Protection status & controls
│   ├── InjectionDemo.js        // SQL injection test panel
│   ├── LogViewer.js            // Operation logs display
│   └── BenchmarkPanel.js       // Benchmark runner & results
├── App.js                      // Main dashboard layout
├── api.js                      // Fetch wrapper for API calls
└── index.js                    // React entry point
```

## Implementation Approach

### API Service Pattern
```javascript
const API = {
  baseUrl: 'http://localhost:8081',
  
  async fetch(endpoint, options = {}) {
    const response = await fetch(`${this.baseUrl}${endpoint}`, {
      headers: { 'Content-Type': 'application/json' },
      ...options
    });
    if (!response.ok) throw new Error(`HTTP ${response.status}`);
    return response.json();
  },
  
  setBackend(port) {
    this.baseUrl = `http://localhost:${port}`;
  }
};
```

### State Management
- Use React's built-in useState for local component state
- Lift state up to App.js for shared data (logs, protection status)
- Refresh pattern: After any operation, fetch updated logs

### Error Handling
- Try-catch blocks around all API calls
- Toast notifications for success/error feedback
- Differentiate between:
  - Network errors
  - Protection blocks
  - Standard API errors

## Implementation Timeline

### Day 1: Foundation
- Set up React project structure
- Create main layout with Material-UI
- Implement backend selector
- Build products table with CRUD operations
- Create basic API service

### Day 2: Security Features
- Add protection status display and controls
- Implement SQL injection demo panel
- Build log viewer with filtering
- Add operation feedback (toasts)

### Day 3: Benchmarks & Polish
- Create benchmark runner
- Implement results visualization
- Add error boundaries
- Final styling and responsive design
- Testing and bug fixes

## Key Design Decisions

1. **Simplicity First**: Using fetch instead of Axios, React state instead of Redux/Zustand
2. **Demo-Focused**: No production features like caching, optimistic updates, or real-time sync
3. **Visual Clarity**: Clear indication of protection status, blocked operations, and performance impact
4. **User Flow**: Select backend → Perform operation → See immediate feedback in logs

## Success Criteria

- [ ] All 6 backends are selectable and functional
- [ ] CRUD operations work on products table
- [ ] Protection can be toggled and affects operations
- [ ] SQL injection attempts show clear difference between JPA/JDBC
- [ ] Logs update after each operation
- [ ] Benchmarks show performance comparison
- [ ] UI is intuitive and visually appealing

## Future Enhancements (Out of Scope)
- Real-time log streaming
- Multi-backend simultaneous comparison
- Export functionality for benchmark results
- User authentication
- Mobile responsive design

---

This plan prioritizes demonstration effectiveness over architectural complexity, ensuring the pg_warden security features are clearly showcased to users.