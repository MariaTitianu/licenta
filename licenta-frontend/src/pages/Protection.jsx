import { useState, useEffect, useRef } from 'react';
import { Typography, TextField, Button, Divider } from '@mui/material';
import { toast } from 'react-toastify';
import BackendSelectorWithToggle from '../components/BackendSelectorWithToggle';
import CustomTable from '../components/CustomTable';
import API from '../api';
import { useTheme } from '../context/ThemeContext';
import { useProtection } from '../context/ProtectionContext';

const Protection = ({ selectedBackend, setSelectedBackend }) => {
  const { colors, isDarkMode } = useTheme();
  const { refreshProtectionStatuses } = useProtection();
  const [protectionSummary, setProtectionSummary] = useState({ protected: [], unprotected: [] });
  const [selectedTable, setSelectedTable] = useState('');
  const [selectedStatus, setSelectedStatus] = useState(null);
  const [loading, setLoading] = useState(false);

  // Form refs
  const tableNameRef = useRef(null);

  // Fetch protection summary on mount and backend change
  useEffect(() => {
    // Clear selection when backend changes
    setSelectedTable('');
    setSelectedStatus(null);
    if (tableNameRef.current) {
      tableNameRef.current.value = '';
    }
    fetchProtectionSummary();
  }, [selectedBackend]);

  const fetchProtectionSummary = async () => {
    try {
      const summary = await API.protection.getSummary();
      
      // Get the list of known tables from the application
      const knownTables = ['products', 'customer_payments'];
      
      // API returns unprotectedTables array
      const unprotectedTables = summary.unprotectedTables || [];
      
      // Determine which tables are protected (not in the unprotected list)
      const protectedTables = knownTables.filter(table => !unprotectedTables.includes(table));
      
      setProtectionSummary({
        protected: protectedTables,
        unprotected: unprotectedTables
      });
    } catch (error) {
      toast.error('Failed to fetch protection summary');
      console.error(error);
      // Set default empty arrays to prevent errors
      setProtectionSummary({ protected: [], unprotected: [] });
    }
  };

  const handleRowClick = (params) => {
    const { tableName, isProtected } = params.row;
    setSelectedTable(tableName);
    setSelectedStatus(isProtected);
    tableNameRef.current.value = tableName;
  };

  const handleProtect = async () => {
    if (!selectedTable) {
      toast.error('Please select a table');
      return;
    }

    setLoading(true);
    try {
      const result = await API.protection.protect(selectedTable);
      if (result.changed) {
        toast.success(`Table '${selectedTable}' is now protected`);
        setSelectedStatus(true); // Update the UI immediately
      } else {
        toast.info(`Table '${selectedTable}' was already protected`);
      }
      fetchProtectionSummary();
      // Refresh logs
      setTimeout(() => API.logs.getRecent(), 500);
      // Refresh sidebar protection status
      refreshProtectionStatuses();
    } catch (error) {
      toast.error(error.message || 'Failed to protect table');
    } finally {
      setLoading(false);
    }
  };

  const handleUnprotect = async () => {
    if (!selectedTable) {
      toast.error('Please select a table');
      return;
    }

    if (!window.confirm(`Are you sure you want to unprotect '${selectedTable}'? This will allow DELETE and UPDATE operations.`)) {
      return;
    }

    setLoading(true);
    try {
      const result = await API.protection.unprotect(selectedTable);
      if (result.changed) {
        toast.success(`Table '${selectedTable}' is now unprotected`);
        setSelectedStatus(false); // Update the UI immediately
      } else {
        toast.info(`Table '${selectedTable}' was already unprotected`);
      }
      fetchProtectionSummary();
      // Refresh logs
      setTimeout(() => API.logs.getRecent(), 500);
      // Refresh sidebar protection status
      refreshProtectionStatuses();
    } catch (error) {
      toast.error(error.message || 'Failed to unprotect table');
    } finally {
      setLoading(false);
    }
  };

  // Combine protected and unprotected tables for display
  const allTables = [
    ...(Array.isArray(protectionSummary.protected) ? protectionSummary.protected : []).map(table => ({ tableName: table, isProtected: true })),
    ...(Array.isArray(protectionSummary.unprotected) ? protectionSummary.unprotected : []).map(table => ({ tableName: table, isProtected: false }))
  ].sort((a, b) => a.tableName.localeCompare(b.tableName));

  const columns = [
    { field: 'tableName', headerName: 'Table Name', flex: 1 },
    { 
      field: 'isProtected', 
      headerName: 'Status', 
      flex: 1,
      renderCell: (params) => (
        <div style={{ 
          display: 'flex',
          justifyContent: 'flex-start',
          alignItems: 'center',
          width: '100%',
          height: '100%'
        }}>
          <div style={{ 
            display: 'inline-block',
            padding: '4px 12px',
            borderRadius: '16px',
            fontWeight: '600',
            fontSize: '0.8125rem',
            lineHeight: '1.5',
            color: isDarkMode ? (params.value ? colors.success : colors.error) : (params.value ? '#2e7d32' : '#c62828'),
            border: `1px solid ${isDarkMode ? (params.value ? colors.success : colors.error) : (params.value ? '#66bb6a' : '#ef5350')}`,
            backgroundColor: isDarkMode ? 'transparent' : (params.value ? '#e8f5e9' : '#ffebee')
          }}>
            {params.value ? 'PROTECTED' : 'UNPROTECTED'}
          </div>
        </div>
      )
    },
  ];

  return (
    <div className="page-container">
      <div className="page-content">
        <div className="sidebar">
          <BackendSelectorWithToggle value={selectedBackend} onChange={setSelectedBackend} />
          
          <Typography variant="h5" gutterBottom style={{ color: colors.text }}>
            Protection Management
          </Typography>

          <Divider sx={{ my: 2, borderColor: colors.border }} />

          <div className="form-fields">
            <TextField
              label="Selected Table"
              size="small"
              inputRef={tableNameRef}
              disabled
              InputLabelProps={{ shrink: true }}
              sx={{
                '& .MuiInputLabel-root': { color: colors.textSecondary },
                '& .MuiOutlinedInput-root': { 
                  color: colors.text,
                  '& fieldset': { borderColor: colors.border },
                  '&.Mui-disabled': {
                    '& fieldset': { borderColor: colors.border },
                  }
                },
                '& .Mui-disabled': { color: colors.textSecondary }
              }}
            />
            
            {selectedTable && (
              <div className={`protection-badge ${selectedStatus === null ? 'loading' : selectedStatus ? 'protected' : 'unprotected'}`}>
                {selectedStatus === null ? 'CHECKING...' : selectedStatus ? 'PROTECTED' : 'UNPROTECTED'}
              </div>
            )}
          </div>

          <div className="button-group">
            <Button 
              variant="contained" 
              color="success" 
              onClick={handleProtect}
              disabled={loading || !selectedTable || selectedStatus === true}
            >
              Protect
            </Button>
            <Button 
              variant="contained" 
              color="error" 
              onClick={handleUnprotect}
              disabled={loading || !selectedTable || selectedStatus === false}
            >
              Unprotect
            </Button>
          </div>

          <Divider sx={{ my: 3, borderColor: colors.border }} />

          <Typography variant="body2" gutterBottom style={{ color: colors.textSecondary }}>
            <strong>Summary:</strong>
          </Typography>
          <Typography variant="body2" style={{ color: colors.textSecondary }}>
            Protected tables: {protectionSummary.protected.length}
          </Typography>
          <Typography variant="body2" style={{ color: colors.textSecondary }}>
            Unprotected tables: {protectionSummary.unprotected.length}
          </Typography>
        </div>

        <div className="main-area">
          <CustomTable
            title="Database Tables"
            rows={allTables}
            columns={columns}
            onRowClick={handleRowClick}
            getRowId={(row) => row.tableName}
          />
        </div>
      </div>
    </div>
  );
};

export default Protection;