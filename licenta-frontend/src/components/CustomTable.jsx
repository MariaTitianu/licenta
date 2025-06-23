import { Box, Typography } from "@mui/material";
import { DataGrid } from "@mui/x-data-grid";
import { useTheme } from '../context/ThemeContext';

export default function CustomTable(props) {
  const { colors, isDarkMode } = useTheme();
  function DataGridTitle() {
    return (
      <Box
        style={{
          width: "100%",
          display: "flex",
          justifyContent: "flex-begin",
          alignItems: "center",
        }}
      >
        <Typography variant="h5" paddingLeft={(1, 1, 1, 1)} style={{ color: colors.text }}>
          {props.title}
        </Typography>
      </Box>
    );
  }

  const styles = {
    div: {
      height: props.height || '100%',
      width: '100%',
      display: 'flex',
      flexDirection: 'column',
    },
  };

  return (
    <div style={styles.div}>
      <DataGrid
        autoHeight={props.autoHeight}
        autoPageSize={props.autoPageSize}
        rows={props.rows}
        columns={props.columns}
        slots={{ toolbar: DataGridTitle }}
        initialState={props.initialState}
        rowCount={props.rowCount}
        onRowClick={props.onRowClick}
        getRowId={props.getRowId}
        pageSizeOptions={[10, 25, 50, 100]}
        disableRowSelectionOnClick
        sx={{
          width: '100%',
          flex: 1,
          backgroundColor: colors.card,
          color: colors.text,
          border: `1px solid ${colors.border}`,
          '& .MuiDataGrid-cell': {
            color: colors.text,
            borderBottom: `1px solid ${colors.border}`,
          },
          '& .MuiDataGrid-columnHeaders': {
            backgroundColor: colors.surface,
            color: colors.text,
            borderBottom: `1px solid ${colors.border}`,
          },
          '& .MuiDataGrid-columnHeader': {
            color: colors.text,
          },
          '& .MuiDataGrid-footerContainer': {
            backgroundColor: colors.surface,
            borderTop: `1px solid ${colors.border}`,
          },
          '& .MuiTablePagination-root': {
            color: colors.text,
          },
          '& .MuiIconButton-root': {
            color: colors.textSecondary,
          },
          '& .MuiDataGrid-row:hover': {
            backgroundColor: colors.surfaceHover,
          },
          '& .MuiDataGrid-row.Mui-selected': {
            backgroundColor: `${colors.accent}20`,
          },
          '& .MuiDataGrid-row.Mui-selected:hover': {
            backgroundColor: `${colors.accent}30`,
          },
          '& .MuiDataGrid-columnSeparator': {
            color: colors.border,
          },
          '& .MuiDataGrid-menuIcon': {
            color: colors.textSecondary,
          },
          '& .MuiDataGrid-sortIcon': {
            color: colors.textSecondary,
          },
        }}
      />
    </div>
  );
}