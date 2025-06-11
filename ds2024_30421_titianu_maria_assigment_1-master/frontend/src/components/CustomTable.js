import {Box, Typography} from "@mui/material";
import {DataGrid} from "@mui/x-data-grid";

export default function CustomTable(props) {
    // console.log(props);
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
                <Typography variant="h5" paddingLeft={(1, 1, 1, 1)}>
                    {props.title}
                </Typography>
            </Box>
        );
    }

    const styles = {
        div: {
            height: props.height,
        },
    };

    return (
        <div style={styles.div}>
            <DataGrid
                autoHeight={props.autoHeight}
                autoPageSize={props.autoPageSize}
                rows={props.rows}
                columns={props.columns}
                slots={{toolbar: DataGridTitle}}
                initialState={props.initialState}
                rowCount={props.rowCount}
                onRowClick={props.onRowClick}
                getRowId={props.getRowId}
                pageSizeOptions={[100]}
            ></DataGrid>
        </div>
    );
}
