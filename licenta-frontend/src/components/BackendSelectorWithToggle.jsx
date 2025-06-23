import { useState, useEffect } from 'react';
import { FormControl, InputLabel, Select, MenuItem, ToggleButton, ToggleButtonGroup, Box, Typography } from '@mui/material';
import { AdminPanelSettings, Person } from '@mui/icons-material';
import { useTheme } from '../context/ThemeContext';

const BackendSelectorWithToggle = ({ value, onChange }) => {
  const { colors } = useTheme();
  
  // Determine initial user type based on current port
  const getInitialUserType = () => {
    return [8081, 8083, 8085, 8086].includes(value) ? 'admin' : 'user';
  };

  const [userType, setUserType] = useState(getInitialUserType());
  
  // Backend configurations
  const backendConfigs = {
    'JPA + pg_warden': { admin: 8081, user: 8082 },
    'JDBC + pg_warden': { admin: 8083, user: 8084 },
    'JPA + Vanilla': { admin: 8085, user: 8085 }, // No user variant for vanilla
    'JDBC + Vanilla': { admin: 8086, user: 8086 }, // No user variant for vanilla
  };

  // Determine current backend type from port
  const getCurrentBackendType = () => {
    if (value === 8081 || value === 8082) return 'JPA + pg_warden';
    if (value === 8083 || value === 8084) return 'JDBC + pg_warden';
    if (value === 8085) return 'JPA + Vanilla';
    if (value === 8086) return 'JDBC + Vanilla';
    return 'JPA + pg_warden';
  };

  const [backendType, setBackendType] = useState(getCurrentBackendType());

  const handleBackendChange = (newBackendType) => {
    setBackendType(newBackendType);
    const newPort = backendConfigs[newBackendType][userType];
    onChange(newPort);
  };

  const handleUserTypeChange = (event, newUserType) => {
    if (newUserType !== null) {
      setUserType(newUserType);
      const newPort = backendConfigs[backendType][newUserType];
      onChange(newPort);
    }
  };

  // Update user type if port changes externally
  useEffect(() => {
    setUserType(getInitialUserType());
    setBackendType(getCurrentBackendType());
  }, [value]);

  // Check if current backend supports user/admin distinction
  const supportsUserAdmin = backendType.includes('pg_warden');

  return (
    <Box>
      <FormControl fullWidth size="small" sx={{ 
        mb: 2,
        '& .MuiInputLabel-root': { color: colors.textSecondary },
        '& .MuiOutlinedInput-root': { 
          color: colors.text,
          '& fieldset': { borderColor: colors.border },
          '&:hover fieldset': { borderColor: colors.accent },
          '&.Mui-focused fieldset': { borderColor: colors.accent },
        },
        '& .MuiSvgIcon-root': { color: colors.textSecondary }
      }}>
        <InputLabel id="backend-selector-label">Backend</InputLabel>
        <Select
          labelId="backend-selector-label"
          id="backend-selector"
          value={backendType}
          label="Backend"
          onChange={(e) => handleBackendChange(e.target.value)}
        >
          <MenuItem value="JPA + pg_warden">JPA + pg_warden</MenuItem>
          <MenuItem value="JDBC + pg_warden">JDBC + pg_warden</MenuItem>
          <MenuItem value="JPA + Vanilla">JPA + Vanilla PostgreSQL</MenuItem>
          <MenuItem value="JDBC + Vanilla">JDBC + Vanilla PostgreSQL</MenuItem>
        </Select>
      </FormControl>

      {supportsUserAdmin && (
        <Box sx={{ mb: 2 }}>
          <Typography variant="caption" sx={{ mb: 1, display: 'block', color: colors.textSecondary }}>
            User Role
          </Typography>
          <ToggleButtonGroup
            value={userType}
            exclusive
            onChange={handleUserTypeChange}
            size="small"
            fullWidth
            sx={{
              '& .MuiToggleButton-root': {
                textTransform: 'none',
                gap: 1,
                color: colors.text,
                borderColor: colors.border,
                '&.Mui-selected': {
                  backgroundColor: colors.accent,
                  color: '#fff',
                  '&:hover': {
                    backgroundColor: colors.accentHover,
                  }
                },
                '&:hover': {
                  backgroundColor: colors.surfaceHover,
                }
              }
            }}
          >
            <ToggleButton value="admin">
              <AdminPanelSettings fontSize="small" />
              Admin
            </ToggleButton>
            <ToggleButton value="user">
              <Person fontSize="small" />
              User
            </ToggleButton>
          </ToggleButtonGroup>
        </Box>
      )}

      <Typography variant="caption" sx={{ display: 'block', mt: 1, color: colors.textSecondary }}>
        Port: :{value}
      </Typography>
    </Box>
  );
};

export default BackendSelectorWithToggle;