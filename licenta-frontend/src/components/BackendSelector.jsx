import { FormControl, InputLabel, Select, MenuItem } from '@mui/material';

const BackendSelector = ({ value, onChange }) => {
  const backends = [
    { port: 8081, label: 'JPA + Admin + pg_warden' },
    { port: 8082, label: 'JPA + User + pg_warden' },
    { port: 8083, label: 'JDBC + Admin + pg_warden' },
    { port: 8084, label: 'JDBC + User + pg_warden' },
    { port: 8085, label: 'JPA + Admin + Vanilla PostgreSQL' },
    { port: 8086, label: 'JDBC + Admin + Vanilla PostgreSQL' },
  ];

  return (
    <FormControl fullWidth size="small" sx={{ mb: 2 }}>
      <InputLabel id="backend-selector-label">Backend</InputLabel>
      <Select
        labelId="backend-selector-label"
        id="backend-selector"
        value={value}
        label="Backend"
        onChange={(e) => onChange(e.target.value)}
      >
        {backends.map((backend) => (
          <MenuItem key={backend.port} value={backend.port}>
            {backend.label} (:{backend.port})
          </MenuItem>
        ))}
      </Select>
    </FormControl>
  );
};

export default BackendSelector;