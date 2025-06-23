import { useState, useRef, useEffect } from 'react';
import { Typography, TextField, Button, Divider, Paper, Box } from '@mui/material';
import { toast } from 'react-toastify';
import BackendSelectorWithToggle from '../components/BackendSelectorWithToggle';
import API from '../api';
import { useTheme } from '../context/ThemeContext';

const Injection = ({ selectedBackend, setSelectedBackend }) => {
  const { colors, isDarkMode } = useTheme();
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState(null);
  const [error, setError] = useState(null);
  const [tableProtectionStatus, setTableProtectionStatus] = useState(null);

  // Form refs
  const injectionInputRef = useRef(null);

  const injectionExamples = [
    { label: 'Basic OR', value: '1 OR 1=1' },
    { label: 'String bypass', value: "' OR '1'='1" },
    { label: 'Delete all payments', value: '1; DELETE FROM customer_payments; --' },
    { label: 'Update amounts', value: "1; UPDATE customer_payments SET amount=9999.99; --" },
    { label: 'Drop payments table', value: '1; DROP TABLE customer_payments; --' },
    { label: 'Steal card data', value: "1 UNION SELECT id, customer_name, card_last_four_digits, card_type FROM customer_payments --" },
  ];

  const handleExampleClick = (example) => {
    injectionInputRef.current.value = example.value;
  };

  const handleExecute = async () => {
    const injection = injectionInputRef.current.value;
    if (!injection.trim()) {
      toast.error('Please enter an SQL injection attempt');
      return;
    }

    setLoading(true);
    setResult(null);
    setError(null);

    try {
      // Use the vulnerable test endpoint that accepts string parameters
      const data = await API.test.deletePaymentById(injection);
      setResult(data);
      
      if (data.success) {
        toast.error(`SQL INJECTION SUCCESSFUL! ${data.rowsAffected} payment records deleted!`);
      } else {
        toast.success('SQL injection blocked by pg_warden');
      }
    } catch (err) {
      // Check if it's a 404 (endpoint not available on JPA backends)
      if (err.message && err.message.includes('404')) {
        setError('Test endpoints only available on JDBC backends (ports 8083, 8084, 8086)');
        toast.warning('Switch to a JDBC backend to test SQL injection');
      } else if (!isJDBC()) {
        // On JPA backends, any error means the injection was blocked
        setResult({
          success: false,
          error: err.message || 'Query failed - injection blocked by JPA',
          attemptedQuery: `DELETE FROM customer_payments WHERE id = ${injection}`
        });
        toast.success('SQL injection blocked by JPA parameterized queries');
      } else {
        // On JDBC backends, this is an actual error
        setError(err.message || 'Query failed - injection blocked or invalid');
        toast.error('SQL injection blocked or failed');
      }
    } finally {
      setLoading(false);
      // Refresh logs to show the attempt
      setTimeout(() => API.logs.getRecent(), 500);
    }
  };

  const getBackendType = () => {
    const types = {
      8081: 'JPA with Admin (pg_warden)',
      8082: 'JPA with User (pg_warden)',
      8083: 'JDBC with Admin (pg_warden)',
      8084: 'JDBC with User (pg_warden)',
      8085: 'JPA with Admin (Vanilla)',
      8086: 'JDBC with Admin (Vanilla)',
    };
    return types[selectedBackend] || 'Unknown';
  };

  const isJDBC = () => selectedBackend === 8083 || selectedBackend === 8084 || selectedBackend === 8086;
  const hasPgWarden = () => selectedBackend !== 8085 && selectedBackend !== 8086;

  // Fetch table protection status
  useEffect(() => {
    const fetchProtectionStatus = async () => {
      if (hasPgWarden()) {
        try {
          const status = await API.protection.getStatus('customer_payments');
          setTableProtectionStatus(status);
        } catch (err) {
          console.error('Failed to fetch protection status:', err);
          setTableProtectionStatus(null);
        }
      } else {
        setTableProtectionStatus(null);
      }
    };
    fetchProtectionStatus();
  }, [selectedBackend]);

  return (
    <div className="page-container">
      <div className="page-content">
        <div className="sidebar">
          <BackendSelectorWithToggle value={selectedBackend} onChange={setSelectedBackend} />
          
          <Typography variant="h5" gutterBottom style={{ color: colors.text }}>
            SQL Injection Demo
          </Typography>

          <Typography variant="body2" gutterBottom style={{ color: colors.textSecondary }}>
            Current backend: {getBackendType()}
          </Typography>

          <Divider sx={{ my: 2, borderColor: colors.border }} />

          <Typography variant="body2" gutterBottom>
            {isJDBC() ? (
              <span style={{ color: colors.error, fontWeight: 'bold' }}>
                ⚠️ JDBC backend - Vulnerable to SQL injection!
              </span>
            ) : (
              <span style={{ color: colors.success, fontWeight: 'bold' }}>
                ✓ JPA backend - Protected against SQL injection
              </span>
            )}
          </Typography>

          {hasPgWarden() && tableProtectionStatus !== null && (
            <Typography variant="body2" gutterBottom>
              {tableProtectionStatus.isProtected ? (
                <span style={{ color: colors.success, fontWeight: 'bold' }}>
                  🛡️ customer_payments table is PROTECTED
                </span>
              ) : (
                <span style={{ color: colors.error, fontWeight: 'bold' }}>
                  ⚠️ customer_payments table is UNPROTECTED
                </span>
              )}
            </Typography>
          )}

          {!hasPgWarden() && (
            <Typography variant="body2" gutterBottom>
              <span style={{ color: colors.textSecondary, fontWeight: 'bold' }}>
                ⚡ Vanilla PostgreSQL - No pg_warden protection
              </span>
            </Typography>
          )}

          <Typography variant="body2" gutterBottom sx={{ mt: 2 }}>
            <span style={{ color: colors.textSecondary }}>
              Target endpoint: 
            </span>
            <span style={{ 
              fontFamily: 'monospace', 
              fontSize: '0.875rem',
              color: colors.accent,
              backgroundColor: isDarkMode ? colors.surfaceHover : '#e3f2fd',
              padding: '2px 8px',
              borderRadius: '4px',
              marginLeft: '8px',
              display: 'inline-block'
            }}>
              DELETE /api/test/payments/by-id/{'{id}'}
            </span>
          </Typography>

          <div className="form-fields">
            <TextField
              label="SQL Injection Input"
              size="small"
              inputRef={injectionInputRef}
              multiline
              rows={3}
              fullWidth
              placeholder="Enter SQL injection attempt..."
              InputLabelProps={{ shrink: true }}
              sx={{
                '& .MuiInputLabel-root': { color: colors.textSecondary },
                '& .MuiOutlinedInput-root': { 
                  color: colors.text,
                  '& fieldset': { borderColor: colors.border },
                  '&:hover fieldset': { borderColor: colors.accent },
                  '&.Mui-focused fieldset': { borderColor: colors.accent },
                },
              }}
            />
          </div>

          <Typography variant="body2" sx={{ mt: 2, mb: 1 }}>
            Example injections:
          </Typography>

          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
            {injectionExamples.map((example, index) => (
              <Box
                key={index}
                onClick={() => handleExampleClick(example)}
                sx={{
                  p: 1.5,
                  borderRadius: 1,
                  border: `1px solid ${colors.border}`,
                  cursor: 'pointer',
                  transition: 'all 0.2s',
                  backgroundColor: colors.card,
                  '&:hover': {
                    backgroundColor: colors.surfaceHover,
                    borderColor: colors.accent,
                  }
                }}
              >
                <Typography 
                  variant="body2" 
                  sx={{ 
                    fontWeight: 600, 
                    color: colors.accent,
                    mb: 0.5 
                  }}
                >
                  {example.label}
                </Typography>
                <Typography 
                  variant="caption" 
                  sx={{ 
                    fontFamily: 'monospace',
                    color: colors.textSecondary,
                    display: 'block',
                    wordBreak: 'break-all'
                  }}
                >
                  {example.value}
                </Typography>
              </Box>
            ))}
          </Box>

          <div className="button-group">
            <Button 
              variant="contained" 
              color="primary" 
              onClick={handleExecute}
              disabled={loading}
              fullWidth
            >
              Execute Query
            </Button>
          </div>
        </div>

        <div className="main-area">
          <Typography variant="h5" gutterBottom style={{ color: colors.text }}>
            SQL Injection Test Results
          </Typography>

          <Paper sx={{ p: 3, mt: 2 }}>
            <Typography variant="h6" gutterBottom>
              Query: DELETE /api/test/payments/by-id/{injectionInputRef.current?.value || '{id}'}
            </Typography>

            {injectionInputRef.current?.value && isJDBC() && (
              <Box sx={{ mb: 2 }}>
                <Typography variant="body2" color="textSecondary">
                  SQL that will be executed:
                </Typography>
                <pre style={{ 
                  background: colors.card, 
                  padding: '0.5rem', 
                  borderRadius: '4px',
                  fontSize: '0.875rem',
                  border: `1px solid ${colors.border}`,
                  color: colors.text
                }}>
                  DELETE FROM customer_payments WHERE id = {injectionInputRef.current.value}
                </pre>
              </Box>
            )}

            {loading && (
              <Typography color="textSecondary">Executing query...</Typography>
            )}

            {result && (
              <Box>
                {result.success ? (
                  <>
                    <Typography variant="h6" color="error.main" gutterBottom>
                      ⚠️ SQL INJECTION SUCCESSFUL!
                    </Typography>
                    <Typography color="error" paragraph>
                      {result.rowsAffected} payment record(s) were deleted!
                    </Typography>
                    <Typography variant="body2" color="textSecondary">
                      Executed SQL:
                    </Typography>
                    <pre style={{ 
                      background: colors.card, 
                      padding: '1rem', 
                      borderRadius: '4px',
                      overflow: 'auto',
                      border: `1px solid ${colors.error}`,
                      color: colors.text
                    }}>
                      {result.executedQuery}
                    </pre>
                  </>
                ) : (
                  <>
                    <Typography variant="h6" color="success.main" gutterBottom>
                      ✓ Injection Blocked {!isJDBC() ? 'by JPA' : 'by pg_warden'}
                    </Typography>
                    <Typography color="textSecondary" paragraph>
                      {!isJDBC() ? 'JPA parameterized queries prevented SQL injection' : `Error: ${result.error}`}
                    </Typography>
                    <Typography variant="body2" color="textSecondary">
                      Attempted SQL:
                    </Typography>
                    <pre style={{ 
                      background: colors.card, 
                      padding: '1rem', 
                      borderRadius: '4px',
                      overflow: 'auto',
                      border: `1px solid ${colors.success}`,
                      color: colors.text
                    }}>
                      {result.attemptedQuery}
                    </pre>
                  </>
                )}
              </Box>
            )}

            {error && (
              <Box>
                <Typography variant="h6" color="error.main" gutterBottom>
                  Failed - Injection Blocked:
                </Typography>
                <Typography color="error">
                  {error}
                </Typography>
              </Box>
            )}

            {!loading && !result && !error && (
              <Typography color="textSecondary">
                Enter an SQL injection attempt and click Execute Query to test.
              </Typography>
            )}
          </Paper>

          <Paper sx={{ p: 3, mt: 3 }}>
            <Typography variant="h6" gutterBottom>
              How it works:
            </Typography>
            <Typography variant="body2" paragraph>
              This demo targets the <strong>customer_payments</strong> table, which contains sensitive payment information.
            </Typography>
            <Typography variant="body2" paragraph>
              <strong>JPA backends:</strong> Do not have the vulnerable test endpoints (404 error).
            </Typography>
            <Typography variant="body2" paragraph>
              <strong>JDBC backends:</strong> Have vulnerable test endpoints that concatenate strings directly into SQL queries. 
              Example: <code>DELETE FROM customer_payments WHERE id = [injection_value]</code>
            </Typography>
            <Typography variant="body2" paragraph>
              <strong>Why payments?</strong> Payment data is highly sensitive and protected by pg_warden by default.
            </Typography>
            <Typography variant="body2" paragraph>
              <strong>Working injections:</strong>
            </Typography>
            <ul style={{ marginLeft: '20px', fontSize: '0.875rem' }}>
              <li><code>1 OR 1=1</code> - Deletes ALL payment records</li>
              <li><code>1; DROP TABLE customer_payments; --</code> - Attempts to drop the entire payments table</li>
            </ul>
            <Typography variant="body2">
              Check the Logs page after testing to see blocked operations and pg_warden's protection in action.
            </Typography>
          </Paper>
        </div>
      </div>
    </div>
  );
};

export default Injection;