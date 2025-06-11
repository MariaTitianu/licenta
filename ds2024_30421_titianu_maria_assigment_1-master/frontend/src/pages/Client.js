import classes from "./Client.module.css";

import React, {useEffect, useRef, useState} from "react";

import {TextField, Typography} from "@mui/material";
import CustomTable from "../components/CustomTable";
import Ribbon from "../components/Ribbon";
import {toast, ToastContainer} from "react-toastify";
import SockJS from "sockjs-client";
import Stomp from "stompjs";
import 'react-toastify/dist/ReactToastify.css';
import dayjs from "dayjs";
import {DateCalendar, LocalizationProvider} from "@mui/x-date-pickers";
import {AdapterDayjs} from "@mui/x-date-pickers/AdapterDayjs";
import {LineChart} from "@mui/x-charts";

const Client = () => {
    const noErrorString = " ";

    const [deviceList, setDeviceList] = useState([]);
    const [userName, setUserName] = useState("");
    const [userRole, setUserRole] = useState("");
    const [stompClient, setStompClient] = useState(null);
    const [hourlyMeasurementNotificationList, setHourlyMeasurementNotificationList] = useState([]);
    const [selectedDate, setSelectedDate] = useState(dayjs());
    const [deviceHourlyData, setDeviceHourlyData] = useState([]);
    const [deviceUuidError, setDeviceUuidError] = useState(noErrorString);

    const deviceUuidRef = useRef(null);

    const hoursList = Array.from({length: 25}, (_, index) => {
        const date = new Date();
        date.setHours(index);
        date.setMinutes(0);
        date.setSeconds(0);
        date.setMilliseconds(0);
        return date;
    });

    const usersURL = 'https://security.localhost/users';
    const devicesUserURL = 'https://security.localhost/devices/users';
    const monitoringURL = 'https://monitoring.localhost/ws'

    const deviceUuidEmptyErrorString = "Device UUID cannot be empty";

    const columnsDevices = [
        {field: "uuid", headerName: "UUID", flex: 1},
        {field: "userUuid", headerName: "Client UUID", flex: 1},
        {field: "name", headerName: "Name", flex: 0.7},
        {field: "description", headerName: "Description", flex: 1},
        {field: "address", headerName: "Address", flex: 0.7},
        {field: "maxHourConsumption", headerName: "Max Hourly Consumption", flex: 0.6},
    ];

    const columnsDevicesNotifications = [
        {field: "id", headerName: "ID", flex: 1},
        {field: "deviceUuid", headerName: "Device UUID", flex: 1},
        {field: "dateTime", headerName: "Date / Time", flex: 1},
        {field: "measurement", headerName: "Total energy consumption (Wh)", flex: 1}
    ];

    useEffect(() => {
        stompClientConnect();

        fetchData();

        return () => {
            if (stompClient) {
                stompClient.disconnect();
            }
        };
    }, []);

    const stompClientConnect = () => {
        const socket = new SockJS(monitoringURL);
        const stomp = Stomp.over(socket);

        stomp.connect({}, () => {
            setStompClient(stomp);
            stomp.subscribe('/topic/notifications', energyExceededNotificationCallback);
            stomp.subscribe('/topic/graphData', graphDataCallback);
        });
    }

    const warn = (text) => toast.warn(text, {
        position: "bottom-right",
        autoClose: 5000,
        hideProgressBar: false,
        closeOnClick: true,
        pauseOnHover: true,
        draggable: true,
        progress: undefined,
        theme: "colored",
    });

    const isNotificationForUser = (userUuid) => {
        return localStorage.getItem("userUuid") === userUuid;
    }

    const energyExceededNotificationCallback = (message) => {
        let hourlyMeasurementNotification = JSON.parse(message.body);
        hourlyMeasurementNotification.measurement = parseFloat(hourlyMeasurementNotification.measurement.toFixed(2));

        const utcDateTimeString = hourlyMeasurementNotification.dateTime + 'Z';
        const date = new Date(utcDateTimeString);
        hourlyMeasurementNotification.dateTime = date.toLocaleString("ro-RO", { timeZone: 'Europe/Bucharest' });

        if (isNotificationForUser(message.headers.userUuid)) {
            setHourlyMeasurementNotificationList((previousList) => {
                return [...previousList, hourlyMeasurementNotification];
            });

            warn(`Device with ID ${hourlyMeasurementNotification.deviceUuid} has exceeded maximum energy consumption limit at ${hourlyMeasurementNotification.dateTime}!`);
        }
    }

    const fetchData = () => {
        fetch(`${usersURL}/${localStorage.username}`, {
            method: "GET",
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
                    setUserName(data.name)
                    setUserRole(data.role)
                    return fetch(`${devicesUserURL}/${localStorage.userUuid}`, {
                        method: "GET",
                        headers: {
                            "Authorization": `Bearer ${localStorage.token}`,
                        },
                    })
                },
                (error) => {
                    console.log(error);
                }
            )
            .then((response) => {
                console.log(response);
                return response.json();
            })
            .then((data2) => {
                console.log(data2.devices);
                setDeviceList(data2.devices);
            });
    };

    const graphDataCallback = (message) => {
        if (message.headers.userUuid === localStorage.getItem("userUuid")) {
            const hourlyMeasurements = JSON.parse(message.body);
            if(hourlyMeasurements[0].deviceUuid === deviceUuidRef.current.value) {
                setDeviceHourlyData(hourlyMeasurements);
            }
        }
    }

    const deviceRowHandler = (params) => {
        deviceUuidRef.current.value = params.row.uuid;
        graphGetHandler(selectedDate, params.row.uuid);
    }

    const handleDateChange = (date) => {
        setSelectedDate(date);
        graphGetHandler(date, deviceUuidRef.current.value);
    }

    const isGraphFormValid = (formData) => {
        setDeviceUuidError(noErrorString);

        let dataValid = true;

        if (formData.deviceUuid === "") {
            setDeviceUuidError(deviceUuidEmptyErrorString);
            dataValid = false;
        }
        if (formData.date === dayjs()) {
            dataValid = false;
        }

        return dataValid;
    }

    const graphGetHandler = (receivedDate, receivedDeviceUuid) => {
        const graphForm = {
            date: receivedDate.toISOString().split('T')[0],
            deviceUuid: receivedDeviceUuid,
            userUuid: localStorage.getItem("userUuid")
        }

        console.log(graphForm);

        if (isGraphFormValid(graphForm)) {
            stompClient.send('/app/graph', {}, JSON.stringify(graphForm))
        }
    }

    return (
        <div className={classes.mainDiv}>
            <div className={classes.topDiv}>
                <Typography
                    variant="h3"
                    marginBottom={1}
                >{`Hi, ${userName}`}</Typography>
                <Ribbon
                    logoutButtonActive={true}
                    toUserButtonActive={false}
                    toAdminButtonActive={userRole === "ADMIN"}
                    toChatButtonActive={true}
                />
            </div>
            <div className={classes.tableDiv}>
                <ToastContainer
                    newestOnTop={false}
                    rtl={false}
                    pauseOnFocusLoss
                    draggable
                />
                <CustomTable
                    title="Device energy limit exceeded notifications"
                    height="50%"
                    rows={hourlyMeasurementNotificationList.map((hourlyMeasurementNotification, index) => ({
                        ...hourlyMeasurementNotification,
                        id: index
                    }))}
                    columns={columnsDevicesNotifications}
                    autoPageSize={true}
                />
                <CustomTable
                    title="Devices"
                    height="50%"
                    rows={deviceList}
                    columns={columnsDevices}
                    getRowId={(row) => row.uuid}
                    autoPageSize={true}
                    onRowClick={deviceRowHandler}
                />
                <div className={classes.graphDiv}>
                    <div className={classes.graphFieldsDiv}>
                        <TextField
                            label='Device UUID (select from table)'
                            id='deviceUuid'
                            type='text'
                            margin='dense'
                            size="small"
                            required
                            inputRef={deviceUuidRef}
                            helperText={deviceUuidError}
                            error={deviceUuidError !== noErrorString}
                            InputLabelProps={{shrink: true}}
                        />
                        <Typography
                            variant="p"
                            marginTop={1}
                        >Selected date:</Typography>
                        <LocalizationProvider dateAdapter={AdapterDayjs}>
                            <DateCalendar
                                label="Selected date"
                                value={selectedDate}
                                onChange={handleDateChange}
                            />
                        </LocalizationProvider>
                    </div>
                    <LineChart
                        xAxis={[{data: hoursList, scaleType: "time"}]}
                        series={[
                            {
                                data: deviceHourlyData.map(deviceData => deviceData.measurement > 0 ? deviceData.measurement : null),
                                color: `#c9c900`,
                                area: true
                            },
                        ]}
                    />
                </div>
            </div>
        </div>
    );
};

export default Client;
