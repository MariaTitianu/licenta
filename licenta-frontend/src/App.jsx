import { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate, useLocation } from 'react-router-dom';
import { ToastContainer } from 'react-toastify';
import { createTheme, ThemeProvider as MuiThemeProvider } from '@mui/material/styles';
import CssBaseline from '@mui/material/CssBaseline';
import 'react-toastify/dist/ReactToastify.css';
import './App.css';
import API from './api';
import { ThemeProvider, useTheme } from './context/ThemeContext';
import { ProtectionProvider } from './context/ProtectionContext';
import Sidebar from './components/Sidebar';

// Import pages
import Products from './pages/Products';
import Payments from './pages/Payments';
import Protection from './pages/Protection';
import Injection from './pages/Injection';
import Benchmark from './pages/Benchmark';
import LogsPanel from './components/LogsPanel';

function AppContent() {
  const location = useLocation();
  const { isDarkMode, colors } = useTheme();
  const [selectedBackend, setSelectedBackend] = useState(8081);
  
  useEffect(() => {
    API.setBackend(selectedBackend);
  }, [selectedBackend]);

  // Always show logs panel
  const showLogsPanel = true;

  // Create MUI theme based on dark mode
  const muiTheme = createTheme({
    palette: {
      mode: isDarkMode ? 'dark' : 'light',
      primary: {
        main: colors.accent,
      },
      secondary: {
        main: colors.accent,
      },
      background: {
        default: colors.background,
        paper: colors.surface,
      },
      text: {
        primary: colors.text,
        secondary: colors.textSecondary,
      },
      divider: colors.border,
      error: {
        main: colors.error,
      },
      warning: {
        main: colors.warning,
      },
      success: {
        main: colors.success,
      },
    },
    components: {
      MuiPaper: {
        styleOverrides: {
          root: {
            backgroundImage: 'none',
          },
        },
      },
      MuiButton: {
        styleOverrides: {
          root: {
            textTransform: 'none',
          },
          outlined: {
            borderColor: colors.border,
            '&:hover': {
              borderColor: colors.accent,
              backgroundColor: colors.surfaceHover,
            },
          },
        },
      },
      MuiChip: {
        styleOverrides: {
          root: {
            fontWeight: 500,
          },
        },
      },
      MuiMenuItem: {
        styleOverrides: {
          root: {
            '&:hover': {
              backgroundColor: colors.surfaceHover,
            },
            '&.Mui-selected': {
              backgroundColor: colors.surfaceHover,
              '&:hover': {
                backgroundColor: colors.surfaceHover,
              },
            },
          },
        },
      },
    },
  });

  return (
    <MuiThemeProvider theme={muiTheme}>
      <CssBaseline />
      <div className="app-container">
        <Sidebar />
        <div 
          className="main-content" 
          style={{ 
            marginLeft: '240px'
          }}
        >
          <Routes>
            <Route path="/" element={<Navigate to="/payments" />} />
            <Route path="/products" element={<Products selectedBackend={selectedBackend} setSelectedBackend={setSelectedBackend} />} />
            <Route path="/payments" element={<Payments selectedBackend={selectedBackend} setSelectedBackend={setSelectedBackend} />} />
            <Route path="/protection" element={<Protection selectedBackend={selectedBackend} setSelectedBackend={setSelectedBackend} />} />
            <Route path="/injection" element={<Injection selectedBackend={selectedBackend} setSelectedBackend={setSelectedBackend} />} />
            <Route path="/benchmark" element={<Benchmark selectedBackend={selectedBackend} setSelectedBackend={setSelectedBackend} />} />
          </Routes>
          <ToastContainer 
            position="bottom-right" 
            autoClose={3000}
            theme={isDarkMode ? "dark" : "light"}
            hideProgressBar={false}
            newestOnTop={false}
            closeOnClick
            rtl={false}
            pauseOnFocusLoss
            draggable
            pauseOnHover
          />
        </div>
        {showLogsPanel && <LogsPanel currentPath={location.pathname} />}
      </div>
    </MuiThemeProvider>
  );
}

function App() {
  return (
    <ThemeProvider>
      <ProtectionProvider>
        <Router>
          <AppContent />
        </Router>
      </ProtectionProvider>
    </ThemeProvider>
  );
}

export default App;
