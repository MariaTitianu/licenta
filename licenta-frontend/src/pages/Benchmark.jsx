import { useState } from 'react';
import { Typography, Button, Divider, TextField, FormControlLabel, Checkbox, Paper } from '@mui/material';
import { BarChart } from '@mui/x-charts/BarChart';
import { toast } from 'react-toastify';
import Ribbon from '../components/Ribbon';
import BackendSelectorWithToggle from '../components/BackendSelectorWithToggle';
import API from '../api';

const Benchmark = ({ selectedBackend, setSelectedBackend }) => {
  const [loading, setLoading] = useState(false);
  const [results, setResults] = useState(null);
  const [iterations, setIterations] = useState(100);
  const [selectedOperations, setSelectedOperations] = useState({
    SELECT: true,
    INSERT: true,
    UPDATE: true,
    DELETE: true
  });
  const [compareWithVanilla, setCompareWithVanilla] = useState(true);
  const [vanillaResults, setVanillaResults] = useState(null);

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

    const operations = Object.keys(results.operationResults);
    const pgWardenData = operations.map(op => results.operationResults[op].avgTimeMs);
    
    const series = [{
      data: pgWardenData,
      label: 'pg_warden',
      color: '#1976d2'
    }];

    if (vanillaResults) {
      const vanillaData = operations.map(op => vanillaResults.operationResults[op].avgTimeMs);
      series.push({
        data: vanillaData,
        label: 'Vanilla PostgreSQL',
        color: '#388e3c'
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
      <Ribbon />
      <div className="page-content">
        <div className="sidebar">
          <BackendSelectorWithToggle value={selectedBackend} onChange={setSelectedBackend} />
          
          <Typography variant="h5" gutterBottom>
            Performance Benchmark
          </Typography>

          <Typography variant="body2" color="textSecondary" gutterBottom>
            Current: {getBackendName()}
          </Typography>

          <Divider sx={{ my: 2 }} />

          <div className="form-fields">
            <TextField
              label="Iterations"
              size="small"
              type="number"
              value={iterations}
              onChange={(e) => setIterations(e.target.value)}
              InputLabelProps={{ shrink: true }}
              inputProps={{ min: 1, max: 1000 }}
            />

            <Typography variant="body2" sx={{ mt: 2 }}>
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
          <Typography variant="h5" gutterBottom>
            Benchmark Results
          </Typography>

          {results && (
            <>
              <Paper sx={{ p: 3, mt: 2 }}>
                <Typography variant="h6" gutterBottom>
                  Average Execution Time (ms)
                </Typography>
                
                <BarChart
                  {...prepareChartData()}
                  height={400}
                  margin={{ left: 50 }}
                />
              </Paper>

              <Paper sx={{ p: 3, mt: 3 }}>
                <Typography variant="h6" gutterBottom>
                  Detailed Results
                </Typography>
                
                {Object.entries(results.operationResults).map(([op, data]) => (
                  <div key={op} style={{ marginBottom: '20px' }}>
                    <Typography variant="subtitle1" fontWeight="bold">
                      {op}
                    </Typography>
                    <Typography variant="body2">
                      Total time: {data.totalTimeMs.toFixed(2)}ms
                    </Typography>
                    <Typography variant="body2">
                      Average time: {data.avgTimeMs.toFixed(2)}ms
                    </Typography>
                    <Typography variant="body2">
                      Success: {data.successCount} / Blocked: {data.blockedCount}
                    </Typography>
                  </div>
                ))}

                <Divider sx={{ my: 2 }} />
                
                <Typography variant="body2" color="textSecondary">
                  Total benchmark time: {results.totalTimeMs.toFixed(2)}ms
                </Typography>
              </Paper>

              {vanillaResults && (
                <Paper sx={{ p: 3, mt: 3 }}>
                  <Typography variant="h6" gutterBottom>
                    Performance Impact
                  </Typography>
                  
                  {Object.entries(results.operationResults).map(([op, data]) => {
                    const vanillaTime = vanillaResults.operationResults[op].avgTimeMs;
                    const pgWardenTime = data.avgTimeMs;
                    const overhead = ((pgWardenTime - vanillaTime) / vanillaTime * 100).toFixed(1);
                    
                    return (
                      <Typography key={op} variant="body2" sx={{ mb: 1 }}>
                        {op}: {overhead > 0 ? '+' : ''}{overhead}% overhead
                      </Typography>
                    );
                  })}
                </Paper>
              )}
            </>
          )}

          {!results && !loading && (
            <Paper sx={{ p: 3, mt: 2 }}>
              <Typography color="textSecondary">
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