import { useState, useEffect, useRef } from 'react';
import { 
  Paper, 
  Typography, 
  Table, 
  TableBody, 
  TableCell, 
  TableContainer, 
  TableHead, 
  TableRow,
  Chip,
  IconButton,
  Collapse,
  Box,
  Button,
  CircularProgress,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Switch,
  FormControlLabel
} from '@mui/material';
import { 
  ExpandMore as ExpandMoreIcon, 
  ExpandLess as ExpandLessIcon,
  Refresh as RefreshIcon,
  FilterList as FilterListIcon
} from '@mui/icons-material';
import API from '../api';

const LogsPanel = () => {
  const [logs, setLogs] = useState([]);
  const [loading, setLoading] = useState(false);
  const [expanded, setExpanded] = useState(true);
  const [lastRefresh, setLastRefresh] = useState(null);
  const [panelHeight, setPanelHeight] = useState(395);
  const [isResizing, setIsResizing] = useState(false);
  const [startY, setStartY] = useState(0);
  const [startHeight, setStartHeight] = useState(0);
  const [filters, setFilters] = useState({
    operationType: 'all',
    status: 'all',
    tableName: 'all'
  });
  const [showFilters, setShowFilters] = useState(true);
  const [autoRefresh, setAutoRefresh] = useState(true);
  const tableContainerRef = useRef(null);
  const previousLogCount = useRef(0);

  const fetchLogs = async (isManual = false) => {
    if (isManual) {
      setLoading(true);
    }
    try {
      let data;
      
      // Apply filters
      if (filters.status === 'blocked') {
        data = await API.logs.getBlocked();
      } else if (filters.status === 'allowed') {
        data = await API.logs.getAllowed();
      } else if (filters.tableName !== 'all') {
        data = await API.logs.getByTable(filters.tableName);
      } else {
        data = await API.logs.getRecent(100);
      }

      // Filter by operation type if needed
      if (filters.operationType !== 'all') {
        data = data.filter(log => log.operationType === filters.operationType);
      }

      // Filter by table name if needed (for cases where we got all logs)
      if (filters.tableName !== 'all' && filters.status === 'all') {
        data = data.filter(log => log.tableName === filters.tableName);
      }

      setLogs(data);
      setLastRefresh(new Date());
    } catch (error) {
      console.error('Failed to fetch logs:', error);
    } finally {
      if (isManual) {
        setLoading(false);
      }
    }
  };

  useEffect(() => {
    fetchLogs();
  }, [filters]);

  // Auto-scroll to bottom when new logs are added
  useEffect(() => {
    if (logs.length > previousLogCount.current && tableContainerRef.current && expanded) {
      // Scroll to bottom with smooth animation
      tableContainerRef.current.scrollTop = tableContainerRef.current.scrollHeight;
    }
    previousLogCount.current = logs.length;
  }, [logs, expanded]);

  useEffect(() => {
    if (autoRefresh && !loading) {
      const timer = setTimeout(fetchLogs, 1000);
      return () => clearTimeout(timer);
    }
  }, [autoRefresh, lastRefresh, loading]);

  // Handle resize
  useEffect(() => {
    const handleMouseMove = (e) => {
      if (!isResizing) return;
      
      // Calculate the delta from start position
      const deltaY = startY - e.clientY;
      const newHeight = startHeight + deltaY;
      
      // Allow heights between 200 and 90% of window height (leaving space for header)
      const maxHeight = window.innerHeight * 0.9;
      setPanelHeight(Math.min(Math.max(200, newHeight), maxHeight));
    };

    const handleMouseUp = () => {
      setIsResizing(false);
      document.body.style.cursor = '';
    };

    if (isResizing) {
      document.body.style.cursor = 'ns-resize';
      document.addEventListener('mousemove', handleMouseMove);
      document.addEventListener('mouseup', handleMouseUp);
    }

    return () => {
      document.removeEventListener('mousemove', handleMouseMove);
      document.removeEventListener('mouseup', handleMouseUp);
      document.body.style.cursor = '';
    };
  }, [isResizing, startY, startHeight]);

  const getStatusColor = (status) => {
    return status === 'SUCCESS' || status === 'ALLOWED' ? 'success' : 'error';
  };

  const getOperationColor = (operation) => {
    const colors = {
      'SELECT': 'info',
      'INSERT': 'success',
      'UPDATE': 'warning',
      'DELETE': 'error',
      'ALTER': 'error',
      'DROP': 'error',
      'PROTECT': 'primary',
      'UNPROTECT': 'primary'
    };
    return colors[operation] || 'default';
  };

  const formatTimestamp = (timestamp) => {
    const date = new Date(timestamp);
    return date.toLocaleTimeString();
  };

  // Get unique table names from logs
  const tableNames = [...new Set(logs.map(log => log.tableName))].filter(Boolean).sort();

  return (
    <Paper 
      sx={{ 
        position: 'fixed',
        bottom: 0,
        left: 0,
        right: 0,
        zIndex: 1200,
        borderTop: 2,
        borderColor: 'divider',
        maxHeight: expanded ? panelHeight + 50 : 50
      }}
      elevation={8}
    >
      {/* Resize handle */}
      {expanded && (
        <Box
          sx={{
            position: 'absolute',
            top: -2,
            left: 0,
            right: 0,
            height: '8px',
            cursor: 'ns-resize',
            backgroundColor: 'transparent',
            '&:hover': {
              backgroundColor: 'primary.main',
              opacity: 0.3
            },
            '&:before': {
              content: '""',
              position: 'absolute',
              top: '3px',
              left: '50%',
              transform: 'translateX(-50%)',
              width: '40px',
              height: '2px',
              backgroundColor: 'divider',
              borderRadius: '1px'
            }
          }}
          onMouseDown={(e) => {
            e.preventDefault();
            setIsResizing(true);
            setStartY(e.clientY);
            setStartHeight(panelHeight);
          }}
        />
      )}
      <Box 
        sx={{ 
          display: 'flex', 
          alignItems: 'center', 
          justifyContent: 'space-between',
          px: 2,
          py: 1,
          backgroundColor: 'background.paper',
          borderBottom: expanded ? 1 : 0,
          borderColor: 'divider',
          cursor: 'pointer'
        }}
        onClick={() => setExpanded(!expanded)}
      >
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
          <Typography variant="h6" sx={{ fontSize: '1rem' }}>
            Security Logs
          </Typography>
          {logs.length > 0 && (
            <Chip 
              label={`${logs.length} recent operations`} 
              size="small" 
              variant="outlined"
            />
          )}
        </Box>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
          <FormControlLabel
            control={
              <Switch
                size="small"
                checked={autoRefresh}
                onChange={(e) => setAutoRefresh(e.target.checked)}
                onClick={(e) => e.stopPropagation()}
              />
            }
            label="Auto-refresh"
            sx={{ mr: 1 }}
            onClick={(e) => e.stopPropagation()}
          />
          <IconButton
            size="small"
            onClick={(e) => {
              e.stopPropagation();
              fetchLogs(true);
            }}
            disabled={loading}
          >
            {loading ? <CircularProgress size={20} /> : <RefreshIcon />}
          </IconButton>
          <IconButton
            size="small"
            onClick={(e) => {
              e.stopPropagation();
              setShowFilters(!showFilters);
            }}
            title="Toggle filters"
          >
            <FilterListIcon color={showFilters ? "primary" : "inherit"} />
          </IconButton>
          <IconButton size="small">
            {expanded ? <ExpandMoreIcon /> : <ExpandLessIcon />}
          </IconButton>
        </Box>
      </Box>

      <Collapse in={expanded}>
        {/* Filters */}
        {showFilters && (
          <Box 
            sx={{ 
              px: 2, 
              py: 1, 
              borderBottom: 1, 
              borderColor: 'divider',
              backgroundColor: 'background.default'
            }}
          >
            <Box sx={{ display: 'flex', gap: 2 }}>
              <FormControl size="small" sx={{ minWidth: 150 }}>
                <InputLabel>Operation</InputLabel>
                <Select
                  value={filters.operationType}
                  label="Operation"
                  onChange={(e) => setFilters(prev => ({ ...prev, operationType: e.target.value }))}
                >
                  <MenuItem value="all">All Operations</MenuItem>
                  <MenuItem value="DELETE">DELETE</MenuItem>
                  <MenuItem value="UPDATE">UPDATE</MenuItem>
                  <MenuItem value="ALTER">ALTER</MenuItem>
                  <MenuItem value="DROP">DROP</MenuItem>
                  <MenuItem value="PROTECT">PROTECT</MenuItem>
                  <MenuItem value="UNPROTECT">UNPROTECT</MenuItem>
                </Select>
              </FormControl>
              
              <FormControl size="small" sx={{ minWidth: 150 }}>
                <InputLabel>Status</InputLabel>
                <Select
                  value={filters.status}
                  label="Status"
                  onChange={(e) => setFilters(prev => ({ ...prev, status: e.target.value }))}
                >
                  <MenuItem value="all">All Statuses</MenuItem>
                  <MenuItem value="blocked">Blocked</MenuItem>
                  <MenuItem value="allowed">Allowed</MenuItem>
                </Select>
              </FormControl>
              
              <FormControl size="small" sx={{ minWidth: 150 }}>
                <InputLabel>Table</InputLabel>
                <Select
                  value={filters.tableName}
                  label="Table"
                  onChange={(e) => setFilters(prev => ({ ...prev, tableName: e.target.value }))}
                >
                  <MenuItem value="all">All Tables</MenuItem>
                  {tableNames.map(table => (
                    <MenuItem key={table} value={table}>{table}</MenuItem>
                  ))}
                </Select>
              </FormControl>
              
              <Button
                size="small"
                onClick={() => setFilters({ operationType: 'all', status: 'all', tableName: 'all' })}
              >
                Clear Filters
              </Button>
            </Box>
          </Box>
        )}
        
        <TableContainer 
          ref={tableContainerRef}
          sx={{ height: panelHeight - (showFilters ? 100 : 50), overflow: 'auto' }}
        >
          <Table stickyHeader size="small" sx={{ tableLayout: 'fixed' }}>
            <TableHead>
              <TableRow>
                <TableCell sx={{ width: '10%' }}>Time</TableCell>
                <TableCell sx={{ width: '10%' }}>Operation</TableCell>
                <TableCell sx={{ width: '12%' }}>Table</TableCell>
                <TableCell sx={{ width: '8%' }}>Status</TableCell>
                <TableCell sx={{ width: '10%' }}>User</TableCell>
                <TableCell sx={{ width: '30%' }}>Query</TableCell>
                <TableCell sx={{ width: '20%' }}>Reason</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {logs.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={7} align="center">
                    <Typography variant="body2" color="textSecondary">
                      No recent operations
                    </Typography>
                  </TableCell>
                </TableRow>
              ) : (
                logs.map((log, index) => (
                  <TableRow key={index} hover>
                    <TableCell sx={{ whiteSpace: 'nowrap' }}>
                      {formatTimestamp(log.operationTime)}
                    </TableCell>
                    <TableCell>
                      <Chip 
                        label={log.operationType} 
                        size="small" 
                        color={getOperationColor(log.operationType)}
                        sx={{ fontSize: '0.75rem' }}
                      />
                    </TableCell>
                    <TableCell>{log.tableName}</TableCell>
                    <TableCell>
                      <Chip 
                        label={log.status} 
                        size="small" 
                        color={getStatusColor(log.status)}
                        variant={log.status === 'BLOCKED' ? 'filled' : 'outlined'}
                        sx={{ fontSize: '0.75rem' }}
                      />
                    </TableCell>
                    <TableCell>{log.userName}</TableCell>
                    <TableCell 
                      sx={{ 
                        overflow: 'hidden', 
                        textOverflow: 'ellipsis',
                        whiteSpace: 'nowrap',
                        fontSize: '0.875rem'
                      }}
                      title={log.queryText}
                    >
                      <Typography variant="caption" sx={{ fontFamily: 'monospace' }}>
                        {log.queryText || '-'}
                      </Typography>
                    </TableCell>
                    <TableCell 
                      sx={{ 
                        overflow: 'hidden', 
                        textOverflow: 'ellipsis',
                        whiteSpace: 'nowrap',
                        fontSize: '0.875rem'
                      }}
                      title={log.blockedReason}
                    >
                      {log.status === 'BLOCKED' ? (
                        <Typography variant="caption" color="error">
                          {log.blockedReason || '-'}
                        </Typography>
                      ) : (
                        <Typography variant="caption" color="textSecondary">
                          -
                        </Typography>
                      )}
                    </TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        </TableContainer>
      </Collapse>
    </Paper>
  );
};

export default LogsPanel;