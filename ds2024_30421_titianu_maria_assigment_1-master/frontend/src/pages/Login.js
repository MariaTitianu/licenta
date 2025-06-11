import {TextField, Button, Typography} from "@mui/material";
import {useState, useRef} from "react";
import {useNavigate} from "react-router-dom";

import classes from "./Login.module.css";

const Login = () => {
    const navigate = useNavigate();

    const [usernameError, setUsernameError] = useState(" ");
    const [passwordError, setPasswordError] = useState(" ");
    const [loginError, setLoginError] = useState(" ");

    const usernameEmptyErrorString = "Username cannot be empty";
    const passwordErrorString = "Password cannot be empty";
    const loginErrorString = "Username and/or password is incorrect!";

    const usernameRef = useRef();
    const passwordRef = useRef();

    const endpointsURL = 'https://security.localhost';

    const isFormValid = (userData) => {
        var formValid = true;
        setUsernameError(" ");
        setPasswordError(" ");
        if (userData.username === "") {
            setUsernameError(usernameEmptyErrorString);
            formValid = false;
        }
        if (userData.password === "") {
            setPasswordError(passwordErrorString);
            formValid = false;
        }
        return formValid;
    };

    const signInHandler = () => {
        const username = usernameRef.current.value;
        const signInDTO = {
            username: username,
            password: passwordRef.current.value,
        };


        if (isFormValid(signInDTO)) {
            setLoginError(" ");
            fetch(`${endpointsURL}/auth/login`, {
                method: "POST", body: JSON.stringify(signInDTO), headers: {
                    "Content-Type": "application/json",
                },
            })
                .then((response) => {
                    console.log(response)
                    if (response.ok) {
                        return response.json();
                    }
                    throw new Error(response);
                })
                .then((data) => {
                    console.log(data);
                    localStorage.setItem("token", data.token)
                    return fetch(`${endpointsURL}/users/${username}`, {
                        method: "GET",
                        headers: {
                            "Authorization": `Bearer ${data.token}`,
                        },
                    })
                },)
                .then((response) => {
                    console.log(response)
                    if (response.ok) {
                        return response.json();
                    }
                    throw new Error(response);
                })
                .then((data) => {
                    console.log(data);
                    localStorage.setItem("userUuid", data.uuid);
                    localStorage.setItem("username", username);
                    switch (data.role) {
                        case "ADMIN":
                            navigate("/admin");
                            break;
                        case "CLIENT":
                            navigate("/client");
                            break;
                        default:
                            console.log("Error while converting user to role!");
                    }
                },)
                .catch((error) => {
                    console.log(error);
                    setLoginError(loginErrorString);
                });
        }
    };
    return (<div className={classes.mainDiv}>
        <div className={classes.loginDiv}>
            <Typography variant="h3" marginBottom={5}>
                DS Energy Management System
            </Typography>
            <TextField
                label="Username"
                id="username"
                type="text"
                margin="dense"
                required
                inputRef={usernameRef}
                helperText={usernameError}
                error={usernameError !== " "}
            />
            <TextField
                label="Password"
                id="password"
                type="password"
                margin="dense"
                required
                inputRef={passwordRef}
                helperText={passwordError}
                error={passwordError !== " "}
            />
            <div className={classes.errorDiv}>
                <Typography variant="p" style={{color: "#FF0000"}}>
                    {loginError}
                </Typography>
            </div>
            <div className={classes.buttonsDiv}>
                <Button variant="contained" onClick={signInHandler}>
                    Sign In
                </Button>
                <Button variant="contained" onClick={() => navigate("/sike")}>
                    Sign Up
                </Button>
            </div>
        </div>
    </div>);
};

export default Login;
