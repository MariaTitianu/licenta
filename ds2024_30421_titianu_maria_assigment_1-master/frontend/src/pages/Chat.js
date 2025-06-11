import classes from "./Chat.module.css";
import {
    Button,
    Collapse,
    Divider,
    List,
    ListItemButton,
    ListItemText,
    TextField,
    Typography
} from "@mui/material";
import React, {useEffect, useRef, useState} from "react";
import {ExpandLess, ExpandMore} from "@mui/icons-material";
import Ribbon from "../components/Ribbon";
import SockJS from "sockjs-client";
import Stomp from "stompjs";
import SendIcon from '@mui/icons-material/Send';
import {toast, ToastContainer} from "react-toastify";

const Chat = () => {
    const userRoleLoading = "Loading";
    const userRoleAdmin = "ADMIN";
    const userRoleUser = "USER";
    const todayDate = new Date();
    const noErrorString = "";
    const stompClientRef = useRef(null);
    let typing = false;

    const usersURL = 'https://security.localhost/users';
    const chatWebsocketURL = 'https://chat.localhost/ws';
    const chatSendTopicString = "app";
    const chatReceiveTopicString = 'chat';
    const adminBroadcastString = 'adminBroadcast';
    const userBroadcastString = 'userBroadcast';
    const adminBroadcastAddress = `/${chatReceiveTopicString}/${adminBroadcastString}`;
    const selfSubscribeAddress = `/${chatReceiveTopicString}/${localStorage.username}`;
    const selfSubscribeNotificationAddress = `/${chatReceiveTopicString}/${localStorage.username}/notifications`;
    const notificationSendAddress = `/${chatSendTopicString}/notification`;
    const messageSendString = `/${chatSendTopicString}/send`;
    const adminBroadcastSendString = `/${chatSendTopicString}/sendAdminBroadcast`;
    const messageErrorString = 'The message cannot be empty!';

    const [userRole, setUserRole] = useState(userRoleLoading);
    const [users, setUsers] = useState([]);
    const [admins, setAdmins] = useState([]);
    const [openAdmins, setOpenAdmins] = useState(true);
    const [openUsers, setOpenUsers] = useState(true);
    const [messageBuffer, setMessageBuffer] = useState([]);
    const [secondUser, setSecondUser] = useState("");
    const secondUserRef = useRef("");
    const [messageError, setMessageError] = useState(noErrorString);
    const [unseenList, setUnseenList] = useState([]);
    const [typingUser, setTypingUser] = useState(" ");
    const messageRef = useRef();

    useEffect(() => {
        secondUserRef.current = secondUser;
    }, [secondUser]);

    useEffect(() => {
        stompClientConnect();
        return () => {
            if (stompClientRef.current) {
                stompClientRef.current.disconnect();
            }
        };
    }, []);

    const stompClientConnect = () => {
        const socket = new SockJS(chatWebsocketURL);
        const stomp = Stomp.over(socket);
        stomp.connect({}, () => {
            stompClientRef.current = stomp;
            stomp.subscribe(adminBroadcastAddress, messageReceivedCallback);
            stomp.subscribe(selfSubscribeAddress, messageReceivedCallback);
            stomp.subscribe(selfSubscribeNotificationAddress, notificationReceivedCallback);
            fetchData();
        });
    };

    const messageReceivedCallback = (message) => {
        const msg = JSON.parse(message.body);
        setMessageBuffer((prev) => [...prev, msg]);
        if (msg.sender !== localStorage.username && msg.receiver === localStorage.username) {
            const client = stompClientRef.current;
            if (!client) return;
            if (secondUserRef.current === msg.sender) {
                client.send(notificationSendAddress, {}, JSON.stringify({
                    sender: localStorage.username,
                    receiver: msg.sender,
                    notification: "SEEN"
                }));
            } else {
                setUnseenList((prev) => {
                    if (!prev.includes(msg.sender)) return [...prev, msg.sender];
                    return prev;
                });
            }
        }
    };

    const notificationReceivedCallback = (message) => {
        const notification = JSON.parse(message.body);
        if (notification.notification === "SEEN") {
            info(notification.sender + " has seen your message!");
        } else if (notification.notification === "TYPING") {
            setTypingUser(notification.sender);
        } else if (notification.notification === "NOT_TYPING") {
            setTypingUser(" ");
        }
    };

    const info = (text) =>
        toast.info(text, {
            position: "bottom-left",
            autoClose: 5000,
            hideProgressBar: false,
            closeOnClick: true,
            pauseOnHover: true,
            draggable: true,
            progress: undefined,
            theme: "colored",
        });

    const fetchData = () => {
        fetch(`${usersURL}/${localStorage.username}`, {
            method: "GET",
            headers: {
                Authorization: `Bearer ${localStorage.token}`,
            },
        })
            .then((response) => {
                if (response.ok) return response.json();
                throw new Error(response);
            })
            .then((data) => {
                setUserRole(data.role);
                if (data.role === userRoleAdmin) {
                    fetch(`${usersURL}?role=ADMIN`, {
                        method: "GET",
                        headers: {
                            Authorization: `Bearer ${localStorage.token}`,
                        },
                    })
                        .then((response) => {
                            if (response.ok) return response.json();
                            throw new Error(response);
                        })
                        .then((data) => {
                            setAdmins(data);
                        })
                        .catch((error) => console.log(error));
                }
                fetch(`${usersURL}?role=CLIENT`, {
                    method: "GET",
                    headers: {
                        Authorization: `Bearer ${localStorage.token}`,
                    },
                })
                    .then((response) => {
                        if (response.ok) return response.json();
                        throw new Error(response);
                    })
                    .then((data) => {
                        setUsers(data);
                    })
                    .catch((error) => console.log(error));
            })
            .catch((error) => {
                console.log(error);
            });
    };

    const handleClickAdmins = () => {
        setOpenAdmins(!openAdmins);
    };

    const handleClickUsers = () => {
        setOpenUsers(!openUsers);
    };

    const handleClickAdminBroadcast = () => {
        setSecondUser(adminBroadcastString);
    };

    const handleListClick = (user) => {
        const newSecondUser = user.username;
        if (newSecondUser !== secondUser) {
            setSecondUser(newSecondUser);
            if (unseenList.includes(newSecondUser)) {
                const client = stompClientRef.current;
                if (client) {
                    client.send(notificationSendAddress, {}, JSON.stringify({
                        sender: localStorage.username,
                        receiver: newSecondUser,
                        notification: "SEEN"
                    }));
                }
                setUnseenList((prev) =>
                    prev.filter((elem) => elem !== newSecondUser)
                );
            }
        }
    };

    const isMessageFormValid = (messageForm) => {
        setMessageError(noErrorString);
        if (!messageForm.content || !messageForm.sender || !messageForm.receiver) {
            setMessageError(messageErrorString);
            return false;
        }
        return true;
    };

    const handleSendMessage = () => {
        const messageForm = {
            sender: localStorage.username,
            receiver: secondUser,
            content: messageRef.current.value,
        };
        if (isMessageFormValid(messageForm)) {
            messageRef.current.value = "";
            const client = stompClientRef.current;
            if (!client) return;
            client.send(messageSendString, {}, JSON.stringify(messageForm));
            client.send(notificationSendAddress, {}, JSON.stringify({
                sender: localStorage.username,
                receiver: secondUser,
                notification: "NOT_TYPING"
            }));
            typing = false;
        }
    };

    const handleSendAdminBroadcast = () => {
        let messageForm = {};
        if (userRole === userRoleAdmin) {
            messageForm = {
                sender: adminBroadcastString,
                receiver: userBroadcastString,
                content: messageRef.current.value,
            };
        } else {
            messageForm = {
                sender: userBroadcastString,
                receiver: adminBroadcastString,
                content: messageRef.current.value,
            };
        }
        if (isMessageFormValid(messageForm)) {
            messageRef.current.value = "";
            const client = stompClientRef.current;
            if (!client) return;
            client.send(adminBroadcastSendString, {}, JSON.stringify(messageForm));
        }
    };

    const handleKeyDown = (e) => {
        if (e.key === "Enter" && !e.shiftKey) {
            e.preventDefault();
            if (secondUser === adminBroadcastString || secondUser === userBroadcastString) {
                handleSendAdminBroadcast();
            } else {
                handleSendMessage();
            }
        }
    };

    const messageFilter = (message) => {
        const isDirect =
            (message.sender === localStorage.username && message.receiver === secondUser) ||
            (message.sender === secondUser && message.receiver === localStorage.username);
        const isBroadcast =
            (secondUser === adminBroadcastString || secondUser === userBroadcastString) &&
            ((message.sender === adminBroadcastString && message.receiver === userBroadcastString) ||
                (message.sender === userBroadcastString && message.receiver === adminBroadcastString));
        return isDirect || isBroadcast;
    };

    const messageMap = (message, i) => {
        const messageDate = new Date(message.timestamp);
        const timestampText =
            todayDate.getDate() === messageDate.getDate()
                ? messageDate.toLocaleTimeString()
                : messageDate.toLocaleString();
        if (
            message.sender === localStorage.username ||
            (userRole === userRoleAdmin && message.sender === adminBroadcastString) ||
            (userRole === userRoleUser && message.sender === userBroadcastString)
        ) {
            return (
                <ListItemText
                    key={i}
                    primary={message.content}
                    secondary={timestampText}
                    primaryTypographyProps={{ style: { color: "white", whiteSpace: "pre-wrap" } }}
                    secondaryTypographyProps={{ style: { color: "white" } }}
                    className={`${classes.message} ${classes.selfMessage}`}
                />
            );
        } else {
            const messageContent = message.content.includes('\n') ? `\n${message.content}` : message.content;
            return (
                <ListItemText
                    key={i}
                    primary={`${message.sender}: ${messageContent}`}
                    secondary={timestampText}
                    primaryTypographyProps={{ style: { whiteSpace: "pre-wrap" } }}
                    className={`${classes.message} ${classes.otherUserMessage}`}
                />
            );
        }
    };

    const checkForTyping = () => {
        if (!typing && messageRef.current.value !== "") {
            const client = stompClientRef.current;
            if (client) {
                client.send(notificationSendAddress, {}, JSON.stringify({
                    sender: localStorage.username,
                    receiver: secondUser,
                    notification: "TYPING"
                }));
            }
            typing = true;
        } else if (typing && messageRef.current.value === "") {
            const client = stompClientRef.current;
            if (client) {
                client.send(notificationSendAddress, {}, JSON.stringify({
                    sender: localStorage.username,
                    receiver: secondUser,
                    notification: "NOT_TYPING"
                }));
            }
            typing = false;
        }
    };

    if (userRole === userRoleLoading) {
        return (
            <div className={classes.loadingDiv}>
                <Typography variant="h3">Loading...</Typography>
            </div>
        );
    } else {
        return (
            <div className={classes.mainDiv}>
                <div className={classes.sidebarDiv}>
                    <Typography sx={{ mb: 2, ml: 2 }} variant="h6">
                        Hi, {localStorage.username}
                    </Typography>
                    <List>
                        <ListItemButton onClick={handleClickAdmins}>
                            <ListItemText primary="Admins" />
                            {openAdmins ? <ExpandLess /> : <ExpandMore />}
                        </ListItemButton>
                        <Collapse in={openAdmins} timeout="auto">
                            <List component="div" disablePadding>
                                <ListItemButton sx={{ pl: 4 }} onClick={handleClickAdminBroadcast}>
                                    <ListItemText primary="Admin Broadcast" />
                                </ListItemButton>
                                {admins.map((user) => (
                                    <ListItemButton
                                        key={user.username}
                                        sx={{ pl: 4 }}
                                        onClick={() => handleListClick(user)}
                                    >
                                        <ListItemText primary={user.username} />
                                    </ListItemButton>
                                ))}
                            </List>
                        </Collapse>
                        <ListItemButton onClick={handleClickUsers}>
                            <ListItemText primary="Users" />
                            {openUsers ? <ExpandLess /> : <ExpandMore />}
                        </ListItemButton>
                        <Collapse in={openUsers} timeout="auto">
                            <List component="div" disablePadding>
                                {users.map((user) => (
                                    <ListItemButton
                                        key={user.username}
                                        sx={{ pl: 4 }}
                                        onClick={() => handleListClick(user)}
                                    >
                                        <ListItemText primary={user.username} />
                                    </ListItemButton>
                                ))}
                            </List>
                        </Collapse>
                    </List>
                </div>
                <Divider sx={{ bgcolor: "#606060" }} orientation="vertical" />
                <div className={classes.chatDiv}>
                    <div>
                        <Ribbon
                            logoutButtonActive={true}
                            chatButtonActive={false}
                            toUserButtonActive={true}
                            toAdminButtonActive={userRole === "ADMIN"}
                        />
                        <ToastContainer newestOnTop={false} rtl={false} pauseOnFocusLoss draggable />
                        <List sx={{ maxHeight: "85vh", overflow: "auto" }}>
                            {messageBuffer.filter(messageFilter).map(messageMap)}
                        </List>
                    </div>
                    <div className={classes.bottomBarDiv}>
                        {typingUser === secondUser && <Typography>Typing...</Typography>}
                        <div className={classes.messageTextFieldDiv}>
                            <TextField
                                label="Type your message here..."
                                id="message"
                                type="text"
                                margin="dense"
                                multiline
                                fullWidth
                                onKeyDown={handleKeyDown}
                                onChange={checkForTyping}
                                inputRef={messageRef}
                                helperText={messageError}
                                error={messageError !== noErrorString}
                                maxRows={6}
                            />
                            <Button
                                className={classes.sendButton}
                                variant="contained"
                                endIcon={<SendIcon />}
                                onClick={
                                    secondUser === adminBroadcastString ||
                                    secondUser === userBroadcastString
                                        ? handleSendAdminBroadcast
                                        : handleSendMessage
                                }
                            >
                                Send
                            </Button>
                        </div>
                    </div>
                </div>
            </div>
        );
    }
};

export default Chat;
