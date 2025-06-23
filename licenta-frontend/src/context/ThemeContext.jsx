import { createContext, useContext, useState, useEffect } from 'react';

const ThemeContext = createContext();

export const useTheme = () => {
  const context = useContext(ThemeContext);
  if (!context) {
    throw new Error('useTheme must be used within a ThemeProvider');
  }
  return context;
};

export const ThemeProvider = ({ children }) => {
  const [isDarkMode, setIsDarkMode] = useState(() => {
    const savedTheme = localStorage.getItem('theme');
    return savedTheme ? savedTheme === 'dark' : true;
  });

  useEffect(() => {
    localStorage.setItem('theme', isDarkMode ? 'dark' : 'light');
    document.documentElement.setAttribute('data-theme', isDarkMode ? 'dark' : 'light');
  }, [isDarkMode]);

  const toggleTheme = () => {
    setIsDarkMode(prev => !prev);
  };

  const theme = {
    isDarkMode,
    toggleTheme,
    colors: isDarkMode ? {
      background: '#0f1114',
      surface: '#1a1d24',
      surfaceHover: '#232730',
      card: '#1f2329',
      border: '#2a2e37',
      text: '#e4e4e7',
      textSecondary: '#9ca3af',
      accent: '#3b82f6',
      accentHover: '#2563eb',
      success: '#10b981',
      error: '#ef4444',
      warning: '#f59e0b'
    } : {
      background: '#f8fafc',
      surface: '#ffffff',
      surfaceHover: '#f3f4f6',
      card: '#ffffff',
      border: '#e5e7eb',
      text: '#1f2937',
      textSecondary: '#6b7280',
      accent: '#3b82f6',
      accentHover: '#2563eb',
      success: '#10b981',
      error: '#ef4444',
      warning: '#f59e0b'
    }
  };

  return (
    <ThemeContext.Provider value={theme}>
      {children}
    </ThemeContext.Provider>
  );
};