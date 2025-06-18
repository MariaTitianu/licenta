import { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate, useLocation } from 'react-router-dom';
import { ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import './App.css';
import API from './api';

// Import pages (to be created)
import Products from './pages/Products';
import Payments from './pages/Payments';
import Protection from './pages/Protection';
import Injection from './pages/Injection';
import Benchmark from './pages/Benchmark';
import LogsPanel from './components/LogsPanel';

function AppContent() {
  const location = useLocation();
  const [selectedBackend, setSelectedBackend] = useState(8081);
  useEffect(() => {
    API.setBackend(selectedBackend);
  }, [selectedBackend]);

  // Always show logs panel
  const showLogsPanel = true;

  return (
    <>
      <div className="App" style={{ paddingBottom: showLogsPanel ? '350px' : '0' }}>
        <Routes>
          <Route path="/" element={<Navigate to="/products" />} />
          <Route path="/products" element={<Products selectedBackend={selectedBackend} setSelectedBackend={setSelectedBackend} />} />
          <Route path="/payments" element={<Payments selectedBackend={selectedBackend} setSelectedBackend={setSelectedBackend} />} />
          <Route path="/protection" element={<Protection selectedBackend={selectedBackend} setSelectedBackend={setSelectedBackend} />} />
          <Route path="/injection" element={<Injection selectedBackend={selectedBackend} setSelectedBackend={setSelectedBackend} />} />
          <Route path="/benchmark" element={<Benchmark selectedBackend={selectedBackend} setSelectedBackend={setSelectedBackend} />} />
        </Routes>
        <ToastContainer 
          position="bottom-right" 
          autoClose={3000}
          theme="colored"
          hideProgressBar={false}
          newestOnTop={false}
          closeOnClick
          rtl={false}
          pauseOnFocusLoss
          draggable
          pauseOnHover
        />
      </div>
      {showLogsPanel && <LogsPanel />}
    </>
  );
}

function App() {
  return (
    <Router>
      <AppContent />
    </Router>
  );
}

export default App;
