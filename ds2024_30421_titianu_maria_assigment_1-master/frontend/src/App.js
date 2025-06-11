import {Route, Routes, Navigate} from "react-router-dom";

import Login from "./pages/Login";
import Sike from "./pages/Sike";
import Admin from "./pages/Admin";
import Client from "./pages/Client";
import Chat from "./pages/Chat";

function App() {
    return (
        <Routes>
            <Route path="/" element={<Navigate to="/login"/>}/>
            <Route path="/login" element={<Login/>}/>
            <Route path="/sike" element={<Sike/>}/>
            <Route path="/admin" element={<Admin/>}/>
            <Route path="/client" element={<Client/>}/>
            <Route path="/chat" element={<Chat/>}/>
        </Routes>
    );
}

export default App;
