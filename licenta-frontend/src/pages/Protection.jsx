import { useState, useEffect, useRef } from 'react';
import { Typography, TextField, Button, Divider } from '@mui/material';
import { toast } from 'react-toastify';
import Ribbon from '../components/Ribbon';
import BackendSelectorWithToggle from '../components/BackendSelectorWithToggle';
import CustomTable from '../components/CustomTable';
import API from '../api';

const Protection = ({ selectedBackend, setSelectedBackend }) => {
  const [protectionSummary, setProtectionSummary] = useState({ protected: [], unprotected: [] });
  const [selectedTable, setSelectedTable] = useState('');
  const [selectedStatus, setSelectedStatus] = useState(null);
  const [loading, setLoading] = useState(false);

  // Form refs
  const tableNameRef = useRef(null);

  // Fetch protection summary on mount and backend change
  useEffect(() => {
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
      } else {
        toast.info(`Table '${selectedTable}' was already protected`);
      }
      fetchProtectionSummary();
      // Refresh logs
      setTimeout(() => API.logs.getRecent(), 500);
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
      } else {
        toast.info(`Table '${selectedTable}' was already unprotected`);
      }
      fetchProtectionSummary();
      // Refresh logs
      setTimeout(() => API.logs.getRecent(), 500);
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
      width: 150,
      renderCell: (params) => (
        <span style={{ 
          color: params.value ? '#2e7d32' : '#c62828',
          fontWeight: 'bold'
        }}>
          {params.value ? 'Protected' : 'Unprotected'}
        </span>
      )
    },
  ];

  return (
    <div className="page-container">
      <Ribbon />
      <div className="page-content">
        <div className="sidebar">
          <BackendSelectorWithToggle value={selectedBackend} onChange={setSelectedBackend} />
          
          <Typography variant="h5" gutterBottom>
            Protection Management
          </Typography>

          <Divider sx={{ my: 2 }} />

          <div className="form-fields">
            <TextField
              label="Selected Table"
              size="small"
              inputRef={tableNameRef}
              disabled
              InputLabelProps={{ shrink: true }}
            />
            
            {selectedStatus !== null && (
              <div className={`protection-badge ${selectedStatus ? 'protected' : 'unprotected'}`}>
                {selectedStatus ? 'PROTECTED' : 'UNPROTECTED'}
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

          <Divider sx={{ my: 3 }} />

          <Typography variant="body2" color="textSecondary" gutterBottom>
            <strong>Summary:</strong>
          </Typography>
          <Typography variant="body2" color="textSecondary">
            Protected tables: {protectionSummary.protected.length}
          </Typography>
          <Typography variant="body2" color="textSecondary">
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
            height="100%"
          />
        </div>
      </div>
    </div>
  );
};

export default Protection;