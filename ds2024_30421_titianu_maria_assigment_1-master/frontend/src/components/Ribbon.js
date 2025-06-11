import classes from './Ribbon.module.css';
import {Button} from "@mui/material";
import {useNavigate} from "react-router-dom";

const Ribbon = (props) => {
    const navigate = useNavigate();

    const logoutHandler = () => {
        localStorage.clear();
        navigate("/login");
    }

    const toUserHandler = () => {
        navigate("/client");
    }

    const toAdminHandler = () => {
        navigate("/admin");
    }

    const toChatHandler = () => {
        navigate("/chat");
    }

    return (
        <div className={classes.mainDiv}>
            {props.logoutButtonActive && <Button variant='contained' sx={{margin: 0.2}} onClick={logoutHandler}>Logout</Button>}
            {props.toUserButtonActive && <Button variant='contained' sx={{margin: 0.2}} onClick={toUserHandler}>To Client</Button>}
            {props.toAdminButtonActive && <Button variant='contained' sx={{margin: 0.2}} onClick={toAdminHandler}>To Admin</Button>}
            {props.toChatButtonActive && <Button variant='contained' sx={{margin: 0.2}} onClick={toChatHandler}>To Chat</Button>}
        </div>
    )
}

export default Ribbon;