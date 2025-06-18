import { Button } from "@mui/material";
import { useNavigate, useLocation } from "react-router-dom";
import classes from './Ribbon.module.css';

const Ribbon = () => {
  const navigate = useNavigate();
  const location = useLocation();

  const isActive = (path) => location.pathname === path;

  const navigationItems = [
    { path: '/products', label: 'Products' },
    { path: '/payments', label: 'Payments' },
    { path: '/protection', label: 'Protection' },
    { path: '/injection', label: 'SQL Injection' },
    { path: '/benchmark', label: 'Benchmarks' },
  ];

  return (
    <div className={classes.mainDiv}>
      {navigationItems.map((item) => (
        <Button 
          key={item.path}
          variant='contained' 
          sx={{ margin: 0.2 }} 
          onClick={() => navigate(item.path)}
          disabled={isActive(item.path)}
        >
          {item.label}
        </Button>
      ))}
    </div>
  );
};

export default Ribbon;