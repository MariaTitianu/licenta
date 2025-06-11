import classes from './Admin.module.css';

import React, {useEffect, useRef, useState} from 'react';

import {
    Typography,
    Divider,
    TextField,
    Button,
    Select,
    MenuItem,
    FormControl,
    InputLabel,
} from '@mui/material';
import CustomTable from '../components/CustomTable';
import Ribbon from "../components/Ribbon";
import {useNavigate} from "react-router-dom";

const Admin = () => {
    const navigate = useNavigate();

    const usersURL = 'https://security.localhost/users';
    const devicesURL = 'https://security.localhost/devices';

    const noErrorString = " ";

    const [authorizedAccess, setAuthorizedAccess] = useState('loading');
    const [adminData, setAdminData] = useState({});
    const [userRoles, setUserRoles] = useState([]);
    const [selectedUserRole, setSelectedUserRole] = useState('USER');
    const [userList, setUserList] = useState([]);
    const [deviceList, setDeviceList] = useState([]);
    const [userUsernameError, setUserUsernameError] = useState(noErrorString);
    const [userPasswordError, setUserPasswordError] = useState(noErrorString);
    const [userRepeatPasswordError, setUserRepeatPasswordError] = useState(noErrorString);
    const [userNameError, setUserNameError] = useState(noErrorString);
    const [userRoleError, setUserRoleError] = useState(noErrorString);
    const [deviceUuidError, setDeviceUuidError] = useState(noErrorString);
    const [deviceUserUuidError, setDeviceUserUuidError] = useState(noErrorString);
    const [deviceNameError, setDeviceNameError] = useState(noErrorString);
    const [deviceDescriptionError, setDeviceDescriptionError] = useState(noErrorString);
    const [deviceAddressError, setDeviceAddressError] = useState(noErrorString);
    const [deviceMaxHourConsumptionError, setDeviceMaxHourConsumptionError] = useState(noErrorString);
    const [deviceError, setDeviceError] = useState(noErrorString);

    const userUsernameRef = useRef(null);
    const userPasswordRef = useRef(null);
    const userRepeatPasswordRef = useRef(null);
    const userNameRef = useRef(null);
    const deviceUuidRef = useRef(null);
    const deviceUserUuidRef = useRef(null);
    const deviceNameRef = useRef(null);
    const deviceDescriptionRef = useRef(null);
    const deviceAddressRef = useRef(null);
    const deviceMaxHourConsumptionRef = useRef(null);

    const usernameEmptyErrorString = "Username cannot be empty";
    const usernameEmptyOrWrongString = "Username is empty or wrong"
    const usernameExistsErrorString = "Username already exists";
    const passwordErrorString = "Password cannot be empty";
    const repeatPasswordErrorString = "Repeated password doesn't match";
    const nameErrorString = "Name cannot be empty";
    const roleErrorString = "Role cannot be empty";
    const deviceUuidEmptyErrorString = "Device UUID cannot be empty";
    const deviceUserUuidEmptyErrorString = "Client UUID cannot be empty";
    const deviceNameErrorString = "Device description cannot be empty";
    const deviceDescriptionErrorString = "Device description cannot be empty";
    const deviceAddressErrorString = "Device address cannot be empty";
    const deviceMaxHourConsumptionErrorString = "Device max Wh cannot be empty";
    const deviceCreateErrorString = "Error when creating device";
    const deviceUpdateErrorString = "Error when updating device";
    const deviceDeleteErrorString = "Error when deleting device";

    const columnsUsers = [
        {field: "uuid", headerName: "UUID", flex: 1},
        {field: "username", headerName: "Username", flex: 1},
        {field: "name", headerName: "Name", flex: 1},
        {field: "role", headerName: "Role", flex: 1},
    ];
    const columnsDevices = [
        {field: "uuid", headerName: "UUID", flex: 1},
        {field: "userUuid", headerName: "Client UUID", flex: 1},
        {field: "name", headerName: "Name", flex: 0.7},
        {field: "description", headerName: "Description", flex: 1},
        {field: "address", headerName: "Address", flex: 0.7},
        {field: "maxHourConsumption", headerName: "Max Hourly Consumption", flex: 0.6},
    ];

    useEffect(() => {
        fetchUserData();
    }, []);

    const fetchUserData = () => {
        fetch(`${usersURL}/${localStorage.username}`, {
            method: "GET",
            headers: {
                "Authorization": `Bearer ${localStorage.token}`,
            }
        })
            .then((response) => {
                console.log(response)
                if (response.ok) {
                    return response.json();
                }
                throw new Error(response);
            })
            .then(
                (data) => {
                    console.log(data);
                    if (data.role !== 'ADMIN') {
                        setAuthorizedAccess('unauthorized');
                    } else {
                        setAuthorizedAccess('authorized');
                        setAdminData(data);
                        fetchUserRoles();
                        fetchUserTableData();
                        fetchDeviceTableData();
                    }
                },
                (error) => {
                    console.log(error);
                }
            );
    };

    const fetchUserRoles = () => {
        fetch(`${usersURL}/roles`, {
            method: "GET",
            headers: {
                "Authorization": `Bearer ${localStorage.token}`,
            }
        })
            .then((response) => {
                if (response.ok) {
                    return response.json();
                }
                throw new Error(response);
            })
            .then(
                (data) => {
                    console.log(data);
                    setUserRoles(data)
                },
                (error) => {
                    console.log(error);
                }
            );
    }

    const fetchUserTableData = () => {
        fetch(`${usersURL}`, {
            method: "GET",
            headers: {
                "Authorization": `Bearer ${localStorage.token}`,
            }
        })
            .then((response) => {
                if (response.ok) {
                    return response.json();
                }
                throw new Error(response);
            })
            .then(
                (data) => {
                    console.log(data);
                    setUserList(data)
                },
                (error) => {
                    console.log(error);
                }
            );
    }

    const fetchDeviceTableData = () => {
        fetch(`${devicesURL}`, {
            method: "GET",
            headers: {
                "Authorization": `Bearer ${localStorage.token}`,
            }
        })
            .then((response) => {
                if (response.ok) {
                    return response.json();
                }
                throw new Error(response);
            })
            .then(
                (data) => {
                    console.log(data);
                    setDeviceList(data)
                },
                (error) => {
                    console.log(error);
                }
            );
    }

    const handleRoleSelectChange = (event) => {
        setSelectedUserRole(event.target.value);
    };

    const userRowHandler = (params) => {
        console.log(params)

        userUsernameRef.current.value = params.row.username;
        userNameRef.current.value = params.row.name;
        setSelectedUserRole(params.row.role);

        deviceUserUuidRef.current.value = params.row.uuid;
    }

    const deviceRowHandler = (params) => {
        console.log(params)

        deviceUuidRef.current.value = params.row.uuid;
        deviceUserUuidRef.current.value = params.row.userUuid;
        deviceNameRef.current.value = params.row.name;
        deviceDescriptionRef.current.value = params.row.description;
        deviceAddressRef.current.value = params.row.address;
        deviceMaxHourConsumptionRef.current.value = params.row.maxHourConsumption;
    }

    const isUserUsernameValid = (username) => {
        setUserUsernameError(noErrorString);
        setUserPasswordError(noErrorString);
        setUserRepeatPasswordError(noErrorString);
        setUserNameError(noErrorString);
        setUserRoleError(noErrorString);

        if (username === "") {
            setUserUsernameError(usernameEmptyErrorString);
            return false;
        }
        return true;
    }

    const isUserCreateFormValid = (formData) => {
        setUserUsernameError(noErrorString);
        setUserPasswordError(noErrorString);
        setUserRepeatPasswordError(noErrorString);
        setUserNameError(noErrorString);
        setUserRoleError(noErrorString);

        let dataValid = true;

        if (formData.username === "") {
            setUserUsernameError(usernameEmptyErrorString);
            dataValid = false;
        }
        if (formData.password === "") {
            setUserPasswordError(passwordErrorString);
            dataValid = false;
        }
        if (userRepeatPasswordRef.current.value !== formData.password) {
            setUserRepeatPasswordError(repeatPasswordErrorString);
            dataValid = false;
        }
        if (formData.name === "") {
            setUserNameError(nameErrorString);
            dataValid = false;
        }
        if (selectedUserRole === "") {
            setUserRoleError(roleErrorString);
            dataValid = false;
        }

        return dataValid;
    }

    const isDeviceUuidValid = (uuid) => {
        setDeviceError(noErrorString);
        setDeviceUuidError(noErrorString);
        setDeviceUserUuidError(noErrorString);
        setDeviceNameError(noErrorString);
        setDeviceDescriptionError(noErrorString);
        setDeviceAddressError(noErrorString);
        setDeviceMaxHourConsumptionError(noErrorString);

        if (uuid === "") {
            setDeviceUuidError(deviceUuidEmptyErrorString);
            return false;
        }
        return true;
    }

    const isDeviceFormValid = (formData) => {
        setDeviceError(noErrorString);
        setDeviceUuidError(noErrorString);
        setDeviceUserUuidError(noErrorString);
        setDeviceNameError(noErrorString);
        setDeviceDescriptionError(noErrorString);
        setDeviceAddressError(noErrorString);
        setDeviceMaxHourConsumptionError(noErrorString);

        let dataValid = true;

        if (formData.userUuid === "") {
            setDeviceUserUuidError(deviceUserUuidEmptyErrorString);
            dataValid = false;
        }
        if (formData.name === "") {
            setDeviceNameError(deviceNameErrorString);
            dataValid = false;
        }
        if (formData.description === "") {
            setDeviceDescriptionError(deviceDescriptionErrorString);
            dataValid = false;
        }
        if (formData.address === "") {
            setDeviceAddressError(deviceAddressErrorString);
            dataValid = false;
        }
        if (formData.maxHourConsumption === "") {
            setDeviceMaxHourConsumptionError(deviceMaxHourConsumptionErrorString);
            dataValid = false;
        }

        return dataValid;
    }

    const userCreateHandler = () => {
        const userForm = {
            username: userUsernameRef.current.value,
            password: userPasswordRef.current.value,
            name: userNameRef.current.value,
            role: selectedUserRole,
        }
        if (isUserCreateFormValid(userForm)) {
            fetch(`${usersURL}`, {
                method: "POST",
                body: JSON.stringify(userForm),
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": `Bearer ${localStorage.token}`,
                },
            })
                .then((response) => {
                    if (response.ok) {
                        return response.json();
                    }
                    throw new Error(response);
                })
                .then(
                    (data) => {
                        console.log(data);
                        fetchUserTableData();
                    },
                    (error) => {
                        setUserUsernameError(usernameExistsErrorString);
                        console.log(error);
                    }
                );
        }
    }

    const userUpdateHandler = () => {
        const userForm = {
            password: userPasswordRef.current.value,
            name: userNameRef.current.value,
            role: selectedUserRole,
        };

        const username = userUsernameRef.current.value;

        if (isUserUsernameValid(username)) {
            fetch(`${usersURL}/${username}`, {
                method: "PATCH",
                body: JSON.stringify(userForm),
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": `Bearer ${localStorage.token}`,
                },
            })
                .then((response) => {
                    if (response.ok) {
                        return response.json();
                    }
                    throw new Error(response);
                })
                .then(
                    (data) => {
                        console.log(data);
                        fetchUserData()
                    },
                    (error) => {
                        setUserUsernameError(usernameEmptyOrWrongString);
                        console.log(error);
                    }
                );
        }
    }

    const userDeleteHandler = () => {
        const username = userUsernameRef.current.value;
        if (isUserUsernameValid(username)) {
            fetch(`${usersURL}/${username}`, {
                method: "DELETE",
                headers: {
                    "Authorization": `Bearer ${localStorage.token}`,
                },
            })
                .then((response) => {
                    if (response.ok) {
                        return response.json();
                    }
                    throw new Error(response);
                })
                .then(
                    (data) => {
                        console.log(data);
                        if(data.uuid === localStorage.getItem("userUuid")){
                            localStorage.clear();
                            navigate("/login");
                        }
                        fetchUserData()
                    },
                    (error) => {
                        console.log(error);
                        setUserUsernameError(usernameEmptyOrWrongString);
                    }
                );
        }
    }

    const deviceCreateHandler = () => {
        const deviceForm = {
            userUuid: deviceUserUuidRef.current.value,
            name: deviceNameRef.current.value,
            description: deviceDescriptionRef.current.value,
            address: deviceAddressRef.current.value,
            maxHourConsumption: deviceMaxHourConsumptionRef.current.value,
        }
        if (isDeviceFormValid(deviceForm)) {
            fetch(`${devicesURL}`, {
                method: "POST",
                body: JSON.stringify(deviceForm),
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": `Bearer ${localStorage.token}`,
                },
            })
                .then((response) => {
                    if (response.ok) {
                        return response.json();
                    }
                    throw new Error(response);
                })
                .then(
                    (data) => {
                        console.log(data);
                        fetchDeviceTableData();
                    },
                    (error) => {
                        setDeviceError(deviceCreateErrorString)
                        console.log(error);
                    }
                );
        }
    }

    const deviceUpdateHandler = () => {
        const deviceForm = {
            userUuid: deviceUserUuidRef.current.value,
            name: deviceNameRef.current.value,
            description: deviceDescriptionRef.current.value,
            address: deviceAddressRef.current.value,
            maxHourConsumption: deviceMaxHourConsumptionRef.current.value,
        }

        const deviceUuid = deviceUuidRef.current.value;

        if (isDeviceUuidValid(deviceUuid)) {
            fetch(`${devicesURL}/${deviceUuid}`, {
                method: "PATCH",
                body: JSON.stringify(deviceForm),
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": `Bearer ${localStorage.token}`,
                },
            })
                .then((response) => {
                    if (response.ok) {
                        return response.json();
                    }
                    throw new Error(response);
                })
                .then(
                    (data) => {
                        console.log(data);
                        fetchDeviceTableData();
                    },
                    (error) => {
                        setDeviceError(deviceUpdateErrorString);
                        console.log(error);
                    }
                );
        }
    }

    const deviceDeleteHandler = () => {
        const deviceUuid = deviceUuidRef.current.value;
        if (isDeviceUuidValid(deviceUuid)) {
            fetch(`${devicesURL}/${deviceUuid}`, {
                method: "DELETE",
                headers: {
                    "Authorization": `Bearer ${localStorage.token}`,
                },
            })
                .then((response) => {
                    if (response.ok) {
                        return response.json();
                    }
                    throw new Error(response);
                })
                .then(
                    (data) => {
                        console.log(data);
                        fetchDeviceTableData();
                    },
                    (error) => {
                        console.log(error);
                        setDeviceError(deviceDeleteErrorString);
                    }
                );
        }
    }

    if (authorizedAccess === 'loading') {
        return (
            <div className={classes.loadingDiv}>
                <Typography variant='h3'> Loading... </Typography>
            </div>
        );
    } else if (authorizedAccess === 'unauthorized') {
        return (
            <div className={classes.unauthorizedDiv}>
                <Typography variant='h3'> 401 Unauthorized</Typography>
                <Ribbon
                    logoutButtonActive={true}
                    toUserButtonActive={true}
                    toAdminButtonActive={false}
                    toChatButtonActive={true}
                />
            </div>
        );
    } else {
        return (
            <div className={classes.mainDiv}>
                <div className={classes.tableDiv}>
                    <Typography variant='h3' marginBottom={1}>
                        {
                            `Hi, ${adminData.name}`
                        }
                    </Typography>
                    <CustomTable
                        title='Users'
                        height='50%'
                        rows={userList} //{doctorList}
                        columns={columnsUsers} //{columnsDoctors}
                        onRowClick={userRowHandler}
                        getRowId={(row) => row.uuid}
                    />
                    <CustomTable
                        title='Devices'
                        height='50%'
                        rows={deviceList} //{locationList}
                        columns={columnsDevices} //{columnsLocations}
                        onRowClick={deviceRowHandler}
                        getRowId={(row) => row.uuid}
                    />
                </div>
                <Divider sx={{bgcolor: "#606060"}} orientation='vertical'/>
                <div className={classes.sidebarDiv}>
                    <Ribbon
                        logoutButtonActive={true}
                        toUserButtonActive={true}
                        toAdminButtonActive={false}
                        toChatButtonActive={true}
                    />
                    <div className={classes.userFieldsDiv}>
                        <Typography variant='h5'>
                            Users
                        </Typography>
                        <TextField
                            label='Username'
                            id='userUsername'
                            type='text'
                            margin='dense'
                            size="small"
                            required
                            inputRef={userUsernameRef}
                            helperText={userUsernameError}
                            error={userUsernameError !== noErrorString}
                            InputLabelProps={{shrink: true}}
                        />
                        <TextField
                            label='Password'
                            id='userPassword'
                            type='password'
                            size="small"
                            margin='dense'
                            inputRef={userPasswordRef}
                            helperText={userPasswordError}
                            error={userPasswordError !== noErrorString}
                            InputLabelProps={{shrink: true}}
                        />
                        <TextField
                            label='Repeat Password'
                            id='userRepeatPassword'
                            type='password'
                            size="small"
                            margin='dense'
                            inputRef={userRepeatPasswordRef}
                            helperText={userRepeatPasswordError}
                            error={userRepeatPasswordError !== noErrorString}
                            InputLabelProps={{shrink: true}}
                        />
                        <TextField
                            label='Name'
                            id='userName'
                            type='text'
                            size="small"
                            margin='dense'
                            inputRef={userNameRef}
                            helperText={userNameError}
                            error={userNameError !== noErrorString}
                            InputLabelProps={{shrink: true}}
                        />
                        <FormControl
                            sx={{my: 1}}
                            helperText={userRoleError}
                            error={userRoleError !== noErrorString}
                            size="small"
                        >
                            <InputLabel id="role">Role</InputLabel>
                            <Select
                                label="Role"
                                id="userRole"
                                value={selectedUserRole}
                                onChange={handleRoleSelectChange}
                            >
                                {userRoles.map(userRole => <MenuItem
                                    value={userRole}>{userRole}</MenuItem>)}
                            </Select>
                        </FormControl>
                        <div className={classes.buttonsDiv}>
                            <Button
                                variant='contained'
                                size='small'
                                sx={{margin: 1, flex: 1}}
                                onClick={userCreateHandler}
                            >
                                Create
                            </Button>
                            <Button
                                variant='contained'
                                size='small'
                                sx={{margin: 1, flex: 1}}
                                onClick={userUpdateHandler}
                            >
                                Update
                            </Button>
                            <Button
                                variant='contained'
                                size='small'
                                sx={{margin: 1, flex: 1}}
                                onClick={userDeleteHandler}
                            >
                                Delete
                            </Button>
                        </div>
                    </div>
                    <Divider sx={{bgcolor: "#606060"}}/>
                    <div className={classes.userFieldsDiv}>
                        <Typography variant='h5'>
                            Devices
                        </Typography>
                        <TextField
                            label='UUID'
                            id='deviceUuid'
                            type='text'
                            size="small"
                            margin='dense'
                            required
                            disabled
                            inputRef={deviceUuidRef}
                            helperText={deviceUuidError}
                            error={deviceUuidError !== noErrorString}
                            InputLabelProps={{shrink: true}}
                        />
                        <TextField
                            label='Client UUID'
                            id='deviceUserUuid'
                            type='text'
                            size="small"
                            margin='dense'
                            required
                            disabled
                            inputRef={deviceUserUuidRef}
                            helperText={deviceUserUuidError}
                            error={deviceUserUuidError !== noErrorString}
                            InputLabelProps={{shrink: true}}
                        />
                        <TextField
                            label='Name'
                            id='deviceName'
                            type='text'
                            size="small"
                            margin='dense'
                            required
                            inputRef={deviceNameRef}
                            helperText={deviceNameError}
                            error={deviceNameError !== noErrorString}
                            InputLabelProps={{shrink: true}}
                        />
                        <TextField
                            label='Description'
                            id='deviceDescription'
                            type='text'
                            size="small"
                            margin='dense'
                            required
                            multiline
                            maxRows={2}
                            inputRef={deviceDescriptionRef}
                            helperText={deviceDescriptionError}
                            error={deviceDescriptionError !== noErrorString}
                            InputLabelProps={{shrink: true}}
                        />
                        <TextField
                            label='Address'
                            id='deviceAddress'
                            type='text'
                            size="small"
                            margin='dense'
                            required
                            multiline
                            maxRows={2}
                            inputRef={deviceAddressRef}
                            helperText={deviceAddressError}
                            error={deviceAddressError !== noErrorString}
                            InputLabelProps={{shrink: true}}
                        />
                        <TextField
                            label='Maximum Energy Consumption (in Wh)'
                            id='deviceMaxHourConsumption'
                            type='number'
                            size="small"
                            margin='dense'
                            required
                            inputRef={deviceMaxHourConsumptionRef}
                            helperText={deviceMaxHourConsumptionError}
                            error={deviceMaxHourConsumptionError !== noErrorString}
                            InputLabelProps={{shrink: true}}
                        />
                        <div className={classes.buttonsDiv}>
                            <Button
                                variant='contained'
                                size='small'
                                sx={{margin: 1, flex: 1}}
                                onClick={deviceCreateHandler}
                            >
                                Create
                            </Button>
                            <Button
                                variant='contained'
                                size='small'
                                sx={{margin: 1, flex: 1}}
                                onClick={deviceUpdateHandler}
                            >
                                Update
                            </Button>
                            <Button
                                variant='contained'
                                size='small'
                                sx={{margin: 1, flex: 1}}
                                onClick={deviceDeleteHandler}
                            >
                                Delete
                            </Button>
                        </div>
                        <Typography variant='p' style={{color: "#FF0000"}}>
                            {deviceError}
                        </Typography>
                    </div>
                </div>
            </div>
        );
    }
};

export default Admin;
