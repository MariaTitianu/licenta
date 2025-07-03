const API = {
  baseUrl: 'http://localhost:8081',
  currentPort: 8081,

  async fetch(endpoint, options = {}) {
    try {
      // Use relative URL when running in production (Docker)
      const baseUrl = window.location.hostname === 'localhost' && window.location.port
        ? this.baseUrl
        : `/backend-${this.currentPort}`;
        
      const response = await fetch(`${baseUrl}${endpoint}`, {
        headers: { 'Content-Type': 'application/json' },
        ...options,
      });

      if (!response.ok) {
        const error = await response.json().catch(() => ({ message: `HTTP ${response.status}` }));
        throw error;
      }

      // Handle 204 No Content responses (typically from DELETE operations)
      if (response.status === 204) {
        return null;
      }

      return response.json();
    } catch (error) {
      throw error;
    }
  },

  setBackend(port) {
    this.currentPort = port;
    this.baseUrl = `http://localhost:${port}`;
  },

  // Products API
  products: {
    getAll: () => API.fetch('/api/products'),
    getById: (id) => API.fetch(`/api/products/${id}`),
    create: (data) => API.fetch('/api/products', { method: 'POST', body: JSON.stringify(data) }),
    update: (id, data) => API.fetch(`/api/products/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
    delete: (id) => API.fetch(`/api/products/${id}`, { method: 'DELETE' }),
  },

  // Payments API
  payments: {
    getAll: () => API.fetch('/api/payments'),
    getById: (id) => API.fetch(`/api/payments/${id}`),
    create: (data) => API.fetch('/api/payments', { method: 'POST', body: JSON.stringify(data) }),
    update: (id, data) => API.fetch(`/api/payments/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
    delete: (id) => API.fetch(`/api/payments/${id}`, { method: 'DELETE' }),
  },

  // Protection API
  protection: {
    getSummary: () => API.fetch('/api/protection/summary'),
    getStatus: (tableName) => API.fetch(`/api/protection/status/${tableName}`),
    protect: (tableName) => API.fetch(`/api/protection/protect/${tableName}`, { method: 'POST' }),
    unprotect: (tableName) => API.fetch(`/api/protection/unprotect/${tableName}`, { method: 'POST' }),
  },

  // Logs API
  logs: {
    getAll: (limit = 500) => API.fetch(`/api/logs/operations?limit=${limit}`),
    getByTable: (tableName, limit = 500) => API.fetch(`/api/logs/operations/table/${tableName}?limit=${limit}`),
    getRecent: (limit = 500) => API.fetch(`/api/logs/operations/recent?limit=${limit}`),
    getBlocked: (limit = 500) => API.fetch(`/api/logs/operations/blocked?limit=${limit}`),
    getAllowed: (limit = 500) => API.fetch(`/api/logs/operations/allowed?limit=${limit}`),
    getSummary: () => API.fetch('/api/logs/summary'),
  },

  // Benchmark API
  benchmark: {
    run: (request) => API.fetch('/api/benchmark/run', { method: 'POST', body: JSON.stringify(request) }),
  },

  // Test API (vulnerable endpoints for SQL injection demo)
  test: {
    deletePaymentById: (id) => API.fetch(`/api/test/payments/by-id/${id}`, { method: 'DELETE' }),
    deletePaymentByCustomer: (customer) => API.fetch(`/api/test/payments/by-customer/${customer}`, { method: 'DELETE' }),
  },
};

export default API;