import { createContext, useContext, useState, useEffect } from 'react';
import API from '../api';

const ProtectionContext = createContext();

export const useProtection = () => {
  const context = useContext(ProtectionContext);
  if (!context) {
    throw new Error('useProtection must be used within a ProtectionProvider');
  }
  return context;
};

export const ProtectionProvider = ({ children }) => {
  const [protectionStatuses, setProtectionStatuses] = useState({});
  const [lastUpdate, setLastUpdate] = useState(Date.now());

  const fetchProtectionStatuses = async () => {
    try {
      const productsStatus = await API.protection.getStatus('products');
      const paymentsStatus = await API.protection.getStatus('customer_payments');
      setProtectionStatuses({
        '/products': productsStatus.isProtected,
        '/payments': paymentsStatus.isProtected
      });
    } catch (error) {
      console.error('Failed to fetch protection statuses:', error);
    }
  };

  useEffect(() => {
    fetchProtectionStatuses();
    const interval = setInterval(fetchProtectionStatuses, 5000);
    return () => clearInterval(interval);
  }, []);

  // Force refresh when lastUpdate changes
  useEffect(() => {
    fetchProtectionStatuses();
  }, [lastUpdate]);

  // Method to force refresh protection statuses
  const refreshProtectionStatuses = () => {
    setLastUpdate(Date.now());
  };

  const value = {
    protectionStatuses,
    refreshProtectionStatuses
  };

  return (
    <ProtectionContext.Provider value={value}>
      {children}
    </ProtectionContext.Provider>
  );
};