import { useState } from 'react';
import { Typography, Button, Divider, TextField, FormControlLabel, Checkbox, Paper, Table, TableBody, TableCell, TableContainer, TableHead, TableRow } from '@mui/material';
import { BarChart } from '@mui/x-charts/BarChart';
import { toast } from 'react-toastify';
import BackendSelectorWithToggle from '../components/BackendSelectorWithToggle';
import API from '../api';
import { useTheme } from '../context/ThemeContext';

const Benchmark = ({ selectedBackend, setSelectedBackend }) => {
  const { colors } = useTheme();
  const [loading, setLoading] = useState(false);
  const [results, setResults] = useState(null);
  const [iterations, setIterations] = useState(1000);
  const [selectedOperations, setSelectedOperations] = useState({
    SELECT: true,
    INSERT: true,
    UPDATE: true,
    DELETE: true
  });
  const [compareWithVanilla, setCompareWithVanilla] = useState(true);
  const [vanillaResults, setVanillaResults] = useState(null);

  // Helper function to format time values with appropriate precision
  const formatTime = (timeMs) => {
    if (timeMs === undefined || timeMs === null) return '-';
    
    // If less than 0.01ms, show in microseconds
    if (timeMs < 0.01) {
      return `${(timeMs * 1000).toFixed(2)}μs`;
    }
    // If less than 1ms, show 4 decimal places
    else if (timeMs < 1) {
      return `${timeMs.toFixed(4)}ms`;
    }
    // Otherwise show 2 decimal places
    else {
      return `${timeMs.toFixed(2)}ms`;
    }
  };

  const handleOperationToggle = (operation) => {
    setSelectedOperations(prev => ({
      ...prev,
      [operation]: !prev[operation]
    }));
  };

  const runBenchmark = async (backend) => {
    const operations = Object.keys(selectedOperations).filter(op => selectedOperations[op]);
    
    if (operations.length === 0) {
      toast.error('Please select at least one operation');
      return null;
    }

    const request = {
      iterations: parseInt(iterations),
      operations: operations
    };

    try {
      // Save current backend
      const currentBackend = API.baseUrl;
      
      // Set backend for benchmark
      API.setBackend(backend);
      
      // Run benchmark
      const result = await API.benchmark.run(request);
      
      // Restore original backend
      API.baseUrl = currentBackend;
      
      return result;
    } catch (error) {
      toast.error(`Benchmark failed: ${error.message}`);
      return null;
    }
  };

  const handleRunBenchmark = async () => {
    setLoading(true);
    setResults(null);
    setVanillaResults(null);

    try {
      // Run benchmark on current backend
      const result = await runBenchmark(selectedBackend);
      if (result) {
        console.log('Benchmark result:', result);
        console.log('Operation results:', result.operationResults);
        console.log('First operation:', Object.keys(result.operationResults)[0], result.operationResults[Object.keys(result.operationResults)[0]]);
        setResults(result);
        toast.success('Benchmark completed successfully');
      }

      // If compare with vanilla is selected
      if (compareWithVanilla && selectedBackend !== 8085 && selectedBackend !== 8086) {
        // Determine vanilla equivalent
        let vanillaPort;
        if (selectedBackend === 8081 || selectedBackend === 8082) {
          vanillaPort = 8085; // JPA vanilla
        } else {
          vanillaPort = 8086; // JDBC vanilla
        }

        const vanillaResult = await runBenchmark(vanillaPort);
        if (vanillaResult) {
          setVanillaResults(vanillaResult);
        }
      }
    } finally {
      setLoading(false);
    }
  };

  const prepareChartData = () => {
    if (!results) return { xAxis: [], series: [] };

    // Define the order of operations
    const operationOrder = ['SELECT', 'INSERT', 'UPDATE', 'DELETE'];
    const operations = operationOrder.filter(op => results.operationResults[op]);
    
    const pgWardenData = operations.map(op => results.operationResults[op].avgTimeMs);
    
    const series = [{
      data: pgWardenData,
      label: 'pg_warden',
      color: colors.accent
    }];

    if (vanillaResults) {
      const vanillaData = operations.map(op => vanillaResults.operationResults[op].avgTimeMs);
      series.push({
        data: vanillaData,
        label: 'Vanilla PostgreSQL',
        color: colors.success
      });
    }

    return {
      xAxis: [{ scaleType: 'band', data: operations }],
      series
    };
  };

  const getBackendName = () => {
    const names = {
      8081: 'JPA + Admin + pg_warden',
      8082: 'JPA + User + pg_warden',
      8083: 'JDBC + Admin + pg_warden',
      8084: 'JDBC + User + pg_warden',
      8085: 'JPA + Admin + Vanilla',
      8086: 'JDBC + Admin + Vanilla'
    };
    return names[selectedBackend] || 'Unknown';
  };

  return (
    <div className="page-container">
      <div className="page-content">
        <div className="sidebar">
          <BackendSelectorWithToggle value={selectedBackend} onChange={setSelectedBackend} />
          
          <Typography variant="h5" gutterBottom style={{ color: colors.text }}>
            Performance Benchmark
          </Typography>

          <Typography variant="body2" gutterBottom style={{ color: colors.textSecondary }}>
            Current: {getBackendName()}
          </Typography>
          
          <Typography variant="body2" gutterBottom style={{ color: colors.textSecondary }}>
            Target table: products
          </Typography>

          <Divider sx={{ my: 2, borderColor: colors.border }} />

          <div className="form-fields">
            <TextField
              label="Iterations"
              size="small"
              type="number"
              value={iterations}
              onChange={(e) => setIterations(e.target.value)}
              InputLabelProps={{ shrink: true }}
              inputProps={{ min: 1, max: 10000 }}
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

            <Typography variant="body2" sx={{ mt: 2, color: colors.text }}>
              Operations to test:
            </Typography>

            {Object.keys(selectedOperations).map(op => (
              <FormControlLabel
                key={op}
                control={
                  <Checkbox
                    checked={selectedOperations[op]}
                    onChange={() => handleOperationToggle(op)}
                    size="small"
                  />
                }
                label={op}
              />
            ))}

            {selectedBackend !== 8085 && selectedBackend !== 8086 && (
              <FormControlLabel
                control={
                  <Checkbox
                    checked={compareWithVanilla}
                    onChange={(e) => setCompareWithVanilla(e.target.checked)}
                    size="small"
                  />
                }
                label="Compare with Vanilla PostgreSQL"
              />
            )}
          </div>

          <Button 
            variant="contained" 
            color="primary" 
            onClick={handleRunBenchmark}
            disabled={loading}
            fullWidth
            sx={{ mt: 2 }}
          >
            {loading ? 'Running Benchmark...' : 'Run Benchmark'}
          </Button>
        </div>

        <div className="main-area">
          <Typography variant="h5" gutterBottom style={{ color: colors.text }}>
            Benchmark Results
          </Typography>

          {results && (
            <>
              <Paper sx={{ p: 1.5, mt: 1, backgroundColor: colors.card, color: colors.text }}>
                <Typography variant="body2" style={{ color: colors.text }}>
                  <strong>Benchmark Configuration:</strong> {results.totalIterations} iterations per operation on the "products" table
                </Typography>
                <Typography variant="body2" style={{ color: colors.text }}>
                  <strong>Operations tested:</strong> {Object.keys(results.operationResults).join(', ')}
                </Typography>
              </Paper>

              <Paper sx={{ p: 2, mt: 1.5, backgroundColor: colors.card }}>
                <Typography variant="h6" gutterBottom style={{ color: colors.text }}>
                  Average Execution Time (ms)
                </Typography>
                
                <BarChart
                  {...prepareChartData()}
                  height={400}
                  margin={{ left: 50 }}
                  sx={{
                    '& .MuiChartsAxis-line': { stroke: colors.border },
                    '& .MuiChartsAxis-tick': { stroke: colors.border },
                    '& .MuiChartsAxis-tickLabel': { fill: colors.textSecondary },
                    '& .MuiChartsLegend-label': { fill: colors.text },
                  }}
                />
              </Paper>

              {results.operationResults && Object.keys(results.operationResults).length > 0 && 
               results.operationResults[Object.keys(results.operationResults)[0]].p50TimeMs !== undefined && (
                <Paper sx={{ p: 2, mt: 1.5, backgroundColor: colors.card }}>
                  <Typography variant="h6" gutterBottom style={{ color: colors.text }}>
                    Response Time Percentiles (ms)
                  </Typography>
                  
                  <BarChart
                    xAxis={[{ 
                      scaleType: 'band', 
                      data: ['SELECT', 'INSERT', 'UPDATE', 'DELETE'].filter(op => results.operationResults[op])
                    }]}
                    series={[
                      { 
                        data: ['SELECT', 'INSERT', 'UPDATE', 'DELETE']
                          .filter(op => results.operationResults[op])
                          .map(op => results.operationResults[op].p50TimeMs), 
                        label: 'p50 (Median)',
                        color: colors.success
                      },
                      { 
                        data: ['SELECT', 'INSERT', 'UPDATE', 'DELETE']
                          .filter(op => results.operationResults[op])
                          .map(op => results.operationResults[op].p95TimeMs), 
                        label: 'p95',
                        color: colors.warning
                      },
                      { 
                        data: ['SELECT', 'INSERT', 'UPDATE', 'DELETE']
                          .filter(op => results.operationResults[op])
                          .map(op => results.operationResults[op].p99TimeMs), 
                        label: 'p99',
                        color: colors.error
                      }
                    ]}
                    height={300}
                    margin={{ left: 50 }}
                    sx={{
                      '& .MuiChartsAxis-line': { stroke: colors.border },
                      '& .MuiChartsAxis-tick': { stroke: colors.border },
                      '& .MuiChartsAxis-tickLabel': { fill: colors.textSecondary },
                      '& .MuiChartsLegend-label': { fill: colors.text },
                    }}
                  />
                </Paper>
              )}

              <Paper sx={{ p: 2, mt: 1.5, backgroundColor: colors.card }}>
                <Typography variant="h6" gutterBottom style={{ color: colors.text }}>
                  Detailed Results
                </Typography>
                
                <TableContainer>
                  <Table size="small" sx={{
                    '& .MuiTableCell-root': {
                      color: colors.text,
                      borderBottom: `1px solid ${colors.border}`,
                    },
                    '& .MuiTableCell-head': {
                      backgroundColor: colors.surface,
                      fontWeight: 600,
                    },
                  }}>
                    <TableHead>
                      <TableRow>
                        <TableCell><strong>Operation{vanillaResults ? ' (pg_warden)' : ''}</strong></TableCell>
                        <TableCell align="right"><strong>Total Time (ms)</strong></TableCell>
                        <TableCell align="right"><strong>Avg Time (ms)</strong></TableCell>
                        <TableCell align="right"><strong>Ops/Second</strong></TableCell>
                        <TableCell align="right"><strong>P50 (ms)</strong></TableCell>
                        <TableCell align="right"><strong>P95 (ms)</strong></TableCell>
                        <TableCell align="right"><strong>P99 (ms)</strong></TableCell>
                        <TableCell align="right"><strong>Success</strong></TableCell>
                        <TableCell align="right"><strong>Blocked</strong></TableCell>
                        <TableCell align="right"><strong>Errors</strong></TableCell>
                        {vanillaResults && (
                          <TableCell align="right"><strong>Overhead vs Vanilla</strong></TableCell>
                        )}
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {['SELECT', 'INSERT', 'UPDATE', 'DELETE']
                        .filter(op => results.operationResults[op])
                        .map((op) => {
                          const data = results.operationResults[op];
                          let overhead = null;
                          if (vanillaResults && vanillaResults.operationResults[op]) {
                            const vanillaTime = vanillaResults.operationResults[op].avgTimeMs;
                            const pgWardenTime = data.avgTimeMs;
                            overhead = ((pgWardenTime - vanillaTime) / vanillaTime * 100).toFixed(1);
                          }
                          return (
                            <TableRow key={op}>
                              <TableCell component="th" scope="row">
                                <strong>{op}</strong>
                              </TableCell>
                              <TableCell align="right">{formatTime(data.totalTimeMs)}</TableCell>
                              <TableCell align="right">{formatTime(data.avgTimeMs)}</TableCell>
                              <TableCell align="right">{data.opsPerSecond ? data.opsPerSecond.toFixed(0) : '-'}</TableCell>
                              <TableCell align="right">{formatTime(data.p50TimeMs)}</TableCell>
                              <TableCell align="right">{formatTime(data.p95TimeMs)}</TableCell>
                              <TableCell align="right">{formatTime(data.p99TimeMs)}</TableCell>
                              <TableCell align="right" sx={{ color: 'success.main' }}>
                                {data.successCount || 0}
                              </TableCell>
                              <TableCell align="right" sx={{ color: data.blockedCount > 0 ? 'warning.main' : 'inherit' }}>
                                {data.blockedCount || 0}
                              </TableCell>
                              <TableCell align="right" sx={{ color: data.errorCount > 0 ? 'error.main' : 'inherit' }}>
                                {data.errorCount || 0}
                              </TableCell>
                              {vanillaResults && (
                                <TableCell align="right" sx={{ 
                                  color: overhead && parseFloat(overhead) > 0 ? 'warning.main' : 'success.main',
                                  fontWeight: 'medium'
                                }}>
                                  {overhead !== null ? `${overhead > 0 ? '+' : ''}${overhead}%` : '-'}
                                </TableCell>
                              )}
                            </TableRow>
                          );
                        })}
                    </TableBody>
                  </Table>
                </TableContainer>

                <Divider sx={{ my: 1, borderColor: colors.border }} />
                
                <Typography variant="body2" style={{ color: colors.textSecondary }}>
                  Total benchmark time: {formatTime(results.totalTimeMs)}
                </Typography>
              </Paper>

            </>
          )}

          {!results && !loading && (
            <Paper sx={{ p: 2, mt: 1.5, backgroundColor: colors.card }}>
              <Typography style={{ color: colors.textSecondary }}>
                Configure benchmark settings and click "Run Benchmark" to start performance testing.
              </Typography>
            </Paper>
          )}
        </div>
      </div>
    </div>
  );
};

export default Benchmark;