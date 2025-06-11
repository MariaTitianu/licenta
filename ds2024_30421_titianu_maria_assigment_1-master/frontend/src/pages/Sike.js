import {Typography} from "@mui/material";

import classes from "./Sike.module.css";

const Sike = () => {
    return (
        <div className={classes.mainDiv}>
            <Typography variant="h1" marginBottom={5}>
                Sike! Not part of the requirements!
            </Typography>
        </div>
    );
}

export default Sike;