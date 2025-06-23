import { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { 
  IconButton,
  Divider
} from '@mui/material';
import {
  Menu as MenuIcon,
  MenuOpen as MenuOpenIcon,
  Inventory as ProductsIcon,
  Payment as PaymentsIcon,
  Security as SecurityIcon,
  BugReport as InjectionIcon,
  Speed as BenchmarkIcon,
  LightMode as LightIcon,
  DarkMode as DarkIcon
} from '@mui/icons-material';
import { useTheme } from '../context/ThemeContext';
import { useProtection } from '../context/ProtectionContext';
import classes from './Sidebar.module.css';

const Sidebar = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { isDarkMode, toggleTheme, colors } = useTheme();
  const { protectionStatuses } = useProtection();

  const navigationItems = [
    { path: '/payments', label: 'Payments', icon: PaymentsIcon, hasProtection: true },
    { path: '/products', label: 'Products', icon: ProductsIcon, hasProtection: true },
    { path: '/protection', label: 'Protection', icon: SecurityIcon },
    { path: '/injection', label: 'SQL Injection', icon: InjectionIcon },
    { path: '/benchmark', label: 'Benchmarks', icon: BenchmarkIcon },
  ];


  const isActive = (path) => location.pathname === path;

  const sidebarStyle = {
    backgroundColor: colors.surface,
    borderRight: `1px solid ${colors.border}`,
    color: colors.text
  };

  return (
    <div 
      className={classes.sidebar}
      style={sidebarStyle}
    >
      <div className={classes.header}>
        <div className={classes.logo}>
          <span className={classes.logoIcon}>🛡️</span>
          <span className={classes.logoText}>PG Warden</span>
        </div>
        <IconButton
          onClick={toggleTheme}
          size="small"
          sx={{
            ml: 'auto',
            color: colors.text,
            '&:hover': {
              backgroundColor: colors.surfaceHover,
            }
          }}
        >
          {isDarkMode ? <DarkIcon /> : <LightIcon />}
        </IconButton>
      </div>

      <nav className={classes.nav}>
        {navigationItems.map((item) => {
          const Icon = item.icon;
          const active = isActive(item.path);
          const protectionStatus = protectionStatuses[item.path];
          
          return (
            <div
              key={item.path}
              className={`${classes.navItem} ${active ? classes.active : ''}`}
              onClick={() => navigate(item.path)}
              style={{
                backgroundColor: active ? colors.accent + '20' : 'transparent',
                borderLeftColor: active ? colors.accent : 'transparent',
                color: active ? colors.accent : colors.text
              }}
              onMouseEnter={(e) => {
                if (!active) {
                  e.currentTarget.style.backgroundColor = colors.surfaceHover;
                }
              }}
              onMouseLeave={(e) => {
                if (!active) {
                  e.currentTarget.style.backgroundColor = 'transparent';
                }
              }}
            >
              <Icon className={classes.navIcon} />
              <span className={classes.navLabel}>{item.label}</span>
              {item.hasProtection && protectionStatuses[item.path] !== undefined && (
                <span 
                  className={classes.protectionDot}
                  style={{
                    backgroundColor: protectionStatuses[item.path] ? colors.success : colors.error
                  }}
                  title={protectionStatuses[item.path] ? 'Protected' : 'Unprotected'}
                />
              )}
            </div>
          );
        })}
      </nav>
    </div>
  );
};

export default Sidebar;